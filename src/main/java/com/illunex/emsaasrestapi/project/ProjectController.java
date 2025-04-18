package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
     * 프로젝트 삭제
     * @param projectId
     * @return
     * @throws CustomException
     */
    @DeleteMapping()
    public CustomResponse<?> deleteProject(@RequestBody RequestProjectDTO.ProjectId projectId) throws CustomException {
        return projectService.deleteProject(projectId);
    }

    /**
     * 프로젝트 조회
     * @param projectIdx
     * @param partnershipIdx
     * @return
     * @throws CustomException
     */
    @GetMapping()
    public CustomResponse<?> getProjectId(@RequestParam(name = "projectIdx") Integer projectIdx,
                                          @RequestParam(name = "partnershipIdx") Integer partnershipIdx) throws CustomException {
        return projectService.getProject(projectIdx, partnershipIdx);
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
}
