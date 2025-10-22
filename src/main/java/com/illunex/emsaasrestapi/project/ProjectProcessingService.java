package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.project.document.database.Column;
import com.illunex.emsaasrestapi.project.document.database.ColumnDetail;
import com.illunex.emsaasrestapi.project.document.excel.Excel;
import com.illunex.emsaasrestapi.project.document.excel.ExcelFile;
import com.illunex.emsaasrestapi.project.document.excel.ExcelSheet;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.EdgeId;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.network.NodeId;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectEdge;
import com.illunex.emsaasrestapi.project.document.project.ProjectNode;
import com.illunex.emsaasrestapi.project.mapper.ProjectFileMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectTableMapper;
import com.illunex.emsaasrestapi.project.session.ProjectDraft;
import com.illunex.emsaasrestapi.project.session.ProjectDraftRepository;
import com.illunex.emsaasrestapi.project.vo.ProjectTableVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.mongodb.MongoBulkWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectProcessingService {
    private final MongoTemplate mongoTemplate;
    private final ProjectMapper projectMapper;
    private final ProjectFileMapper projectFileMapper;
    private final ProjectTableMapper projectTableMapper;
    private final TaskExecutor projectExecutor;
    private final ProjectComponent projectComponent;
    private final AwsS3Component awsS3Component;
    private final ProjectDraftRepository draftRepo;

    // 라이브 데이터 기반 비동기 정제
    public void processAsync(Integer projectIdx) {
        log.info("[Thread-start] 프로젝트 정제 작업 시작 projectIdx={}", projectIdx);
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx).orElseThrow();
        if (EnumCode.Project.StatusCd.Step5.getCode().equals(projectVO.getStatusCd())) {
            projectExecutor.execute(() -> {
                try {
                    processProject(projectVO);
                } catch (Exception e) {
                    projectVO.setStatusCd(EnumCode.Project.StatusCd.Fail.getCode());
                    projectMapper.updateByProjectVO(projectVO);
                    log.error("[Thread-FAIL] 프로젝트 정제 실패 projectIdx={}", projectIdx, e);
                }
            });
        }
    }

    /** ✅ 드래프트 기반 비동기 정제 (worker 의존 제거, 인라인 처리) */
    public void processAsyncWithDraft(Integer projectIdx, ObjectId sessionId) {
        log.info("[Thread-start] Draft 정제 시작 projectIdx={}, sessionId={}", projectIdx, sessionId);
        projectExecutor.execute(() -> {
            ProjectVO projectVO = null;
            try {
                projectVO = projectMapper.selectByIdx(projectIdx).orElseThrow();
                ProjectDraft d = draftRepo.get(sessionId);
                if (d == null) throw new IllegalStateException("Draft not found: " + sessionId);
                processProjectFromDraft(projectVO, d);   // ← 핵심
                log.info("[THREAD-SUCCESS] Draft 정제 완료 projectIdx={}", projectIdx);
            } catch (Exception e) {
                log.error("[THREAD-FAIL] Draft 정제 실패 projectIdx={}, sessionId={}", projectIdx, sessionId, e);
                if (projectVO != null) {
                    projectVO.setStatusCd(EnumCode.Project.StatusCd.Fail.getCode());
                    projectMapper.updateByProjectVO(projectVO);
                }
            }
        });
    }

    /** 라이브(본 DB) 메타를 읽어서 정제 */
    public void processProject(ProjectVO vo) throws IOException, CustomException {
        final int projectIdx = vo.getIdx();
        long start = System.currentTimeMillis();

        Excel   excel   = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Excel.class);
        Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
        if (excel == null || project == null || excel.getExcelFileList().isEmpty())
            throw new RuntimeException("정제용 메타 또는 파일 정보 없음");

        ExcelFile meta = excel.getExcelFileList().get(0);
        try (InputStream is = awsS3Component.downloadInputStream(meta.getFilePath());
             Workbook wb = WorkbookFactory.create(is)) {

            wipeAndBuildAll(projectIdx, wb, excel.getExcelSheetList(), project);

            vo.setStatusCd(EnumCode.Project.StatusCd.Complete.getCode());
            projectMapper.updateByProjectVO(vo);
            log.info("[THREAD-SUCCESS] 프로젝트 정제 완료 projectIdx={} {}ms", projectIdx, System.currentTimeMillis()-start);
            System.gc();
        }
    }

    /** ✅ 드래프트 메타를 읽어서 정제 (라이브와 동일 로직, 소스만 draft로) */
    public void processProjectFromDraft(ProjectVO vo, ProjectDraft draft) throws IOException, CustomException {
        final int projectIdx = vo.getIdx();
        long start = System.currentTimeMillis();

        Excel   excelMeta = draft.getExcelMeta();
        Project project   = draft.getProjectDoc();
        if (excelMeta == null || project == null || excelMeta.getExcelFileList().isEmpty())
            throw new RuntimeException("Draft 정제용 메타 또는 파일 정보 없음");

        ExcelFile meta = excelMeta.getExcelFileList().get(0);
        try (InputStream is = awsS3Component.downloadInputStream(meta.getFilePath());
             Workbook wb = WorkbookFactory.create(is)) {

            wipeAndBuildAll(projectIdx, wb, excelMeta.getExcelSheetList(), project);

            vo.setStatusCd(EnumCode.Project.StatusCd.Complete.getCode());
            vo.setNodeCnt(draft.getNodeCnt());
            vo.setEdgeCnt(draft.getEdgeCnt());
            projectMapper.updateByProjectVO(vo);
            log.info("[THREAD-SUCCESS] Draft 프로젝트 정제 완료 projectIdx={} {}ms", projectIdx, System.currentTimeMillis()-start);
            System.gc();
        }
    }

    /**
     * 노드/엣지 초기화 후, 시트별로 노드/엣지/컬럼 문서 생성
     * * MongoDB : Node, Edge, Column
     * * MariaDB : project_table
     * @param projectIdx
     * @param wb
     * @param sheets
     * @param project
     * @throws CustomException
     */
    private void wipeAndBuildAll(int projectIdx, Workbook wb, List<ExcelSheet> sheets, Project project) throws CustomException {
        // 0) 기존 데이터 제거
        Query byProject = Query.query(Criteria.where("_id.projectIdx").is(projectIdx));
        Query byProjectColumn = Query.query(Criteria.where("projectIdx").is(projectIdx));
        mongoTemplate.remove(byProject, Node.class);
        mongoTemplate.remove(byProject, Edge.class);
        mongoTemplate.remove(byProjectColumn, Column.class);
        projectTableMapper.deleteAllByProjectIdx(projectIdx);

        final int BATCH = 2000;
        DataFormatter fmt = new DataFormatter(Locale.KOREA);
        List<ProjectNode> nodeDefs = project.getProjectNodeList();
        List<ProjectEdge> edgeDefs = project.getProjectEdgeList();

        for (int s = 0; s < sheets.size(); s++) {
            ExcelSheet sheetMeta = sheets.get(s);
            final String sheetName = sheetMeta.getExcelSheetName();
            Sheet ws = wb.getSheet(sheetName);
            if (ws == null) continue;

            // 스트림 없이 탐색
            ProjectNode nodeDef = null;
            if (nodeDefs != null) {
                for (int i = 0; i < nodeDefs.size(); i++) {
                    ProjectNode cand = nodeDefs.get(i);
                    if (sheetName.equals(cand.getNodeType())) { nodeDef = cand; break; }
                }
            }
            ProjectEdge edgeDef = null;
            if (edgeDefs != null) {
                for (int i = 0; i < edgeDefs.size(); i++) {
                    ProjectEdge cand = edgeDefs.get(i);
                    if (sheetName.equals(cand.getEdgeType())) { edgeDef = cand; break; }
                }
            }
            if (nodeDef == null && edgeDef == null) continue;

            List<String> headers = sheetMeta.getExcelCellList();
            final int colCnt = headers.size();

            // 1) project_table 메타
            ProjectTableVO tbl = new ProjectTableVO();
            tbl.setProjectIdx(projectIdx);
            tbl.setTitle(sheetName);
            tbl.setDataCount(sheetMeta.getTotalRowCnt());
            tbl.setTypeCd((nodeDef != null ? EnumCode.ProjectTable.TypeCd.Node : EnumCode.ProjectTable.TypeCd.Edge).getCode());
            projectTableMapper.insertByProjectTableVO(tbl);

            // 2) Column 문서 (시트당 1회)
            if (!headers.isEmpty()) {
                List<ColumnDetail> detailList = new ArrayList<>(colCnt);
                for (int c = 0; c < colCnt; c++) {
                    ColumnDetail d = new ColumnDetail();
                    String h = headers.get(c);
                    d.setColumnName(h);
                    d.setAlias(h);
                    d.setOrder(c);
                    d.setVisible(true);
                    detailList.add(d);
                }
                Column colDoc = new Column();
                colDoc.setProjectIdx(projectIdx);
                colDoc.setType(sheetName);
                colDoc.setColumnDetailList(detailList);
                mongoTemplate.insert(colDoc);
            }

            // 3) 벌크 작성 (언오더드, 소배치)
            BulkOperations nodeBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Node.class);
            BulkOperations edgeBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Edge.class);
            int nodeCnt = 0, edgeCnt = 0;

            // 배치 한정 중복 필터 (전역 Set 제거)
            Set<Object> seenKeysBatch = new HashSet<>(BATCH * 2);

            for (Iterator<Row> it = ws.rowIterator(); it.hasNext();) {
                Row row = it.next();
                if (row.getRowNum() == 0) continue; // 헤더행 스킵

                // props 구축: 빈값 스킵, fmt 재사용
                LinkedHashMap<String, Object> props = new LinkedHashMap<>();
                for (int c = 0; c < colCnt; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell == null) continue;
                    String v = fmt.formatCellValue(cell);
                    if (v == null || v.isEmpty()) continue;
                    props.put(headers.get(c), v);
                }
                if (props.isEmpty()) continue;

                if (nodeDef != null) {
                    Object key = props.get(nodeDef.getUniqueCellName());
                    if (key == null) continue;
                    if (!seenKeysBatch.add(key)) continue;

                    nodeBulk.insert(Node.builder()
                            .nodeId(new NodeId(projectIdx, sheetName, key))
                            .id(key)
                            .label(sheetName)
                            .properties(props)
                            .build());

                    if (++nodeCnt % BATCH == 0) {
                        try { nodeBulk.execute(); } catch (MongoBulkWriteException ignored) {}
                        nodeBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Node.class);
                        seenKeysBatch.clear();
                    }
                } else {
                    Object src = props.get(edgeDef.getSrcEdgeCellName());
                    Object dst = props.get(edgeDef.getDestEdgeCellName());
                    if (src == null || dst == null) continue;

                    edgeBulk.insert(Edge.builder()
                            .edgeId(new EdgeId(projectIdx, sheetName, row.getRowNum()))
                            .id(row.getRowNum())
                            .startType(edgeDef.getSrcNodeType())
                            .start(src)
                            .endType(edgeDef.getDestNodeType())
                            .end(dst)
                            .type(sheetName)
                            .properties(props)
                            .build());

                    if (++edgeCnt % BATCH == 0) {
                        try { edgeBulk.execute(); } catch (MongoBulkWriteException ignored) {}
                        edgeBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Edge.class);
                    }
                }
            }

            // 잔여 flush
            if (nodeCnt % BATCH != 0) { try { nodeBulk.execute(); } catch (MongoBulkWriteException ignored) {} }
            if (edgeCnt % BATCH != 0) { try { edgeBulk.execute(); } catch (MongoBulkWriteException ignored) {} }

            // 큰 객체 레퍼런스 해제
            seenKeysBatch.clear();
        }
    }
}
