package com.illunex.emsaasrestapi.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.database.dto.EdgeDataDTO;
import com.illunex.emsaasrestapi.database.dto.RequestDatabaseDTO;
import com.illunex.emsaasrestapi.database.dto.ResponseDatabaseDTO;
import com.illunex.emsaasrestapi.database.dto.SaveResultRecord;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.document.database.Column;
import com.illunex.emsaasrestapi.project.document.database.ColumnDetail;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectNode;
import com.illunex.emsaasrestapi.project.mapper.ProjectCategoryMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectTableMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import com.illunex.emsaasrestapi.project.vo.ProjectTableVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.illunex.emsaasrestapi.query.mapper.ProjectQueryCategoryMapper;
import com.illunex.emsaasrestapi.query.vo.ProjectQueryCategoryVO;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {
    private final MongoTemplate mongoTemplate;
    private final DatabaseComponent databaseComponent;
    private final ProjectMapper projectMapper;
    private final ProjectCategoryMapper projectCategoryMapper;
    private final ProjectTableMapper projectTableMapper;
    private final ModelMapper modelMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final ProjectQueryCategoryMapper projectQueryCategoryMapper;

    /**
     * 데이터베이스 검색 기능
     *
     * @param projectIdx  프로젝트 인덱스
     * @param query       검색 쿼리
     * @param pageRequest 페이지 요청 정보
     * @return 검색 결과를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> searchDatabase(Integer projectIdx, RequestDatabaseDTO.Search query, CustomPageRequest pageRequest) {
        Class<?> docTypeClass = getDocTypeClass(query.getDocType());
        String docName = query.getDocName();

        // 공통 Criteria 구성
        Criteria criteria = Criteria.where("_id.projectIdx").is(projectIdx)
                .and("_id.type").is(docName);

        // 필터 조건 추가
        if (query.getFilters() != null && !query.getFilters().isEmpty()) {
            List<Criteria> filterCriteriaList = new ArrayList<>();
            for (RequestDatabaseDTO.SearchFilter filter : query.getFilters()) {
                String field = "properties." + filter.getColumnName();
                String raw = filter.getSearchString() == null ? "" : filter.getSearchString();
                ParsedCandidates c = parseCandidates(raw);

                List<Criteria> orParts = new ArrayList<>();
                switch (filter.getFilterCondition()) {
                    case EQUALS, IS -> {
                        // 문자열
                        orParts.add(Criteria.where(field).is(c.asString()));
                        // 숫자
                        if (c.asNumber() != null) orParts.add(Criteria.where(field).is(c.asNumber()));
                        // 불리언
                        if (c.asBoolean() != null) orParts.add(Criteria.where(field).is(c.asBoolean()));
                        // 날짜
                        if (c.asDate() != null) orParts.add(Criteria.where(field).is(c.asDate()));
                        filterCriteriaList.add(orParts.size() == 1 ? orParts.get(0) : new Criteria().orOperator(orParts.toArray(new Criteria[0])));
                    }
                    case NOT_EQUALS, IS_NOT -> {
                        // NOT은 AND
                        List<Criteria> andParts = new ArrayList<>();
                        andParts.add(Criteria.where(field).ne(c.asString()));
                        if (c.asNumber() != null) andParts.add(Criteria.where(field).ne(c.asNumber()));
                        if (c.asBoolean() != null) andParts.add(Criteria.where(field).ne(c.asBoolean()));
                        if (c.asDate() != null) andParts.add(Criteria.where(field).ne(c.asDate()));
                        filterCriteriaList.add(new Criteria().andOperator(andParts.toArray(new Criteria[0])));
                    }
                    case LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL -> {
                        // 비교는 숫자/날짜만
                        if (c.asNumber() == null && c.asDate() == null) {
                            // 비교 불가: 아무 것도 매치시키지 않음 (전부 false)
                            filterCriteriaList.add(Criteria.where("_id").is("__no_match__"));
                            break;
                        }
                        if (c.asNumber() != null) {
                            switch (filter.getFilterCondition()) {
                                case LESS_THAN -> orParts.add(Criteria.where(field).lt(c.asNumber()));
                                case LESS_THAN_OR_EQUAL -> orParts.add(Criteria.where(field).lte(c.asNumber()));
                                case GREATER_THAN -> orParts.add(Criteria.where(field).gt(c.asNumber()));
                                case GREATER_THAN_OR_EQUAL -> orParts.add(Criteria.where(field).gte(c.asNumber()));
                            }
                        }
                        if (c.asDate() != null) {
                            switch (filter.getFilterCondition()) {
                                case LESS_THAN -> orParts.add(Criteria.where(field).lt(c.asDate()));
                                case LESS_THAN_OR_EQUAL -> orParts.add(Criteria.where(field).lte(c.asDate()));
                                case GREATER_THAN -> orParts.add(Criteria.where(field).gt(c.asDate()));
                                case GREATER_THAN_OR_EQUAL -> orParts.add(Criteria.where(field).gte(c.asDate()));
                            }
                        }
                        filterCriteriaList.add(orParts.size() == 1 ? orParts.get(0) : new Criteria().orOperator(orParts.toArray(new Criteria[0])));
                    }
                    case EMPTY -> filterCriteriaList.add(emptyCriteria(field));
                    case NOT_EMPTY -> filterCriteriaList.add(notEmptyCriteria(field));
                    case CONTAINS -> {
                        // 문자열 전용
                        filterCriteriaList.add(Criteria.where(field).regex(raw, "i"));
                    }
                    case NOT_CONTAINS -> {
                        // 문자열 전용: field가 비어있거나(EMPTY) 또는 문자열이면서 not regex
                        filterCriteriaList.add(new Criteria().orOperator(
                                emptyCriteria(field),
                                new Criteria().andOperator(
                                        Criteria.where(field).type(org.bson.BsonType.STRING.getValue()),
                                        Criteria.where(field).not().regex(raw, "i")
                                )
                        ));
                    }
                }
            }
            criteria = criteria.andOperator(filterCriteriaList.toArray(new Criteria[0]));
        }

        // 정렬 조건 설정
        List<String> sortList = new ArrayList<>();
        if (query.getSorts() != null && !query.getSorts().isEmpty()) {
            for (RequestDatabaseDTO.SearchSort sort : query.getSorts()) {
                if (sort.getColumnName() == null || sort.getColumnName().isEmpty()) {
                    throw new IllegalArgumentException("정렬 기준의 컬럼명이 비어있습니다.");
                }
                String sortColumn = "properties." + sort.getColumnName();
                String sortDirection = sort.getIsAsc() ? "ASC" : "DESC";
                sortList.add(sortColumn + "," + sortDirection);
            }
        } else {
            sortList.add("id,DESC"); // 기본 정렬
        }

        // 조회용 쿼리 (limit, skip 포함)
        Query findQuery = new Query(criteria).with(pageRequest.of(sortList.toArray(new String[0])));

        // 카운트용 쿼리 (limit, skip 없음)
        Query countQuery = new Query(criteria);

        // 데이터 조회
        List<?> results = mongoTemplate.find(findQuery, docTypeClass);

        // 전체 개수 조회
        long totalCount = mongoTemplate.count(countQuery, docTypeClass);

        // 컬럼 순서 조회
        Query columnOrderQuery = Query.query(Criteria.where("projectIdx").is(projectIdx).and("type").is(docName));
        Column column = mongoTemplate.findOne(columnOrderQuery, Column.class);

        // 결과 매핑
        List<Object> mappedResults = new ArrayList<>();
        if (column != null && column.getColumnDetailList() != null) {
            for (Object result : results) {
                LinkedHashMap<String, Object> mappedData = new LinkedHashMap<>();
                for (ColumnDetail columnDetail : column.getColumnDetailList()) {
                    String columnName = columnDetail.getColumnName();
                    Object value = null;
                    if (result instanceof Node node) {
                        value = node.getProperties().get(columnName);
                    } else if (result instanceof Edge edge) {
                        value = edge.getProperties().get(columnName);
                    }
                    mappedData.put(columnName, value);
                }

                if (result instanceof Node) {
                    mappedResults.add(mappedData);
                } else if (result instanceof Edge edge) {
                    EdgeDataDTO dto = new EdgeDataDTO();
                    dto.setId(edge.getId());
                    dto.setProperties(mappedData);
                    mappedResults.add(dto);
                }
            }
        }

        // 결과 반환
        return CustomResponse.builder()
                .data(new PageImpl<>(mappedResults, pageRequest.of(sortList.toArray(new String[0])), totalCount))
                .build();
    }

    /**
     * 템플릿 쿼리 생성
     *
     * @param projectIdx  프로젝트 인덱스
     * @param query       검색 쿼리
     * @return 검색 결과를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> createQueryByTemplate(Integer projectIdx, RequestDatabaseDTO.SearchTemplate query) {
        String nodeType = query.getNodeType();
        List<RequestDatabaseDTO.SearchFilter> filters = query.getFilters();
        List<RequestDatabaseDTO.SearchSort> sorts = query.getSorts();

        // 1. 기본 검색 조건 생성
        Criteria criteria = Criteria.where(MongoDBUtils.Node._ID_PROJECT_IDX.getField()).is(projectIdx)
                .and(MongoDBUtils.Node._ID_TYPE.getField()).is(nodeType);

        // 2. 필터 조건 추가
        List<RequestDatabaseDTO.SearchFilter> searchFilterList = Optional.ofNullable(filters)
                .orElse(List.of());
        
        Criteria filterCriteriaResult = null;
        for (RequestDatabaseDTO.SearchFilter searchFilter : searchFilterList) {
            Criteria filterCriteria = createFilterCriteria(searchFilter);

            if (filterCriteria == null) {
                continue;
            }

            if (filterCriteriaResult == null) {
                // - 첫 번째 유효한 필터
                filterCriteriaResult = filterCriteria;
            } else {
                RequestDatabaseDTO.FilterOperator operator = searchFilter.getFilterOperator();
                // - 연산자 기본값: AND
                if (operator == null) {
                    operator = RequestDatabaseDTO.FilterOperator.AND;
                }

                if (operator == RequestDatabaseDTO.FilterOperator.AND) {
                    filterCriteriaResult = new Criteria().andOperator(filterCriteriaResult, filterCriteria);
                } else {
                    filterCriteriaResult = new Criteria().orOperator(filterCriteriaResult, filterCriteria);
                }
            }
        }

        // - 기본 조건과 필터 조건을 AND로 결합
        if (filterCriteriaResult != null) {
            criteria = new Criteria().andOperator(criteria, filterCriteriaResult);
        }

        // 3. 정렬 조건 생성
        List<String> sortList = new ArrayList<>();

        List<RequestDatabaseDTO.SearchSort> searchSortList = Optional.ofNullable(sorts)
                .orElse(List.of());

        if (searchSortList.isEmpty()) {
            sortList.add("id,DESC");
        } else {
            for (RequestDatabaseDTO.SearchSort searchSort : searchSortList) {
                String columnName = searchSort.getColumnName();
                Boolean isAsc = searchSort.getIsAsc();

                if (columnName == null || columnName.isEmpty()) {
                    throw new IllegalArgumentException("정렬 기준의 컬럼명이 비어있습니다.");
                }

                String sortColumn = MongoDBUtils.Node.PROPERTIES.getPropertyField(columnName);
                String sortDirection = isAsc ? "ASC" : "DESC";
                String sort = sortColumn + "," + sortDirection;

                sortList.add(sort);
            }
        }

        // 4. 쿼리 생성
        Query findNodeQuery = Query.query(criteria);

        // - 생성된 쿼리 로그 출력
        log.info("[searchDatabaseByTemplate] findQuery={}", findNodeQuery);

        // - Document를 Extended JSON 형식으로 변환
        Document queryDocument = findNodeQuery.getQueryObject();
        JsonWriterSettings settings = JsonWriterSettings.builder()
                .outputMode(JsonMode.EXTENDED)
                .build();
        String queryJson = queryDocument.toJson(settings);

        // - JSON 문자열을 JsonNode로 변환
        JsonNode queryJsonNode;
        try {
            queryJsonNode = new ObjectMapper().readTree(queryJson);
        } catch (Exception e) {
            throw new RuntimeException("쿼리 JSON 변환 중 오류 발생", e);
        }

        return CustomResponse.builder()
                .data(queryJsonNode)
                .build();
    }

    /**
     * 프로젝트 노드 타입 검색
     *
     * @param projectIdx  프로젝트 인덱스
     * @return 검색 결과를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> searchDatabaseNodeType(Integer projectIdx) throws CustomException {
        // 1. 프로젝트 조회
        Query query = Query.query(
                Criteria.where("_id").is(projectIdx)
        );

        Project project = mongoTemplate.findOne(query, Project.class);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 프로젝트 노드 정보 조회
        List<ProjectNode> projectNodeList = project.getProjectNodeList();
        return CustomResponse.builder()
                .data(projectNodeList)
                .build();
    }

    /**
     * 프로젝트 노드 컬럼 타입 검색
     *
     * @param projectIdx 프로젝트 인덱스
     * @param request
     * @return 검색 결과를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> searchDatabaseNodeColumnType(Integer projectIdx,
                                                          RequestDatabaseDTO.SearchTemplate request) throws CustomException {
        String nodeType = request.getNodeType();

        // 1. 프로젝트 조회 (projectIdx 유효성 검사)
        Query query = Query.query(
                Criteria.where("_id").is(projectIdx)
        );

        Project project = mongoTemplate.findOne(query, Project.class);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 노드 타입 조회 (nodeType 유효성 검사)
        List<String> nodeTypeList = project.getProjectNodeList().stream()
                .map(ProjectNode::getNodeType)
                .toList();

        if (!nodeTypeList.contains(nodeType)) {
            throw new CustomException(ErrorCode.PROJECT_INVALID_NODE_TYPE);
        }

        // 3. 컬럼 조회
        Query findColumnQuery = Query.query(
                Criteria.where(MongoDBUtils.Column.PROJECT_IDX.getField()).is(projectIdx)
                        .and(MongoDBUtils.Column.TYPE.getField()).is(nodeType)
        );

        Column column = (mongoTemplate.findOne(findColumnQuery, Column.class));
        if (column == null) {
            throw new CustomException(ErrorCode.PROJECT_COLUMN_NOT_FOUND);
        }

        List<ColumnDetail> columnDetailList = column.getColumnDetailList();
        return CustomResponse.builder()
                .data(columnDetailList)
                .build();
    }

    private Criteria createFilterCriteria(RequestDatabaseDTO.SearchFilter filter) {
        String field = MongoDBUtils.Node.PROPERTIES.getPropertyField(filter.getColumnName());
        String raw = filter.getSearchString() == null ? "" : filter.getSearchString();
        ParsedCandidates c = parseCandidates(raw);

        RequestDatabaseDTO.FilterCondition filterCondition = filter.getFilterCondition();
        switch (filterCondition) {
            case EQUALS, IS:
                return createEqualsCriteria(field, c);
            case NOT_EQUALS, IS_NOT:
                return createNotEqualsCriteria(field, c);
            case LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL:
                return createComparisonCriteria(field, filter.getFilterCondition(), c);
            case EMPTY:
                return emptyCriteria(field);
            case NOT_EMPTY:
                return notEmptyCriteria(field);
            case CONTAINS:
                return Criteria.where(field).regex(escapeCharacter(raw), "i");
            case NOT_CONTAINS:
                return new Criteria().orOperator(
                        // - 조건 1: 필드가 비어있는 경우 (null, 빈 문자열, 필드 없음)
                        emptyCriteria(field),
                        // or
                        // - 조건 2: 필드가 있고 문자열인데, 검색어와 매치되지 않는 경우
                        new Criteria().andOperator(
                                Criteria.where(field).type(BsonType.STRING.getValue()),
                                Criteria.where(field).not().regex(escapeCharacter(raw), "i")
                        )
                );
            default:
                return null;
        }
    }

    private Criteria createEqualsCriteria(String field, ParsedCandidates c) {
        List<Criteria> orParts = new ArrayList<>();
        orParts.add(Criteria.where(field).is(c.asString()));

        if (c.asNumber() != null) orParts.add(Criteria.where(field).is(c.asNumber()));
        if (c.asBoolean() != null) orParts.add(Criteria.where(field).is(c.asBoolean()));
        if (c.asDate() != null) orParts.add(Criteria.where(field).is(c.asDate()));

        return orParts.size() == 1 ? orParts.get(0) : new Criteria().orOperator(orParts.toArray(new Criteria[0]));
    }

    private Criteria createNotEqualsCriteria(String field, ParsedCandidates c) {
        List<Criteria> andParts = new ArrayList<>();
        andParts.add(Criteria.where(field).ne(c.asString()));

        if (c.asNumber() != null) andParts.add(Criteria.where(field).ne(c.asNumber()));
        if (c.asBoolean() != null) andParts.add(Criteria.where(field).ne(c.asBoolean()));
        if (c.asDate() != null) andParts.add(Criteria.where(field).ne(c.asDate()));

        return andParts.size() == 1 ? andParts.get(0) : new Criteria().andOperator(andParts.toArray(new Criteria[0]));
    }

    private Criteria createComparisonCriteria(String field, RequestDatabaseDTO.FilterCondition condition, ParsedCandidates c) {
        if (c.asNumber() == null && c.asDate() == null) {
            return Criteria.where("_id").is("__no_match__");
        }

        List<Criteria> orParts = new ArrayList<>();

        // 1. 숫자 비교
        if (c.asNumber() != null) {
            Criteria numberComparison;
            Long number = c.asNumber();

            switch (condition) {
                case LESS_THAN:
                    numberComparison = Criteria.where(field).lt(number);
                    break;
                case LESS_THAN_OR_EQUAL:
                    numberComparison = Criteria.where(field).lte(number);
                    break;
                case GREATER_THAN:
                    numberComparison = Criteria.where(field).gt(number);
                    break;
                case GREATER_THAN_OR_EQUAL:
                    numberComparison = Criteria.where(field).gte(number);
                    break;
                default:
                    numberComparison = null;
                    break;
            }

            orParts.add(numberComparison);
        }

        // 2. 날짜 비교
        if (c.asDate() != null) {
            Criteria dateComparison;
            Date date = c.asDate();

            switch (condition) {
                case LESS_THAN:
                    dateComparison = Criteria.where(field).lt(date);
                    break;
                case LESS_THAN_OR_EQUAL:
                    dateComparison = Criteria.where(field).lte(date);
                    break;
                case GREATER_THAN:
                    dateComparison = Criteria.where(field).gt(date);
                    break;
                case GREATER_THAN_OR_EQUAL:
                    dateComparison = Criteria.where(field).gte(date);
                    break;
                default:
                    dateComparison = null;
                    break;
            }

            orParts.add(dateComparison);
        }

        return orParts.size() == 1 ? orParts.get(0) : new Criteria().orOperator(orParts.toArray(new Criteria[0]));
    }

    private String escapeCharacter(String raw) {
        if (raw == null) return "";

        // 정규식 특수 문자 목록: \ . + * ? ^ $ [ ] ( ) { } |
        // 이 문자들을 찾아서 앞에 백슬래시(\) 추가
        return raw.replaceAll("([\\\\\\.\\+\\*\\?\\^\\$\\[\\]\\(\\)\\{\\}\\|])", "\\\\$1");
    }

    /**
     * 요청된 DocType에 따라 해당하는 클래스 반환
     *
     * @param docType 요청된 DocType
     * @return 해당하는 클래스
     */
    private Class<?> getDocTypeClass(RequestDatabaseDTO.DocType docType) {
        return switch (docType) {
            case Node -> Node.class; // Node 클래스
            case Link -> Edge.class; // Link 클래스
            default -> throw new IllegalArgumentException("Unsupported DocType: " + docType);
        };
    }

    /**
     * 데이터베이스 목록 조회 기능
     *
     * @param memberVO 현재 로그인한 멤버 정보
     * @param projectIdx 프로젝트 인덱스
     * @return 데이터베이스 목록을 포함한 CustomResponse 객체
     */
    public CustomResponse<?> getDatabaseList(MemberVO memberVO, Integer projectIdx, String searchString) {
        // --- 1) RDB에서 프로젝트 요약 + 카테고리 한 번에 확보 (Mongo Project 불필요하면 제거) ---
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectIdx));

        Optional<ProjectCategoryVO> projectCategoryVOOpt =
                projectCategoryMapper.selectByIdx(projectVO.getProjectCategoryIdx());

        ResponseDatabaseDTO.DatabaseList response = new ResponseDatabaseDTO.DatabaseList();

        response.setProject(new ResponseDatabaseDTO.DatabaseProjectSummary(
                projectVO.getIdx(),
                projectVO.getPartnershipIdx(),
                projectVO.getTitle(),
                projectVO.getDescription(),
                projectVO.getCreateDate(),
                projectVO.getUpdateDate()
        ));
        response.setProjectCategoryName(projectCategoryVOOpt.map(ProjectCategoryVO::getName).orElse("미분류"));

        // --- 2) 테이블 목록 조회 (검색어 유무) ---
        final boolean hasSearch = StringUtils.hasText(searchString);
        List<ProjectTableVO> projectTableList = hasSearch
                ? projectTableMapper.selectAllByProjectIdxAndTitle(projectIdx, searchString)
                : projectTableMapper.selectAllByProjectIdx(projectIdx);

