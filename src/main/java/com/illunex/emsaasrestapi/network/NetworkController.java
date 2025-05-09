package com.illunex.emsaasrestapi.network;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.network.dto.RequestNetworkDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("network")
public class NetworkController {
    private final NetworkService networkService;

    /**
     * 전체 관계먕 조회
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    @GetMapping("/all")
    public CustomResponse<?> getNetworkAll(@RequestParam(name = "projectIdx") Integer projectIdx) {
        return networkService.getNetworkAll(projectIdx);
    }

    /**
     * 단일 노드 확장 조회
     * @param extend
     * @return
     * @throws CustomException
     */
    @PostMapping("/extend")
    public CustomResponse<?> getNetworkSingleExtend(@RequestBody RequestNetworkDTO.Extend extend) {
        return networkService.getNetworkSingleExtend(extend);
    }

    /**
     * 단일노드 상세정보 조회
     * @param extend
     * @return
     * @throws CustomException
     */
    @PostMapping("/info")
    public CustomResponse<?> getNetworkInfo(@RequestBody RequestNetworkDTO.Extend extend) throws CustomException {
        return networkService.getNetworkInfo(extend);
    }
}
