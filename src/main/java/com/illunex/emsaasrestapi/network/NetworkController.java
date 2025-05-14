package com.illunex.emsaasrestapi.network;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.network.dto.RequestNetworkDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("network")
public class NetworkController {
    private final NetworkService networkService;

    /**
     * 전체 관계먕 조회
     * @param memberVO
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getNetworkAll(@CurrentMember MemberVO memberVO,
                                           @RequestParam(name = "projectIdx") Integer projectIdx) throws CustomException {
        return networkService.getNetworkAll(memberVO, projectIdx);
    }

    /**
     * 단일 노드 확장 조회
     * @param memberVO
     * @param selectNode
     * @return
     * @throws CustomException
     */
    @PostMapping("/extend")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getNetworkSingleExtend(@CurrentMember MemberVO memberVO,
                                                    @RequestBody RequestNetworkDTO.SelectNode selectNode) throws CustomException {
        return networkService.getNetworkSingleExtend(memberVO, selectNode);
    }

    /**
     * 단일노드 상세정보 조회
     * @param memberVO
     * @param selectNode
     * @return
     * @throws CustomException
     */
    @PostMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getNetworkInfo(@CurrentMember MemberVO memberVO,
                                            @RequestBody RequestNetworkDTO.SelectNode selectNode) throws CustomException {
        return networkService.getNetworkInfo(memberVO, selectNode);
    }


    /**
     * 관계망 조회 API
     * @param search
     * @return
     * @throws CustomException
     */
    @PostMapping("/search")
    public CustomResponse<?> getNetworkSearch(@RequestBody RequestNetworkDTO.Search search) throws CustomException {
        return networkService.getNetworkSearch(search);
    }
}
