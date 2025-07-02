package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.common.aws.dto.AwsS3ResourceDTO;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.dto.ResponseMemberDTO;
import com.illunex.emsaasrestapi.member.mapper.MemberMapper;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.document.excel.Excel;
import com.illunex.emsaasrestapi.project.document.excel.ExcelSheet;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.EdgeId;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.network.NodeId;
import com.illunex.emsaasrestapi.project.document.project.Project;
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
import org.apache.poi.ss.usermodel.*;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectFileMapper projectFileMapper;
    private final MemberMapper memberMapper;

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
            PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectVO.getIdx());

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
     * 단일 엑셀 파일 업로드
     * @param projectIdx
     * @param excelFile
     * @return
     */
    @Transactional
    public CustomResponse<?> uploadSingleExcelFile(MemberVO memberVO, Integer projectIdx, MultipartFile excelFile) throws CustomException, IOException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(projectIdx, partnershipMemberVO.getIdx());

        // 프로젝트 생성 여부 확인
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        // 프로젝트 삭제 여부 확인
        if(projectVO.getDeleteDate() == null) {
            // 등록된 엑셀 파일 찾아서 있을 경우 S3 삭제 및 데이터 삭제 처리
            List<ProjectFileVO> projectFileVOList = projectFileMapper.selectAllByProjectIdx(projectIdx);
            for (ProjectFileVO projectFileVO : projectFileVOList) {
                awsS3Component.delete(projectFileVO.getFilePath());
            }
            Integer deleteCnt = projectFileMapper.deleteAllByProjectIdx(projectIdx);

            // s3 업로드
            AwsS3ResourceDTO awsS3ResourceDTO = AwsS3ResourceDTO.builder()
                    .fileName(excelFile.getOriginalFilename())
                    .s3Resource(awsS3Component.upload(excelFile, AwsS3Component.FolderType.ProjectFile, projectIdx.toString()))
                    .build();
            ProjectFileVO projectFileVO = new ProjectFileVO();
            projectFileVO.setProjectIdx(projectIdx);
            projectFileVO.setFileName(awsS3ResourceDTO.getOrgFileName());
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
     * 프로젝트 한번에 수정
     * @param project
     * @return
     */
    public CustomResponse<?> replaceProject(MemberVO memberVO, RequestProjectDTO.Project project) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, project.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(project.getProjectIdx(), partnershipMemberVO.getIdx());

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
     * 프로젝트 최종 저장(관계망 데이터 정제 처리)
     * @param projectIdx
     * @return
     */
    @Transactional
    public CustomResponse<?> completeProject(MemberVO memberVO, Integer projectIdx) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(projectIdx, partnershipMemberVO.getIdx());

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

        // 프로젝트 상태 정제중으로 변경
        projectVO.setStatusCd(EnumCode.Project.StatusCd.Step5.getCode());
        projectMapper.updateByProjectVO(projectVO);

        // 여기까지 확인됐으면, 워커에 요청만 전달
        projectProcessingService.processAsync(projectIdx);

        return CustomResponse.builder()
                .build();
    }

    /**
     * 프로젝트 조회
     * @param projectIdx
     * @return
     */
    public CustomResponse<?> getProjectDetail(MemberVO memberVO, Integer projectIdx) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(projectIdx, partnershipMemberVO.getIdx());

        return CustomResponse.builder()
                .data(projectComponent.createResponseProject(projectIdx))
                .build();
    }

    /**
     * 카테고리에 속한 프로젝트 목록 조회
     * @param searchProject
     * @param pageRequest
     * @param sort
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getProjectList(MemberVO memberVO, RequestProjectDTO.SearchProject searchProject, CustomPageRequest pageRequest, String[] sort) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, searchProject.getPartnershipIdx());

        Pageable pageable = pageRequest.of(sort);
        // 프로젝트 목록 조회
        List<ProjectVO> projectList = projectMapper.selectAllBySearchProjectListAndPartnershipMemberIdx(searchProject, partnershipMemberVO.getIdx(), pageable);
        Integer totalProjectCount = projectMapper.countAllBySearchProjectListAndPartnershipMemberIdx(searchProject, partnershipMemberVO.getIdx());

        List<ResponseProjectDTO.ProjectListItem> response = modelMapper.map(projectList, new TypeToken<List<ResponseProjectDTO.ProjectListItem>>(){}.getType());

        for(ResponseProjectDTO.ProjectListItem projectListItem : response){
            // 프로젝트 구성원 조회
            List<MemberVO> projectMemberList = memberMapper.selectByProjectIdx(projectListItem.getIdx());
            List<ResponseMemberDTO.Member> members = modelMapper.map(projectMemberList, new TypeToken<List<ResponseMemberDTO.Member>>(){}.getType());
            projectListItem.setMembers(members);
        }

        return CustomResponse.builder()
                .data(new PageImpl<>(response, pageable, totalProjectCount.longValue()))
                .build();
    }

    /**
     * 카테고리에 속한 프로젝트 목록 조회 (드롭다운용)
     * @param searchProject
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getProjectListDropdown(MemberVO memberVO, RequestProjectDTO.SearchProject searchProject) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, searchProject.getPartnershipIdx());

        // 프로젝트 목록 조회
        List<ProjectVO> projectList = projectMapper.selectAllBySearchProjectListAndPartnershipMemberIdxNotPaging(searchProject, partnershipMemberVO.getIdx());

        List<ResponseProjectDTO.ProjectListItem> response = modelMapper.map(projectList, new TypeToken<List<ResponseProjectDTO.ProjectListItem>>(){}.getType());

        for(ResponseProjectDTO.ProjectListItem projectListItem : response){
            // 프로젝트 구성원 조회
            List<MemberVO> projectMemberList = memberMapper.selectByProjectIdx(projectListItem.getIdx());
            List<ResponseMemberDTO.Member> members = modelMapper.map(projectMemberList, new TypeToken<List<ResponseMemberDTO.Member>>(){}.getType());
            projectListItem.setMembers(members);
        }

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 프로젝트 삭제
     * @param projectIdxList
     * @return
     */
    @Transactional
    public CustomResponse<?> deleteProject(MemberVO memberVO, List<Integer> projectIdxList) throws CustomException {
        int deleteCnt = 0;
        for(Integer projectIdx : projectIdxList) {
            // 파트너쉽 회원 여부 체크
            PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectIdx);
            // 프로젝트 구성원 여부 체크
            projectComponent.checkProjectMember(projectIdx, partnershipMemberVO.getIdx());

            // RDB 프로젝트 조회
            ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

//            // MongoDB 프로젝트 조회
//            Project findProject = mongoTemplate.findById(projectIdxList, Project.class);
//            if(projectVO == null || findProject == null) {
//                throw new CustomException(ErrorCode.COMMON_EMPTY);
//            }
            // 프로젝트 삭제일 저장
            deleteCnt += projectMapper.updateByDeleteDate(projectIdx);
        }

        return CustomResponse.builder()
                .data(deleteCnt)
                .build();
    }

    /**
     * 프로젝트 복제
     * @param memberVO
     * @param proejectIdxList
     * @return
     * @throws CustomException
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> copyProject(MemberVO memberVO, List<Integer> proejectIdxList) throws CustomException {
        for(Integer projectIdx : proejectIdxList){
            // 파트너쉽 회원 여부 체크
            PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectIdx);
            // 프로젝트 구성원 여부 체크
            projectComponent.checkProjectMember(projectIdx, partnershipMemberVO.getIdx());

            // MariaDB 프로젝트 조회
            ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
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
            List<ProjectMemberVO> projectMemberList = projectMemberMapper.selectAllByProjectIdx(projectIdx);
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
            Project findProject = mongoTemplate.findById(projectIdx, Project.class);
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
                            Criteria.where("_id.projectIdx").is(projectIdx)
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
                            Criteria.where("_id.projectIdx").is(projectIdx)
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
                .data(proejectIdxList.size())
                .build();
    }

    /**
     * 프로젝트 카테고리 이동
     * @param projectIdList
     * @return
     * @throws CustomException
     */
    @Transactional
    public CustomResponse<?> moveProject(MemberVO memberVO, List<RequestProjectDTO.ProjectId> projectIdList) throws CustomException {
        for(RequestProjectDTO.ProjectId projectId : projectIdList){
            // 파트너쉽 회원 여부 체크
            PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectId.getProjectIdx());
            // 프로젝트 구성원 여부 체크
            projectComponent.checkProjectMember(projectId.getProjectIdx(), partnershipMemberVO.getIdx());

            //maraiDB
            ProjectVO projectVO = ProjectVO.builder()
                    .idx(projectId.getProjectIdx())
                    .projectCategoryIdx(projectId.getProjectCategoryIdx())
                    .build();

            projectMapper.updateProjectCategoryIdxByProjectVO(projectVO);
        }

        return CustomResponse.builder()
                .data(null)
                .message("카테고리 이동 되었습니다.")
                .build();
    }

    /**
     * 선택 컬럼 범위값 조회
     * @param memberVO
     * @param search
     * @return
     * @throws CustomException
     * @throws IOException
     */
    public CustomResponse<?> getExcelValueRange(MemberVO memberVO, RequestProjectDTO.ProjectExcelSummary search) throws CustomException, IOException {
        // 파트너쉽 회원 여부 체크
        partnershipComponent.checkPartnershipMemberAndProject(memberVO, search.getProjectIdx());
        long start = System.currentTimeMillis();

        // 엑셀 정보 조회
        Excel excel = mongoTemplate.findById(search.getProjectIdx(), Excel.class);
        if (excel == null) throw new CustomException(ErrorCode.PROJECT_EMPTY_DATA);

        // 저장된 엑셀 정보에 존재하는 시트명 여부 확인
        ExcelSheet sheetInfo = excel.getExcelSheetList().stream()
                .filter(s -> s.getExcelSheetName().equals(search.getExcelSheetName()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY));

        // 해당 엑셀 파일 workbook 변환
        try (InputStream is = awsS3Component.downloadInputStream(sheetInfo.getFilePath());
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet(sheetInfo.getExcelSheetName());
            if (sheet == null) throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            Row header = sheet.getRow(0);
            if (header == null) throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            // 해당 컬럼명의 idx값 추출
            int colIdx = findColumnIndex(header, search.getExcelCellName());

            Comparable<?> min = null, max = null;
            Class<?> type = null;

            // 저장된 row count만큼 loop
            for (int r = 1; r <= sheetInfo.getTotalRowCnt(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || row.getLastCellNum() == -1) continue;

                // 해당 컬럼의 row값 추출
                Cell cell = row.getCell(colIdx);
                Object raw = projectComponent.getExcelColumnData(cell);
                // 빈 값이면 skip
                if (!(raw instanceof Comparable<?> comp) || ObjectUtils.isEmpty(raw)) continue;

                // 최초에 잡힌 변수타입으로 비교변수타입 설정
                if (type == null) type = comp.getClass();
                // 최초에 설정된 변수타입과 다른경우 skip
                if (!type.isInstance(comp)) continue;

                @SuppressWarnings("unchecked")
                Comparable<Object> current = (Comparable<Object>) comp;

                if (min == null || current.compareTo(min) < 0) min = current;
                if (max == null || current.compareTo(max) > 0) max = current;
            }

            ResponseProjectDTO.ExcelValueRange range = new ResponseProjectDTO.ExcelValueRange();
            range.setMax(max);
            range.setMin(min);
            log.info("Excel summary type={} {}ms", search.getType(), System.currentTimeMillis()-start);
            return CustomResponse.builder().data(range).build();
        }
    }


    /**
     * 선택 컬럼 선택값 상위 20개 조회
     * @param memberVO
     * @param search
     * @return
     * @throws CustomException
     * @throws IOException
     */
    public CustomResponse<?> getExcelValueDistinct(MemberVO memberVO, RequestProjectDTO.ProjectExcelSummary search) throws CustomException, IOException {
        // 파트너쉽 회원 여부 체크
        partnershipComponent.checkPartnershipMemberAndProject(memberVO, search.getProjectIdx());

        long start = System.currentTimeMillis();
        // 엑셀 정보 조회
        Excel excel = mongoTemplate.findById(search.getProjectIdx(), Excel.class);
        if (excel == null) throw new CustomException(ErrorCode.PROJECT_EMPTY_DATA);

        // 저장된 엑셀 정보에 존재하는 시트명 여부 확인
        ExcelSheet sheetInfo = excel.getExcelSheetList().stream()
                .filter(s -> s.getExcelSheetName().equals(search.getExcelSheetName()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY));

        // 해당 엑셀 파일 workbook 변환
        try (InputStream is = awsS3Component.downloadInputStream(sheetInfo.getFilePath());
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet(sheetInfo.getExcelSheetName());
            if (sheet == null) throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            Row header = sheet.getRow(0);
            if (header == null) throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            // 해당 컬럼명의 idx값 추출
            int colIdx = findColumnIndex(header, search.getExcelCellName());

            // 빈도수 저장 Map
            Map<String, Integer> freq = new HashMap<>();

            // 저장된 row Count 만큼 loop
            for (int r = 1; r <= sheetInfo.getTotalRowCnt(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || row.getLastCellNum() == -1) continue;

                Cell cell = row.getCell(colIdx);
                Object raw = projectComponent.getExcelColumnData(cell);
                if (ObjectUtils.isEmpty(raw)) continue;

                String key = raw.toString().trim();
                freq.merge(key, 1, Integer::sum);
            }

            // 저장된 빈도수가 잦은 순대로 정렬 후 20개 cut
            List<ResponseProjectDTO.ExcelValueDistinctItem> list = freq.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(20)
                    .map(e -> {
                        ResponseProjectDTO.ExcelValueDistinctItem item = new ResponseProjectDTO.ExcelValueDistinctItem();
                        item.setValue(e.getKey());
                        item.setCount(e.getValue());
                        return item;
                    })
                    .toList();

            ResponseProjectDTO.ExcelValueDistinct distinct = new ResponseProjectDTO.ExcelValueDistinct();
            distinct.setList(list);
            log.info("Excel summary type={} {}ms", search.getType(), System.currentTimeMillis()-start);
            return CustomResponse.builder().data(distinct).build();
        }
    }

    /**
     * header row 에서 조회하고자 하는 col idx 추출
     * @param header
     * @param columnName
     * @return
     * @throws CustomException
     */
    private int findColumnIndex(Row header, String columnName) throws CustomException {
        for (int i = 0; i < header.getLastCellNum(); i++) {
            Cell c = header.getCell(i);
            if (c != null && columnName.equals(c.getStringCellValue().trim())) {
                return i;
            }
        }
        throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);
    }

}
