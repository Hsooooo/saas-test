package com.illunex.emsaasrestapi.project;


import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.project.document.data.Data;
import com.illunex.emsaasrestapi.project.document.data.DataRow;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

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
    public Object getExcelColumnData(Cell cell) throws CustomException{
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

    /**
     * 저장된 엑셀 데이터 정보 조회
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    public ResponseProjectDTO.Data responseProjectData(Integer projectIdx) throws CustomException {
        // 저장된 엑셀 데이터 정보 조회
        Data selectData = mongoTemplate.findOne(
                Query.query(
                        Criteria.where("projectIdx").is(projectIdx)
                ),
                Data.class
        );
        if(selectData == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }
        // 응답 구조 맵핑
        ResponseProjectDTO.Data response = modelMapper.map(selectData, ResponseProjectDTO.Data.class);
        // 응답 구조 안에 데이터 목록 추가
        response.getSheetList().forEach(dataSheet -> {
            List<DataRow> dataRow = mongoTemplate.find(
                    Query.query(
                            Criteria.where("_id.projectIdx").is(projectIdx)
                                    .and("_id.dataSheetIdx").is(dataSheet.getSheetIdx())
                    ),
                    DataRow.class
            );
            dataSheet.setRowList(modelMapper.map(dataRow, new TypeToken<List<ResponseProjectDTO.DataRow>>(){}.getType()));
        });

        return response;
    }
}
