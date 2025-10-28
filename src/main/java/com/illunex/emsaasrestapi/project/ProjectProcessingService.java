package com.illunex.emsaasrestapi.project;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.illunex.emsaasrestapi.common.CustomException;
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
import com.alibaba.excel.EasyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.bson.types.ObjectId;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    private static final int LOG_EVERY_FLUSH = 20;

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
                projectVO = projectMapper.selectByIdx(projectIdx).orElseThrow(() ->
                        new IllegalStateException("프로젝트 정보 없음 idx=" + projectIdx));
                ProjectDraft d = draftRepo.get(sessionId);
                if (d == null) throw new IllegalStateException("Draft not found: " + sessionId);
                processProjectFromDraftByEasyExcel(projectVO, d);   // ← 핵심
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

        final int BATCH = 1000;
        DataFormatter fmt = new DataFormatter(Locale.KOREA);
        List<ProjectNode> nodeDefs = project.getProjectNodeList();
        List<ProjectEdge> edgeDefs = project.getProjectEdgeList();

        for (ExcelSheet sheetMeta : sheets) {
            final String sheetName = sheetMeta.getExcelSheetName();
            Sheet ws = wb.getSheet(sheetName);
            if (ws == null) continue;

            // 시트 매핑 대상 탐색
            ProjectNode nodeDef = null;
            if (nodeDefs != null) {
                for (ProjectNode cand : nodeDefs) {
                    if (sheetName.equals(cand.getNodeType())) { nodeDef = cand; break; }
                }
            }
            ProjectEdge edgeDef = null;
            if (edgeDefs != null) {
                for (ProjectEdge cand : edgeDefs) {
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

            // 2) Column 문서 업서트(필수 필드 setOnInsert)
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
                Query colQ = Query.query(Criteria.where("projectIdx").is(projectIdx).and("type").is(sheetName));
                Update colU = new Update()
                        .set("columnDetailList", detailList)
                        .setOnInsert("projectIdx", projectIdx)
                        .setOnInsert("type", sheetName);
                mongoTemplate.upsert(colQ, colU, Column.class);
            }

            // 3) 벌크 INSERT (중복은 시트 전역 Set으로 스킵)
            BulkOperations nodeBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Node.class);
            BulkOperations edgeBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Edge.class);
            int nodeCnt = 0, edgeCnt = 0;
            int nodeFlushSeq = 0, edgeFlushSeq = 0;

            // 시트 전역 중복 차단용 Set (Node 키: uniqueCellName 정규화)
            Set<String> seenNodeKeys = (nodeDef != null)
                    ? new HashSet<>(Math.min(Math.max(sheetMeta.getTotalRowCnt(), 16), 200000))
                    : Collections.emptySet();
            // Edge도 혹시 모를 중복 대비(기본 rowNum 사용)
            Set<Integer> seenEdgeKeys = (edgeDef != null)
                    ? new HashSet<>(Math.min(Math.max(sheetMeta.getTotalRowCnt(), 16), 200000))
                    : Collections.emptySet();

            for (Iterator<Row> it = ws.rowIterator(); it.hasNext();) {
                Row row = it.next();
                if (row.getRowNum() == 0) continue; // 헤더 스킵

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
                    String key = norm(props.get(nodeDef.getUniqueCellName()));
                    if (key == null || key.isEmpty()) continue;
                    if (!seenNodeKeys.add(key)) continue; // 중복 스킵

                    nodeBulk.insert(Node.builder()
                            .nodeId(new NodeId(projectIdx, sheetName, key))
                            .id(key)
                            .label(sheetName)
                            .properties(props)
                            .build());

                    // node flush 시점
                    if (++nodeCnt % BATCH == 0) {
                        silentExecute(nodeBulk);
                        logFlush("node:" + sheetName, nodeCnt, ++nodeFlushSeq);
                        nodeBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Node.class);
                    }
                } else {
                    String src = norm(props.get(edgeDef.getSrcEdgeCellName()));
                    String dst = norm(props.get(edgeDef.getDestEdgeCellName()));
                    if (src == null || dst == null) continue;

                    int rowKey = row.getRowNum();
                    if (!seenEdgeKeys.add(rowKey)) continue; // 혹시 모를 중복 스킵

                    edgeBulk.insert(Edge.builder()
                            .edgeId(new EdgeId(projectIdx, sheetName, rowKey))
                            .id(rowKey)
                            .startType(edgeDef.getSrcNodeType())
                            .start(src)
                            .endType(edgeDef.getDestNodeType())
                            .end(dst)
                            .type(sheetName)
                            .properties(props)
                            .build());

                    // edge flush 시점
                    if (++edgeCnt % BATCH == 0) {
                        silentExecute(edgeBulk);
                        logFlush("edge:" + sheetName, edgeCnt, ++edgeFlushSeq);
                        edgeBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Edge.class);
                    }
                }
            }

            // 잔여 flush
            if (nodeCnt % BATCH != 0) {
                silentExecute(nodeBulk);
                logFlush("node:" + sheetName, nodeCnt, ++nodeFlushSeq);
            }
            if (edgeCnt % BATCH != 0) {
                silentExecute(edgeBulk);
                logFlush("edge:" + sheetName, edgeCnt, ++edgeFlushSeq);
            }
        }
    }

    private static String norm(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC);
        s = s.replace('\u00A0',' ').replaceAll("\\s+"," ");
        return s; // 필요하면 .toLowerCase(Locale.ROOT)
    }

    private static void silentExecute(BulkOperations bulk) {
        try { bulk.execute(); } catch (RuntimeException ignored) { /* no-op */ }
    }

    private static void logFlush(String label, int processed, int flushSeq) {
        if (flushSeq % LOG_EVERY_FLUSH != 0) return;   // 10분의 1로 감소
        log.info("[{}] bulk commit processed={}", label, processed);
    }

    /** Draft 기반 정제: EasyExcel로 스트리밍 파싱 */
    public void processProjectFromDraftByEasyExcel(ProjectVO vo, ProjectDraft draft) throws IOException, CustomException {
        final int projectIdx = vo.getIdx();
        long start = System.currentTimeMillis();

        Excel   excelMeta = draft.getExcelMeta();
        Project project   = draft.getProjectDoc();
        if (excelMeta == null || project == null || excelMeta.getExcelFileList().isEmpty())
            throw new RuntimeException("Draft 정제용 메타 또는 파일 정보 없음");

        ExcelFile meta = excelMeta.getExcelFileList().get(0);

        // S3 → 로컬 임시파일(스트리밍 파서 안정성↑)
        Path tmp = Files.createTempFile("xlsx_", ".xlsx");
        try (InputStream is = awsS3Component.downloadInputStream(meta.getFilePath())) {
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
        }

        // 스트리밍 정제
        wipeAndBuildAllStreamed(projectIdx, tmp.toFile(), excelMeta.getExcelSheetList(), project);

        vo.setStatusCd(EnumCode.Project.StatusCd.Complete.getCode());
        vo.setNodeCnt(draft.getNodeCnt());
        vo.setEdgeCnt(draft.getEdgeCnt());
        projectMapper.updateByProjectVO(vo);
        log.info("[THREAD-SUCCESS] Draft 프로젝트 정제 완료 projectIdx={} {}ms", projectIdx, System.currentTimeMillis()-start);

        // 임시파일 정리
        try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
    }

    /** EasyExcel 기반: 시트 전역 중복 스킵 + 벌크 INSERT + 1/10 로그 */
    private void wipeAndBuildAllStreamed(int projectIdx, File xlsxFile, List<ExcelSheet> sheets, Project project) throws CustomException {
        // 0) 기존 데이터 제거
        Query byProject = Query.query(Criteria.where("_id.projectIdx").is(projectIdx));
        Query byProjectColumn = Query.query(Criteria.where("projectIdx").is(projectIdx));
        mongoTemplate.remove(byProject, Node.class);
        mongoTemplate.remove(byProject, Edge.class);
        mongoTemplate.remove(byProjectColumn, Column.class);
        projectTableMapper.deleteAllByProjectIdx(projectIdx);

        final int BATCH = 1000;
        List<ProjectNode> nodeDefs = project.getProjectNodeList();
        List<ProjectEdge> edgeDefs = project.getProjectEdgeList();

        for (ExcelSheet sheetMeta : sheets) {
            final String sheetName = sheetMeta.getExcelSheetName();
            final List<String> headers = sheetMeta.getExcelCellList();
            final int colCnt = headers.size();
            if (colCnt == 0) continue;

            // 시트 매핑 대상 탐색
            ProjectNode nodeDef = null;
            if (nodeDefs != null) for (ProjectNode cand : nodeDefs) { if (sheetName.equals(cand.getNodeType())) { nodeDef = cand; break; } }
            ProjectEdge edgeDef = null;
            if (edgeDefs != null) for (ProjectEdge cand : edgeDefs) { if (sheetName.equals(cand.getEdgeType())) { edgeDef = cand; break; } }
            if (nodeDef == null && edgeDef == null) continue;

            // 1) project_table 메타
            ProjectTableVO tbl = new ProjectTableVO();
            tbl.setProjectIdx(projectIdx);
            tbl.setTitle(sheetName);
            tbl.setDataCount(sheetMeta.getTotalRowCnt());
            tbl.setTypeCd((nodeDef != null ? EnumCode.ProjectTable.TypeCd.Node : EnumCode.ProjectTable.TypeCd.Edge).getCode());
            projectTableMapper.insertByProjectTableVO(tbl);

            // 2) Column 업서트
            if (!headers.isEmpty()) {
                List<ColumnDetail> detailList = new ArrayList<>(colCnt);
                for (int c = 0; c < colCnt; c++) {
                    String h = headers.get(c);
                    ColumnDetail d = new ColumnDetail();
                    d.setColumnName(h); d.setAlias(h); d.setOrder(c); d.setVisible(true);
                    detailList.add(d);
                }
                Query colQ = Query.query(Criteria.where("projectIdx").is(projectIdx).and("type").is(sheetName));
                Update colU = new Update()
                        .set("columnDetailList", detailList)
                        .setOnInsert("projectIdx", projectIdx)
                        .setOnInsert("type", sheetName);
                mongoTemplate.upsert(colQ, colU, Column.class);
            }

            // 3) 시트 전역 중복 Set
            final int expected = Math.max(sheetMeta.getTotalRowCnt(), 16);
            final Set<String>  seenNodeKeys = (nodeDef != null) ? new HashSet<>(expected) : java.util.Collections.emptySet();
            final Set<Integer> seenEdgeKeys = (edgeDef != null) ? new HashSet<>(expected) : java.util.Collections.emptySet();

            // 4) 벌크 준비(배열 래핑으로 재할당 허용)
            final BulkOperations[] nodeBulk = { mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Node.class) };
            final BulkOperations[] edgeBulk = { mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Edge.class) };
            final int[] nodeCnt = {0}, edgeCnt = {0};
            final int[] nodeFlushSeq = {0}, edgeFlushSeq = {0};
            final int[] rowNum = {0}; // 0=헤더

            // 내부 클래스 캡처용 final 사본
            final ProjectNode nodeDefFinal = nodeDef;
            final ProjectEdge edgeDefFinal = edgeDef;
            final String sheetNameFinal = sheetName;
            final List<String> headersFinal = headers;
            final int colCntFinal = colCnt;
            final int projectIdxFinal = projectIdx;

            // 5) EasyExcel 리스너 — Map<Integer,String> 사용
            ReadListener<java.util.Map<Integer,String>> listener = new ReadListener<>() {
                @Override public void invoke(java.util.Map<Integer,String> row, com.alibaba.excel.context.AnalysisContext ctx) {
                    int r = rowNum[0]++;              // 0부터 증가
                    if (r == 0) return;               // 헤더 스킵

                    // props 구성
                    LinkedHashMap<String,Object> props = new LinkedHashMap<>(colCntFinal);
                    for (int c = 0; c < colCntFinal; c++) {
                        String v = row.get(c);
                        if (v != null && !v.isEmpty()) props.put(headersFinal.get(c), v);
                    }
                    if (props.isEmpty()) return;

                    if (nodeDefFinal != null) {
                        String key = norm(props.get(nodeDefFinal.getUniqueCellName()));
                        if (key == null || key.isEmpty()) return;
                        if (!seenNodeKeys.add(key)) return; // 중복 스킵

                        nodeBulk[0].insert(Node.builder()
                                .nodeId(new NodeId(projectIdxFinal, sheetNameFinal, key))
                                .id(key)
                                .label(sheetNameFinal)
                                .properties(props)
                                .build());

                        if (++nodeCnt[0] % BATCH == 0) {
                            silentExecute(nodeBulk[0]);
                            logFlush("node:" + sheetNameFinal, nodeCnt[0], ++nodeFlushSeq[0]);
                            nodeBulk[0] = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Node.class);
                        }
                    } else {
                        String src = norm(props.get(edgeDefFinal.getSrcEdgeCellName()));
                        String dst = norm(props.get(edgeDefFinal.getDestEdgeCellName()));
                        if (src == null || dst == null) return;

                        int rowKey = r; // 헤더 제외 실제 데이터 행 번호
                        if (!seenEdgeKeys.add(rowKey)) return;

                        edgeBulk[0].insert(Edge.builder()
                                .edgeId(new EdgeId(projectIdxFinal, sheetNameFinal, rowKey))
                                .id(rowKey)
                                .startType(edgeDefFinal.getSrcNodeType())
                                .start(src)
                                .endType(edgeDefFinal.getDestNodeType())
                                .end(dst)
                                .type(sheetNameFinal)
                                .properties(props)
                                .build());

                        if (++edgeCnt[0] % BATCH == 0) {
                            silentExecute(edgeBulk[0]);
                            logFlush("edge:" + sheetNameFinal, edgeCnt[0], ++edgeFlushSeq[0]);
                            edgeBulk[0] = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Edge.class);
                        }
                    }
                }
                @Override public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext ctx) { /* no-op */ }
            };

            // 6) 리더 빌드 + ReadSheet 지정 후 실행
            ExcelReaderBuilder builder = EasyExcel.read(xlsxFile, listener).autoCloseStream(true);
            ExcelReader excelReader = builder.build();

            ReadSheet readSheet = EasyExcel.readSheet(sheetNameFinal)
                    .headRowNumber(1)
                    .build();

            excelReader.read(readSheet);
            excelReader.finish();

            // 7) 잔여 flush
            if (nodeCnt[0] % BATCH != 0) { silentExecute(nodeBulk[0]); logFlush("node:" + sheetNameFinal, nodeCnt[0], ++nodeFlushSeq[0]); }
            if (edgeCnt[0] % BATCH != 0) { silentExecute(edgeBulk[0]); logFlush("edge:" + sheetNameFinal, edgeCnt[0], ++edgeFlushSeq[0]); }
        }
    }
}
