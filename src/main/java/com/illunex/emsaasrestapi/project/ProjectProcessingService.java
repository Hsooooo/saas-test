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
import com.illunex.emsaasrestapi.project.vo.ProjectTableVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
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

    // 멀티스레드 프로젝트 정제 비동기 작업 처리
    public void processAsync(Integer projectIdx) {
        log.info("[Thread-start] 프로젝트 정제 작업 시작 projectIdx={}", projectIdx);
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow();
        if (projectVO.getStatusCd().equals(EnumCode.Project.StatusCd.Step5.getCode())) {
            projectExecutor.execute(() -> {
                try {
                    processProject(projectVO);
                } catch (Exception e) {
                    projectVO.setStatusCd(EnumCode.Project.StatusCd.Fail.getCode());
                    projectMapper.updateByProjectVO(projectVO);
                    log.error("[Thread-FAIl] 프로젝트 정제 실패 projectIdx={}", projectIdx, e);
                }
            });
        }
    }

    private void processProject(ProjectVO vo) throws IOException, CustomException {
        final int projectIdx = vo.getIdx();
        long start = System.currentTimeMillis();

        /* 1. 엑셀 메타 & 프로젝트 정의 로드 */
        Excel   excel   = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Excel.class);
        Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);
        if (excel == null || project == null || excel.getExcelFileList().isEmpty())
            throw new RuntimeException("정제용 메타 또는 파일 정보 없음");

        /* 2. S3 → Workbook */
        ExcelFile meta   = excel.getExcelFileList().get(0);
        try (InputStream is = awsS3Component.downloadInputStream(meta.getFilePath())) {
            Workbook wb = WorkbookFactory.create(is);

            /* 3. 기존 Node·Edge 삭제 */
            Query byProject = Query.query(Criteria.where("_id.projectIdx").is(projectIdx));
            mongoTemplate.remove(byProject, Node.class);
            mongoTemplate.remove(byProject, Edge.class);

            /* 4. 시트 단위로 Node 또는 Edge 생성 */
            for (ExcelSheet sheet : excel.getExcelSheetList()) {
                String sheetName = sheet.getExcelSheetName();
                Sheet ws = wb.getSheet(sheetName);
                if (ws == null) continue;

                /* 4-1. 이 시트가 Node 정의인지 Edge 정의인지 결정 */
                ProjectNode nodeDef = project.getProjectNodeList().stream()
                        .filter(n -> n.getNodeType().equals(sheetName))
                        .findFirst().orElse(null);

                ProjectEdge edgeDef = project.getProjectEdgeList().stream()
                        .filter(e -> e.getEdgeType().equals(sheetName))
                        .findFirst().orElse(null);

                if (nodeDef == null && edgeDef == null) continue;

                /* 4-2. Row 루프 → Node/Edge 리스트 빌드 */
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
                    if (row == null || row.getLastCellNum() == -1) {
                        break;
                    }

                    /* properties Map 구성 */
                    LinkedHashMap<String,Object> props = new LinkedHashMap<>(colCnt);
                    for (int c = 0; c < colCnt; c++) {
                        props.put(columns.get(c), projectComponent.getExcelColumnData(row.getCell(c)));
                    }

                    if (nodeDef != null) {              // Node 시트
                        Object key = props.get(nodeDef.getUniqueCellName());
                        if (key != null) {
                            nodeBatch.add(Node.builder()
                                    .nodeId(new NodeId(projectIdx, sheetName, key))
                                    .id(key)
                                    .label(sheetName)
                                    .properties(props)
                                    .build());
                        }
                    } else {                            // Edge 시트
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

                /* 4-3. Mongo bulk insert */
                if (!nodeBatch.isEmpty()) mongoTemplate.insertAll(nodeBatch);
                if (!edgeBatch.isEmpty()) mongoTemplate.insertAll(edgeBatch);
            }

            /* 5. 상태 완료 */
            vo.setStatusCd(EnumCode.Project.StatusCd.Complete.getCode());
            projectMapper.updateByProjectVO(vo);
            log.info("[THREAD-SUCCESS] 프로젝트 정제 완료 projectIdx={} {}ms", projectIdx, System.currentTimeMillis()-start);
        }
    }

}
