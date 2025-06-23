package com.illunex.emsaasrestapi.database;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.database.dto.RequestDatabaseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("database")
@Slf4j
public class DatabaseController {
    private final DatabaseService databaseService;

    @GetMapping("/list")
    public CustomResponse<?> getDatabaseList(@RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        log.info("Received request to get database list for project index: {}", projectIdx);
        return databaseService.getDatabaseList(projectIdx);
    }

    @PostMapping("/search")
    public CustomResponse<?> searchDatabase(@RequestParam(name = "projectIdx") Integer projectIdx,
                                            @RequestBody RequestDatabaseDTO.Search query,
                                            CustomPageRequest pageRequest, String sort) throws CustomException {
        log.info("Received database search request with query: {}", query);
        return databaseService.searchDatabase(projectIdx, query, pageRequest, sort);
    }

}
