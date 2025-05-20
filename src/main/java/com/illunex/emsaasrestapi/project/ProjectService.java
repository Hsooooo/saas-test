package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.common.aws.dto.AwsS3ResourceDTO;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.dto.ResponseMemberDTO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.document.excel.Excel;
import com.illunex.emsaasrestapi.project.document.excel.ExcelRow;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.EdgeId;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.network.NodeId;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectEdge;
import com.illunex.emsaasrestapi.project.document.project.ProjectNode;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectFileMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMemberMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectFileVO;
import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectFileMapper projectFileMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;

    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;
    private final PartnershipComponent partnershipComponent;
    private final ProjectProcessingService projectProcessingService;
    private final ProjectComponent projectComponent;
    private final AwsS3Component awsS3Component;

    /**
     * 프로젝트 생성
     * @param project
     * @return
     */
    @Transactional
    public CustomResponse<?> createProject(MemberVO memberVO, RequestProjectDTO.Project project) throws CustomException {
        // RDB 처리 부분
        ProjectVO projectVO = ProjectVO.builder()
                .partnershipIdx(project.getPartnershipIdx())
                .projectCategoryIdx(project.getProjectCategoryIdx())
                .title(project.getTitle())
                .description(project.getDescription())
                .statusCd(EnumCode.Project.StatusCd.Created.getCode())
                .build();
        // 프로젝트 저장
        int insertCnt = projectMapper.insertByProjectVO(projectVO);

        if(insertCnt > 0) {
            // 파트너쉽 회원 여부 체크
            PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectVO.getIdx());

            // MongoDB 처리 부분
            // Document 맵핑
            Project mappingProject = modelMapper.map(project, Project.class);
            // projectIdx 업데이트
            mappingProject.setProjectIdx(projectVO.getIdx());

            // 기존 데이터 확인
            Project mongoResult = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectVO.getIdx())), Project.class);
            if (mongoResult == null) {
                // 없을 경우 추가
                mappingProject.setCreateDate(LocalDateTime.now());
                mongoTemplate.insert(mappingProject);
            } else {
                // 있을 경우 업데이트
                mappingProject.setUpdateDate(LocalDateTime.now());
                mappingProject.setCreateDate(mongoResult.getCreateDate());
                mongoTemplate.replace(Query.query(Criteria.where("_id").is(projectVO.getIdx())), mappingProject);
            }

            // 프로젝트 생성자를 관리자로 프로젝트 구성원에 추가
            ProjectMemberVO projectMemberVO = new ProjectMemberVO();
            projectMemberVO.setProjectIdx(projectVO.getIdx());
            projectMemberVO.setPartnershipMemberIdx(partnershipMemberVO.getIdx());
            projectMemberVO.setTypeCd(EnumCode.ProjectMember.TypeCd.Manager.getCode());
            projectMemberMapper.insertByProjectMemberVO(projectMemberVO);

            return CustomResponse.builder()
                    .data(projectComponent.createResponseProject(projectVO.getIdx()))
                    .build();
        }
        throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
    }

    /**
     * 프로젝트 조회
     * @param projectIdx
     * @return
     */
    public CustomResponse<?> getProjectDetail(MemberVO memberVO, Integer projectIdx) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        return CustomResponse.builder()
                .data(projectComponent.createResponseProject(projectIdx))
                .build();
    }

    /**
     * 프로젝트 한번에 수정
     * @param project
     * @return
     */
    public CustomResponse<?> replaceProject(MemberVO memberVO, RequestProjectDTO.Project project) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, project.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        // RDB 처리 부분
        ProjectVO projectVO = projectMapper.selectByIdx(project.getProjectIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if(projectVO.getDeleteDate() == null) {
            int nodeCount = 0;
            int edgeCount = 0;
            if (project.getProjectNodeList() != null) {
                for (RequestProjectDTO.ProjectNode projectNode : project.getProjectNodeList()) {
                    MatchOperation match = Aggregation.match(Criteria.where("_id").is(project.getProjectIdx()));
                    UnwindOperation unwind = Aggregation.unwind("excelSheetList");
                    MatchOperation sheetMatch = Aggregation.match(
                            Criteria.where("excelSheetList.excelSheetName").is(projectNode.getNodeType())
                    );
                    GroupOperation group = Aggregation.group("_id")
                            .sum("excelSheetList.totalRowCnt").as("nodeCount");

                    Aggregation aggregation = Aggregation.newAggregation(match, unwind, sheetMatch, group);
                    AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "excel", Document.class);

                    int count = Optional.ofNullable(results.getUniqueMappedResult())
                            .map(doc -> doc.getInteger("nodeCount"))
                            .orElse(0);
                    nodeCount += count;
                }
            }

            if (project.getProjectEdgeList() != null) {
                for (RequestProjectDTO.ProjectEdge projectEdge : project.getProjectEdgeList()) {
                    MatchOperation match = Aggregation.match(Criteria.where("_id").is(project.getProjectIdx()));
                    UnwindOperation unwind = Aggregation.unwind("excelSheetList");
                    MatchOperation sheetMatch = Aggregation.match(
                            Criteria.where("excelSheetList.excelSheetName").is(projectEdge.getEdgeType())
                    );
                    GroupOperation group = Aggregation.group("_id")
                            .sum("excelSheetList.totalRowCnt").as("edgeCount");

                    Aggregation aggregation = Aggregation.newAggregation(match, unwind, sheetMatch, group);
                    AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "excel", Document.class);

                    int count = Optional.ofNullable(results.getUniqueMappedResult())
                            .map(doc -> doc.getInteger("edgeCount"))
                            .orElse(0);
                    edgeCount += count;
                }
            }
            projectVO.setTitle(project.getTitle());
            projectVO.setDescription(project.getDescription());
            projectVO.setNodeCnt(nodeCount);
            projectVO.setEdgeCnt(edgeCount);
            // 프로젝트 상태 변경
            projectVO.setStatusCd(projectComponent.getProjectStatusCd(project, projectVO));
            projectVO.setUpdateDate(ZonedDateTime.now());
            // 프로젝트 업데이트
            int updateCnt = projectMapper.updateByProjectVO(projectVO);

            if (updateCnt > 0) {
                // 프로젝트 데이터 조회
                Project targetProject = mongoTemplate.findById(project.getProjectIdx(), Project.class);
                if (targetProject == null) {
                    throw new CustomException(ErrorCode.COMMON_EMPTY);
                }
                // 데이터 맵핑
                Project replaceProject = modelMapper.map(project, Project.class);
                replaceProject.setUpdateDate(LocalDateTime.now());
                replaceProject.setCreateDate(targetProject.getCreateDate());

                // 데이터 덮어쓰기
                UpdateResult result = mongoTemplate.replace(Query.query(Criteria.where("_id").is(project.getProjectIdx())), replaceProject);

                return CustomResponse.builder()
                        .data(projectComponent.createResponseProject(projectVO.getIdx()))
                        .build();
            }
            throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }

        // 프로젝트 삭제 예외 응답
        throw new CustomException(ErrorCode.PROJECT_DELETED);
    }

    /**
     * 프로젝트 삭제
     * @param projectIdx
     * @return
     */
    @Transactional
    public CustomResponse<?> deleteProject(MemberVO memberVO, Integer projectIdx) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        // RDB 프로젝트 조회
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        // MongoDB 프로젝트 조회
        Project findProject = mongoTemplate.findById(projectIdx, Project.class);
        if(projectVO == null || findProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }
        // 프로젝트 삭제일 저장
        int deleteCnt = projectMapper.updateByDeleteDate(projectIdx);

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
    public CustomResponse<?> uploadSingleExcelFile(MemberVO memberVO, Integer projectIdx, MultipartFile excelFile) throws CustomException, IOException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        // 프로젝트 생성 여부 확인
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        // 프로젝트 삭제 여부 확인
        if(projectVO.getDeleteDate() == null) {
            // s3 업로드
            AwsS3ResourceDTO awsS3ResourceDTO = AwsS3ResourceDTO.builder()
                    .fileName(excelFile.getOriginalFilename())
                    .s3Resource(awsS3Component.upload(excelFile, AwsS3Component.FolderType.ProjectFile, projectIdx.toString()))
                    .build();
            ProjectFileVO projectFileVO = new ProjectFileVO();
            projectFileVO.setProjectIdx(projectIdx);
            projectFileVO.setFileName(awsS3ResourceDTO.getFileName());
            projectFileVO.setFileUrl(awsS3ResourceDTO.getUrl());
            projectFileVO.setFilePath(awsS3ResourceDTO.getPath());
            projectFileVO.setFileSize(awsS3ResourceDTO.getSize());
            projectFileVO.setFileCd(EnumCode.ProjectFile.FileCd.Single.getCode());
            Integer updateCnt = projectFileMapper.insertByProjectFileVO(projectFileVO);

            // 엑셀 파싱 및 MongoDB 저장
            projectComponent.parseExcel(projectVO.getIdx(), excelFile, projectFileVO);
            // 프로젝트 상태 변경(엑셀 업로드)
            projectVO.setStatusCd(EnumCode.Project.StatusCd.Step1.getCode());
            projectMapper.updateByProjectVO(projectVO);

            return CustomResponse.builder()
                    .data(projectComponent.createResponseProjectExcel(projectIdx))
                    .build();
        }

        // 프로젝트 삭제 예외 응답
        throw new CustomException(ErrorCode.PROJECT_DELETED);
    }

    /**
     * 프로젝트 최종 저장(관계망 데이터 정제 처리)
     * @param projectIdx
     * @return
     */
    @Transactional
    public CustomResponse<?> completeProject(MemberVO memberVO, Integer projectIdx) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        // 1. RDB 프로젝트 조회
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if(projectVO.getDeleteDate() != null) {
            throw new CustomException(ErrorCode.PROJECT_DELETED);
        }

        Excel excel = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Excel.class);
        if (excel == null || excel.getExcelFileList().isEmpty()) {
            throw new RuntimeException("정제에 필요한 엑셀 파일 정보 없음");
        }

        // 여기까지 확인됐으면, 워커에 요청만 전달
        projectProcessingService.processAsync(projectIdx);

        return CustomResponse.builder()
                .build();
    }

    /**
     * 프로젝트 카테고리 이동
     * @param projectId
     * @return
     * @throws CustomException
     */
    @Transactional
    public CustomResponse<?> moveProject(MemberVO memberVO, List<RequestProjectDTO.ProjectId> projectId) throws CustomException {
        for(RequestProjectDTO.ProjectId dto : projectId){
            // 파트너쉽 회원 여부 체크
            PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, dto.getProjectIdx());
            // 프로젝트 구성원 여부 체크
            projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

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
     * 카테고리에 속한 프로젝트 목록 조회
     * @param projectCategoryIdx
     * @param pageRequest
     * @param sort
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getProjectList(MemberVO memberVO, Integer projectCategoryIdx, CustomPageRequest pageRequest, String[] sort) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember2(memberVO, projectCategoryIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        Pageable pageable = pageRequest.of(sort);
        // 프로젝트 조회
        List<ProjectVO> projectList = projectMapper.selectAllByPartnershipIdxAndProjectCategoryIdx(partnershipMemberVO.getPartnershipIdx() ,projectCategoryIdx, pageable);
        Integer totalProjectList = projectMapper.countAllByPartnershipIdxAndProjectCategoryIdx(partnershipMemberVO.getPartnershipIdx(), projectCategoryIdx);

        List<ResponseProjectDTO.ProjectPreview> result = projectList.stream().map(projectVO ->
                ResponseProjectDTO.ProjectPreview.builder()
                        .partnershipIdx(projectVO.getPartnershipIdx())
                        .categoryIdx(projectVO.getProjectCategoryIdx())
                        .projectIdx(projectVO.getIdx())
                        .title(projectVO.getTitle())
                        .nodeCnt(projectVO.getNodeCnt())
                        .edgeCnt(projectVO.getEdgeCnt())
                        .createDate(projectVO.getCreateDate())
                        .updateDate(projectVO.getUpdateDate())
                        .statusCd(projectVO.getStatusCd())
                        .build()
        ).toList();

        for(ResponseProjectDTO.ProjectPreview projectPreview : result){
            // 프로젝트 구성원 조회
            List<PartnershipMemberVO> projectMemberList = partnershipMemberMapper.selectAllByProjectIdx(projectPreview.getProjectIdx());
            List<ResponseMemberDTO.Member> members = modelMapper.map(projectMemberList, new TypeToken<List<ResponseMemberDTO.Member>>(){}.getType());
            projectPreview.setMembers(members);
        }

        return CustomResponse.builder()
                .data(new PageImpl<>(result, pageable, totalProjectList))
                .build();
    }

    /**
     * 프로젝트 복제
     * @param memberVO
     * @param projectIds
     * @param pageRequest
     * @param sort
     * @return
     * @throws CustomException
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> copyProject(MemberVO memberVO, List<RequestProjectDTO.ProjectId> projectIds, CustomPageRequest pageRequest, String[] sort) throws CustomException {
        for(RequestProjectDTO.ProjectId projectId : projectIds){
            // 파트너쉽 회원 여부 체크
            PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectId.getProjectIdx());
            // 프로젝트 구성원 여부 체크
            projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

            // MariaDB 프로젝트 조회
            ProjectVO projectVO = projectMapper.selectByIdx(projectId.getProjectIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));


            // MariaDB 프로젝트 복제
            ProjectVO copiedProjectVO = modelMapper.map(projectVO, ProjectVO.class);
            copiedProjectVO.setIdx(null);
            copiedProjectVO.setTitle(copiedProjectVO.getTitle() + "(복사)");
            int insertCnt = projectMapper.insertByProjectVO(copiedProjectVO);
            if (insertCnt == 0) {
                throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
            }

            // MariaDB 프로젝트 멤버 복제
            List<ProjectMemberVO> projectMemberList = projectMemberMapper.selectAllByProjectIdx(projectId.getProjectIdx());
            for (ProjectMemberVO projectMemberVO : projectMemberList) {
                ProjectMemberVO copiedProjectMemberVO = modelMapper.map(projectMemberVO, ProjectMemberVO.class);
                copiedProjectMemberVO.setIdx(null);
                copiedProjectMemberVO.setProjectIdx(copiedProjectVO.getIdx());
                int insertMemberCnt = projectMemberMapper.insertByProjectMemberVO(copiedProjectMemberVO);
                if (insertMemberCnt == 0) {
                    throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
                }
            }

            // MongoDB 프로젝트 조회
            Project findProject = mongoTemplate.findById(projectId.getProjectIdx(), Project.class);
            if(findProject == null) {
                throw new CustomException(ErrorCode.COMMON_EMPTY);
            }
            // MongoDB 프로젝트 복제
            Project copiedProject = modelMapper.map(findProject, Project.class);
            copiedProject.setProjectIdx(projectVO.getIdx());
            // MongoDB 저장
            mongoTemplate.save(copiedProject);

            // MongoDB 노드 조회
            List<Node> findNodeList = mongoTemplate.find(
                    Query.query(
                            Criteria.where("_id.projectIdx").is(projectId.getProjectIdx())
                    ),
                    Node.class
            );
            // Node 데이터 삭제
            mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(copiedProjectVO.getIdx())), Node.class);
            // 조회된 노드 프로젝트 번호 변경 후 저장
            findNodeList.forEach(node -> {
                node.setNodeId(NodeId.builder()
                        .projectIdx(copiedProjectVO.getIdx())
                        .nodeIdx(node.getId())
                        .build());
                mongoTemplate.insert(node);
            });

            // MongoDB 엣지 조회
            List<Edge> findEdgeList = mongoTemplate.find(
                    Query.query(
                            Criteria.where("_id.projectIdx").is(projectId.getProjectIdx())
                    ),
                    Edge.class
            );
            // Edge 데이터 삭제
            mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(copiedProjectVO.getIdx())), Edge.class);
            // 조회된 노드 프로젝트 번호 변경 후 저장
            findEdgeList.forEach(edge -> {
                edge.setEdgeId(EdgeId.builder()
                        .projectIdx(copiedProjectVO.getIdx())
                        .edgeIdx(edge.getId())
                        .build());
                mongoTemplate.insert(edge);
            });
        }

        return CustomResponse.builder()
                .data(projectIds.size())
                .build();
    }
}