//        // 제목 리스트 분리
        List<String> nodeTitles = projectTableList.stream()
                .filter(t -> EnumCode.ProjectTable.TypeCd.Node.getCode().equals(t.getTypeCd()))
                .map(ProjectTableVO::getTitle)
                .collect(Collectors.toList());

        List<String> edgeTitles = projectTableList.stream()
                .filter(t -> EnumCode.ProjectTable.TypeCd.Edge.getCode().equals(t.getTypeCd()))
                .map(ProjectTableVO::getTitle)
                .collect(Collectors.toList());

        // --- 3) 한 방 집계: Nodes / Edges 각각 1회 왕복 ---
        Map<String, Long> nodeCounts = aggregateCountsByType(projectIdx, nodeTitles, Node.class);
        Map<String, Long> edgeCounts = aggregateCountsByType(projectIdx, edgeTitles, Edge.class);

        // --- 4) DTO 매핑 ---
        List<ResponseDatabaseDTO.TableData> nodeTableList = new ArrayList<>();
        List<ResponseDatabaseDTO.TableData> edgeTableList = new ArrayList<>();

        for (ProjectTableVO t : projectTableList) {
            ResponseDatabaseDTO.TableData td = new ResponseDatabaseDTO.TableData();
            td.setTitle(t.getTitle());
            td.setTypeCd(t.getTypeCd());

            if (EnumCode.ProjectTable.TypeCd.Node.getCode().equals(t.getTypeCd())) {
                td.setDataCount(nodeCounts.getOrDefault(t.getTitle(), 0L));
                td.setTypeCdDesc(EnumCode.ProjectTable.TypeCd.Node.getValue());
                nodeTableList.add(td);
            } else {
                td.setDataCount(edgeCounts.getOrDefault(t.getTitle(), 0L));
                td.setTypeCdDesc(EnumCode.ProjectTable.TypeCd.Edge.getValue());
                edgeTableList.add(td);
            }
        }
