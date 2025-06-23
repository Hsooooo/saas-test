package com.illunex.emsaasrestapi.database;

import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.database.dto.RequestDatabaseDTO;
import com.illunex.emsaasrestapi.database.dto.ResponseDatabaseDTO;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseService {
    private final MongoTemplate mongoTemplate;

    /**
     * 데이터베이스 검색 기능
     *
     * @param projectIdx 프로젝트 인덱스
     * @param query      검색 쿼리
     * @param pageRequest 페이지 요청 정보
     * @param sort       정렬 기준
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

        // 전체 데이터 개수 조회
        long totalCount = mongoTemplate.count(Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(docName)), docTypeClass);

        // 결과 반환
        return CustomResponse.builder()
                .data(new PageImpl<>(results, pageRequest.of(), totalCount))
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
}
