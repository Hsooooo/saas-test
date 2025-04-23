package com.illunex.emsaasrestapi.category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.illunex.emsaasrestapi.category.dto.RequestCategoryDTO;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.project.ProjectService;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("category")
public class CategoryController {

    private final CategoryService categoryService;

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
    public CustomResponse<?> updateProjectCategory(@RequestBody RequestCategoryDTO.ProjectCategoryModify projectCategoryModify,
                                                   @AuthenticationPrincipal User user) throws CustomException {
        return categoryService.updateProjectCategory(projectCategoryModify, user);
    }

}
