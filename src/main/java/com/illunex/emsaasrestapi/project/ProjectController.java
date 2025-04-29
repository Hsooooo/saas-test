package com.illunex.emsaasrestapi.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import lombok.RequiredArgsConstructor;
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
     * @param project
     * @return
     */
    @PostMapping()
    public CustomResponse<?> createProject(@RequestBody RequestProjectDTO.Project project) throws CustomException {
        return projectService.createProject(project);
    }

    /**
     * 프로젝트 상세 조회
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    @GetMapping()
    public CustomResponse<?> getProjectDetail(@RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        return projectService.getProjectDetail(projectIdx);
    }

    /**
     * 프로젝트 수정
     * @param project
     * @return
     * @throws CustomException
     */
    @PutMapping()
    public CustomResponse<?> replaceProject(@RequestBody RequestProjectDTO.Project project) throws CustomException {
        return projectService.replaceProject(project);
    }

    /**
     * 프로젝트 삭제
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    @DeleteMapping()
    public CustomResponse<?> deleteProject(@RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        return projectService.deleteProject(projectIdx);
    }

    /**
     * 프로젝트 데이터 엑셀 파일 업로드
     * @param projectIdx
     * @param excelFile
     * @return
     */
    @PostMapping("upload/single")
    public CustomResponse<?> uploadSingleExcelFile(@RequestParam(name = "projectIdx") Integer projectIdx,
                                                   @RequestPart(name = "excel") MultipartFile excelFile) throws CustomException, IOException {
        return projectService.uploadSingleExcelFile(projectIdx, excelFile);
    }


    /**
     * 프로젝트 카테고리 이동
     * @param projectId
     * @return
     * @throws CustomException
     */
    @PatchMapping("/move")
    public CustomResponse<?> moveProject(@RequestBody List<RequestProjectDTO.ProjectId> projectId) throws CustomException {
        return projectService.moveProject(projectId);
    }

    /**
     * 카테고리별 프로젝트 단순 내용 조회
     * @param projectId
     * @param pageRequest
     * @param sort
     * @return
     * @throws CustomException
     */
    @GetMapping("/list")
    public CustomResponse<?> getProjectList(RequestProjectDTO.ProjectId projectId,
                                            CustomPageRequest pageRequest, String[] sort) throws CustomException {
        return projectService.getProjectList(projectId, pageRequest, sort);
    }

    /**
     * 프로젝트 복제
     * @param projectIds
     * @return
     * @throws CustomException
     */
    @PostMapping("/copy")
    public CustomResponse<?> copyProject(@RequestBody List<RequestProjectDTO.ProjectId> projectIds,
                                         CustomPageRequest pageRequest, String[] sort) throws CustomException, JsonProcessingException {
        return projectService.copyProject(projectIds, pageRequest, sort);
    }
}
