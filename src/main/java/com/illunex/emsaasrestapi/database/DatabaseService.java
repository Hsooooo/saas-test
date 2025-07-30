package com.illunex.emsaasrestapi.database;

import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.database.dto.EdgeDataDTO;
import com.illunex.emsaasrestapi.database.dto.RequestDatabaseDTO;
import com.illunex.emsaasrestapi.database.dto.ResponseDatabaseDTO;
import com.illunex.emsaasrestapi.database.dto.SaveResultRecord;
import com.illunex.emsaasrestapi.project.document.database.ColumnDetail;
import com.illunex.emsaasrestapi.project.document.database.Column;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectEdge;
import com.illunex.emsaasrestapi.project.document.project.ProjectNode;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectCategoryMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectTableMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import com.illunex.emsaasrestapi.project.vo.ProjectTableVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DatabaseService {
    private final MongoTemplate mongoTemplate;
    private final DatabaseComponent databaseComponent;
    private final ProjectMapper projectMapper;
    private final ProjectCategoryMapper projectCategoryMapper;
    private final ProjectTableMapper projectTableMapper;
    private final ModelMapper modelMapper;

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
                Criteria filterCriteria = Criteria.where("properties." + filter.getColumnName());
                switch (filter.getFilterCondition()) {
                    case EQUALS, IS -> filterCriteria.is(filter.getSearchString());
                    case NOT_EQUALS, IS_NOT -> filterCriteria.ne(filter.getSearchString());
                    case LESS_THAN -> filterCriteria.lt(filter.getSearchString());
                    case LESS_THAN_OR_EQUAL -> filterCriteria.lte(filter.getSearchString());
                    case GREATER_THAN -> filterCriteria.gt(filter.getSearchString());
                    case GREATER_THAN_OR_EQUAL -> filterCriteria.gte(filter.getSearchString());
                    case EMPTY -> filterCriteria.exists(false);
                    case NOT_EMPTY -> filterCriteria.exists(true);
                    case CONTAINS -> filterCriteria.regex(filter.getSearchString(), "i");
                    case NOT_CONTAINS -> filterCriteria.not().regex(filter.getSearchString(), "i");
                }
                filterCriteriaList.add(filterCriteria);
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
            sortList.add("properties._id,DESC"); // 기본 정렬
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
     * @param projectIdx 프로젝트 인덱스
     * @return 데이터베이스 목록을 포함한 CustomResponse 객체
     */
    public CustomResponse<?> getDatabaseList(Integer projectIdx, String searchString) {
        // 프로젝트 정보 조회
        Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectIdx));
        // 프로젝트 카테고리 정보 조회
        Optional<ProjectCategoryVO> projectCategoryVOOpt = projectCategoryMapper.selectByIdx(projectVO.getProjectCategoryIdx());
        if (project == null) {
            throw new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectIdx);
        }
        ResponseDatabaseDTO.DatabaseList response = new ResponseDatabaseDTO.DatabaseList();
        // 프로젝트 정보 매핑
        response.setProject(new ResponseDatabaseDTO.DatabaseProjectSummary(
                projectVO.getIdx(),
                projectVO.getPartnershipIdx(),
                projectVO.getTitle(),
                projectVO.getDescription(),
                projectVO.getCreateDate(),
                projectVO.getUpdateDate()
        ));
        // 프로젝트 카테고리 이름 설정
        response.setProjectCategoryName(projectCategoryVOOpt.map(ProjectCategoryVO::getName).orElse("미분류"));
        List<ProjectTableVO> projectTableList = new ArrayList<>();
        if (searchString != null && !searchString.isEmpty()) {
            // 검색어가 있는 경우, 프로젝트 테이블 정보에서 검색
            projectTableList = projectTableMapper.selectAllByProjectIdxAndTitle(projectIdx, searchString);
        } else {
            projectTableList = projectTableMapper.selectAllByProjectIdx(projectIdx);
        }

        List<ResponseDatabaseDTO.TableData> nodeTableList = new ArrayList<>();
        List<ResponseDatabaseDTO.TableData> edgeTableList = new ArrayList<>();
        // 프로젝트 테이블 정보에 따라 노드와 엣지 테이블 데이터 생성
        for (ProjectTableVO projectTable : projectTableList) {
            ResponseDatabaseDTO.TableData tableData = new ResponseDatabaseDTO.TableData();
            // 테이블 데이터 설정
            tableData.setTitle(projectTable.getTitle());
            tableData.setTypeCd(projectTable.getTypeCd());
            if (projectTable.getTypeCd().equals(EnumCode.ProjectTable.TypeCd.Node.getCode())) {
                // 데이터 개수 조회
                tableData.setDataCount(mongoTemplate.count(Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                        .and("_id.type").is(projectTable.getTitle())), Node.class));
                tableData.setTypeCdDesc(EnumCode.ProjectTable.TypeCd.Node.getValue());
                nodeTableList.add(tableData);
            } else {
                tableData.setDataCount(mongoTemplate.count(Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                        .and("_id.type").is(projectTable.getTitle())), Edge.class));
                tableData.setTypeCdDesc(EnumCode.ProjectTable.TypeCd.Edge.getValue());
                edgeTableList.add(tableData);
            }
        }
        response.setNodeTableList(nodeTableList);
        response.setEdgeTableList(edgeTableList);

        return CustomResponse.builder()
                .data(response)
                .build();
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
            detail.setVisible(dto.getIsVisible());
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

        List<ColumnDetail> columnDetails = (column != null && column.getColumnDetailList() != null)
                ? column.getColumnDetailList()
                : new ArrayList<>();

        return CustomResponse.builder()
                .data(columnDetails)
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
}
