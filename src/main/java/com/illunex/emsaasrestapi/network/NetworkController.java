package com.illunex.emsaasrestapi.network;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("network")
public class NetworkController {
    private final NetworkService networkService;

    /**
     * 프로젝트 전체 관계먕 조회
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    @GetMapping("/all")
    public CustomResponse<?> getNetworkAll(@RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        return networkService.getNetworkAll(projectIdx);
    }

    /**
     * 프로젝트 단일 노드 확장 검색 API
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    @GetMapping("/extend")
    public CustomResponse<?> getNetworkOne(@RequestParam(name = "projectIdx") Integer projectIdx,
                                           @RequestParam(name = "nodeIdx") Integer nodeIdx) throws CustomException {
        return networkService.getNetworkOne(projectIdx, nodeIdx);
    }


    /**
     * 프로젝트 단일노드 상세정보 조회
     * @param projectIdx
     * @param nodeIdx
     * @param label
     * @return
     * @throws CustomException
     */
    @GetMapping("/info")
    public CustomResponse<?> getNetworkInfo(@RequestParam(name = "projectIdx") Integer projectIdx,
                                            @RequestParam(name = "nodeIdx") Integer nodeIdx,
                                            @RequestParam(name = "label") String label) throws CustomException {
        return networkService.getNetworkInfo(projectIdx, nodeIdx, label);
    }
}
