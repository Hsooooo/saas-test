package com.illunex.emsaasrestapi.knowledge;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.knowledge.dto.ResponseKnowledgeDTO;
import com.illunex.emsaasrestapi.knowledge.mapper.KnowledgeGardenLinkMapper;
import com.illunex.emsaasrestapi.knowledge.mapper.KnowledgeGardenNodeMapper;
import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenLinkVO;
import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgeComponent {
    private final KnowledgeGardenNodeMapper knowledgeGardenNodeMapper;
    private final KnowledgeGardenLinkMapper knowledgeGardenLinkMapper;

    /**
     * 노드 VO와 부모 노드 idx로 노드 및 링크 생성
     * @param nodeVO
     * @param parentNodeIdx
     */
    @Transactional
    public void insertByNodeVOAndParentNodeIdx(KnowledgeGardenNodeVO nodeVO, Integer parentNodeIdx) {
        // 1) 부모 노드 존재 여부에 따라 Depth/Parent 설정
        if (parentNodeIdx == null) {
            // 루트 노드
            nodeVO.setDepth(0);
            nodeVO.setParentNodeIdx(null);
        } else {
            // 부모 노드 조회
            KnowledgeGardenNodeVO parent = selectNotTrashedByIdx(parentNodeIdx);

            nodeVO.setDepth(parent.getDepth() + 1);
            nodeVO.setParentNodeIdx(parent.getIdx());
        }

        // 2) 노드 저장
        Double nextSortOrder = knowledgeGardenNodeMapper.selectNextSortOrder(nodeVO.getPartnershipMemberIdx(), parentNodeIdx);
        nodeVO.setSortOrder(nextSortOrder);
        knowledgeGardenNodeMapper.insertByKnowledgeGardenNodeVO(nodeVO);

        // ===== 루트 노드는 Link 생성 안함 =====
        if (parentNodeIdx == null) {
            return;
        }

        KnowledgeGardenLinkVO linkVO = new KnowledgeGardenLinkVO();
        linkVO.setStartNodeIdx(parentNodeIdx);
        linkVO.setEndNodeIdx(nodeVO.getIdx());
        linkVO.setTypeCd(EnumCode.KnowledgeGardenLink.TypeCd.TREE.getCode());
        linkVO.setStateCd(EnumCode.KnowledgeGardenLink.StateCd.ACTIVE.getCode());
        knowledgeGardenLinkMapper.insertByKnowledgeGardenLinkVO(linkVO);
    }

    public void networkSearch(ResponseKnowledgeDTO.SearchNetwork response,
                              List<KnowledgeGardenNodeVO> nodes,
                              Integer partnershipMemberIdx,
                              int depth) {

        if (nodes == null || nodes.isEmpty() || depth <= 0) return;

        // 1. 현재까지 response에 들어간 노드/엣지 ID 집합 (중복 방지용)
        Set<Integer> existingNodeIds = response.getNodes().stream()
                .map(ResponseKnowledgeDTO.NodeInfo::getNodeId)
                .collect(Collectors.toSet());

        Set<Integer> existingEdgeIds = response.getEdges().stream()
                .map(ResponseKnowledgeDTO.EdgeInfo::getEdgeId)
                .collect(Collectors.toSet());

        // 2. 이번 단계 frontier 노드 ID 목록
        List<Integer> frontierNodeIds = nodes.stream()
                .map(KnowledgeGardenNodeVO::getIdx)
                .distinct()
                .toList();

        if (frontierNodeIds.isEmpty()) return;

        // ====== 엣지 조회 ======
        // start_node_idx IN frontier OR end_node_idx IN frontier
        List<KnowledgeGardenLinkVO> edgeList = knowledgeGardenLinkMapper
                .selectByNodeIds(frontierNodeIds);

        if (edgeList.isEmpty()) return;

        // 3. 엣지 → DTO 변환 + 중복 제거
        List<ResponseKnowledgeDTO.EdgeInfo> newEdgeInfos = edgeList.stream()
                .filter(e -> !existingEdgeIds.contains(e.getIdx()))
                .map(this::toEdgeInfo)
                .toList();

        if (!newEdgeInfos.isEmpty()) {
            List<ResponseKnowledgeDTO.EdgeInfo> mutableLinkList = new ArrayList<>(response.getEdges());
            mutableLinkList.addAll(newEdgeInfos);
            // distinct는 equals/hashCode 정의에 따라 다름. edgeId 기준이면 Set으로 바꿔도 됨.
            response.setEdges(mutableLinkList.stream().distinct().toList());
        }

        // ====== 엣지에서 새 노드 찾기 ======
        Set<Integer> neighborNodeIds = new HashSet<>();
        for (KnowledgeGardenLinkVO e : edgeList) {
            neighborNodeIds.add(e.getStartNodeIdx());
            neighborNodeIds.add(e.getEndNodeIdx());
        }

        // 이미 response에 들어간 노드는 제외
        neighborNodeIds.removeAll(existingNodeIds);

        if (neighborNodeIds.isEmpty()) return;

        // ====== 새 노드들 조회 ======
        List<KnowledgeGardenNodeVO> neighborNodes = knowledgeGardenNodeMapper.selectByIdxInAndPartnershipMemberIdx(neighborNodeIds, partnershipMemberIdx);

        if (neighborNodes.isEmpty()) return;

        // 새 노드 → DTO
        List<ResponseKnowledgeDTO.NodeInfo> newNodeInfos = neighborNodes.stream()
                .filter(n -> !existingNodeIds.contains(n.getIdx()))
                .map(this::toNodeInfo)
                .toList();

        if (!newNodeInfos.isEmpty()) {
            List<ResponseKnowledgeDTO.NodeInfo> mutableNodeList = new ArrayList<>(response.getNodes());
            mutableNodeList.addAll(newNodeInfos);
            response.setNodes(mutableNodeList.stream().distinct().toList());
        }

        // ====== 재귀 호출 (depth-1) ======
        networkSearch(response, neighborNodes, partnershipMemberIdx, depth - 1);
    }

    @Transactional
    public void updateSubtreeDepth(Integer nodeIdx, int parentDepth) {
        // 1) 현재 노드 depth 수정
        int newDepth = parentDepth + 1;
        knowledgeGardenNodeMapper.updateDepth(nodeIdx, newDepth);

        // 2) 자식 노드들 조회
        List<KnowledgeGardenNodeVO> children = knowledgeGardenNodeMapper.selectChildrenByNodeIdx(nodeIdx);

        // 3) 자식 각각에 대해 재귀 수행
        for (KnowledgeGardenNodeVO child : children) {
            updateSubtreeDepth(child.getIdx(), newDepth);
        }
    }

    public KnowledgeGardenNodeVO selectNotTrashedByIdx(Integer idx) {
        KnowledgeGardenNodeVO node = knowledgeGardenNodeMapper
                .selectByIdx(idx)
                .orElseThrow(() -> new IllegalArgumentException("노드를 찾을 수 없습니다. idx=" + idx));
        if (node.getStateCd() != null && node.getStateCd().equals(EnumCode.KnowledgeGardenNode.StateCd.TRASH.getCode())) {
            throw new IllegalArgumentException("노드가 휴지통 상태입니다. idx=" + idx);
        }
        return node;
    }

    private Map<String, Object> buildNodeProperties(KnowledgeGardenNodeVO n) {
        Map<String, Object> props = new HashMap<>();

        props.put("stateCd", n.getStateCd());
        props.put("viewCount", n.getViewCount());
        props.put("depth", n.getDepth());
        props.put("parentNodeIdx", n.getParentNodeIdx());
        props.put("createDate", n.getCreateDate());

        if (EnumCode.KnowledgeGardenNode.TypeCd.NOTE.getCode().equals(n.getTypeCd())) {
            props.put("noteStatusCd", n.getNoteStatusCd());
            props.put("currentVersionIdx", n.getCurrentVersionIdx());
        }

        return props;
    }

    public ResponseKnowledgeDTO.NodeInfo toNodeInfo(KnowledgeGardenNodeVO n) {
        return ResponseKnowledgeDTO.NodeInfo.builder()
                .nodeId(n.getIdx())
                .type(n.getTypeCd())
                .label(n.getLabel())
                .properties(buildNodeProperties(n))
                .build();
    }

    private Map<String, Object> buildEdgeProperties(KnowledgeGardenLinkVO l) {
        Map<String, Object> props = new HashMap<>();
        props.put("stateCd", l.getStateCd());
        props.put("typeCd", l.getTypeCd());
        return props;
    }

    public ResponseKnowledgeDTO.EdgeInfo toEdgeInfo(KnowledgeGardenLinkVO l) {
        return ResponseKnowledgeDTO.EdgeInfo.builder()
                .edgeId(l.getIdx())
                .startNodeId(l.getStartNodeIdx())
                .endNodeId(l.getEndNodeIdx())
                .type(l.getTypeCd())
                .weight(l.getWeight())
                .properties(buildEdgeProperties(l))
                .build();
    }

}