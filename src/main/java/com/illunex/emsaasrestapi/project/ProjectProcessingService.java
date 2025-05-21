package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.project.document.excel.Excel;
import com.illunex.emsaasrestapi.project.document.excel.ExcelFile;
import com.illunex.emsaasrestapi.project.document.excel.ExcelRow;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.EdgeId;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.network.NodeId;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectEdge;
import com.illunex.emsaasrestapi.project.document.project.ProjectNode;
import com.illunex.emsaasrestapi.project.mapper.ProjectFileMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectProcessingService {
    private final MongoTemplate mongoTemplate;
    private final ProjectMapper projectMapper;
    private final ProjectFileMapper projectFileMapper;
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
                    projectVO.setStatusCd(EnumCode.Project.StatusCd.Step6.getCode());
                    projectMapper.updateByProjectVO(projectVO);
                    log.error("[Thread-FAIl] 프로젝트 정제 실패 projectIdx={}", projectIdx, e);
                }
            });
        }
    }

    private void processProject(ProjectVO projectVO) throws IOException, CustomException {
        int projectIdx = projectVO.getIdx();
        // 1. 파일 정보 조회
        Excel excel = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Excel.class);
        if (excel == null || excel.getExcelFileList().isEmpty()) {
            throw new RuntimeException("정제에 필요한 엑셀 파일 정보 없음");
        }

        if (excel.getExcelFileList().size() == 1) {
            ExcelFile excelFile = excel.getExcelFileList().get(0);
            // 2. S3에서 엑셀 InputStream 로드
            InputStream inputStream = awsS3Component.downloadInputStream(excelFile.getFilePath());
            Workbook workbook = WorkbookFactory.create(inputStream);

            // 3. ExcelRow 생성
            log.info("[THREAD] ExcelRow 생성 시작 projectIdx={}", projectIdx);
            projectComponent.parseExcelRowsOnly(projectIdx, workbook);

            // 4. Node/Edge 생성
            Project project = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Project.class);

            if (project == null) {
                throw new RuntimeException("정제용 Mongo 데이터 없음");
            }

            mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(projectIdx)), Node.class);
            for (ProjectNode nodeDef : project.getProjectNodeList()) {
                List<ExcelRow> rows = mongoTemplate.find(Query.query(
                                Criteria.where("_id.projectIdx").is(projectIdx)
                                        .and("_id.excelSheetName").is(nodeDef.getNodeType())),
                        ExcelRow.class);

                long startMillisecond = System.currentTimeMillis();
                log.info(Utils.getLogMaker(Utils.eLogType.USER), "Start parse node - projectIdx : {}, type : {}, size : {}", projectIdx, nodeDef.getNodeType(), rows.size());
                List<Node> nodeList = new ArrayList<>();
                rows.forEach(excelRow -> {
                    Node node = Node.builder()
                            .nodeId(NodeId.builder()
                                    .projectIdx(projectIdx)
                                    .type(excelRow.getExcelRowId().getExcelSheetName())
                                    .nodeIdx(excelRow.getData().get(nodeDef.getUniqueCellName()))
                                    .build())
                            .id(excelRow.getData().get(nodeDef.getUniqueCellName()))
                            .label(excelRow.getExcelRowId().getExcelSheetName())
                            .properties(excelRow.getData())
                            .build();
                    nodeList.add(node);
                });
                mongoTemplate.insertAll(nodeList);
                log.info(Utils.getLogMaker(Utils.eLogType.USER), "End parse node - projectIdx : {},time : {}ms", projectIdx, System.currentTimeMillis() - startMillisecond);
            }

            mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(projectIdx)), Edge.class);
            for (ProjectEdge edgeDef : project.getProjectEdgeList()) {
                List<ExcelRow> rows = mongoTemplate.find(Query.query(
                                Criteria.where("_id.projectIdx").is(projectIdx)
                                        .and("_id.excelSheetName").is(edgeDef.getEdgeType())),
                        ExcelRow.class);

                long startMillisecond = System.currentTimeMillis();
                log.info(Utils.getLogMaker(Utils.eLogType.USER), "Start parse edge - projectIdx : {}, type : {}, size : {}", projectIdx, edgeDef.getEdgeType(), rows.size());
                List<Edge> edgeList = new ArrayList<>();
                rows.forEach(excelRow -> {
                    Edge edge = Edge.builder()
                            .edgeId(EdgeId.builder()
                                    .projectIdx(projectIdx)
                                    .type(excelRow.getExcelRowId().getExcelSheetName())
                                    .edgeIdx(excelRow.getExcelRowId().getExcelRowIdx())
                                    .build())
                            .id(excelRow.getExcelRowId().getExcelRowIdx())
                            .startType(edgeDef.getSrcNodeType())
                            .start(excelRow.getData().get(edgeDef.getSrcEdgeCellName()))
                            .endType(edgeDef.getDestNodeType())
                            .end(excelRow.getData().get(edgeDef.getDestEdgeCellName()))
                            .type(excelRow.getExcelRowId().getExcelSheetName())
                            .properties(excelRow.getData())
                            .build();
                    edgeList.add(edge);
                });
                mongoTemplate.insertAll(edgeList);
                log.info(Utils.getLogMaker(Utils.eLogType.USER), "End parse edge - projectIdx : {}, time : {}ms", projectIdx, System.currentTimeMillis() - startMillisecond);
            }

            // 5. 상태 변경
            projectVO.setStatusCd(EnumCode.Project.StatusCd.Complete.getCode());
            projectMapper.updateByProjectVO(projectVO);

            log.info("[THREAD-SUCCESS] 정제 완료 projectIdx={}", projectIdx);
        }
    }
}
