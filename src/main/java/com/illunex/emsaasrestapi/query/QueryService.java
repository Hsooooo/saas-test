package com.illunex.emsaasrestapi.query;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.query.dto.RequestQueryDTO;
import com.illunex.emsaasrestapi.query.dto.ResponseQueryDTO;
import com.illunex.emsaasrestapi.query.mapper.ProjectQueryCategoryMapper;
import com.illunex.emsaasrestapi.query.mapper.ProjectQueryMapper;
import com.illunex.emsaasrestapi.query.vo.ProjectQueryCategoryVO;
import com.illunex.emsaasrestapi.query.vo.ProjectQueryVO;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    private final ModelMapper modelMapper;

    /**
     * 쿼리 실행
     * @param memberVO
     * @param executeQuery
     * @return
     */
    public Object findQuery(MemberVO memberVO, RequestQueryDTO.FindQuery executeQuery) {
        QueryComponent.QueryResult queryResult = queryComponent.resolveQuery(executeQuery);
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

    public Object executeQuery(MemberVO memberVO, RequestQueryDTO.ExecuteQuery executeQuery) {
//        QueryComponent.QueryResult queryResult = queryComponent.resolveQuery(executeQuery);
//        List<Map> results = mongoTemplate.find(queryResult.query(), Map.class, queryResult.collection());
        return null;
    }



    /**
     * 프로젝트에 속한 쿼리 카테고리 목록 조회
     * @param memberVO
     * @param projectIdx
     * @return
     */
    public Object getQueryCategories(MemberVO memberVO, Integer projectIdx) {
        PartnershipMemberVO partnershipMemberVO = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(projectIdx, memberVO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 파트너십 멤버가 존재하지 않습니다."));
        List<ProjectQueryCategoryVO> categories = projectQueryCategoryMapper.selectByProjectIdxAndPartnershipMemberIdx(projectIdx, partnershipMemberVO.getIdx());
        List<ResponseQueryDTO.Categories> responseCategories = new ArrayList<>();
        for (ProjectQueryCategoryVO category : categories) {
            ResponseQueryDTO.Categories responseCategory = new ResponseQueryDTO.Categories();
            responseCategory.setCategoryName(category.getName());
            responseCategory.setQueryCategoryIdx(category.getIdx());
            List<ResponseQueryDTO.Query> queries = projectQueryMapper.selectByProjectQueryCategoryIdx(category.getIdx())
                    .stream()
                    .map(query -> {
                        ResponseQueryDTO.Query responseQuery = new ResponseQueryDTO.Query();
                        responseQuery.setIdx(query.getIdx());
                        responseQuery.setTitle(query.getTitle());
                        responseQuery.setRawQuery(query.getRawQuery());
                        responseQuery.setTypeCd(query.getTypeCd());
                        responseQuery.setUpdateDate(query.getUpdateDate());
                        responseQuery.setCreateDate(query.getCreateDate());
                        return responseQuery;
                    }).toList();
            responseCategory.setQueries(queries);
        }
        return responseCategories;
    }

    /**
     * 특정 카테고리에 속한 쿼리 목록을 조회
     * @param memberVO
     * @param projectIdx
     * @param queryCategoryIdx
     * @return
     */
    public Object getQueriesByCategory(MemberVO memberVO, Integer projectIdx, Integer queryCategoryIdx) {
        PartnershipMemberVO partnershipMemberVO = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(projectIdx, memberVO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 파트너십 멤버가 존재하지 않습니다."));
        ProjectQueryCategoryVO queryCategoryVO = projectQueryCategoryMapper.selectByIdx(queryCategoryIdx)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));
        List<ProjectQueryVO> queries = projectQueryMapper.selectByProjectQueryCategoryIdxAndPartnershipMemberIdx(queryCategoryIdx, partnershipMemberVO.getIdx());
        return new ResponseQueryDTO.QueriesByCategory(
                queryCategoryVO.getIdx(),
                queryCategoryVO.getName(),
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



    public void saveQuery(MemberVO memberVO, RequestQueryDTO.SaveQuery saveQuery) {
        PartnershipMemberVO partnershipMemberVO = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(saveQuery.getPartnershipIdx(), memberVO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 파트너십 멤버가 존재하지 않습니다."));
        if (saveQuery.getProjectIdx() == null) {
            throw new IllegalArgumentException("projectIdx는 필수입니다.");
        }
        if (saveQuery.getRawQuery() == null || saveQuery.getRawQuery().isBlank()) {
            throw new IllegalArgumentException("rawQuery는 필수입니다.");
        }
        // 쿼리 카테고리 정보 전달 시
        ProjectQueryCategoryVO categoryVO = null;
        if (saveQuery.getQueryCategory() != null && StringUtils.isNotBlank(saveQuery.getQueryCategory().getCategoryName())) {
            if (saveQuery.getQueryCategory().getQueryCategoryIdx() != null) {
                categoryVO = projectQueryCategoryMapper.selectByIdx(saveQuery.getQueryCategory().getQueryCategoryIdx())
                    .map(vo -> {
                        vo.setName(saveQuery.getQueryCategory().getCategoryName());
                        projectQueryCategoryMapper.updateByProjectQueryCategoryVO(vo);
                        return vo;
                    }).orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));
            } else {
                // 카테고리 인덱스가 전달되지 않은 경우, 새로 생성
                categoryVO = new ProjectQueryCategoryVO();
                categoryVO.setProjectIdx(saveQuery.getProjectIdx());
                categoryVO.setName(saveQuery.getQueryCategory().getCategoryName());
                categoryVO.setPartnershipMemberIdx(partnershipMemberVO.getIdx());
                projectQueryCategoryMapper.insertByProjectQueryCategoryVO(categoryVO);
            }
        }

        ProjectQueryVO projectQueryVO = new ProjectQueryVO();
        projectQueryVO.setProjectIdx(saveQuery.getProjectIdx());
        projectQueryVO.setTitle(saveQuery.getQueryTitle());
        projectQueryVO.setRawQuery(saveQuery.getRawQuery());
        projectQueryVO.setPartnershipMemberIdx(partnershipMemberVO.getIdx());
        projectQueryVO.setProjectQueryCategoryIdx(categoryVO != null ? categoryVO.getIdx() : null);
        projectQueryVO.setTypeCd(EnumCode.ProjectQuery.TypeCd.Mongo_Shell.getCode());

        // 쿼리 저장
        projectQueryMapper.insertByProjectQueryVO(projectQueryVO);
    }

}
