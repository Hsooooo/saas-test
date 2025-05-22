package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.project.dto.RequestProjectCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project/category")
public class ProjectCategoryController {

    private final ProjectCategoryService categoryService;

    /**
     * 프로젝트 카테고리 목록 조회
     * @param memberVO
     * @param partnershipIdx
     * @return
     * @throws CustomException
     */
    @GetMapping()
    public CustomResponse<?> getProjectCategory(@CurrentMember MemberVO memberVO,
                                                @RequestParam(name = "partnershipIdx") Integer partnershipIdx) throws CustomException {
        return categoryService.getProjectCategory(memberVO, partnershipIdx);
    }

    /**
     * 프로젝트 카테고리 추가, 수정, 삭제
     * @param memberVO
     * @param projectCategoryModify
     * @return
     * @throws CustomException
     */
    @PostMapping()
    public CustomResponse<?> updateProjectCategory(@CurrentMember MemberVO memberVO,
                                                   @RequestBody RequestProjectCategoryDTO.ProjectCategoryModify projectCategoryModify) throws CustomException {
        return categoryService.updateProjectCategory(memberVO, projectCategoryModify);
    }

}
