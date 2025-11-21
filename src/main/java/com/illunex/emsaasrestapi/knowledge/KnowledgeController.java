package com.illunex.emsaasrestapi.knowledge;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.knowledge.dto.RequestKnowledgeDTO;
import com.illunex.emsaasrestapi.knowledge.dto.ResponseKnowledgeDTO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("knowledge")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    /**
     * 지식 노드 생성
     * @param req
     * @param memberVO
     * @throws CustomException
     */
    @PostMapping("/node")
    public void createNode(@RequestBody @Valid RequestKnowledgeDTO.CreateNode req,
                           @CurrentMember MemberVO memberVO) throws CustomException {
        knowledgeService.createKnowledgeNode(req, memberVO);
    }

    /**
     * 지식 노드 트리 조회
     * @param partnershipIdx
     * @param parentNodeIdx
     * @param includeTypes
     * @param limit
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @GetMapping("/node/tree")
    public CustomResponse<?> getNodeTree(@RequestParam Integer partnershipIdx,
                                         @RequestParam(required = false) Integer parentNodeIdx,
                                         @RequestParam String[] includeTypes,
                                         @RequestParam(required = false) Integer limit,
                                         @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(knowledgeService.getKnowledgeNodeTree(partnershipIdx, parentNodeIdx, includeTypes, limit, memberVO))
                .build();
    }

    /**
     * 지식 노드 트리 위치 변경
     * @param req
     * @param memberVO
     * @throws CustomException
     */
    @PatchMapping("/node/tree/position")
    public void updateNodeTreePosition(@RequestBody @Valid RequestKnowledgeDTO.TreePosition req,
                                       @CurrentMember MemberVO memberVO) throws CustomException {
        knowledgeService.updateKnowledgeNodeTreePosition(req, memberVO);
    }


    /**
     * 지식 노드 폴더 검색
     * @param partnershipIdx
     * @param searchStr
     * @param limit
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @GetMapping("/folders/search")
    public CustomResponse<?> searchFolders(@RequestParam Integer partnershipIdx,
                                        @RequestParam String searchStr,
                                        @RequestParam Integer limit,
                                        @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(knowledgeService.searchKnowledgeFolders(partnershipIdx, searchStr, limit, memberVO))
                .build();
    }

    /**
     * 지식 노드 검색
     * @param req
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @PatchMapping("/node/search")
    public CustomResponse<?> getNodeSearchList(@RequestBody @Valid RequestKnowledgeDTO.NodeSearch req,
                                               @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(knowledgeService.searchKnowledgeNodes(req, memberVO))
                .build();
    }

    /**
     * 지식정원 전체 관계망 조회
     * @param partnershipIdx
     * @param limit
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @GetMapping("/network/all")
    public CustomResponse<?> getKnowledgeGardenNetworkAll(@RequestParam Integer partnershipIdx,
                                                          @RequestParam Integer limit,
                                                          @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(knowledgeService.searchKnowledgeGardenAll(partnershipIdx, memberVO, limit))
                .build();
    }

    /**
     * 지식정원 관계망 단일 확장
     * @param req
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @PatchMapping("/network/extend")
    public CustomResponse<?> getKnowledgeGardenNetworkExtend(@RequestBody RequestKnowledgeDTO.ExtendSearch req,
                                                             @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(knowledgeService.getKnowledgeGardenNetworkExtend(req, memberVO))
                .build();
    }
}
