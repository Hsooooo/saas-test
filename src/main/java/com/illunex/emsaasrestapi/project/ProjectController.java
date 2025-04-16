package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("project")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("test")
    public CustomResponse<?> testMongoDB() {
        return projectService.testMongoDB();
    }
}
