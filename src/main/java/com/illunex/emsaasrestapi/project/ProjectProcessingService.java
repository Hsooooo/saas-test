package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.common.code.EnumCode;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.bson.types.ObjectId;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
            projectMapper.updateByProjectVO(vo);
            log.info("[THREAD-SUCCESS] Draft 프로젝트 정제 완료 projectIdx={} {}ms", projectIdx, System.currentTimeMillis()-start);
        }
    }

    /** 공통 빌드 로직: 노드/엣지 초기화 → 시트 돌며 생성 → 저장 */
    private void wipeAndBuildAll(int projectIdx, Workbook wb, List<ExcelSheet> sheets, Project project) throws CustomException {
        // 기존 Node·Edge 삭제 + 테이블 메타 삭제
        Query byProject = Query.query(Criteria.where("_id.projectIdx").is(projectIdx));
        mongoTemplate.remove(byProject, Node.class);
        mongoTemplate.remove(byProject, Edge.class);
        projectTableMapper.deleteAllByProjectIdx(projectIdx);

        for (ExcelSheet sheet : sheets) {
            String sheetName = sheet.getExcelSheetName();
            Sheet ws = wb.getSheet(sheetName);
            if (ws == null) continue;

            ProjectNode nodeDef = null;
            if (project.getProjectNodeList() != null) {
                nodeDef = project.getProjectNodeList().stream()
                        .filter(n -> sheetName.equals(n.getNodeType()))
                        .findFirst().orElse(null);
            }

            ProjectEdge edgeDef = null;
            if (project.getProjectEdgeList() != null) {
                edgeDef = project.getProjectEdgeList().stream()
                        .filter(e -> sheetName.equals(e.getEdgeType()))
                        .findFirst().orElse(null);
            }

            if (nodeDef == null && edgeDef == null) continue;

            List<Node> nodeBatch = new ArrayList<>();
            List<Edge> edgeBatch = new ArrayList<>();
            List<String> columns = sheet.getExcelCellList();
            int colCnt = columns.size();

            ProjectTableVO projectTableVO = new ProjectTableVO();
            projectTableVO.setProjectIdx(projectIdx);
            projectTableVO.setTitle(sheetName);
            projectTableVO.setDataCount(sheet.getTotalRowCnt());
            EnumCode.ProjectTable.TypeCd typeCd = nodeDef != null ? EnumCode.ProjectTable.TypeCd.Node : EnumCode.ProjectTable.TypeCd.Edge;
            projectTableVO.setTypeCd(typeCd.getCode());
            projectTableMapper.insertByProjectTableVO(projectTableVO);

            for (int r = 1; r <= sheet.getTotalRowCnt(); r++) {
                Row row = ws.getRow(r);
                if (row == null || row.getLastCellNum() == -1) break;

                LinkedHashMap<String,Object> props = new LinkedHashMap<>(colCnt);
                for (int c = 0; c < colCnt; c++) {
                    props.put(columns.get(c), projectComponent.getExcelColumnData(row.getCell(c)));
                }

                if (nodeDef != null) { // Node
                    Object key = props.get(nodeDef.getUniqueCellName());
                    if (key != null && !(key instanceof String && ((String) key).trim().isEmpty())) {
                        nodeBatch.add(Node.builder()
                                .nodeId(new NodeId(projectIdx, sheetName, key))
                                .id(key)
                                .label(sheetName)
                                .properties(props)
                                .build());
                    }
                } else {               // Edge
                    Object src = props.get(edgeDef.getSrcEdgeCellName());
                    Object dst = props.get(edgeDef.getDestEdgeCellName());
                    if (src != null && dst != null) {
                        edgeBatch.add(Edge.builder()
                                .edgeId(new EdgeId(projectIdx, sheetName, r))
                                .id(r)
                                .startType(edgeDef.getSrcNodeType())
                                .start(src)
                                .endType(edgeDef.getDestNodeType())
                                .end(dst)
                                .type(sheetName)
                                .properties(props)
                                .build());
                    }
                }
            }

            if (!nodeBatch.isEmpty()) mongoTemplate.insertAll(nodeBatch);
            if (!edgeBatch.isEmpty()) mongoTemplate.insertAll(edgeBatch);
        }
    }
}
