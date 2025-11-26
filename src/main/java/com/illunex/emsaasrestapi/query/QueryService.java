package com.illunex.emsaasrestapi.query;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import com.illunex.emsaasrestapi.project.document.database.Column;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.illunex.emsaasrestapi.query.dto.RequestQueryDTO;
import com.illunex.emsaasrestapi.query.dto.ResponseQueryDTO;
import com.illunex.emsaasrestapi.query.mapper.ProjectQueryCategoryMapper;
import com.illunex.emsaasrestapi.query.mapper.ProjectQueryMapper;
import com.illunex.emsaasrestapi.query.vo.ProjectQueryCategoryVO;
import com.illunex.emsaasrestapi.query.vo.ProjectQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueryService {
    private final QueryComponent queryComponent;
    private final MongoTemplate mongoTemplate;
    private final ProjectQueryMapper projectQueryMapper;
    private final ProjectQueryCategoryMapper projectQueryCategoryMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final ProjectMapper projectMapper;
    private final PartnershipMapper partnershipMapper;

    @Value("${ai.url}") String aiGptBase;

    private final WebClient webClient;

    /**
     * 쿼리 실행
     * @param memberVO
     * @param executeQuery
     * @return
     */
    public Object findQuery(MemberVO memberVO, RequestQueryDTO.FindQuery executeQuery) {
        QueryResult queryResult = queryComponent.resolveQuery(executeQuery);
        List<Map> results = mongoTemplate.find(queryResult.query(), Map.class, queryResult.collection());
        long total = mongoTemplate.count(Query.of(queryResult.query()).limit(0).skip(0), queryResult.collection());
        int page = (executeQuery.getSkip() / executeQuery.getLimit()) + 1;
        int size = executeQuery.getLimit();
        return ResponseEntity.ok(ResponseQueryDTO.ExecuteFind.builder()
                .total(total)
                .page(page)
                .size(size)
                .skip(executeQuery.getSkip())
                .limit(size)
                .result(results)
                .build());
    }

    /**
     * 쿼리 실행 (MYSQL, PostgreSQL 등 관계형 DB용)
     * @param memberVO 현재 멤버 정보
     * @param req 쿼리 요청 DTO
     * @return
     */
    public Object executeQuery(MemberVO memberVO, RequestQueryDTO.ExecuteQuery req) {
        ProjectVO projectVO = projectMapper.selectByIdx(req.getProjectIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다."));
        PartnershipVO partnershipVO = partnershipMapper.selectByIdx(projectVO.getPartnershipIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 파트너십이 존재하지 않습니다."));
        PartnershipMemberVO pm = partnershipMemberMapper
                .selectByPartnershipIdxAndMemberIdx(partnershipVO.getIdx(), memberVO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 파트너십 멤버가 존재하지 않습니다."));

        QueryResult qr = queryComponent.resolveSql(req);

        // 실제 적용된 limit/skip
        int appliedLimit = qr.query().getLimit();
        int appliedSkip  = (int) qr.query().getSkip();

        Long total = mongoTemplate.count(Query.of(qr.query()).limit(0).skip(0), qr.collection());

        int page = (appliedLimit > 0) ? (appliedSkip / appliedLimit) + 1 : 1;
        int size = appliedLimit;

        List<Map> results = mongoTemplate.find(qr.query(), Map.class, qr.collection());

        return ResponseEntity.ok(ResponseQueryDTO.ExecuteFind.builder()
                .total(total)
                .page(page)
                .size(size)
                .skip(appliedSkip)
                .limit(size)
                .result(results)
                .build());
    }



    /**
     * 프로젝트에 속한 쿼리 카테고리 목록 조회
     *
     * @param memberVO
     * @param projectIdx
     * @param partnershipIdx
     * @return
     */
    public Object getQueryCategories(MemberVO memberVO, Integer projectIdx, Integer partnershipIdx) {
        PartnershipMemberVO partnershipMemberVO = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 파트너십 멤버가 존재하지 않습니다."));

        List<ProjectQueryCategoryVO> categories = projectQueryCategoryMapper.selectByProjectIdxAndPartnershipMemberIdx(projectIdx, partnershipMemberVO.getIdx());

        List<ResponseQueryDTO.Categories> responseCategories = new ArrayList<>();
        for (ProjectQueryCategoryVO category : categories) {
            ResponseQueryDTO.Categories responseCategory = new ResponseQueryDTO.Categories();
            responseCategory.setCategoryName(category.getName());
            responseCategory.setQueryCategoryIdx(category.getIdx());
            responseCategories.add(responseCategory);
        }

        return responseCategories;
    }

    /**
     * 특정 카테고리에 속한 쿼리 목록을 조회
     */
    public Object getQueriesByCategory(MemberVO memberVO, Integer partnershipIdx, Integer queryCategoryIdx) {
        PartnershipMemberVO partnershipMemberVO = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 파트너십 멤버가 존재하지 않습니다."));

        ProjectQueryCategoryVO projectQueryCategoryVO = projectQueryCategoryMapper.selectByIdx(queryCategoryIdx)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        List<ProjectQueryVO> queries = projectQueryMapper.selectByProjectQueryCategoryIdxAndPartnershipMemberIdx(projectQueryCategoryVO.getIdx(), partnershipMemberVO.getIdx());
        return new ResponseQueryDTO.QueriesByCategory(
                projectQueryCategoryVO.getIdx(),
                projectQueryCategoryVO.getName(),
                queries.stream().map(query -> {
                    ResponseQueryDTO.Query responseQuery = new ResponseQueryDTO.Query();
                    responseQuery.setIdx(query.getIdx());
                    responseQuery.setTitle(query.getTitle());
                    responseQuery.setRawQuery(query.getRawQuery());
                    responseQuery.setTypeCd(query.getTypeCd());
                    responseQuery.setUpdateDate(query.getUpdateDate());
                    responseQuery.setCreateDate(query.getCreateDate());
                    return responseQuery;
                }).toList()
        );
    }

    @Transactional
    public void saveQuery(MemberVO memberVO, RequestQueryDTO.SaveQuery saveQuery) {
        // 1. 파트너십 멤버 조회
        PartnershipMemberVO partnershipMemberVO = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(saveQuery.getPartnershipIdx(), memberVO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 파트너십 멤버가 존재하지 않습니다."));

        // 2. 필수값 검증
        Integer projectIdx = saveQuery.getProjectIdx();
        if (projectIdx == null) {
            throw new IllegalArgumentException("projectIdx는 필수입니다.");
        }

        // 3. 쿼리 카테고리 처리
        Integer queryCategoryIdx;
        if (saveQuery.getQueryCategoryIdx() == null) {
            // - queryCategoryIdx null인 경우 : 쿼리 카테고리 생성
            ProjectQueryCategoryVO projectQueryCategoryVO = new ProjectQueryCategoryVO();
            projectQueryCategoryVO.setProjectIdx(projectIdx);
            projectQueryCategoryVO.setName(saveQuery.getQueryCategoryName());
            projectQueryCategoryVO.setPartnershipMemberIdx(partnershipMemberVO.getIdx());

            projectQueryCategoryMapper.insertByProjectQueryCategoryVO(projectQueryCategoryVO);
            queryCategoryIdx = projectQueryCategoryVO.getIdx();
        } else {
            queryCategoryIdx = saveQuery.getQueryCategoryIdx();
        }

        // 4. 쿼리 처리
        List<RequestQueryDTO.RequestProjectQuery> queryList = saveQuery.getQueryList();
        for (RequestQueryDTO.RequestProjectQuery requestQuery : queryList) {
            String typeCd = null;

            if (requestQuery.getQueryType() != null) {
                if (requestQuery.getQueryType().equals(EnumCode.ProjectQuery.TypeCd.Select.name())) {
                    typeCd = EnumCode.ProjectQuery.TypeCd.Select.getCode();
                }

                if (requestQuery.getQueryType().equals(EnumCode.ProjectQuery.TypeCd.Update.name())) {
                    typeCd = EnumCode.ProjectQuery.TypeCd.Update.getCode();
                }
            }

            if (requestQuery.getIdx() == null) {
                // - queryIdx null인 경우 : 쿼리 생성
                ProjectQueryVO projectQueryVO = new ProjectQueryVO();
                projectQueryVO.setProjectIdx(projectIdx);
                projectQueryVO.setTitle(requestQuery.getTitle());
                projectQueryVO.setRawQuery(requestQuery.getRawQuery().toString());
                projectQueryVO.setPartnershipMemberIdx(partnershipMemberVO.getIdx());
                projectQueryVO.setProjectQueryCategoryIdx(queryCategoryIdx);
                projectQueryVO.setTypeCd(typeCd);

                projectQueryMapper.insertByProjectQueryVO(projectQueryVO);

            } else {
                // - queryIdx null이 아닌 경우 : 쿼리 업데이트
                ProjectQueryVO existingQuery = projectQueryMapper.selectByIdx(requestQuery.getIdx())
                        .orElseThrow(() -> new IllegalArgumentException("해당 쿼리가 존재하지 않습니다."));

                existingQuery.setTitle(requestQuery.getTitle());
                existingQuery.setRawQuery(requestQuery.getRawQuery().toString());

                if (typeCd != null) {
                    existingQuery.setTypeCd(typeCd);
                }

                projectQueryMapper.updateByProjectQueryVO(existingQuery);
            }
        }
    }

    public Object aiQuery(MemberVO memberVO, RequestQueryDTO.AIQuery aiQuery) {
        // 컬럼 정보 조회
        Query query = Query.query(Criteria.where("projectIdx").is(aiQuery.getProjectIdx()));
        List<Column> columns = mongoTemplate.find(query, Column.class);

        List<Map<String, Object>> columnList = columns.stream()
                .map(column -> Map.of(
                        "type", column.getType(),
                        "columns", column.getColumnDetailList().stream()
                                .map(detail -> {
                                    Map<String, Object> m = new HashMap<String, Object>();
                                    m.put("columnName", detail.getColumnName());
                                    m.put("alias", detail.getAlias());
                                    m.put("visible", detail.isVisible());
                                    m.put("order", detail.getOrder());
                                    return m;
                                })
                                .toList()
                ))
                .toList();

        // AI 요청
        RequestQueryDTO.AIQueryRequest req = new RequestQueryDTO.AIQueryRequest();
        req.setQuery(aiQuery.getQueryPrompt());
        req.setExcel_info(columnList);

        final String graphUrl = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                .path("/v3/text_to_sql").toUriString();
        Map graphResp = webClient.post().uri(graphUrl)
                .bodyValue(req)
                .retrieve().bodyToMono(Map.class).block();
        graphResp.put("excelInfo", columnList);

        return graphResp;

    }
}
