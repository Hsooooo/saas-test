package com.illunex.emsaasrestapi.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("project")
public class ProjectController {
    private final ProjectService projectService;

    /**
     * 프로젝트 생성
     * @param memberVO
     * @param project
     * @return
     */
    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> createProject(@CurrentMember MemberVO memberVO,
                                           @RequestBody RequestProjectDTO.Project project) throws CustomException {
        return projectService.createProject(memberVO, project);
    }

    /**
     * 프로젝트 상세 조회
     * @param memberVO
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getProjectDetail(@CurrentMember MemberVO memberVO,
                                              @RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        return projectService.getProjectDetail(memberVO, projectIdx);
    }

    /**
     * 프로젝트 수정
     * @param memberVO
     * @param project
     * @return
     * @throws CustomException
     */
    @PutMapping()
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> replaceProject(@CurrentMember MemberVO memberVO,
                                            @RequestBody RequestProjectDTO.Project project) throws CustomException {
        return projectService.replaceProject(memberVO, project);
    }

    /**
     * 프로젝트 삭제
     * @param memberVO
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    @DeleteMapping()
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> deleteProject(@CurrentMember MemberVO memberVO,
                                           @RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        return projectService.deleteProject(memberVO, projectIdx);
    }

    /**
     * 프로젝트 데이터 엑셀 파일 업로드
     * @param memberVO
     * @param projectIdx
     * @param excelFile
     * @return
     */
    @PostMapping("upload/single")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> uploadSingleExcelFile(@CurrentMember MemberVO memberVO,
                                                   @RequestParam(name = "projectIdx") Integer projectIdx,
                                                   @RequestPart(name = "excel") MultipartFile excelFile) throws CustomException, IOException {
        return projectService.uploadSingleExcelFile(memberVO, projectIdx, excelFile);
    }

    /**
     * 프로젝트 최종 저장(관계망 데이터 정제 처리)
     * @param memberVO
     * @param projectIdx
     * @return
     */
    @PostMapping("complete")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> completeProject(@CurrentMember MemberVO memberVO,
                                             @RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        return projectService.completeProject(memberVO, projectIdx);
    }


    /**
     * 프로젝트 카테고리 이동
     * @param memberVO
     * @param projectId
     * @return
     * @throws CustomException
     */
    @PatchMapping("/move")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> moveProject(@CurrentMember MemberVO memberVO,
                                         @RequestBody List<RequestProjectDTO.ProjectId> projectId) throws CustomException {
        return projectService.moveProject(memberVO, projectId);
    }

    /**
     * 카테고리에 속한 프로젝트 목록 조회
     * @param memberVO
     * @param projectId
     * @param pageRequest
     * @param sort
     * @return
     * @throws CustomException
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getProjectList(@CurrentMember MemberVO memberVO,
                                            Integer projectCategoryIdx,
                                            CustomPageRequest pageRequest, String[] sort) throws CustomException {
        return projectService.getProjectList(memberVO, projectCategoryIdx, pageRequest, sort);
    }

    /**
     * 프로젝트 복제
     * @param memberVO
     * @param projectIds
     * @return
     * @throws CustomException
     */
    @PostMapping("/copy")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> copyProject(@CurrentMember MemberVO memberVO,
                                         @RequestBody List<RequestProjectDTO.ProjectId> projectIds,
                                         CustomPageRequest pageRequest, String[] sort) throws CustomException, JsonProcessingException {
        return projectService.copyProject(memberVO, projectIds, pageRequest, sort);
    }
}
