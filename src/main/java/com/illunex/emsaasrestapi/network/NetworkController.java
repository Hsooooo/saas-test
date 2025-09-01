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
     * 단일 노드 확장 조회
     * @param memberVO
     * @param multiExtendSearch
     * @return
     * @throws CustomException
     */
    @PostMapping("/extend/multi")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getNetworkMultiExtend(@CurrentMember MemberVO memberVO,
                                                    @RequestBody RequestNetworkDTO.MultiExtendSearch multiExtendSearch) throws CustomException {
        return networkService.getNetworkMultiExtend(memberVO, multiExtendSearch);
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
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getNetworkSearch(@CurrentMember MemberVO memberVO,
                    @RequestBody RequestNetworkDTO.Search search) throws CustomException {
        return networkService.getNetworkSearch(memberVO, search);
    }

    /**
     * 관계망 확장 조회 API
     * @param search
     * @return
     * @throws CustomException
     */
    @PostMapping("/search/extend")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getExtendNetworkSearch(@CurrentMember MemberVO memberVO,
                                              @RequestBody RequestNetworkDTO.ExtendSearch search) throws CustomException {
        return networkService.getExtendNetworkSearch(memberVO, search);
    }

    /**
     * 자동완성 API
     * @param memberVO
     * @param autoCompleteSearch
     * @return
     * @throws CustomException
     */
    @PatchMapping("/autoComplete")
    public CustomResponse<?> getAutoComplete(@CurrentMember MemberVO memberVO,
                                             @RequestBody RequestNetworkDTO.AutoCompleteSearch autoCompleteSearch) throws CustomException {
        return networkService.getAutoComplete(memberVO,autoCompleteSearch);
    }

    /**
     * 관계망 엣지 최소/최대값 조회 API
     * @param memberVO
     * @param aggregationMinMax
     * @return
     */
    @PostMapping("/search/minmax")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getAggregationMinMax(@CurrentMember MemberVO memberVO,
                                               @RequestBody RequestNetworkDTO.AggregationMinMax aggregationMinMax) throws CustomException {
        return networkService.getAggregationMinMax(memberVO, aggregationMinMax);
    }
}
