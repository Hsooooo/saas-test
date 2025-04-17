package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.project.document.Project;
import com.illunex.emsaasrestapi.project.document.ProjectId;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {

    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;

    /**
     * 프로젝트 생성
     * @param project
     * @return
     */
    public CustomResponse<?> createProject(RequestProjectDTO.Project project) throws CustomException {
//        mongoTemplate.find(Query.query(Criteria.where("idx").is(1)), RequestProjectDTO.Project.class);

        // Document 맵핑
        Project mappingProject = modelMapper.map(project, Project.class);

        mongoTemplate.insert(mappingProject);

//        List<Project> result = mongoTemplate.find(Query.query(Criteria.where("nodeList").elemMatch(Criteria.where("nodeType").is("Company"))), Project.class);

        Project result = mongoTemplate.findOne(Query.query(Criteria.where("projectId").is(project.getProjectId())), Project.class);

        if(result == null) {
            throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }

        return CustomResponse.builder()
                .data(modelMapper.map(result, ResponseProjectDTO.Project.class))
                .build();
    }

    /**
     * 프로젝트 삭제
     * @param projectId
     * @return
     */
    public CustomResponse<?> deleteProject(RequestProjectDTO.ProjectId projectId) throws CustomException {
        // 프로젝트 조회
        Project findProject = mongoTemplate.findById(projectId, Project.class);
        if(findProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        return CustomResponse.builder()
                .data(mongoTemplate.remove(findProject))
                .build();
    }

    /**
     * 프로젝트 조회
     * @param projectIdx
     * @param partnershipIdx
     * @return
     */
    public CustomResponse<?> getProject(Integer projectIdx, Integer partnershipIdx) throws CustomException {
        // 프로젝트 조회
        Query query = Query.query(Criteria.where("projectId.projectIdx").is(projectIdx).and("projectId.partnershipIdx").is(partnershipIdx));
        Project findProject = mongoTemplate.findOne(query, Project.class);
        if(findProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        ResponseProjectDTO.Project response = modelMapper.map(findProject, ResponseProjectDTO.Project.class);

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 프로젝트 한번에 수정
     * @param project
     * @return
     */
    public CustomResponse<?> replaceProject(RequestProjectDTO.Project project) throws CustomException {
        // 프로젝트 조회
        Project targetProject = mongoTemplate.findById(project.getProjectId(), Project.class);
        if(targetProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        // 데이터 맵핑
        Project replaceProject = modelMapper.map(project, Project.class);

        // 데이터 덮어쓰기
        UpdateResult result = mongoTemplate.replace(Query.query(Criteria.where("projectId").is(project.getProjectId())), replaceProject);

        if(result.getModifiedCount() == 0) {
            throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }

        return CustomResponse.builder()
                .data(result)
                .build();
    }
}
