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
     * 프로젝트 이미지 업로드
     * @param file
     * @return
     */
    @PostMapping ("/image")
    public CustomResponse<?> updateProjectImage(@RequestPart(name = "image") MultipartFile file,
                                                @CurrentMember MemberVO memberVO) throws CustomException, IOException {
        return projectService.uploadProjectImage(memberVO, file);
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
     * 프로젝트 최종 저장(관계망 데이터 정제 처리)
     * @param memberVO
     * @param projectIdx
     * @return
     */
    @PostMapping("complete")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> completeProject(@CurrentMember MemberVO memberVO,
                                             @RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException, IOException {
        return projectService.completeProject(memberVO, projectIdx);
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
     * 카테고리에 속한 프로젝트 목록 조회
     * @param memberVO
     * partnershipIdx
     * @param searchProject
     * @param sort
     * @return
     * @throws CustomException
     */
    @PatchMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getProjectList(@CurrentMember MemberVO memberVO,
                                            @RequestBody RequestProjectDTO.SearchProject searchProject,
                                            CustomPageRequest pageRequest, String[] sort) throws CustomException {
        return projectService.getProjectList(memberVO, searchProject, pageRequest, sort);
    }

    /**
     * 카테고리에 속한 프로젝트 목록 조회 (드롭다운용)
     * @param memberVO
     * partnershipIdx
     * @param searchProject
     * @return
     * @throws CustomException
     */
    @PatchMapping("/dropdown")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getProjectListDropdown(@CurrentMember MemberVO memberVO,
                                                    @RequestBody RequestProjectDTO.SearchProject searchProject) throws CustomException {
        return projectService.getProjectListDropdown(memberVO, searchProject);
    }

    /**
     * 프로젝트 삭제
     * @param memberVO
     * @param proejectIdxList
     * @return
     * @throws CustomException
     */
    @DeleteMapping()
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> deleteProject(@CurrentMember MemberVO memberVO,
                                           @RequestBody List<Integer> proejectIdxList) throws CustomException {
        return projectService.deleteProject(memberVO, proejectIdxList);
    }

    /**
     * 프로젝트 복제
     * @param memberVO
     * @param proejectIdxList
     * @return
     * @throws CustomException
     */
    @PostMapping("/copy")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> copyProject(@CurrentMember MemberVO memberVO,
                                         @RequestBody List<Integer> proejectIdxList) throws CustomException, JsonProcessingException {
        return projectService.copyProject(memberVO, proejectIdxList);
    }

    /**
     * 프로젝트 카테고리 이동
     * @param memberVO
     * @param projectIdList
     * @return
     * @throws CustomException
     */
    @PatchMapping("/move")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> moveProject(@CurrentMember MemberVO memberVO,
                                         @RequestBody List<RequestProjectDTO.ProjectId> projectIdList) throws CustomException {
        return projectService.moveProject(memberVO, projectIdList);
    }

    /**
     * 엑셀 컬럼 요약 조회
     * @param memberVO
     * @param search
     * @return
     * @throws CustomException
     * @throws Exception
     */
    @PostMapping("/excel/summary")
    public CustomResponse<?> projectExcelSummary(@CurrentMember MemberVO memberVO,
                                                @RequestBody RequestProjectDTO.ProjectExcelSummary search) throws CustomException, Exception {
        if ("range".equals(search.getType())) {
            return projectService.getExcelValueRange(memberVO, search);
        } else if ("distinct".equals(search.getType())) {
            return projectService.getExcelValueDistinct(memberVO, search);
        }
        return CustomResponse.builder().build();
    }
}
