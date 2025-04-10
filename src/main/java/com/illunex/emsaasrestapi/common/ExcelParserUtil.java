package com.illunex.emsaasrestapi.common;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelParserUtil {
    public static Map<String, Object> parseExcel(MultipartFile file) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());

        for (Sheet sheet : workbook) {
            List<Map<String, Object>> rowList = new ArrayList<>();
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) continue;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    Object value = getCellValue(cell);
                    rowData.put(headers.get(j), value);
                }
                rowList.add(rowData);
            }

            Map<String, Object> sheetData = new LinkedHashMap<>();
            sheetData.put("header", headers);
            sheetData.put("rows", rowList);

            result.put(sheet.getSheetName(), sheetData);
        }

        workbook.close();
        return result;
    }

    private static Object getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue();
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return cell.getNumericCellValue();
            }
            case BOOLEAN -> {
                return cell.getBooleanCellValue();
            }
            case FORMULA -> {
                return cell.getCellFormula();
            }
            default -> {
                return null;
            }
        }
    }
}
