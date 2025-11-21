package com.illunex.emsaasrestapi.knowledge;

import com.illunex.emsaasrestapi.chat.mapper.ChatHistoryMapper;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.knowledge.dto.RequestKnowledgeDTO;
import com.illunex.emsaasrestapi.knowledge.dto.ResponseKnowledgeDTO;
import com.illunex.emsaasrestapi.knowledge.mapper.KnowledgeGardenLinkMapper;
import com.illunex.emsaasrestapi.knowledge.mapper.KnowledgeGardenNodeMapper;
import com.illunex.emsaasrestapi.knowledge.mapper.KnowledgeGardenNodeVersionMapper;
import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenLinkVO;
import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeVO;
import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeVersionVO;
import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeViewVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {
    private final KnowledgeGardenNodeMapper knowledgeGardenNodeMapper;
    private final KnowledgeGardenNodeVersionMapper knowledgeGardenNodeVersionMapper;
    private final KnowledgeGardenLinkMapper knowledgeGardenLinkMapper;
    private final ChatHistoryMapper chatHistoryMapper;

    private final PartnershipComponent partnershipComponent;
    private final KnowledgeComponent knowledgeComponent;


    /**
     * 지식 노드 생성
     * @param req
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public void createKnowledgeNode(RequestKnowledgeDTO.CreateNode req, MemberVO memberVO) throws CustomException {
        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(memberVO, req.getPartnershipIdx());

        String content = "";
        if (req.getTypeCd().equals(EnumCode.KnowledgeGardenNode.TypeCd.NOTE.getCode())) {
            if (req.getChatHistoryIdx() != null) {
                // 검색도구 조회 결과 있는 경우 내용에 마크다운 변환하여 저장 TODO
                ChatHistoryVO chat = chatHistoryMapper.selectByIdx(req.getChatHistoryIdx()).orElseThrow(
                        () -> new CustomException(ErrorCode.COMMON_EMPTY)
                );
                content = chat.getMessage();
            }else if (req.getContent() != null) {
                content = req.getContent();
            } else {
                throw new CustomException(ErrorCode.KNOWLEDGE_NOTE_CONTENT_EMPTY);
            }
        }

        // Create Knowledge Garden Node
        KnowledgeGardenNodeVO nodeVO = new KnowledgeGardenNodeVO();
        nodeVO.setPartnershipMemberIdx(pmVO.getIdx());
        nodeVO.setDepth(0);
        nodeVO.setLabel(req.getLabel());
        nodeVO.setSortOrder(0.0);
        nodeVO.setTypeCd(req.getTypeCd());
        knowledgeComponent.insertByNodeVOAndParentNodeIdx(nodeVO, req.getParentNodeIdx());

        // Create Knowledge Garden Node Version
        KnowledgeGardenNodeVersionVO versionVO = new KnowledgeGardenNodeVersionVO();
        versionVO.setContent(content);
        versionVO.setNodeIdx(nodeVO.getIdx());
        versionVO.setTitle(req.getLabel());
        knowledgeGardenNodeVersionMapper.insertByKnowledgeGardenNodeVersionVO(versionVO);

        nodeVO.setCurrentVersionIdx(versionVO.getIdx());
        knowledgeGardenNodeMapper.updateByKnowledgeGardenNodeVO(nodeVO);
    }

    /**
     * 노드 트리 조회
     * @param partnershipIdx
     * @param parentNodeIdx
     * @param includeTypes
     * @param memberVO
     * @return
     * @throws CustomException
     */
    public List<KnowledgeGardenNodeViewVO> getKnowledgeNodeTree(Integer partnershipIdx,
                                                                Integer parentNodeIdx,
                                                                String[] includeTypes,
                                                                Integer limit,
                                                                MemberVO memberVO) throws CustomException {
        limit = limit == null ? 20 : limit;
        // 파트너십 멤버 체크
        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);
        // 트리 노드 조회
        return knowledgeGardenNodeMapper.selectTreeNodes(
                pmVO.getIdx(),
                includeTypes,
                parentNodeIdx,
                10
        );
    }

    /**
     * 지식 폴더 검색
     * @param partnershipIdx
     * @param searchStr
     * @param limit
     * @param memberVO
     * @return
     * @throws CustomException
     */
    public List<KnowledgeGardenNodeVO> searchKnowledgeFolders(Integer partnershipIdx, String searchStr, Integer limit, MemberVO memberVO) throws CustomException {
        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);
        return knowledgeGardenNodeMapper.selectBySearchFolder(
                        pmVO.getIdx(),
                        EnumCode.KnowledgeGardenNode.TypeCd.FOLDER.getCode(),
                        searchStr,
                        limit
                );
    }

    /**
     * 지식정원 전체 관계망 조회
     * @param partnershipIdx
     * @param memberVO
     * @param limit
     * @return
     * @throws CustomException
     */
    public ResponseKnowledgeDTO.SearchNetwork searchKnowledgeGardenAll(Integer partnershipIdx, MemberVO memberVO, Integer limit) throws CustomException {
        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);
        Integer partnershipMemberIdx = pmVO.getIdx();
        long startAll = System.currentTimeMillis();
        log.info("지식정원 전체 관계망 조회 시작 - ownerMemberIdx: {}, limit: {}", partnershipMemberIdx, limit);

        ResponseKnowledgeDTO.SearchNetwork response = new ResponseKnowledgeDTO.SearchNetwork();

        // 1) maxNodeSize 결정 (추후 설정 테이블이 있으면 거기서 가져와도 됨)
        Integer maxNodeSize = limit != null ? limit : 10000;

        // 2) 노드 조회
        long t1 = System.currentTimeMillis();
        List<KnowledgeGardenNodeVO> nodes = knowledgeGardenNodeMapper
                .selectByPartnershipMemberIdxWithLimit(partnershipMemberIdx, maxNodeSize);
        long t2 = System.currentTimeMillis();
        log.info("노드 조회 완료 - 소요시간: {}ms, 노드개수: {}", (t2 - t1), nodes.size());

        if (nodes.isEmpty()) {
            return response;
        }

        // 3) 노드 ID 수집
        List<Integer> nodeIds = nodes.stream()
                .map(KnowledgeGardenNodeVO::getIdx)
                .toList();

        // 4) 엣지 조회 (현재 노드들과 연결된 링크만)
        t1 = System.currentTimeMillis();
        List<KnowledgeGardenLinkVO> links = knowledgeGardenLinkMapper
                .selectByNodeIds(nodeIds);    // 아래에 SQL 예시
        t2 = System.currentTimeMillis();
        log.info("엣지 조회 완료 - 소요시간: {}ms, 엣지개수: {}", (t2 - t1), links.size());

        // 5) DTO 변환
        t1 = System.currentTimeMillis();
        List<ResponseKnowledgeDTO.NodeInfo> nodeInfoList = nodes.stream()
                .map(n -> ResponseKnowledgeDTO.NodeInfo.builder()
                        .nodeId(n.getIdx())
                        .type(n.getTypeCd())
                        .label(n.getLabel())
                        .build()
                ).toList();

        List<ResponseKnowledgeDTO.EdgeInfo> edgeInfoList = links.stream()
                .map(l -> ResponseKnowledgeDTO.EdgeInfo.builder()
                        .edgeId(l.getIdx())
                        .startNodeId(l.getStartNodeIdx())
                        .endNodeId(l.getEndNodeIdx())
                        .type(l.getTypeCd())
                        .weight(l.getWeight())
                        .build()
                ).toList();
        t2 = System.currentTimeMillis();
        log.info("DTO 변환 완료 - 소요시간: {}ms", (t2 - t1));

        // 6) 노드 정렬 및 중복 제거 (원래 코드처럼)
        t1 = System.currentTimeMillis();
        nodeInfoList = nodeInfoList.stream()
                .distinct()
                .sorted(Comparator.comparing(
                        ResponseKnowledgeDTO.NodeInfo::getLabel,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();
        List<ResponseKnowledgeDTO.EdgeInfo> distinctEdges = edgeInfoList.stream()
                .distinct()
                .toList();
        t2 = System.currentTimeMillis();
        log.info("노드 정렬 및 중복제거 완료 - 소요시간: {}ms", (t2 - t1));

        response.setNodes(nodeInfoList);
        response.setEdges(distinctEdges);
        response.setNodeSize(nodeInfoList.size());
        response.setEdgeSize(distinctEdges.size());

        long endAll = System.currentTimeMillis();
        log.info("지식정원 전체 관계망 조회 완료 - 총 소요시간: {}ms, 노드: {}, 엣지: {}",
                (endAll - startAll),
                nodeInfoList.size(),
                distinctEdges.size()
        );

        return response;
    }

    /**
     * 지식정원 노드 관계망 확장 조회
     * @param req
     * @param memberVO
     * @return
     * @throws CustomException
     */
    public ResponseKnowledgeDTO.SearchNetwork getKnowledgeGardenNetworkExtend(RequestKnowledgeDTO.ExtendSearch req, MemberVO memberVO) throws CustomException {
        // 파트너쉽 회원 여부 체크 (기존 구조와 맞추기)
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(
                memberVO,
                req.getPartnershipIdx()
        );

        // 여기서 owner 기준을 partnershipMemberVO.getIdx() 로 잡는다고 가정
        Integer partnershipMemberIdx = partnershipMemberVO.getIdx();

        // 시작 노드 조회
        KnowledgeGardenNodeVO startNode = knowledgeGardenNodeMapper.selectByIdxAndPartnershipMemberIdx(req.getNodeIdx(), partnershipMemberIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

        ResponseKnowledgeDTO.SearchNetwork response = new ResponseKnowledgeDTO.SearchNetwork();

        // 시작 노드를 response에 세팅
        ResponseKnowledgeDTO.NodeInfo startNodeInfo = ResponseKnowledgeDTO.NodeInfo.builder()
                .nodeId(startNode.getIdx())
                .type(startNode.getTypeCd())
                .label(startNode.getLabel())
                .build();

        response.setNodes(new ArrayList<>(List.of(startNodeInfo)));
        response.setEdges(new ArrayList<>());

        // 관계망 확장 (depth 단계)
        knowledgeComponent.networkSearch(
                response,
                List.of(startNode),
                partnershipMemberIdx,
                req.getDepth()
        );

        if (response.getNodes() != null) response.setNodeSize(response.getNodes().size());
        if (response.getEdges() != null) response.setEdgeSize(response.getEdges().size());

        return response;
    }


    /**
     * 지식 노드 트리 위치 업데이트
     * @param req
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public void updateKnowledgeNodeTreePosition(RequestKnowledgeDTO.TreePosition req,
                                                MemberVO memberVO) throws CustomException {

        // 0. 파트너쉽 회원 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(
                memberVO,
                req.getPartnershipIdx()
        );

        // 1. 이동 대상 노드 조회
        KnowledgeGardenNodeVO moving = knowledgeGardenNodeMapper.selectByIdx(req.getNodeIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

        // 2. 드롭 타겟 노드 조회
        KnowledgeGardenNodeVO target = knowledgeGardenNodeMapper.selectByIdx(req.getTargetNodeIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

        // 자기 자신에게 드롭하는 거 방지
        if (moving.getIdx().equals(target.getIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        Integer oldParentId = moving.getParentNodeIdx();
        Integer newParentId;
        Double newSortOrder;

        RequestKnowledgeDTO.TreePosition.DropPosition position = req.getPosition();

        // ===== 3. 위치 계산 =====
        if (position == RequestKnowledgeDTO.TreePosition.DropPosition.INTO) {
            // 3-1. 타겟의 자식으로 (마지막 순서)
            newParentId = target.getIdx();

            Double maxSort = knowledgeGardenNodeMapper
                    .selectNextSortOrder(partnershipMemberVO.getIdx(), newParentId);
            newSortOrder = (maxSort == null ? 1.0 : maxSort);

        } else if (position == RequestKnowledgeDTO.TreePosition.DropPosition.BEFORE) {
            // 3-2. 타겟과 같은 부모, 타겟 앞에
            newParentId = target.getParentNodeIdx();

            KnowledgeGardenNodeVO prev = knowledgeGardenNodeMapper
                    .selectPrevByNodeIdx(target.getIdx());

            Double prevSort = (prev == null ? null : prev.getSortOrder());
            Double nextSort = target.getSortOrder();   // target 자기 자신

            if (prevSort == null) {
                newSortOrder = nextSort - 0.1;
            } else {
                newSortOrder = (prevSort + nextSort) / 2.0;
            }

        } else { // AFTER
            // 3-3. 타겟과 같은 부모, 타겟 뒤에
            newParentId = target.getParentNodeIdx();

            KnowledgeGardenNodeVO next = knowledgeGardenNodeMapper
                    .selectNextByNodeIdx(target.getIdx());

            Double prevSort = target.getSortOrder();
            Double nextSort = (next == null ? null : next.getSortOrder());

            if (nextSort == null) {
                newSortOrder = prevSort + 1.0;
            } else {
                newSortOrder = (prevSort + nextSort) / 2.0;
            }
        }

        // ===== 4. 깊이(depth) 계산 =====
        int newDepth;
        if (newParentId == null) {
            newDepth = 0;
        } else {
            KnowledgeGardenNodeVO newParent = knowledgeGardenNodeMapper
                    .selectByIdx(newParentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
            newDepth = newParent.getDepth() + 1;
        }

        // ===== 5. 노드 row 업데이트 =====
        moving.setParentNodeIdx(newParentId);
        moving.setDepth(newDepth);
        moving.setSortOrder(newSortOrder);
        knowledgeGardenNodeMapper.updateByKnowledgeGardenNodeVO(moving);

        // ===== 6. TREE 링크 동기화 =====
        if (!Objects.equals(oldParentId, newParentId)) {
            // 6-1. 기존 TREE 링크 제거
            if (oldParentId != null) {
                knowledgeGardenLinkMapper.deleteByParentAndNodeIdx(oldParentId, moving.getIdx());
            }

            // 6-2. 새 TREE 링크 생성
            if (newParentId != null) {
                KnowledgeGardenLinkVO linkVO = new KnowledgeGardenLinkVO();
                linkVO.setStartNodeIdx(newParentId);
                linkVO.setEndNodeIdx(moving.getIdx());
                linkVO.setTypeCd(EnumCode.KnowledgeGardenLink.TypeCd.TREE.getCode());
                knowledgeGardenLinkMapper.insertByKnowledgeGardenLinkVO(linkVO);
            }
        }

        // ===== 7. 자식 depth 일괄 업데이트 =====
        knowledgeComponent.updateSubtreeDepth(moving.getIdx(), newDepth);
    }

    public List<KnowledgeGardenNodeVO> searchKnowledgeNodes(RequestKnowledgeDTO.NodeSearch req, MemberVO memberVO) throws CustomException {
        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(memberVO, req.getPartnershipIdx());

        return knowledgeGardenNodeMapper.selectByPmIdxAndSearchStrAndTypeCdInWithLimit(
                pmVO.getIdx(),
                req.getSearchStr(),
                req.getIncludeTypes(),
                req.getLimit()
        );
    }
}
