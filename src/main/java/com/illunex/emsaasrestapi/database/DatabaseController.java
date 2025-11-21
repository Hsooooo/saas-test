package com.illunex.emsaasrestapi.database;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.database.dto.EdgeDataDTO;
import com.illunex.emsaasrestapi.database.dto.RequestDatabaseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("database")
@Slf4j
public class DatabaseController {
    private final DatabaseService databaseService;

    /**
     * 프로젝트 데이터베이스 테이블 목록 조회
     *
     * @param projectIdx 프로젝트 인덱스
     * @return 각 노드, 링크의 하위 타입 목록을 포함하는 데이터베이스 목록
     */
    @GetMapping("/list")
    public CustomResponse<?> getDatabaseList(@RequestParam(name = "projectIdx") Integer projectIdx,
                                             @RequestParam(name = "searchString", required = false) String searchString) throws CustomException {
        log.info("Received request to get database list for project index: {}", projectIdx);
        return databaseService.getDatabaseList(projectIdx, searchString);
    }

//    @GetMapping("/summary")
//    public CustomResponse<?> getDatabaseSummary(@RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
//        log.info("Received request to get database summary for project index: {}", projectIdx);
//        return databaseService.getDatabaseSummary(projectIdx);
//    }

    /**
     * 프로젝트 데이터베이스 검색
     *
     * @param projectIdx 프로젝트 인덱스
     * @param query      검색 쿼리
     * @param pageRequest 페이지 요청 정보
     * @return 검색 결과
     */
    @PostMapping("/search")
    public CustomResponse<?> searchDatabase(@RequestParam(name = "projectIdx") Integer projectIdx,
                                            @RequestBody RequestDatabaseDTO.Search query,
                                            CustomPageRequest pageRequest) throws CustomException {
        log.info("Received database search request with query: {}", query);
        return databaseService.searchDatabase(projectIdx, query, pageRequest);
    }

    /**
     * 템플릿 데이터베이스 검색
     *
     * @param projectIdx 프로젝트 인덱스
     * @param query 검색 조건
     * @param pageRequest 페이지 요청 정보
     * @return 검색 결과
     */
    @PostMapping("/search/template")
    public CustomResponse<?> searchDatabaseByTemplate(@RequestParam(name = "projectIdx") Integer projectIdx,
                                                      @RequestBody RequestDatabaseDTO.SearchTemplate query,
                                                      CustomPageRequest pageRequest) throws CustomException {
        log.info("Received database search request with query: {}, projectIdx = {}", query, projectIdx);
        return databaseService.searchDatabaseByTemplate(projectIdx, query, pageRequest);
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

    /**
     * 노드 또는 엣지 업데이트
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type 시트명
     * @param data 업데이트할 데이터 (LinkedHashMap 형태)
     * @return
     */
    @PutMapping("/data/node")
    public CustomResponse<?> updateNode(@RequestParam(name = "projectIdx") Integer projectIdx,
                                     @RequestParam(name = "type") String type,
                                     @RequestBody LinkedHashMap<String, Object> data) {
        return databaseService.updateNode(projectIdx, type, data);
    }

    /**
     * 엣지 업데이트
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type 시트명
     * @param data 업데이트할 엣지 데이터 (EdgeDataDTO 형태)
     * @return
     */
    @PutMapping("/data/edge")
    public CustomResponse<?> updateEdge(@RequestParam(name = "projectIdx") Integer projectIdx,
                                        @RequestParam(name = "type") String type,
                                        @RequestBody EdgeDataDTO data) {
        return databaseService.updateEdge(projectIdx, type, data);
    }

    /**
     * 데이터 삭제
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type 시트명
     * @param docType 데이터 타입 (Node 또는 Edge)
     * @param data 삭제할 데이터 ID 목록 (Object 형태의 리스트)
     * @return
     */
    @DeleteMapping("/data")
    public CustomResponse<?> deleteData(@RequestParam(name = "projectIdx") Integer projectIdx,
                                        @RequestParam(name = "type") String type,
                                        @RequestParam(name = "docType") RequestDatabaseDTO.DocType docType,
                                        @RequestBody List<Object> data) {
        return databaseService.deleteData(projectIdx, type, data, docType);
    }

    /**
     * 엑셀 컬럼 목록 조회
     *
     * @param projectIdx 프로젝트 인덱스
     * @return
     */
    @GetMapping("/column")
    public CustomResponse<?> getColumnList(@RequestParam(name = "projectIdx") Integer projectIdx,
                                           @RequestParam(name = "type") String type) throws CustomException {
        log.info("Received request to get column list for project index: {}", projectIdx);
        return databaseService.getColumnList(projectIdx, type);
    }

    /**
     * 엑셀 컬럼 정보 업데이트
     *
     * @param columnOrder 컬럼 정보
     * @return
     */
    @PutMapping("/column")
    public CustomResponse<?> updateColumn(@RequestBody RequestDatabaseDTO.ColumnOrder columnOrder) throws CustomException {
        return databaseService.saveColumnOrder(columnOrder);
    }

    /**
     * 데이터베이스 커밋
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type 시트명
     * @param docType 데이터 타입 (Node 또는 Edge)
     * @param commit 커밋 정보
     * @return 커밋 결과
     */
    @PostMapping("/commit")
    public CustomResponse<?> commitDatabase(@RequestParam(name = "projectIdx") Integer projectIdx,
                                            @RequestParam(name = "type") String type,
                                            @RequestParam(name = "docType") RequestDatabaseDTO.DocType docType,
                                            @RequestBody RequestDatabaseDTO.Commit commit) throws CustomException {
        log.info("Received request to commit database for project index: {}", projectIdx);
        return databaseService.commitDatabase(projectIdx, commit, type, docType);
    }

}
