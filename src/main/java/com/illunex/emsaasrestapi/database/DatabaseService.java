package com.illunex.emsaasrestapi.database;

import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.database.dto.RequestDatabaseDTO;
import com.illunex.emsaasrestapi.database.dto.ResponseDatabaseDTO;
import com.illunex.emsaasrestapi.project.document.database.ColumnDetail;
import com.illunex.emsaasrestapi.project.document.database.Column;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatabaseService {
    private final MongoTemplate mongoTemplate;

    /**
     * 데이터베이스 검색 기능
     *
     * @param projectIdx  프로젝트 인덱스
     * @param query       검색 쿼리
     * @param pageRequest 페이지 요청 정보
     * @param sort        정렬 기준
     * @return 검색 결과를 포함한 CustomResponse 객체
     */
    public CustomResponse<?> searchDatabase(Integer projectIdx, RequestDatabaseDTO.Search query, CustomPageRequest pageRequest, String sort) {
        Class<?> docTypeClass = getDocTypeClass(query.getDocType());
        String docName = query.getDocName();

        // MongoDB 쿼리 생성
        Query mongoQuery = Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                        .and("_id.type").is(docName))
                .with(pageRequest.of("properties." + sort));

        // 데이터 조회
        List<?> results = mongoTemplate.find(mongoQuery, docTypeClass);

        // 컬럼 순서 조회
        Query columnOrderQuery = Query.query(Criteria.where("projectIdx").is(projectIdx).and("type").is(docName));
        Column column = mongoTemplate.findOne(columnOrderQuery, Column.class);

        // 컬럼 순서에 따라 데이터 매핑
        List<Map<String, Object>> mappedResults = new ArrayList<>();
        if (column != null && column.getColumnDetailList() != null) {
            for (Object result : results) {
                if (result instanceof Node node) {
                    Map<String, Object> mappedData = new LinkedHashMap<>();
                    for (ColumnDetail columnDetail : column.getColumnDetailList()) {
                        String columnName = columnDetail.getColumnName();
                        mappedData.put(columnName, node.getProperties().get(columnName));
                    }
                    mappedResults.add(mappedData);
                }
                if (result instanceof Edge edge) {
                    Map<String, Object> mappedData = new LinkedHashMap<>();
                    for (ColumnDetail columnDetail : column.getColumnDetailList()) {
                        String columnName = columnDetail.getColumnName();
                        mappedData.put(columnName, edge.getProperties().get(columnName));
                    }
                    mappedResults.add(mappedData);
                }
            }
        }

        // 전체 데이터 개수 조회
        long totalCount = mongoTemplate.count(Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(docName)), docTypeClass);

        // 결과 반환
        return CustomResponse.builder()
                .data(new PageImpl<>(mappedResults, pageRequest.of(), totalCount))
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
    public CustomResponse<?> getDatabaseList(Integer projectIdx) {
        // Node 타입 조회
        List<String> nodeTypes = mongoTemplate.findDistinct(
                Query.query(Criteria.where("_id.projectIdx").is(projectIdx)),
                "_id.type",
                Node.class,
                String.class
        );

        // Link 타입 조회
        List<String> linkTypes = mongoTemplate.findDistinct(
                Query.query(Criteria.where("_id.projectIdx").is(projectIdx)),
                "_id.type",
                Edge.class,
                String.class
        );

        // 계층 구조 생성
        ResponseDatabaseDTO.DatabaseList response = new ResponseDatabaseDTO.DatabaseList();
        response.setNodeTypes(nodeTypes);
        response.setLinkTypes(linkTypes);

        // 결과 반환
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
            detail.setColumnNameKor(dto.getColumnNameKor());
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
}
