package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.dto.RequestProjectCategoryDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectCategoryMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectCategoryService {
    private final ProjectMapper projectMapper;
    private final ProjectCategoryMapper projectCategoryMapper;

    private final PartnershipComponent partnershipComponent;
    private final ProjectComponent projectComponent;
    private final ModelMapper modelMapper;

    /**
     * 프로젝트 카테고리 목록 조회
     * @param memberVO
     * @param partnershipIdx
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getProjectCategory(MemberVO memberVO, Integer partnershipIdx) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);

        return CustomResponse.builder()
                .data(projectComponent.createResponseProjectCategory(partnershipMemberVO))
                .build();
    }

    /**
     * 프로젝트 카테고리 수정,삭제,순서변경
     * @param memberVO
     * @param projectCategoryModify
     * @return
     * @throws CustomException
     */
    @Transactional
    public CustomResponse<?> updateProjectCategory(MemberVO memberVO, RequestProjectCategoryDTO.ProjectCategoryModify projectCategoryModify) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectCategoryModify.getPartnershipIdx());

        List<ProjectCategoryVO> projectCategoryVOList = modelMapper.map(projectCategoryModify.getProjectCategoryList(), new TypeToken<List<ProjectCategoryVO>>() {}.getType());

        // 카테고리 삭제
        if (!CollectionUtils.isEmpty(projectCategoryModify.getDeleteCategoryIdxList())) {
            for (Integer deleteCategoryIdx : projectCategoryModify.getDeleteCategoryIdxList()) {
                // 삭제하려는 카테고리에 파트너쉽 회원 체크
                projectCategoryMapper.selectByProjectCategoryIdxAndPartnershipIdx(deleteCategoryIdx, partnershipMemberVO.getPartnershipIdx())
                        .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));

                // 삭제되는 카테고리에 포함된 프로젝트는 기본 카테고리로 넣어줌
                List<ProjectVO> projectVOList = projectMapper.selectAllByProjectCategoryIdx(deleteCategoryIdx);
                if (!CollectionUtils.isEmpty(projectVOList)) {
                    // 카테고리가 삭제된 프로젝트는 '보관함' 카테고리로 프론트에 보여짐. project_category_idx = null
                    for (ProjectVO projectVO : projectVOList) {
                        projectVO.setProjectCategoryIdx(null);
                        projectMapper.updateProjectCategoryIdxByProjectVO(projectVO);
                    }
                }
                projectCategoryMapper.deleteByIdx(deleteCategoryIdx);
            }
        }

        // 카테고리 추가 & 수정
        for (ProjectCategoryVO projectCategoryVO : projectCategoryVOList) {
            projectCategoryVO.setPartnershipIdx(partnershipMemberVO.getPartnershipIdx());
            if (projectCategoryVO.getIdx() == null) {
                projectCategoryMapper.insertByProjectCategoryVO(projectCategoryVO);
            } else {
                // 수정하려는 카테고리에 파트너쉽 회원 체크
                projectCategoryMapper.selectByProjectCategoryIdxAndPartnershipIdx(projectCategoryVO.getIdx(), partnershipMemberVO.getPartnershipIdx())
                        .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));
                projectCategoryMapper.updateByProjectCategoryVO(projectCategoryVO);
            }
        }

        return CustomResponse.builder()
                .data(projectComponent.createResponseProjectCategory(partnershipMemberVO))
                .build();
    }
}
