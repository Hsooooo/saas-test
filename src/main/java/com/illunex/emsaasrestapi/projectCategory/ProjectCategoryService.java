package com.illunex.emsaasrestapi.projectCategory;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.illunex.emsaasrestapi.projectCategory.dto.RequestProjectCategoryDTO;
import com.illunex.emsaasrestapi.projectCategory.mapper.ProjectCategoryMapper;
import com.illunex.emsaasrestapi.projectCategory.vo.ProjectCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectCategoryService {
    private final ProjectMapper projectMapper;
    private final ProjectCategoryMapper projectCategoryMapper;
    private final PartnershipMapper partnershipMapper;

    private final ModelMapper modelMapper;

    /**
     * 프로젝트 카테고리 조회
     * @param user
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getProjectCategory(User user) throws CustomException {
        PartnershipVO partnershipVO = getPartnershipVOFromUser(user);

        Integer partnershipIdx = partnershipVO.getIdx();
        List<ProjectCategoryVO> projectCategoryVOList = projectCategoryMapper.selectAllByPartnershipIdx(partnershipIdx);

        List<ResponseProjectDTO.ProjectCategory> result = modelMapper.map(projectCategoryVOList, new TypeToken<List<ResponseProjectDTO.ProjectCategory>>() {}.getType());

        // 카테고리별 프로젝트 개수 세팅
        for(ResponseProjectDTO.ProjectCategory res : result){
            Integer cnt = projectMapper.countByProjectCategoryIdx(res.getIdx());
            res.setProjectCnt(cnt);
        }

        return CustomResponse.builder()
                .data(result)
                .build();
    }

    /**
     * 프로젝트 카테고리 수정,삭제,순서변경
     * @param projectCategoryModify
     * @param user
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> updateProjectCategory(RequestProjectCategoryDTO.ProjectCategoryModify projectCategoryModify, User user) throws CustomException {
        PartnershipVO partnershipVO = getPartnershipVOFromUser(user);

        List<ProjectCategoryVO> projectCategoryVOList = modelMapper.map(projectCategoryModify.getProjectCategoryList(), new TypeToken<List<ProjectCategoryVO>>() {}.getType());

        // 카테고리 삭제
        if (!CollectionUtils.isEmpty(projectCategoryModify.getDeleteCategoryIds())) {
            for (Integer idx : projectCategoryModify.getDeleteCategoryIds()) {
                // 삭제되는 카테고리에 포함된 프로젝트는 기본 카테고리로 넣어줌
                List<ProjectVO> projectVOList = projectMapper.selectAllByProjectCategoryIdx(idx);
                if (!CollectionUtils.isEmpty(projectVOList)) {
                    // TODO[JCW] : 기본 카테고리를 어떻게 잡을지 정해야됨
                    ProjectCategoryVO defaultCategory = projectCategoryVOList.stream()
                            .filter(category -> category.getName().equals("미분류")).findFirst()
                            .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
                    for (ProjectVO projectVO : projectVOList) {
                        projectVO.setProjectCategoryIdx(defaultCategory.getIdx());
                        projectMapper.updateProjectCategoryIdxByProjectVO(projectVO);
                    }
                }
                projectCategoryMapper.deleteByIdx(idx);
            }
        }

        // 카테고리 추가 & 수정
        for (ProjectCategoryVO projectCategoryVO : projectCategoryVOList) {
            if (projectCategoryVO.getIdx() == null) {
                projectCategoryVO.setPartnershipIdx(partnershipVO.getIdx());
                projectCategoryMapper.insertByProjectCategoryVO(projectCategoryVO);
            } else {
                projectCategoryMapper.updateByProjectCategoryVO(projectCategoryVO);
            }
        }

        // 추가 및 수정 완료된 카테고리 리스트를 재조회
        List<ProjectCategoryVO> result = projectCategoryMapper.selectAllByPartnershipIdx(partnershipVO.getIdx());

        // response DTO 맵핑
        List<ResponseProjectDTO.ProjectCategory> response = modelMapper
                .map(result, new TypeToken<List<ResponseProjectDTO.ProjectCategory>>() {}.getType());

        // 카테고리별 프로젝트 개수 세팅
        for (ResponseProjectDTO.ProjectCategory projectCategory : response) {
            Integer cnt = projectMapper.countByProjectCategoryIdx(projectCategory.getIdx());
            projectCategory.setProjectCnt(cnt);
        }

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    private PartnershipVO getPartnershipVOFromUser(User user) throws CustomException {
        // TODO[JCW] : partnershipId를 어디서 가져올 것인지 정해야함. parameter or userDetailService
//        if (user == null) {
//            throw new CustomException(ErrorCode.COMMON_FAIL_AUTHENTICATION);
//        }
//
//        String email = user.getUsername();
//        String domain = email.split("@")[1];

        // 테스트를 위한 임시 선언
        String domain = "1";

        return partnershipMapper.selectByDomain(domain)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
    }
}
