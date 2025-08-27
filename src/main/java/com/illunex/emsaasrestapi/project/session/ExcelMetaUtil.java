package com.illunex.emsaasrestapi.project.session;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.project.document.excel.Excel;
import com.illunex.emsaasrestapi.project.document.excel.ExcelFile;
import com.illunex.emsaasrestapi.project.document.excel.ExcelSheet;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExcelMetaUtil {
    private final AwsS3Component s3;

    public Excel buildExcelMeta(String fileName, String filePath, String fileUrl, long size) throws CustomException {
        List<ExcelSheet> sheets = new ArrayList<>();
        try (InputStream is = s3.downloadInputStream(filePath);
             Workbook wb = WorkbookFactory.create(is)) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sh = wb.getSheetAt(i);
                Row header = sh.getRow(0);
                if (header == null) throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);

                int totalRows = sh.getLastRowNum(); // 헤더 제외
                List<String> headerNames = new ArrayList<>();
                for (int c = 0; c < header.getLastCellNum(); c++) {
                    Cell h = header.getCell(c);
                    if (h != null && !h.getStringCellValue().isEmpty()) {
                        headerNames.add(h.getStringCellValue().trim());
                    }
                }
                sheets.add(ExcelSheet.builder()
                        .excelSheetName(sh.getSheetName())
                        .excelCellList(headerNames)   // 기존 구조 유지
                        .filePath(filePath)           // S3 path
                        .totalRowCnt(totalRows)
                        .build());
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }

        ExcelFile ef = ExcelFile.builder()
                .fileName(fileName).filePath(filePath).fileUrl(fileUrl).fileSize(size)
                .build();

        return Excel.builder()
                .projectIdx(null)                     // 커밋 때 세팅
                .excelFileList(List.of(ef))
                .excelSheetList(sheets)
                .createDate(LocalDateTime.now())
                .build();
    }
}