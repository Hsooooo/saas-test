package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
