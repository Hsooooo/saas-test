package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.chat.ChatService;
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
import com.illunex.emsaasrestapi.project.document.project.ProjectEdgeCount;
import com.illunex.emsaasrestapi.project.document.project.ProjectNodeCount;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectFileMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMemberMapper;
import com.illunex.emsaasrestapi.project.session.DraftContext;
import com.illunex.emsaasrestapi.project.session.ExcelMetaUtil;
import com.illunex.emsaasrestapi.project.session.ProjectDraft;
import com.illunex.emsaasrestapi.project.session.ProjectDraftRepository;
import com.illunex.emsaasrestapi.project.vo.ProjectFileVO;
import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.mongodb.client.result.UpdateResult;
import jakarta.servlet.http.HttpServletRequest;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import static com.illunex.emsaasrestapi.common.ErrorCode.PROJECT_EMPTY_DATA;
import static com.illunex.emsaasrestapi.common.ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY;

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
    private final ChatService chatService;

    private final ProjectDraftRepository draftRepo;
    private final ExcelMetaUtil excelMetaUtil;

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
                .imagePath(project.getImagePath())
                .imageUrl(project.getImageUrl())
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
     * [Draft] 프로젝트 생성
     * @param me
     * @param project
     * @param dc
     * @return
     * @throws CustomException
     */
    @Transactional
    public CustomResponse<?> createProject(MemberVO me, RequestProjectDTO.Project project,
                                           DraftContext dc) throws CustomException {
        // 1) 세션 없으면 여기서 생성
        ObjectId sid = (dc.getSessionId() != null)
                ? dc.getSessionId()
                : draftRepo.open(null, me.getIdx().longValue(), project);

        // 2) 드래프트 도큐 업서트
        var projDoc = modelMapper.map(project, com.illunex.emsaasrestapi.project.document.project.Project.class);
        projDoc.setCreateDate(null);
        projDoc.setUpdateDate(null);
        draftRepo.upsert(sid, new Update().set("projectDoc", projDoc));
        ProjectDraft d = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(sid)), ProjectDraft.class);

        // 3) 응답으로 sessionId 내려줌 → 프론트는 이후 헤더에 실어 재호출
        return CustomResponse.builder().data(
                projectComponent.createResponseProject(null, d)
        ).build();
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
     * [Draft] 단일 엑셀 파일 업로드
     * @param me
     * @param projectIdx
     * @param excelFile
     * @param dc
     * @return
     * @throws CustomException
     * @throws java.io.IOException
     */
    @Transactional
    public CustomResponse<?> uploadSingleExcelFile(MemberVO me, Integer projectIdx, MultipartFile excelFile,
                                                   DraftContext dc) throws CustomException, java.io.IOException {
        dc.require();

        // S3 업로드 (기존처럼)
        var res = com.illunex.emsaasrestapi.common.aws.dto.AwsS3ResourceDTO.builder()
                .fileName(excelFile.getOriginalFilename())
                .s3Resource(awsS3Component.upload(excelFile, AwsS3Component.FolderType.ProjectFile, "draft/" + dc.getSessionId()))
                .build();

        // 시트 메타만 추출
        Excel excelMeta = excelMetaUtil.buildExcelMeta(res.getOrgFileName(), res.getPath(), res.getUrl(), res.getSize());

        draftRepo.upsert(dc.getSessionId(), new Update()
                .set("excelMeta", excelMeta)
        );

        return CustomResponse.builder().data(java.util.Map.of(
                "sessionId", dc.getSessionId().toHexString(),
                "step", 3,
                "sheets", excelMeta.getExcelSheetList()
        )).build();
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

        List<ProjectNodeCount> nodeCountList = new ArrayList<>();
        List<ProjectEdgeCount> edgeCountList = new ArrayList<>();
        if(projectVO.getDeleteDate() == null) {
            int nodeCount = 0;
            int edgeCount = 0;
            if (project.getProjectNodeList() != null) {
                for (RequestProjectDTO.ProjectNode projectNode : project.getProjectNodeList()) {
                    ProjectNodeCount pnc = new ProjectNodeCount();

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
                    pnc.setType(projectNode.getNodeType());
                    pnc.setCount(count);
                    nodeCount += count;
                    nodeCountList.add(pnc);
                }
            }

            if (project.getProjectEdgeList() != null) {
                for (RequestProjectDTO.ProjectEdge projectEdge : project.getProjectEdgeList()) {
                    ProjectEdgeCount pec = new ProjectEdgeCount();
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
                    pec.setType(projectEdge.getEdgeType());
                    pec.setCount(count);
                    edgeCount += count;
                    edgeCountList.add(pec);
                }
            }
            projectVO.setTitle(project.getTitle());
            projectVO.setDescription(project.getDescription());
            projectVO.setNodeCnt(nodeCount);
            projectVO.setEdgeCnt(edgeCount);
            // 기존 이미지 있는 경우 삭제
            if (projectVO.getImagePath() != null && !projectVO.getImagePath().isEmpty()) {
                if (!projectVO.getImagePath().equals(project.getImagePath()) && !projectVO.getImageUrl().equals(project.getImageUrl())) {
                    // 기존 이미지 삭제
                    awsS3Component.delete(projectVO.getImagePath());
                }
            }
            projectVO.setImagePath(project.getImagePath());
            projectVO.setImageUrl(project.getImageUrl());
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
                replaceProject.setProjectNodeCountList(nodeCountList);
                replaceProject.setProjectEdgeCountList(edgeCountList);
                replaceProject.setMaxNodeSize(project.getMaxNodeSize());

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
     * [Draft] 프로젝트 한번에 수정
     * @param memberVO
     * @param project
     * @param dc
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> replaceProject(MemberVO memberVO, RequestProjectDTO.Project project,
                                            DraftContext dc) throws CustomException {
        if (!dc.hasSession()) {
            // === 세션 없음: 여기서 자동 발급 & 스냅샷 적재 후 sessionId만 리턴 ===
            if (!dc.hasSession()) {
                // 권한 체크
                PartnershipMemberVO pm = partnershipComponent.checkPartnershipMemberAndProject(memberVO, project.getProjectIdx());
                projectComponent.checkProjectMember(project.getProjectIdx(), pm.getIdx());

                // 스냅샷 준비 (RDB + Mongo)
                ProjectVO pvo = projectMapper.selectByIdx(project.getProjectIdx())
                        .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
                var projDoc = mongoTemplate.findById(project.getProjectIdx(),
                        com.illunex.emsaasrestapi.project.document.project.Project.class); // null 허용
                var excelMeta = mongoTemplate.findById(project.getProjectIdx(), Excel.class); // null 허용

                // 세션 발급 + 드래프트 오픈
                var sid = draftRepo.openFromExistingProject(project.getProjectIdx(),
                        memberVO.getIdx().longValue(), pvo, projDoc, excelMeta);

                // 프론트는 이 sessionId 저장 후, 같은 replace를 다시 호출하면 정상 진행됨
                return CustomResponse.builder()
                        .data(projectComponent.createResponseProject(null, draftRepo.get(sid)))
                        .build();
            }
        }
        // --- 여기부터는 세션 존재: 일반 드래프트 업데이트 ---
        dc.require();
        var sid = dc.getSessionId();

        // 1) projectDoc + 얕은 필드 동시 반영
        draftRepo.upsert(sid, buildDraftUpdateFromRequest(project));

        // 2) 노드/엣지 예상 카운트 (엑셀 메타 기반)
        var draft = draftRepo.get(dc.getSessionId());
        int nodeCount = 0, edgeCount = 0;
        List<ProjectNodeCount> nodeCountList = new ArrayList<>();
        List<ProjectEdgeCount> edgeCountList = new ArrayList<>();
        if (draft != null && draft.getExcelMeta() != null) {
            var sheets = draft.getExcelMeta().getExcelSheetList();
            if (project.getProjectNodeList() != null) {
                for (var n : project.getProjectNodeList()) {
                    int count = sheets.stream()
                            .filter(s -> s.getExcelSheetName().equals(n.getNodeType()))
                            .mapToInt(ExcelSheet::getTotalRowCnt).sum();
                    ProjectNodeCount pnc = new ProjectNodeCount();
                    pnc.setType(n.getNodeType());
                    pnc.setCount(count);
                    nodeCount += count;
                    nodeCountList.add(pnc);
                }
            }
            if (project.getProjectEdgeList() != null) {
                for (var e : project.getProjectEdgeList()) {
                    int count = sheets.stream()
                            .filter(s -> s.getExcelSheetName().equals(e.getEdgeType()))
                            .mapToInt(ExcelSheet::getTotalRowCnt).sum();
                    ProjectEdgeCount pec = new ProjectEdgeCount();
                    pec.setType(e.getEdgeType());
                    pec.setCount(count);
                    edgeCount += count;
                    edgeCountList.add(pec);
                }
            }
        }

        return CustomResponse.builder().data(
                projectComponent.createResponseProject(null, draft)
        ).build();
    }

    /**
     * 프로젝트 최종 저장(관계망 데이터 정제 처리)
     * @param projectIdx
     * @return
     */
    @Transactional
    public CustomResponse<?> completeProject(MemberVO memberVO, Integer projectIdx) throws CustomException, IOException {
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
        projectProcessingService.processProject(projectVO);
        // 여기까지 확인됐으면, 워커에 요청만 전달
//        projectProcessingService.processAsync(projectIdx);

        return CustomResponse.builder()
                .build();
    }

    @Transactional
    public CustomResponse<?> completeProject(MemberVO memberVO, Integer projectIdx, DraftContext dc) throws CustomException, IOException {
        dc.require();

        var d = draftRepo.get(dc.getSessionId());

        if (d == null || !"OPEN".equals(d.getStatus())) throw new CustomException(ErrorCode.COMMON_EMPTY);
        if (d.getProjectDoc() == null || d.getExcelMeta() == null)
            throw new CustomException(PROJECT_EMPTY_DATA);

        Integer pid = d.getProjectIdx();
        ProjectVO pvo = null;
        if (pid == null) {
            // 신규 생성: RDB 프로젝트 한 번에 생성
            var info = d.getProjectDoc();
            pvo = com.illunex.emsaasrestapi.project.vo.ProjectVO.builder()
                    .partnershipIdx(d.getPartnershipIdx())
                    .projectCategoryIdx(d.getProjectCategoryIdx())
                    .title(d.getTitle())
                    .description(d.getDescription())
                    .imagePath(d.getImagePath())
                    .imageUrl(d.getImageUrl())
                    .statusCd(com.illunex.emsaasrestapi.common.code.EnumCode.Project.StatusCd.Step5.getCode()) // 정제중
                    .build();
            int ok = projectMapper.insertByProjectVO(pvo);
            if (ok == 0) throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
            // 생성자 멤버 추가 등 네 기존 로직 필요시 수행
            pid = pvo.getIdx();

            // Mongo Project 메타 저장
            var projDoc = d.getProjectDoc();
            projDoc.setProjectIdx(pid);

            var now = java.time.LocalDateTime.now();

            // 1) 기준 쿼리
            Query q = Query.query(Criteria.where("_id").is(pid));

            // 2) POJO → Document 변환
            org.bson.Document doc = new org.bson.Document();
            mongoTemplate.getConverter().write(projDoc, doc);

            // 3) 업데이트 바디에서 _id 제거 (immutable)
            doc.remove("_id");
            doc.remove("id"); // @Id 필드명이 id라면 보호차원에서 같이 제거

            // 4) createDate는 최초 insert시에만, updateDate는 매번 갱신
            Update u = Update.fromDocument(doc)          // doc에 있는 필드를 $set으로 적용
                    .setOnInsert("createDate", now)      // 새 문서에만
                    .set("updateDate", now);             // 항상 갱신

            // 5) 단일 upsert로 끝
            mongoTemplate.upsert(q, u, com.illunex.emsaasrestapi.project.document.project.Project.class);
            // 파트너쉽 회원 여부 체크
            PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, pvo.getIdx());

            // 프로젝트 생성자를 관리자로 프로젝트 구성원에 추가
            ProjectMemberVO projectMemberVO = new ProjectMemberVO();
            projectMemberVO.setProjectIdx(pvo.getIdx());
            projectMemberVO.setPartnershipMemberIdx(partnershipMemberVO.getIdx());
            projectMemberVO.setTypeCd(EnumCode.ProjectMember.TypeCd.Manager.getCode());
            projectMemberMapper.insertByProjectMemberVO(projectMemberVO);
        } else {
            // 수정: 권한 체크만 하고 본 Project 도큐 덮어쓰기
            var pm = partnershipComponent.checkPartnershipMemberAndProject(memberVO, pid);
            projectComponent.checkProjectMember(pid, pm.getIdx());

            var target = mongoTemplate.findById(pid, com.illunex.emsaasrestapi.project.document.project.Project.class);
            if (target == null) throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);

            var replace = d.getProjectDoc();
            replace.setProjectIdx(pid);
            replace.setCreateDate(target.getCreateDate());
            replace.setUpdateDate(java.time.LocalDateTime.now());
            mongoTemplate.save(replace); // replace by _id = pid
            // RDB 타이틀/이미지 등 필요한 필드만 업데이트 (원하면)
            pvo = projectMapper.selectByIdx(pid).orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
            pvo.setTitle(d.getTitle());
            pvo.setDescription(d.getDescription());
            pvo.setImagePath(d.getImagePath());
            pvo.setImageUrl(d.getImageUrl());
            pvo.setStatusCd(com.illunex.emsaasrestapi.common.code.EnumCode.Project.StatusCd.Step5.getCode()); // 정제중
            projectMapper.updateByProjectVO(pvo);
        }

        // Excel 메타 본 컬렉션으로 저장(동기화)
        var excel = d.getExcelMeta();
        excel.setProjectIdx(pid);
        mongoTemplate.findAndRemove(Query.query(Criteria.where("_id").is(pid)), Excel.class);
        mongoTemplate.insert(excel);

        // 워커 비동기 정제 시작 (sessionId 전달해서 S3+매핑 읽게)
//        projectProcessingService.processAsyncWithDraft(pid, dc.getSessionId());
        projectProcessingService.processProjectFromDraft(pvo, d);   // ← 핵심
        log.info("[THREAD-SUCCESS] Draft 정제 완료 projectIdx={}", projectIdx);

        draftRepo.mark(dc.getSessionId(), "COMMITTED");
        return CustomResponse.builder().data(projectComponent.createResponseProject(pid)).build();
    }

    /**
     * 프로젝트 조회 (Draft)
     * @param projectIdx
     * @return
     */
    public CustomResponse<?> getProjectDetail(MemberVO memberVO, Integer projectIdx, DraftContext dc) throws CustomException {
        if (projectIdx != null) {
            return getProjectDetail(memberVO, projectIdx);
        }
        dc.require();

        ProjectDraft d = draftRepo.get(dc.getSessionId()); // 존재 여부 체크용

        return CustomResponse.builder()
                .data(projectComponent.createResponseProject(null, d))
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
        return CustomResponse.builder()
                .data(projectComponent.createResponseProjectDropdown(searchProject, partnershipMemberVO))
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
        if (excel == null) throw new CustomException(PROJECT_EMPTY_DATA);

        // 저장된 엑셀 정보에 존재하는 시트명 여부 확인
        ExcelSheet sheetInfo = excel.getExcelSheetList().stream()
                .filter(s -> s.getExcelSheetName().equals(search.getExcelSheetName()))
                .findFirst()
                .orElseThrow(() -> new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY));

        // 해당 엑셀 파일 workbook 변환
        try (InputStream is = awsS3Component.downloadInputStream(sheetInfo.getFilePath());
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet(sheetInfo.getExcelSheetName());
            if (sheet == null) throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            Row header = sheet.getRow(0);
            if (header == null) throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

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
     * [Draft] 선택 컬럼 범위값 조회
     * @param me
     * @param search
     * @param req
     * @return
     * @throws CustomException
     * @throws IOException
     */
    public CustomResponse<?> getExcelValueRange(MemberVO me, RequestProjectDTO.ProjectExcelSummary search,
                                                HttpServletRequest req) throws CustomException, IOException {
        var dc = DraftContext.from(req);

        dc.require();
        var d = draftRepo.get(dc.getSessionId());
        if (d == null || d.getExcelMeta() == null) throw new CustomException(PROJECT_EMPTY_DATA);

        var sheetInfo = d.getExcelMeta().getExcelSheetList().stream()
                .filter(s -> s.getExcelSheetName().equals(search.getExcelSheetName()))
                .findFirst().orElseThrow(() -> new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY));

        try (var is = awsS3Component.downloadInputStream(sheetInfo.getFilePath());
             var workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheet(sheetInfo.getExcelSheetName());
            if (sheet == null) throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);
            Row header = sheet.getRow(0);
            if (header == null) throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            int colIdx = findColumnIndex(header, search.getExcelCellName());
            Comparable<?> min = null, max = null;
            Class<?> type = null;

            for (int r = 1; r <= sheetInfo.getTotalRowCnt(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || row.getLastCellNum() == -1) continue;

                Cell cell = row.getCell(colIdx);
                Object raw = projectComponent.getExcelColumnData(cell);
                if (!(raw instanceof Comparable<?> comp) || org.springframework.util.ObjectUtils.isEmpty(raw)) continue;

                if (type == null) type = comp.getClass();
                if (!type.isInstance(comp)) continue;

                @SuppressWarnings("unchecked")
                Comparable<Object> current = (Comparable<Object>) comp;
                if (min == null || current.compareTo(min) < 0) min = current;
                if (max == null || current.compareTo(max) > 0) max = current;
            }

            var range = new com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO.ExcelValueRange();
            range.setMax(max); range.setMin(min);
            return com.illunex.emsaasrestapi.common.CustomResponse.builder().data(range).build();
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
        if (excel == null) throw new CustomException(PROJECT_EMPTY_DATA);

        // 저장된 엑셀 정보에 존재하는 시트명 여부 확인
        ExcelSheet sheetInfo = excel.getExcelSheetList().stream()
                .filter(s -> s.getExcelSheetName().equals(search.getExcelSheetName()))
                .findFirst()
                .orElseThrow(() -> new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY));

        // 해당 엑셀 파일 workbook 변환
        try (InputStream is = awsS3Component.downloadInputStream(sheetInfo.getFilePath());
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet(sheetInfo.getExcelSheetName());
            if (sheet == null) throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            Row header = sheet.getRow(0);
            if (header == null) throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

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


    public CustomResponse<?> getExcelValueDistinct(MemberVO memberVO,
                                                   RequestProjectDTO.ProjectExcelSummary search,
                                                   HttpServletRequest req) throws CustomException, IOException {
        var dc = DraftContext.from(req);

        // === Draft 모드 ===
        dc.require();
        var d = draftRepo.get(dc.getSessionId());
        if (d == null || d.getExcelMeta() == null) throw new CustomException(PROJECT_EMPTY_DATA);

        var sheetInfo = d.getExcelMeta().getExcelSheetList().stream()
                .filter(s -> s.getExcelSheetName().equals(search.getExcelSheetName()))
                .findFirst()
                .orElseThrow(() -> new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY));

        try (InputStream is = awsS3Component.downloadInputStream(sheetInfo.getFilePath());
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet(sheetInfo.getExcelSheetName());
            if (sheet == null) throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            Row header = sheet.getRow(0);
            if (header == null) throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

            // 대상 컬럼 인덱스
            int colIdx = findColumnIndex(header, search.getExcelCellName());

            // 빈도수
            Map<String, Integer> freq = new HashMap<>();

            // 저장된 rowCount 만큼 루프
            for (int r = 1; r <= sheetInfo.getTotalRowCnt(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || row.getLastCellNum() == -1) continue;

                Cell cell = row.getCell(colIdx);
                Object raw = projectComponent.getExcelColumnData(cell);
                if (ObjectUtils.isEmpty(raw)) continue;

                String key = raw.toString().trim();
                if (!key.isEmpty()) freq.merge(key, 1, Integer::sum);
            }

            // 상위 20개
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
        throw new CustomException(PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);
    }

    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> uploadProjectImage(MemberVO memberVO, MultipartFile file) throws CustomException, IOException {
        AwsS3ResourceDTO response = AwsS3ResourceDTO.builder()
                .fileName(file.getOriginalFilename())
                .s3Resource(awsS3Component.upload(file, AwsS3Component.FolderType.ProjectImage, memberVO.getIdx().toString()))
                .build();

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 프로젝트에 저장된 엑셀 파일로 ai를 통해 프로젝트 설정 저장
     * @param memberVO
     * @param projectIdx
     * @param dc
     * @return
     */
    public CustomResponse<?> replaceProjectByAi(MemberVO memberVO, Integer projectIdx, DraftContext dc) throws CustomException {
        dc.require();

        var d = draftRepo.get(dc.getSessionId());
        if (d == null || d.getExcelMeta() == null) throw new CustomException(PROJECT_EMPTY_DATA);
        if (d.getProjectDoc() == null) throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        var excel = d.getExcelMeta();

        RequestProjectDTO.Project project = chatService.convertExcelProject(excel.getExcelFileList().get(0).getFileUrl());

        if (!dc.hasSession()) {
            // === 세션 없음: 여기서 자동 발급 & 스냅샷 적재 후 sessionId만 리턴 ===
            if (!dc.hasSession()) {
                // 권한 체크
                PartnershipMemberVO pm = partnershipComponent.checkPartnershipMemberAndProject(memberVO, project.getProjectIdx());
                projectComponent.checkProjectMember(project.getProjectIdx(), pm.getIdx());

                // 스냅샷 준비 (RDB + Mongo)
                ProjectVO pvo = projectMapper.selectByIdx(project.getProjectIdx())
                        .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
                var projDoc = mongoTemplate.findById(project.getProjectIdx(),
                        com.illunex.emsaasrestapi.project.document.project.Project.class); // null 허용
                var excelMeta = mongoTemplate.findById(project.getProjectIdx(), Excel.class); // null 허용

                // 세션 발급 + 드래프트 오픈
                var sid = draftRepo.openFromExistingProject(project.getProjectIdx(),
                        memberVO.getIdx().longValue(), pvo, projDoc, excelMeta);

                // 프론트는 이 sessionId 저장 후, 같은 replace를 다시 호출하면 정상 진행됨
                return CustomResponse.builder()
                        .data(projectComponent.createResponseProject(null, draftRepo.get(sid)))
                        .build();
            }
        }
        dc.require();

        // 1) 드래프트 projectDoc 갱신
        var projDoc = modelMapper.map(project, com.illunex.emsaasrestapi.project.document.project.Project.class);
        projDoc.setUpdateDate(java.time.LocalDateTime.now());
        draftRepo.upsert(dc.getSessionId(), new Update().set("projectDoc", projDoc));

        // 2) 노드/엣지 예상 카운트 (엑셀 메타 기반)
        var draft = draftRepo.get(dc.getSessionId());
        int nodeCount = 0, edgeCount = 0;
        if (draft != null && draft.getExcelMeta() != null) {
            var sheets = draft.getExcelMeta().getExcelSheetList();
            if (project.getProjectNodeList() != null) {
                for (var n : project.getProjectNodeList()) {
                    nodeCount += sheets.stream()
                            .filter(s -> s.getExcelSheetName().equals(n.getNodeType()))
                            .mapToInt(ExcelSheet::getTotalRowCnt).sum();
                }
            }
            if (project.getProjectEdgeList() != null) {
                for (var e : project.getProjectEdgeList()) {
                    edgeCount += sheets.stream()
                            .filter(s -> s.getExcelSheetName().equals(e.getEdgeType()))
                            .mapToInt(ExcelSheet::getTotalRowCnt).sum();
                }
            }
        }

        return CustomResponse.builder().data(
                projectComponent.createResponseProject(null, draft)
        ).build();
    }


    public CustomResponse<?> replaceProjectByAi(MemberVO memberVO, Integer projectIdx) throws CustomException {
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        Excel excel = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Excel.class);
        if (excel == null || excel.getExcelFileList().isEmpty()) {
            throw new CustomException(PROJECT_EMPTY_DATA);
        }
        RequestProjectDTO.Project project = chatService.convertExcelProject(excel.getExcelFileList().get(0).getFileUrl());
        project.setProjectIdx(projectIdx);
        project.setTitle(projectVO.getTitle());
        project.setDescription(projectVO.getDescription());
        project.setImagePath(projectVO.getImagePath());
        project.setImageUrl(projectVO.getImageUrl());
        project.setPartnershipIdx(projectVO.getPartnershipIdx());
        project.setProjectCategoryIdx(projectVO.getProjectCategoryIdx());

        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, project.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(project.getProjectIdx(), partnershipMemberVO.getIdx());

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
            // 기존 이미지 있는 경우 삭제
            if (projectVO.getImagePath() != null && !projectVO.getImagePath().isEmpty()) {
                if (!projectVO.getImagePath().equals(project.getImagePath()) && !projectVO.getImageUrl().equals(project.getImageUrl())) {
                    // 기존 이미지 삭제
                    awsS3Component.delete(projectVO.getImagePath());
                }
            }
            projectVO.setImagePath(project.getImagePath());
            projectVO.setImageUrl(project.getImageUrl());
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

    public CustomResponse<?> getSeesion(MemberVO memberVO, Integer projectIdx, DraftContext dc) throws CustomException {
        // 권한 체크
        PartnershipMemberVO pm = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectIdx);
        projectComponent.checkProjectMember(projectIdx, pm.getIdx());

        // 스냅샷 준비 (RDB + Mongo)
        ProjectVO pvo = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        var projDoc = mongoTemplate.findById(projectIdx,
                com.illunex.emsaasrestapi.project.document.project.Project.class); // null 허용
        var excelMeta = mongoTemplate.findById(projectIdx, Excel.class); // null 허용

        // 세션 발급 + 드래프트 오픈
        var sid = draftRepo.openFromExistingProject(pvo.getIdx(),
                memberVO.getIdx().longValue(), pvo, projDoc, excelMeta);

        // 프론트는 이 sessionId 저장 후, 같은 replace를 다시 호출하면 정상 진행됨
        return CustomResponse.builder()
                .data(projectComponent.createResponseProject(null, draftRepo.get(sid)))
                .build();
    }

    private Update buildDraftUpdateFromRequest(RequestProjectDTO.Project req) {
        Update u = new Update().set("updatedAt", new Date());

        // 1) projectDoc 반영 (널이면 skip)
        if (req != null) {
            // req -> Project 문서 매핑
            var projDoc = modelMapper.map(req, com.illunex.emsaasrestapi.project.document.project.Project.class);
            projDoc.setUpdateDate(java.time.LocalDateTime.now());
            u.set("projectDoc", projDoc);
        }

        // 2) 얕은 필드(메타) 조건부 반영
        if (req.getTitle() != null)                 u.set("title", req.getTitle());
        if (req.getPartnershipIdx() != null)        u.set("partnershipIdx", req.getPartnershipIdx());
        if (req.getProjectCategoryIdx() != null)    u.set("projectCategoryIdx", req.getProjectCategoryIdx());
        if (req.getDescription() != null)           u.set("description", req.getDescription());
        if (req.getImagePath() != null)             u.set("imagePath", req.getImagePath());
        if (req.getImageUrl() != null)              u.set("imageUrl", req.getImageUrl());

        return u;
    }
}
