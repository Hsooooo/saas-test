package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.project.dto.RequestProjectCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projectCategory")
public class ProjectCategoryController {

    private final ProjectCategoryService categoryService;

    /**
     * 프로젝트 카테고리 조회
     * @return
     * @throws CustomException
     */
    @GetMapping()
    public CustomResponse<?> getProjectCategory(@AuthenticationPrincipal User user) throws CustomException {
        return categoryService.getProjectCategory(user);
    }

    /**
     * 프로젝트 카테고리 추가, 수정, 삭제
     * @param projectCategoryModify
     * @return
     * @throws CustomException
     */
    @PostMapping()
    public CustomResponse<?> updateProjectCategory(@RequestBody RequestProjectCategoryDTO.ProjectCategoryModify projectCategoryModify,
                                                   @AuthenticationPrincipal User user) throws CustomException {
        return categoryService.updateProjectCategory(projectCategoryModify, user);
    }

}
