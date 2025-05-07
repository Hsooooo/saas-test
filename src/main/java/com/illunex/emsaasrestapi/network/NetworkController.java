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
}
