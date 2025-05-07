package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.dto.ResponseMemberDTO;
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
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMemberMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.checkerframework.checker.units.qual.C;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.CountOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
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
                .partnershipIdx(project.getPartnershipIdx())
                .projectCategoryIdx(project.getProjectCategoryIdx())
                .title(project.getTitle())
                .description(project.getDescription())
                .statusCd(EnumCode.Project.StatusCd.MongoDB.getCode())
                .build();
        // 프로젝트 저장
        int insertCnt = projectMapper.insertByProjectVO(projectVO);

        if(insertCnt > 0) {
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
    public CustomResponse<?> getProjectDetail(Integer projectIdx) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크

        return CustomResponse.builder()
                .data(projectComponent.createResponseProject(projectIdx))
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
        // RDB 처리 부분
        ProjectVO projectVO = projectMapper.selectByIdx(project.getProjectIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if(projectVO.getDeleteDate() == null) {
            Integer nodeCount = 0;
            Integer edgeCount = 0;
            // 노드&엣지 정의 정보가 있을 경우 노드&엣지 카운트 뽑기
            if(project.getProjectNodeList() != null && project.getProjectEdgeList() != null) {
                // 노드 정보 개수 만큼 노드 총 개수 추출
                for (RequestProjectDTO.ProjectNode projectNode : project.getProjectNodeList()) {
                    MatchOperation matchOperation = Aggregation.match(
                            Criteria.where("_id.projectIdx")
                                    .is(project.getProjectIdx())
                                    .and("_id.excelSheetName")
                                    .is(projectNode.getNodeType())
                    );

                    CountOperation countOperation = Aggregation.count().as("nodeCount");
                    Aggregation aggregation = Aggregation.newAggregation(matchOperation, countOperation);
                    AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "excel_row", Document.class);
                    nodeCount += results.getUniqueMappedResult().getInteger("nodeCount");
                }
                // 엣지 정보 개수 만큼 엣지 총 개수 추출
                for (RequestProjectDTO.ProjectEdge projectEdge : project.getProjectEdgeList()) {
                    MatchOperation matchOperation = Aggregation.match(
                            Criteria.where("_id.projectIdx")
                                    .is(project.getProjectIdx())
                                    .and("_id.excelSheetName")
                                    .is(projectEdge.getEdgeType())
                    );

                    CountOperation countOperation = Aggregation.count().as("edgeCount");
                    Aggregation aggregation = Aggregation.newAggregation(matchOperation, countOperation);
                    AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "excel_row", Document.class);
                    edgeCount += results.getUniqueMappedResult().getInteger("edgeCount");
                }
            }
            projectVO.setTitle(project.getTitle());
            projectVO.setDescription(project.getDescription());
            projectVO.setNodeCnt(nodeCount);
            projectVO.setEdgeCnt(edgeCount);
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
    public CustomResponse<?> deleteProject(Integer projectIdx) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
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
    public CustomResponse<?> uploadSingleExcelFile(Integer projectIdx, MultipartFile excelFile) throws CustomException, IOException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // 프로젝트 생성 여부 확인
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        // 프로젝트 삭제 여부 확인
        if(projectVO.getDeleteDate() == null) {

            // 엑셀 파싱 및 MongoDB 저장
            projectComponent.parseExcel(projectVO.getIdx(), excelFile);

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
    public CustomResponse<?> completeProject(Integer projectIdx) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // 1. RDB 프로젝트 조회
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if(projectVO.getDeleteDate() == null) {
            // 1. MongoDB에 프로젝트 정보 조회
            Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
            // 2. MongoDB에 엑셀 시트 정보 조회
            Excel excel = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Excel.class);

            if(project == null || excel == null) {
                throw new CustomException(ErrorCode.PROJECT_EMPTY_DATA);
            }

            // 3. MongoDB에 프로젝트번호에 해당하는 Node 정보 모두 삭제
            mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(projectIdx)), Node.class);

            // 4. 프로젝트 정보와 엑셀 시트 정보로 노드 정보 정제 후 생성
            for (ProjectNode projectNode : project.getProjectNodeList()) {
                List<ExcelRow> rowList = mongoTemplate.find(
                        Query.query(
                                Criteria.where("_id.projectIdx").is(projectIdx)
                                        .and("_id.excelSheetName").is(projectNode.getNodeType())
                        ),
                        ExcelRow.class
                );
                rowList.forEach(excelRow -> {
                    Node node = Node.builder()
                            .nodeId(NodeId.builder()
                                    .projectIdx(projectIdx)
                                    .nodeIdx(excelRow.getData().get(projectNode.getUniqueCellName()))       // 노드 고유키
                                    .build()
                            )
                            .id(excelRow.getData().get(projectNode.getUniqueCellName()))                    // 노드 고유키
                            .label(excelRow.getExcelRowId().getExcelSheetName())                            // 노드명
                            .properties(excelRow.getData())
                            .build();
                    // 노드 저장
                    mongoTemplate.save(node);
                });
            }
            // 5. MongoDB에 프로젝트번호에 해당하는 Edge 정보 모두 삭제
            mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(projectIdx)), Edge.class);

            // 6. 프로젝트 정보와 엑셀 시트 정보로 엣지 정보 정제 후 생성
            for (ProjectEdge projectEdge : project.getProjectEdgeList()) {
                List<ExcelRow> rowList = mongoTemplate.find(
                        Query.query(
                                Criteria.where("_id.projectIdx").is(projectIdx)
                                        .and("_id.excelSheetName").is(projectEdge.getEdgeType())
                        ),
                        ExcelRow.class
                );
                rowList.forEach(excelRow -> {
                    Edge edge = Edge.builder()
                            .edgeId(EdgeId.builder()
                                    .projectIdx(projectIdx)
                                    .edgeIdx(excelRow.getExcelRowId().getExcelRowIdx())                     // 엑셀 파싱 시 생성된 고유키
                                    .build()
                            )
                            .id(excelRow.getExcelRowId().getExcelRowIdx())
                            .startType(projectEdge.getSrcNodeType())                                        // 시작 노드 타입
                            .start(excelRow.getData().get(projectEdge.getSrcEdgeCellName()))                // 시작 노드 id
                            .endType(projectEdge.getDestNodeType())                                         // 도착 노드 타입
                            .end(excelRow.getData().get(projectEdge.getDestEdgeCellName()))                 // 도착 노드 id
                            .type(excelRow.getExcelRowId().getExcelSheetName())                             // 엣지명
                            .properties(excelRow.getData())
                            .build();
                    // 엣지 저장
                    mongoTemplate.save(edge);
                });
            }

            return CustomResponse.builder()
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
    public CustomResponse<?> getProjectList(RequestProjectDTO.ProjectId projectId, CustomPageRequest pageRequest, String[] sort) {
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
                        .nodeCnt(projectVO.getNodeCnt())
                        .edgeCnt(projectVO.getEdgeCnt())
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
            copiedProject.setProjectIdx(projectVO.getIdx());

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


    public CustomResponse<?> getProjectNetwork(RequestProjectDTO.ProjectNetwork projectNetworkDto) throws CustomException {

        ResponseProjectDTO.ProjectNetwork response = new ResponseProjectDTO.ProjectNetwork();

        //TODO [PYJ] 노드검색
//        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectNetworkDto.getProjectIdx()));
        Query query = Query.query(Criteria.where("id").is(8));
        List<Node> nodes = mongoTemplate.find(query, Node.class);

        response.setNodes(nodes);

        //TODO [PYJ] 엣지검색
        projectComponent.extendRepeatNetworkSearch(response, nodes, projectNetworkDto.getExtendDepth());

        if (response.getNodes() != null) {
            response.setNodeSize(response.getNodes().size());
        }
        if (response.getEdges() != null) {
            response.setEdgeSize(response.getEdges().size());
        }

        return CustomResponse.builder()
                .data(response)
                .build();
    }
}
