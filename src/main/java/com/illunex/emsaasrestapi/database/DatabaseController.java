package com.illunex.emsaasrestapi.database;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.database.dto.RequestDatabaseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("database")
@Slf4j
public class DatabaseController {
    private final DatabaseService databaseService;

    /**
     * 프로젝트 데이터베이스 목록 조회
     *
     * @param projectIdx 프로젝트 인덱스
     * @return 각 노드, 링크의 하위 타입 목록을 포함하는 데이터베이스 목록
     */
    @GetMapping("/list")
    public CustomResponse<?> getDatabaseList(@RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        log.info("Received request to get database list for project index: {}", projectIdx);
        return databaseService.getDatabaseList(projectIdx);
    }

    /**
     * 프로젝트 데이터베이스 검색
     *
     * @param projectIdx 프로젝트 인덱스
     * @param query      검색 쿼리
     * @param pageRequest 페이지 요청 정보
     * @param sort       정렬 기준
     * @return 검색 결과
     */
    @PostMapping("/search")
    public CustomResponse<?> searchDatabase(@RequestParam(name = "projectIdx") Integer projectIdx,
                                            @RequestBody RequestDatabaseDTO.Search query,
                                            CustomPageRequest pageRequest, String sort) throws CustomException {
        log.info("Received database search request with query: {}", query);
        return databaseService.searchDatabase(projectIdx, query, pageRequest, sort);
    }

    /**
     * 데이터 추가
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type 시트명
     * @param docType 데이터 타입 (Node 또는 Edge)
     * @param data 추가할 데이터 (LinkedHashMap 형태)
     * @return
     */
    @PostMapping("/data")
    public CustomResponse<?> addData(@RequestParam(name = "projectIdx") Integer projectIdx,
                                     @RequestParam(name = "type") String type,
                                     @RequestParam(name = "docType") RequestDatabaseDTO.DocType docType,
                                     @RequestBody LinkedHashMap<String, Object> data) {
        return databaseService.addData(projectIdx, type, data, docType);
    }

    @GetMapping("/column")
    public CustomResponse<?> getColumnList(@RequestParam(name = "projectIdx") Integer projectIdx,
                                           @RequestParam(name = "type") String type) throws CustomException {
        log.info("Received request to get column list for project index: {}", projectIdx);
        return databaseService.getColumnList(projectIdx, type);
    }

    @PutMapping("/column")
    public CustomResponse<?> updateColumn(@RequestBody RequestDatabaseDTO.ColumnOrder columnOrder) throws CustomException {
        return databaseService.saveColumnOrder(columnOrder);
    }

}
