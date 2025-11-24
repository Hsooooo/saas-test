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
    public CustomResponse<?> createNode(@RequestBody @Valid RequestKnowledgeDTO.CreateNode req,
                                        @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(knowledgeService.createKnowledgeNode(req, memberVO))
                .build();
    }

    /**
     * 지식 노드 수정
     * @param req
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @PatchMapping("/node")
    public CustomResponse<?> updateNode(@RequestBody @Valid RequestKnowledgeDTO.UpdateNode req,
                                        @CurrentMember MemberVO memberVO) throws CustomException {
        knowledgeService.updateKnowledgeNode(req, memberVO);
        return CustomResponse.builder()
                .build();
    }

    /**
     * 노드 휴지통 이동
     */
    @PatchMapping("/node/trash/{nodeIdx}")
    public CustomResponse<?> moveToTrash(@PathVariable Integer nodeIdx,
                                         @RequestParam Integer partnershipIdx,
                                         @CurrentMember MemberVO memberVO) throws CustomException {
        knowledgeService.moveToTrash(partnershipIdx, nodeIdx, memberVO);
        return CustomResponse.builder().build();
    }

    /**
     * 노드 휴지통 복구
     */
    @PatchMapping("/node/restore/{nodeIdx}")
    public CustomResponse<?> restoreTrash(@PathVariable Integer nodeIdx,
                                          @RequestParam Integer partnershipIdx,
                                          @CurrentMember MemberVO memberVO) throws CustomException {
        knowledgeService.restoreTrash(partnershipIdx, nodeIdx, memberVO);
        return CustomResponse.builder().build();
    }

    /**
     * 휴지통 목록 조회
     */
    @PatchMapping("/node/trash/list")
    public CustomResponse<?> getTrashList(@RequestBody @Valid RequestKnowledgeDTO.TrashSearch req,
                                          @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(knowledgeService.getTrashNodes(req, memberVO))
                .build();
    }

    /**
     * 지식 노드 상세 조회
     * @param nodeIdx
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @GetMapping("/node/{nodeIdx}")
    public CustomResponse<?> getNodeDetail(@PathVariable Integer nodeIdx,
                                           @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(knowledgeService.getKnowledgeNodeDetail(nodeIdx, memberVO))
                .build();
    }

    /**
     * 지식 노드 버전 목록 조회
     * @param nodeIdx
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @GetMapping("/node/{nodeIdx}/versions")
    public CustomResponse<?> getNodeVersions(@PathVariable Integer nodeIdx,
                                             @CurrentMember MemberVO memberVO,
                                             CustomPageRequest pageRequest) throws CustomException {
        return knowledgeService.getKnowledgeNodeVersions(nodeIdx, memberVO, pageRequest);
    }

    /**
     * 지식 노드 버전 복원
     * @param nodeIdx
     * @param versionIdx
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @PatchMapping("/node/{nodeIdx}/restore/{versionIdx}")
    public CustomResponse<?> restoreNodeVersion(@PathVariable Integer nodeIdx,
                                                @PathVariable Integer versionIdx,
                                                @CurrentMember MemberVO memberVO) throws CustomException {
        knowledgeService.restoreKnowledgeNodeVersion(nodeIdx, versionIdx, memberVO);
        return CustomResponse.builder()
                .build();
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
                                        @RequestParam(required = false) String searchStr,
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
