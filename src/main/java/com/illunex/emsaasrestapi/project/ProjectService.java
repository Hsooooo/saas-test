package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.dto.ResponseMemberDTO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectCategoryMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMemberMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;

    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;
    private final ProjectComponent projectComponent;

    /**
     * 프로젝트 생성
     * @param project
     * @return
     */
    @Transactional
    public CustomResponse<?> createProject(RequestProjectDTO.Project project) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // RDB 처리 부분
        ProjectVO projectVO = ProjectVO.builder()
                .partnershipIdx(project.getProjectId().getPartnershipIdx())
                .projectCategoryIdx(project.getProjectId().getProjectCategoryIdx())
                .title(project.getTitle())
                .description(project.getDescription())
                .statusCd(EnumCode.Project.StatusCd.MongoDB.getCode())
                .build();
        // 프로젝트 저장
        int insertCnt = projectMapper.insertByProjectVO(projectVO);

        if(insertCnt > 0) {
            // MongoDB 처리 부분
//            mongoTemplate.find(Query.query(Criteria.where("idx").is(1)), RequestProjectDTO.Project.class);
            // Document 맵핑
            Project mappingProject = modelMapper.map(project, Project.class);
            // projectIdx 업데이트
            mappingProject.getProjectId().setProjectIdx(projectVO.getIdx());

            // 기존 데이터 확인
            Project result = mongoTemplate.findOne(Query.query(Criteria.where("projectId").is(mappingProject.getProjectId())), Project.class);
            if (result == null) {
                // 없을 경우 추가
                mongoTemplate.insert(mappingProject);
            } else {
                // 있을 경우 업데이트
                mongoTemplate.replace(Query.query(Criteria.where("projectId").is(mappingProject.getProjectId())), mappingProject);
            }
            // 변경 된 데이터 조회
            result = mongoTemplate.findOne(Query.query(Criteria.where("projectId").is(mappingProject.getProjectId())), Project.class);
//            List<Project> result = mongoTemplate.find(Query.query(Criteria.where("nodeList").elemMatch(Criteria.where("nodeType").is("Company"))), Project.class);

            return CustomResponse.builder()
                    .data(modelMapper.map(result, ResponseProjectDTO.Project.class))
                    .build();
        }
        throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
    }

    /**
     * 프로젝트 조회
     * @param projectIdx
     * @param partnershipIdx
     * @return
     */
    public CustomResponse<?> getProject(Integer projectIdx, Integer partnershipIdx) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // 3. 프로젝트 조회
        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.partnershipIdx").is(partnershipIdx));
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
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
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

    /**
     * 프로젝트 삭제
     * @param projectId
     * @return
     */
    @Transactional
    public CustomResponse<?> deleteProject(RequestProjectDTO.ProjectId projectId) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // RDB 프로젝트 조회
        ProjectVO projectVO = projectMapper.selectByProjectCategoryIdxAndProjectIdx(projectId.getProjectCategoryIdx(), projectId.getProjectIdx());
        // MongoDB 프로젝트 조회
        Project findProject = mongoTemplate.findById(projectId, Project.class);
        if(projectVO == null || findProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }
        // 프로젝트 삭제일 저장
        int deleteCnt = projectMapper.updateByDeleteDate(projectId.getProjectIdx());

        return CustomResponse.builder()
                .data(deleteCnt)
                .build();
    }

    /**
     * 단일 엑셀 파일 업로드
     * @param projectIdx
     * @param excelFile
     * @return
     */
    @Transactional
    public CustomResponse<?> uploadSingleExcelFile(Integer projectIdx, MultipartFile excelFile) throws CustomException, IOException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // 프로젝트 생성 여부 확인
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

        // 프로젝트 삭제 여부 확인
        if(projectVO.getDeleteDate() == null) {

            // 엑셀 파싱 및 MongoDB 저장
            projectComponent.parseExcel(projectVO.getIdx(), excelFile);

            // 응답 데이터 조회
            ResponseProjectDTO.Excel response = projectComponent.responseProjectData(projectIdx);

            return CustomResponse.builder()
                    .data(response)
                    .build();
        }

        // 프로젝트 삭제 예외 응답
        throw new CustomException(ErrorCode.PROJECT_DELETED);
    }

    /**
     * 프로젝트 카테고리 이동
     * @param projectId
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> moveProject(List<RequestProjectDTO.ProjectId> projectId) {
        for(RequestProjectDTO.ProjectId dto : projectId){
            //maraiDB
            ProjectVO projectVO = ProjectVO.builder()
                    .idx(dto.getProjectIdx())
                    .projectCategoryIdx(dto.getProjectCategoryIdx())
                    .build();

            projectMapper.updateProjectCategoryIdxByProjectVO(projectVO);
        }

        return CustomResponse.builder()
                .data(null)
                .message("카테고리 이동 되었습니다.")
                .build();
    }

    /**
     * 카테고리별 프로젝트 단순 내용 조회
     * @param projectId
     * @param pageRequest
     * @param sort
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> selectProject(RequestProjectDTO.ProjectId projectId, CustomPageRequest pageRequest, String[] sort) {
        Pageable pageable = pageRequest.of(sort);
        // 프로젝트 조회
        List<ProjectVO> projectList = projectMapper.selectAllByProjectId(projectId, pageable);
        Integer totalProjectList = projectMapper.countAllByProjectId(projectId);

        List<ResponseProjectDTO.ProjectPreview> result = projectList.stream().map(projectVO ->
                ResponseProjectDTO.ProjectPreview.builder()
                        .partnershipIdx(projectVO.getPartnershipIdx())
                        .categoryIdx(projectVO.getProjectCategoryIdx())
                        .projectIdx(projectVO.getIdx())
                        .title(projectVO.getTitle())
                        .createDate(projectVO.getCreateDate())
                        .updateDate(projectVO.getUpdateDate())
                        .statusCd(projectVO.getStatusCd())
                        .build()
        ).toList();

        for(ResponseProjectDTO.ProjectPreview projectPreview : result){
            // TODO [PYJ] : 노드 개수 추가 필요
            // TODO [PYJ] : 엣지 개수 추가 필요
            // 프로젝트 구성원 조회
            List<PartnershipMemberVO> projectMemberList = partnershipMemberMapper.selectAllByProjectIdx(projectPreview.getProjectIdx());
            List<ResponseMemberDTO.Member> members = modelMapper.map(projectMemberList, new TypeToken<List<ResponseMemberDTO.Member>>(){}.getType());
            projectPreview.setMembers(members);
        }

        return CustomResponse.builder()
                .data(new PageImpl<>(result, pageable, totalProjectList))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> copyProject(List<RequestProjectDTO.ProjectId> projectIds, CustomPageRequest pageRequest, String[] sort) throws CustomException {
        for(RequestProjectDTO.ProjectId projectId : projectIds){
            // MariaDB 프로젝트 조회
            ProjectVO projectVO = projectMapper.selectByIdx(projectId.getProjectIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));


            // MariaDB 프로젝트 복제
            ProjectVO copiedProjectVO = modelMapper.map(projectVO, ProjectVO.class);
            copiedProjectVO.setIdx(null);
            int insertCnt = projectMapper.insertByProjectVO(copiedProjectVO);
            if (insertCnt == 0) {
                throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
            }

            // MariaDB 프로젝트 멤버 복제
            List<ProjectMemberVO> projectMemberList = projectMemberMapper.selectAllByProjectIdx(projectId.getProjectIdx());
            for (ProjectMemberVO projectMemberVO : projectMemberList) {
                ProjectMemberVO copiedProjectMemberVO = modelMapper.map(projectMemberVO, ProjectMemberVO.class);
                copiedProjectMemberVO.setIdx(null);
                int insertMemberCnt = projectMemberMapper.insertByProjectMemberVO(copiedProjectMemberVO);
                if (insertMemberCnt == 0) {
                    throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
                }
            }

            // TODO[JCW] : 노드 및 엣지 추가 필요

            // MongoDB 조회
            Project findProject = mongoTemplate.findById(projectId, Project.class);
            if(findProject == null) {
                throw new CustomException(ErrorCode.COMMON_EMPTY);
            }

            // MongoDB 복제
            Project copiedProject = modelMapper.map(findProject, Project.class);
            copiedProject.getProjectId().setProjectIdx(projectVO.getIdx());

            // MongoDB 저장
            mongoTemplate.save(copiedProject);
        }

        Pageable pageable = pageRequest.of(sort);
        Integer totalProjectList = projectMapper.countAllByProjectId(projectIds.get(0));

        List<ProjectVO> result = projectMapper.selectAllByProjectId(projectIds.get(0), pageable);
        List<ResponseProjectDTO.ProjectPreview> response = result.stream().map(projectVO ->
                ResponseProjectDTO.ProjectPreview.builder()
                        .partnershipIdx(projectVO.getPartnershipIdx())
                        .categoryIdx(projectVO.getProjectCategoryIdx())
                        .projectIdx(projectVO.getIdx())
                        .title(projectVO.getTitle())
                        .createDate(projectVO.getCreateDate())
                        .updateDate(projectVO.getUpdateDate())
                        .statusCd(projectVO.getStatusCd())
                        .build()
        ).toList();

        for(ResponseProjectDTO.ProjectPreview projectPreview : response){
            // TODO [JCW] : 노드 개수 추가 필요
            // TODO [JCW] : 엣지 개수 추가 필요
            // 프로젝트 구성원 조회
            List<PartnershipMemberVO> projectMemberList = partnershipMemberMapper.selectAllByProjectIdx(projectPreview.getProjectIdx());
            List<ResponseMemberDTO.Member> members = modelMapper.map(projectMemberList, new TypeToken<List<ResponseMemberDTO.Member>>(){}.getType());
            projectPreview.setMembers(members);
        }

        return CustomResponse.builder()
                .data(new PageImpl<>(response, pageable, totalProjectList))
                .build();
    }
}
