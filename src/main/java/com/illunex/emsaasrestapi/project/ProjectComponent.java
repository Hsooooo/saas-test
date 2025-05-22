package com.illunex.emsaasrestapi.project;


import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.document.excel.*;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectCategoryDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectCategoryMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectFileMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMemberMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import com.illunex.emsaasrestapi.project.vo.ProjectFileVO;
import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.CountOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectComponent {
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectFileMapper projectFileMapper;
    private final ProjectCategoryMapper projectCategoryMapper;

    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

    /**
     * 프로젝트 카테고리 타입
     */
    public enum CategorySearchType {
        all,                // 전체 조회
        empty,              // 미분류 조회
        category            // 카테고리번호 조회
    }

    /**
     * 프로젝트 구성원 여부 체크
     * @param projectIdx
     * @param partnershipMemberIdx
     * @return
     * @throws CustomException
     */
    public ProjectMemberVO checkProjectMember(Integer projectIdx, Integer partnershipMemberIdx) throws CustomException {
        // 프로젝트 구성원 조회
        ProjectMemberVO projectMemberVO = projectMemberMapper.selectByProjectIdxAndPartnershipMemberIdx(projectIdx, partnershipMemberIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_INVALID_MEMBER));

        return projectMemberVO;
    }

    /**
     * 프로젝트 상태 코드 조회
     * @param project
     * @param projectVO
     * @return
     */
    public String getProjectStatusCd(RequestProjectDTO.Project project, ProjectVO projectVO) {
        // 프로젝트 설정 정보에 따라서 상태 변경
        if(project.getProjectNodeContentList() != null) {
            // 속성 정의 있을 경우
            return EnumCode.Project.StatusCd.Step4.getCode();
        } else if (project.getProjectNodeSizeList() != null && project.getProjectFilterList() != null) {
            // 기능 정의 있을 경우
            return EnumCode.Project.StatusCd.Step3.getCode();
        } else if (project.getProjectNodeList() != null && project.getProjectEdgeList() != null) {
            // 노드&엣지 정의 있을 경우
            return EnumCode.Project.StatusCd.Step2.getCode();
        }
        return projectVO.getStatusCd();
    }

    /**
     * 단일 엑셀파일 파싱 함수
     * @param projectIdx
     * @param excelFile
     * @throws CustomException
     * @throws IOException
     */
    public void parseExcel(Integer projectIdx, MultipartFile excelFile, ProjectFileVO projectFileVO) throws CustomException, IOException {
        // 확장자 체크
        String ext = FilenameUtils.getExtension(excelFile.getOriginalFilename());
        if(ext == null || !ext.equals("xlsx") && !ext.equals("xls")) {
            throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_EXTENSION);
        }

        Workbook workbook = null;
        if (ext.equals("xlsx")) {
            workbook = new XSSFWorkbook(excelFile.getInputStream());
        } else {
            workbook = new HSSFWorkbook(excelFile.getInputStream());
        }
        ExcelFile excelFileDoc = modelMapper.map(projectFileVO, ExcelFile.class);

        // 엑셀 파싱 구조 생성
        Excel excel = com.illunex.emsaasrestapi.project.document.excel.Excel.builder()
                .projectIdx(projectIdx)
                .excelSheetList(new ArrayList<>())
                .excelFileList(Collections.singletonList(excelFileDoc))
                .build();

        // 엑셀 데이터 Row 데이터 삭제
        mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(projectIdx)), ExcelRow.class);
        // 엑셀 파싱 정보 삭제
        mongoTemplate.findAndRemove(Query.query(Criteria.where("_id").is(projectIdx)), Excel.class);

        // 엑셀 시트 읽기
        for(int sheetIdx = 0; sheetIdx < workbook.getNumberOfSheets(); sheetIdx++) {
            // Cell 목록
            List<String> excelCellList = new ArrayList<>();

            Sheet workSheet = workbook.getSheetAt(sheetIdx);
            // 데이터 개수 체크
            if(workSheet.getLastRowNum() <= 1) {
                // Row 데이터 없음
                workbook.close();
                throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_ROW_EMPTY);
            }

            // 첫번째 Row에서 컬럼명 추출
            Row firstRow = workSheet.getRow(0);
            if(firstRow == null) {
                // column 데이터 없음
                workbook.close();
                throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);
            }

            // Cell, Row 최대 개수 추출
            int totalCellCnt = firstRow.getLastCellNum();
            int totalRowCnt = workSheet.getLastRowNum();

            // 첫번째 Row에 Cell 개수 만큼 컬럼명 추출
            for(int cellIdx = 0; cellIdx < totalCellCnt; cellIdx++) {
                if(firstRow.getCell(cellIdx).getStringCellValue().isEmpty()) {
                    // 셀에 빈값이면 컬럼 총개수 감소
                    totalCellCnt--;
                } else {
                    excelCellList.add(firstRow.getCell(cellIdx).getStringCellValue());
                }
            }
            for(int rowIdx = 1; rowIdx < totalRowCnt + 1; rowIdx++) {
                Row row = workSheet.getRow(rowIdx);
                if (row == null || row.getLastCellNum() == -1) {
                    // Row 데이터가 없으면 종료
                    totalRowCnt = rowIdx;
                    break;
                }
            }

            // 엑셀 시트 정보 추가
            excel.getExcelSheetList()
                    .add(ExcelSheet.builder()
                            .excelSheetName(workSheet.getSheetName())
                            .excelCellList(excelCellList)
                            .totalRowCnt(totalRowCnt)
                            .build());
            excel.setCreateDate(LocalDateTime.now());
        }

        // 엑셀 파싱 정보 저장
        mongoTemplate.insert(excel);
    }

    /**
     * 프로젝트 카테고리 응답 구조 생성
     * @param partnershipMemberVO
     * @return
     */
    public List<ResponseProjectCategoryDTO.ProjectCategory> createResponseProjectCategory(PartnershipMemberVO partnershipMemberVO) {
        List<ProjectCategoryVO> projectCategoryVOList = new ArrayList<>();
        // 전체 카테고리 추가
        projectCategoryVOList.add(ProjectCategoryVO.builder()
                .name("전체")
                .partnershipIdx(partnershipMemberVO.getPartnershipIdx())
                .build()
        );
        // 미분류 카테고리 추가
        projectCategoryVOList.add(ProjectCategoryVO.builder()
                .name("미분류")
                .partnershipIdx(partnershipMemberVO.getPartnershipIdx())
                .build()
        );

        // 등록된 카테고리 추가
        projectCategoryVOList.addAll(projectCategoryMapper.selectAllByPartnershipIdx(partnershipMemberVO.getPartnershipIdx()));

        List<ResponseProjectCategoryDTO.ProjectCategory> response = modelMapper.map(projectCategoryVOList, new TypeToken<List<ResponseProjectCategoryDTO.ProjectCategory>>() {
        }.getType());
        response.forEach(projectCategory -> {
            if(projectCategory.getIdx() == null && projectCategory.getName().equals("전체")) {
                projectCategory.setSearchType(CategorySearchType.all);
            } else if(projectCategory.getIdx() == null && projectCategory.getName().equals("미분류")) {
                projectCategory.setSearchType(CategorySearchType.empty);
            } else {
                projectCategory.setSearchType(CategorySearchType.category);
            }
        });

        // 카테고리별 프로젝트 개수 조회
        for (int i = 0; i < response.size(); i++) {
            Integer cnt;
            if (i == 0) {
                // 전체 프로젝트 카운트 조회
                cnt = projectMapper.countAllByPartnershipMemberIdx(partnershipMemberVO.getIdx());
            } else if (i == 1) {
                // 미분류 프로젝트 카운트 조회
                cnt = projectMapper.countAllByProjectCategoryIdxAndPartnershipMemberIdx(null, partnershipMemberVO.getIdx());
            } else {
                // 카테고리별 카운트 조회
                cnt = projectMapper.countAllByProjectCategoryIdxAndPartnershipMemberIdx(response.get(i).getIdx(), partnershipMemberVO.getIdx());
            }
            response.get(i).setProjectCnt(cnt);
        }

        return response;
    }

    /**
     * 엑셀파일 excel_row 파싱 함수
     * @param projectIdx
     * @param workbook
     * @throws CustomException
     */
    public void parseExcelRowsOnly(Integer projectIdx, Workbook workbook) throws CustomException {
        // 기존 Row 데이터 삭제
        mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(projectIdx)), ExcelRow.class);

        // 이미 저장된 시트 정보 불러오기
        Excel excel = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(projectIdx)), Excel.class);
        if (excel == null) {
            throw new CustomException(ErrorCode.PROJECT_EMPTY_DATA);
        }

        for (ExcelSheet sheetInfo : excel.getExcelSheetList()) {
            Sheet workSheet = workbook.getSheet(sheetInfo.getExcelSheetName());
            if (workSheet == null) continue;

            long startMillisecond = System.currentTimeMillis();
            log.info(Utils.getLogMaker(Utils.eLogType.USER), "Start parse excel - projectIdx : {}, sheet : {}, size : {}", projectIdx, sheetInfo.getExcelSheetName(), sheetInfo.getTotalRowCnt());
            List<ExcelRow> excelRowList = new ArrayList<>();
            for (int rowIdx = 1; rowIdx <= sheetInfo.getTotalRowCnt(); rowIdx++) {
                Row row = workSheet.getRow(rowIdx);
                if (row == null) break;

                LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
                for (int cellIdx = 0; cellIdx < sheetInfo.getExcelCellList().size(); cellIdx++) {
                    dataMap.put(sheetInfo.getExcelCellList().get(cellIdx), getExcelColumnData(row.getCell(cellIdx)));
                }

                ExcelRow excelRow = ExcelRow.builder()
                        .excelRowId(ExcelRowId.builder()
                                .projectIdx(projectIdx)
                                .excelSheetName(sheetInfo.getExcelSheetName())
                                .excelRowIdx(rowIdx)
                                .build())
                        .data(dataMap)
                        .createDate(LocalDateTime.now())
                        .build();

                excelRowList.add(excelRow);
            }
            mongoTemplate.insertAll(excelRowList);
            log.info(Utils.getLogMaker(Utils.eLogType.USER), "End parse excel - projectIdx : {}, time : {}ms", projectIdx, System.currentTimeMillis() - startMillisecond);
        }
    }

    /**
     * RDB & MongoDB 데이터 조회 후 응답 구조 생성
     * @param projectIdx
     * @return
     */
    public ResponseProjectDTO.Project createResponseProject(Integer projectIdx) throws CustomException {
        // RDB 조회
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if(projectVO.getDeleteDate() == null) {
            ResponseProjectDTO.Project response = modelMapper.map(projectVO, ResponseProjectDTO.Project.class);

            // MongoDB 조회
            Project project = mongoTemplate.findOne(
                    Query.query(
                            Criteria.where("_id").is(projectIdx)
                    ),
                    Project.class
            );

            if(project == null) {
                throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
            }

            modelMapper.map(project, response);

            // 프로젝트 파일 업로드 맵핑
            List<ProjectFileVO> projectFileList = projectFileMapper.selectAllByProjectIdx(projectIdx);
            if(projectFileList.size() > 0) {
                response.setProjectFileList(modelMapper.map(projectFileList, new TypeToken<List<ResponseProjectDTO.ProjectFile>>(){}.getType()));
            } else {
                response.setProjectFileList(new ArrayList<>());
            }

            return response;
        }

        // 프로젝트 삭제 예외 응답
        throw new CustomException(ErrorCode.PROJECT_DELETED);
    }

    /**
     * 저장된 엑셀 데이터 정보 조회
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    public ResponseProjectDTO.Excel createResponseProjectExcel(Integer projectIdx) throws CustomException {
        // 저장된 엑셀 데이터 정보 조회
        Excel selectExcel = mongoTemplate.findOne(
                Query.query(
                        Criteria.where("_id").is(projectIdx)
                ),
                Excel.class
        );
        if(selectExcel == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }
        // 응답 구조 맵핑
        ResponseProjectDTO.Excel response = modelMapper.map(selectExcel, ResponseProjectDTO.Excel.class);
        // 3. 시트별 row 정보 세팅
        response.getExcelSheetList().forEach(dataSheet -> {
            String sheetName = dataSheet.getExcelSheetName();

            // 3-1. ExcelRow 미리보기 (10개)
            List<ExcelRow> previewRows = mongoTemplate.find(
                    Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                                    .and("_id.excelSheetName").is(sheetName))
                            .limit(10),
                    ExcelRow.class
            );

            List<ResponseProjectDTO.ExcelRow> mappedPreview = modelMapper.map(
                    previewRows,
                    new TypeToken<List<ResponseProjectDTO.ExcelRow>>() {}.getType()
            );
            dataSheet.setExcelRowList(mappedPreview);

            // 3-2. 총 row 수 계산: 존재할 경우 집계, 없을 경우 fallback
            if (!previewRows.isEmpty()) {
                // ExcelRow가 존재하면 aggregation 수행
                MatchOperation matchOperation = Aggregation.match(
                        Criteria.where("_id.projectIdx").is(projectIdx)
                                .and("_id.excelSheetName").is(sheetName)
                );
                CountOperation countOperation = Aggregation.count().as("rowCount");
                Aggregation aggregation = Aggregation.newAggregation(matchOperation, countOperation);

                AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "excel_row", Document.class);
                int rowCount = Optional.ofNullable(results.getUniqueMappedResult())
                        .map(doc -> doc.getInteger("rowCount"))
                        .orElse(0);

                dataSheet.setTotalRowCnt(rowCount);
            } else {
                // ExcelRow가 없으면 엑셀 시트 정보에 저장된 값 사용
                log.info("[FALLBACK] ExcelRow 없음. totalRowCnt fallback 사용: {}:{}", projectIdx, sheetName);
                dataSheet.setTotalRowCnt(dataSheet.getTotalRowCnt()); // 유지
            }
        });

        return response;
    }

    /**
     * 엑셀 cell 타입에 맞게 데이터 반환
     * @param cell
     * @return
     * @throws CustomException
     */
    private Object getExcelColumnData(Cell cell) throws CustomException{
        if(cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue();
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            }
            case BOOLEAN -> {
                return cell.getBooleanCellValue();
            }
            // 수식 셀은 getCellFormula() 또는 evaluate 사용
            case FORMULA -> {
                return cell.getCellFormula();
            }
            case BLANK -> {
                return "";
            }
            case ERROR -> {
                return cell.getErrorCellValue();
            }
            default -> throw new CustomException(ErrorCode.COMMON_INVALID_FILE_EXTENSION);
        }
    }
}