//
//        List<ResponseDatabaseDTO.TableData> nodeTableList = new ArrayList<>();
//        List<ResponseDatabaseDTO.TableData> edgeTableList = new ArrayList<>();
//        for (ProjectTableVO tableVO : projectTableList) {
//            if (EnumCode.ProjectTable.TypeCd.Node.getCode().equals(tableVO.getTypeCd())) {
//                ResponseDatabaseDTO.TableData td = modelMapper.map(tableVO, ResponseDatabaseDTO.TableData.class);
//                td.setTypeCdDesc(EnumCode.ProjectTable.TypeCd.Node.getValue());
//                nodeTableList.add(td);
//            } else {
//                ResponseDatabaseDTO.TableData td = modelMapper.map(tableVO, ResponseDatabaseDTO.TableData.class);
//                td.setTypeCdDesc(EnumCode.ProjectTable.TypeCd.Edge.getValue());
//                edgeTableList.add(td);
//            }
//        }

        response.setNodeTableList(nodeTableList);
        response.setEdgeTableList(edgeTableList);

        // --- 5) 쿼리 카테고리 목록 조회 ---
        List<ResponseDatabaseDTO.QueryCategory> queryList = new ArrayList<>();

        // - 파트너쉽 멤버 조회
        Optional<PartnershipMemberVO> partnershipMemberOpt = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(projectVO.getPartnershipIdx(), memberVO.getIdx());
        if (partnershipMemberOpt.isPresent()) {
            PartnershipMemberVO partnershipMemberVO = partnershipMemberOpt.get();

            // - 프로젝트 쿼리 카테고리 조회
            List<ProjectQueryCategoryVO> categories = projectQueryCategoryMapper.selectByProjectIdxAndPartnershipMemberIdx(projectIdx, partnershipMemberVO.getIdx());
            for (ProjectQueryCategoryVO category : categories) {
                ResponseDatabaseDTO.QueryCategory queryCategory = new ResponseDatabaseDTO.QueryCategory();
                queryCategory.setQueryCategoryIdx(category.getIdx());
                queryCategory.setCategoryName(category.getName());

                queryList.add(queryCategory);
            }
        }

        response.setQueryList(queryList);

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    private Map<String, Long> aggregateCountsByType(Integer projectIdx, List<String> titles, Class<?> collectionClass) {
        Map<String, Long> map = new HashMap<>();
        if (titles == null || titles.isEmpty()) return map;

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").in(titles)),
                // _id에 type이 그대로 들어가므로, 읽을 때는 "_id"에서 꺼냄
                Aggregation.group("_id.type").count().as("cnt")
        );

        AggregationResults<org.bson.Document> results =
                mongoTemplate.aggregate(agg, collectionClass, org.bson.Document.class);

        for (org.bson.Document d : results.getMappedResults()) {
            // type 읽기 (group("_id.type")이므로 _id가 곧 type 값)
            Object id = d.get("_id");
            String type = (id == null) ? null : String.valueOf(id);

            // cnt는 Integer 또는 Long로 올 수 있음 → Number로 받아서 longValue()
            Number n = (Number) d.get("cnt");
            long cnt = (n == null) ? 0L : n.longValue();

            if (type != null) {
                map.put(type, cnt);
            }
        }
        return map;
    }

    /**
     * 컬럼 정보 저장
     *
     * @param request 컬럼 정렬 요청 DTO
     * @return 성공 메시지를 포함한 CustomResponse 객체
     */
    @Transactional
    public CustomResponse<?> saveColumnOrder(RequestDatabaseDTO.ColumnOrder request) {
        // 기존 컬럼 정렬값 삭제
        Query query = Query.query(
                Criteria.where("projectIdx").is(request.getProjectIdx())
                        .and("type").is(request.getType())
        );

        Update update = new Update()
                .set("projectIdx", request.getProjectIdx())
                .set("type", request.getType())
                .set("columnDetailList", convertToDetails(request.getColumnDetailList()));

        mongoTemplate.upsert(query, update, Column.class);

        return CustomResponse.builder()
                .build();
    }

    private List<ColumnDetail> convertToDetails(List<RequestDatabaseDTO.ColumnDetailDTO> dtos) {
        List<ColumnDetail> list = new ArrayList<>();
        for (RequestDatabaseDTO.ColumnDetailDTO dto : dtos) {
            ColumnDetail detail = new ColumnDetail();
            detail.setColumnName(dto.getColumnName());
            detail.setVisible(dto.getVisible());
            detail.setOrder(dto.getOrder());
            detail.setAlias(dto.getAlias());
            list.add(detail);
        }
        return list;
    }

    /**
     * 컬럼 목록 조회 기능
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type       Node 또는 Edge의 타입
     * @return 컬럼 목록을 포함한 CustomResponse 객체
     */
    public CustomResponse<?> getColumnList(Integer projectIdx, String type) {
        // 컬럼 정보 조회
        Query query = Query.query(Criteria.where("projectIdx").is(projectIdx).and("type").is(type));
        Column column = mongoTemplate.findOne(query, Column.class);

        List<RequestDatabaseDTO.ColumnDetailDTO> result = column.getColumnDetailList().stream()
                .map(detail -> {
                    RequestDatabaseDTO.ColumnDetailDTO dto = new RequestDatabaseDTO.ColumnDetailDTO();
                    dto.setColumnName(detail.getColumnName());
                    dto.setAlias(detail.getAlias());
                    dto.setVisible(detail.isVisible()); // 동일 필드명
                    dto.setOrder(detail.getOrder());
                    return dto;
                })
                .collect(Collectors.toList());

        return CustomResponse.builder()
                .data(result)
                .build();
    }

    /**
     * 데이터 추가 기능
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type       Node 또는 Edge의 타입
     * @param data       추가할 데이터
     * @param docType    요청된 DocType
     * @return 성공 메시지를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> addData(Integer projectIdx, String type, LinkedHashMap<String, Object> data, RequestDatabaseDTO.DocType docType) {
        Class<?> docTypeClass = getDocTypeClass(docType);
        Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
        if (project == null) throw new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectIdx);

        if (docTypeClass == Node.class) {
            databaseComponent.handleNodeSave(project, projectIdx, type, data);
        } else if (docTypeClass == Edge.class) {
            databaseComponent.handleEdgeSave(project, projectIdx, type, data, null);
        }

        return CustomResponse.builder().message("데이터가 성공적으로 추가되었습니다.").build();
    }

    /**
     * 노드 데이터 업데이트 기능
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type       노드 타입
     * @param data       노드 데이터 DTO
     * @return 성공 메시지를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> updateNode(Integer projectIdx, String type, LinkedHashMap<String, Object> data) {
        Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
        if (project == null) throw new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectIdx);
        databaseComponent.handleNodeSave(project, projectIdx, type, data);
        return CustomResponse.builder().message("데이터가 성공적으로 추가되었습니다.").build();
    }

    /**
     * 엣지 데이터 업데이트 기능
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type       엣지 타입
     * @param data       엣지 데이터 DTO
     * @return 성공 메시지를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> updateEdge(Integer projectIdx, String type, EdgeDataDTO data) {
        Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
        if (project == null) throw new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectIdx);
        databaseComponent.handleEdgeSave(project, projectIdx, type, data.getProperties(), data.getId());
        return CustomResponse.builder().message("데이터가 성공적으로 추가되었습니다.").build();
    }

    /**
     * 데이터 삭제 기능
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type       Node 또는 Edge의 타입
     * @param data       삭제할 데이터 ID 목록
     * @param docType    요청된 DocType
     * @return 성공 메시지를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> deleteData(Integer projectIdx, String type, List<Object> data, RequestDatabaseDTO.DocType docType) {
        Class<?> docTypeClass = getDocTypeClass(docType);
        Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
        if (project == null) throw new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectIdx);
        for (Object id : data) {
            Query deleteQuery = Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type).and("id").is(id));
            mongoTemplate.remove(deleteQuery, docTypeClass);
        }
        return null;
    }

    /**
     * 데이터베이스 커밋 기능
     *
     * @param projectIdx 프로젝트 인덱스
     * @param commit     커밋 요청 DTO
     * @param type       Node 또는 Edge의 타입
     * @param docType    요청된 DocType
     * @return 성공 메시지를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> commitDatabase(Integer projectIdx, RequestDatabaseDTO.Commit commit, String type, RequestDatabaseDTO.DocType docType) {
        Class<?> docTypeClass = getDocTypeClass(docType);
        Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
        if (project == null) throw new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectIdx);

        int createdCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;


        // 새 데이터 추가
        if (commit.getNewData() != null) {
            for (LinkedHashMap<String, Object> newData : commit.getNewData()) {
                SaveResultRecord result = (docTypeClass == Node.class)
                        ? databaseComponent.handleNodeInsert(project, projectIdx, type, newData)
                        : databaseComponent.handleEdgeInsert(project, projectIdx, type, newData);

                if (result.action() == SaveResultRecord.SaveAction.CREATED) createdCount++;
            }
        }

        // 데이터 업데이트
        if (commit.getUpdateData() != null) {
            for (RequestDatabaseDTO.UpdateData updateData : commit.getUpdateData()) {
                SaveResultRecord result = (docTypeClass == Node.class)
                        ? databaseComponent.handleNodeUpdate(project, projectIdx, type, updateData.getId(), updateData.getData())
                        : databaseComponent.handleEdgeUpdate(project, projectIdx, type, updateData.getId(), updateData.getData());

                if (result.action() == SaveResultRecord.SaveAction.UPDATED) updatedCount++;
            }
        }

        // 데이터 삭제
        if (commit.getDeleteData() != null) {
            for (Object id : commit.getDeleteData()) {
                Query deleteQuery = Query.query(
                        Criteria.where("_id.projectIdx").is(projectIdx)
                                .and("_id.type").is(type)
                                .and("id").is(id)
                );
                DeleteResult result = mongoTemplate.remove(deleteQuery, docTypeClass);
                deletedCount += (int) result.getDeletedCount();
            }
        }

        // 커밋 결과 반환
        ResponseDatabaseDTO.Commit responseCommit = new ResponseDatabaseDTO.Commit();
        responseCommit.setCreatedCount(createdCount);
        responseCommit.setUpdatedCount(updatedCount);
        responseCommit.setDeletedCount(deletedCount);
        return CustomResponse.builder().message("데이터베이스 커밋이 성공적으로 완료되었습니다.")
                .data(responseCommit)
                .build();
    }

    // 1) 유틸: 타입 후보 파싱
    private record ParsedCandidates(Object asString, Long asNumber, Boolean asBoolean, Date asDate) {
    }

    private ParsedCandidates parseCandidates(String s) {
        Object asString = s;

        Long asNumber = null;
        try {
            asNumber = Long.parseLong(s);
        } catch (Exception ignored) {
        }

        Boolean asBoolean = null;
        if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
            asBoolean = Boolean.parseBoolean(s);
        }

        Date asDate = null;
        // 자주 쓰는 포맷
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"
        };
        for (String p : patterns) {
            try {
                java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p)
                        .withZone(java.time.ZoneId.systemDefault());
                java.time.temporal.TemporalAccessor ta = f.parse(s);
                java.time.Instant inst = java.time.Instant.from(ta);
                asDate = Date.from(inst);
                break;
            } catch (Exception ignored) {
            }
        }

        return new ParsedCandidates(asString, asNumber, asBoolean, asDate);
    }

    // 2) 유틸: EMPTY/NOT_EMPTY 공통
    private Criteria emptyCriteria(String field) {
        return new Criteria().orOperator(
                Criteria.where(field).exists(false),
                Criteria.where(field).is(null),
                Criteria.where(field).is("")
        );
    }

    private Criteria notEmptyCriteria(String field) {
        return new Criteria().norOperator(
                Criteria.where(field).exists(false),
                Criteria.where(field).is(null),
                Criteria.where(field).is("")
        );
    }
}
