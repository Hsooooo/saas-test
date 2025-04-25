package com.illunex.emsaasrestapi.project;


import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.project.document.excel.Excel;
import com.illunex.emsaasrestapi.project.document.excel.ExcelRow;
import com.illunex.emsaasrestapi.project.document.excel.ExcelRowId;
import com.illunex.emsaasrestapi.project.document.excel.ExcelSheet;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectComponent {
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

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

    public void parseExcel(Integer projectIdx, MultipartFile excelFile) throws CustomException, IOException {
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

        // 엑셀 파싱 구조 생성
        Excel excel = com.illunex.emsaasrestapi.project.document.excel.Excel.builder()
                .projectIdx(projectIdx)
                .excelSheetList(new ArrayList<>())
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

            // 엑셀 Row 개수 만큼 데이터 추출
            for(int rowIdx = 1; rowIdx < totalRowCnt; rowIdx++) {
                Row row = workSheet.getRow(rowIdx);
                if(row == null) {
                    // 열 데이터가 없으면 종료
                    totalRowCnt = rowIdx;
                    break;
                }
                // 엑셀 데이터 Row 구조 생성
                ExcelRow excelRow = ExcelRow.builder()
                        .excelRowId(ExcelRowId.builder()
                                .projectIdx(projectIdx)
                                .excelSheetIdx(sheetIdx + 1)
                                .excelRowIdx(rowIdx)
                                .build())
                        .build();
                LinkedHashMap<String, Object> ExcelRowMap = new LinkedHashMap<>();
                // Cell 개수 만큼 데이터 추출
                for (int cellCnt = 0; cellCnt < excelCellList.size(); cellCnt++) {
                    ExcelRowMap.put(excelCellList.get(cellCnt), getExcelColumnData(row.getCell(cellCnt)));
                }
                excelRow.setExcelRow(ExcelRowMap);
                // 엑셀 데이터 Row 저장
                mongoTemplate.insert(excelRow);
            }

            // 엑셀 시트 정보 추가
            excel.getExcelSheetList()
                    .add(ExcelSheet.builder()
                            .excelSheetIdx(sheetIdx + 1)
                            .excelSheetName(workSheet.getSheetName())
                            .excelCellList(excelCellList)
                            .totalRowCnt(totalRowCnt)
                            .build());
        }

        // 엑셀 파싱 정보 저장
        mongoTemplate.insert(excel);
    }

    /**
     * 저장된 엑셀 데이터 정보 조회
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    public ResponseProjectDTO.Excel responseProjectData(Integer projectIdx) throws CustomException {
        // 저장된 엑셀 데이터 정보 조회
        Excel selectExcel = mongoTemplate.findOne(
                Query.query(
                        Criteria.where("projectIdx").is(projectIdx)
                ),
                Excel.class
        );
        if(selectExcel == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }
        // 응답 구조 맵핑
        ResponseProjectDTO.Excel response = modelMapper.map(selectExcel, ResponseProjectDTO.Excel.class);
        // 응답 구조 안에 데이터 목록 추가
        response.getExcelSheetList().forEach(dataSheet -> {
            List<ExcelRow> excelRow = mongoTemplate.find(
                    Query.query(
                            Criteria.where("_id.projectIdx").is(projectIdx)
                                    .and("_id.excelSheetIdx").is(dataSheet.getExcelSheetIdx())
                    )
                            .limit(10),
                    ExcelRow.class
            );
            dataSheet.setExcelRowList(modelMapper.map(excelRow, new TypeToken<List<ResponseProjectDTO.ExcelRow>>(){}.getType()));
        });

        return response;
    }
}
