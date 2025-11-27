package com.illunex.emsaasrestapi.knowledge;

import com.illunex.emsaasrestapi.chat.mapper.ChatHistoryMapper;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
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
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final ModelMapper modelMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;


    /**
     * 지식 노드 생성
     * @param req
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public ResponseKnowledgeDTO.CreateNode createKnowledgeNode(RequestKnowledgeDTO.CreateNode req, MemberVO memberVO) throws CustomException {
        ResponseKnowledgeDTO.CreateNode response = new ResponseKnowledgeDTO.CreateNode();
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
        String noteStatusCd = req.getNoteStatusCd();

        // Create Knowledge Garden Node
        KnowledgeGardenNodeVO nodeVO = new KnowledgeGardenNodeVO();
        nodeVO.setPartnershipMemberIdx(pmVO.getIdx());
        nodeVO.setDepth(0);
        nodeVO.setLabel(req.getLabel());
        nodeVO.setSortOrder(0.0);
        nodeVO.setTypeCd(req.getTypeCd());
        nodeVO.setStateCd(EnumCode.KnowledgeGardenNode.StateCd.ACTIVE.getCode());
        nodeVO.setNoteStatusCd(noteStatusCd);
        nodeVO.setSourceCd(req.getSourceCd());
        knowledgeComponent.insertByNodeVOAndParentNodeIdx(nodeVO, req.getParentNodeIdx());

        // Create Knowledge Garden Node Version
        KnowledgeGardenNodeVersionVO versionVO = new KnowledgeGardenNodeVersionVO();
        versionVO.setContent(content);
        versionVO.setNodeIdx(nodeVO.getIdx());
        versionVO.setTitle(req.getLabel());
        versionVO.setNoteStatusCd(noteStatusCd);
        versionVO.setStateCd(EnumCode.KnowledgeGardenNode.StateCd.ACTIVE.getCode());
        versionVO.setSourceCd(req.getSourceCd());
        knowledgeGardenNodeVersionMapper.insertByKnowledgeGardenNodeVersionVO(versionVO);

        nodeVO.setCurrentVersionIdx(versionVO.getIdx());
        knowledgeGardenNodeMapper.updateByKnowledgeGardenNodeVO(nodeVO);

        modelMapper.map(nodeVO, response);
        response.setNodeIdx(nodeVO.getIdx());
        return response;
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
        searchStr = searchStr == null ? "" : searchStr;
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
        List<KnowledgeGardenNodeVO> nodes = knowledgeGardenNodeMapper.selectByPartnershipMemberIdxWithLimit(partnershipMemberIdx, maxNodeSize);
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
        List<KnowledgeGardenLinkVO> links = knowledgeGardenLinkMapper.selectByNodeIds(nodeIds);
        t2 = System.currentTimeMillis();
        log.info("엣지 조회 완료 - 소요시간: {}ms, 엣지개수: {}", (t2 - t1), links.size());

        // 5) DTO 변환
        t1 = System.currentTimeMillis();
        List<ResponseKnowledgeDTO.NodeInfo> nodeInfoList = nodes.stream()
                .map(knowledgeComponent::toNodeInfo).toList();
        List<ResponseKnowledgeDTO.EdgeInfo> edgeInfoList = links.stream()
                .map(knowledgeComponent::toEdgeInfo).toList();

        t2 = System.currentTimeMillis();
        log.info("DTO 변환 완료 - 소요시간: {}ms", (t2 - t1));

        // 6) 노드 정렬 및 중복 제거
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
     *
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

        Integer partnershipMemberIdx = partnershipMemberVO.getIdx();

        // 시작 노드 조회
        KnowledgeGardenNodeVO startNode = knowledgeComponent.selectNotTrashedByIdx(req.getNodeIdx());

        ResponseKnowledgeDTO.SearchNetwork response = new ResponseKnowledgeDTO.SearchNetwork();

        ResponseKnowledgeDTO.NodeInfo startNodeInfo = knowledgeComponent.toNodeInfo(startNode);

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

    /**
     * 지식 노드 수정
     * @param req
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public void updateKnowledgeNode(RequestKnowledgeDTO.UpdateNode req, MemberVO memberVO) throws CustomException {
        // 1) 파트너십 회원 검증
        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(
                memberVO, req.getPartnershipIdx());

        // 2) 노드 조회 + 소유자 확인
        KnowledgeGardenNodeVO node = knowledgeComponent.selectNotTrashedByIdx(req.getNodeIdx());

        if (!Objects.equals(node.getPartnershipMemberIdx(), pmVO.getIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        boolean nodeChanged = false; // label / currentVersionIdx 변경 여부 추적

        // 2-1) label 변경 (폴더/노트/키워드 공통)
        // 기존 값
        String oldLabel = node.getLabel();
        String newLabel = oldLabel;
        if (req.getLabel() != null && !req.getLabel().equals(oldLabel)) {
            newLabel = req.getLabel();
            node.setLabel(newLabel);
            nodeChanged = true;
        }

        String oldNoteStatusCd = node.getNoteStatusCd();
        String newNoteStatusCd = oldNoteStatusCd;
        boolean noteStatusChanged = false;
        if (req.getNoteStatusCd() != null && !req.getNoteStatusCd().equals(oldNoteStatusCd)) {
            newNoteStatusCd = req.getNoteStatusCd();
            node.setNoteStatusCd(newNoteStatusCd);
            nodeChanged = true;
            noteStatusChanged = true;
        }


        // NOTE 타입 여부
        boolean isNote = EnumCode.KnowledgeGardenNode.TypeCd.NOTE.getCode().equals(node.getTypeCd());

        // 3) NOTE 타입일 때만 content / 링크 처리
        if (isNote) {
            // 1) 현재 버전 내용 가져오기 (라벨만 바뀌는 경우 복사용)
            KnowledgeGardenNodeVersionVO currentVersion =
                    knowledgeGardenNodeVersionMapper.selectByIdx(node.getCurrentVersionIdx())
                            .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

            String newContent = currentVersion.getContent();
            boolean contentChanged = false;

            if (req.getContent() != null && !req.getContent().equals(currentVersion.getContent())) {
                newContent = req.getContent();
                contentChanged = true;
            }

            boolean labelChanged = !Objects.equals(newLabel, currentVersion.getTitle());

            // 라벨/컨텐츠 둘 중 하나라도 바뀌었으면 새 버전 생성
            if (labelChanged || contentChanged || noteStatusChanged) {
                KnowledgeGardenNodeVersionVO version = new KnowledgeGardenNodeVersionVO();
                version.setNodeIdx(node.getIdx());
                version.setTitle(newLabel);      // 새 제목
                version.setContent(newContent);  // 새 내용 or 기존 내용
                version.setNoteStatusCd(newNoteStatusCd); // 새 노트 상태 코드
                knowledgeGardenNodeVersionMapper.insertByKnowledgeGardenNodeVersionVO(version);

                node.setCurrentVersionIdx(version.getIdx());
                nodeChanged = true;
            }
            // 3-2) 키워드 링크 동기화
            if (req.getKeywordNodeIdxList() != null) {
                knowledgeGardenLinkMapper.deleteByStartNodeAndType(
                        node.getIdx(), EnumCode.KnowledgeGardenLink.TypeCd.KEYWORD.getCode());

                for (Integer keywordIdx : req.getKeywordNodeIdxList()) {
                    KnowledgeGardenNodeVO keyword = knowledgeGardenNodeMapper.selectByIdx(keywordIdx)
                            .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

                    if (!EnumCode.KnowledgeGardenNode.TypeCd.KEYWORD.getCode().equals(keyword.getTypeCd())) {
                        throw new CustomException(ErrorCode.KNOWLEDGE_LINK_TYPE_INVALID);
                    }
                    if (!Objects.equals(keyword.getPartnershipMemberIdx(), pmVO.getIdx())) {
                        throw new CustomException(ErrorCode.COMMON_INVALID);
                    }

                    KnowledgeGardenLinkVO link = new KnowledgeGardenLinkVO();
                    link.setStartNodeIdx(node.getIdx());
                    link.setEndNodeIdx(keywordIdx);
                    link.setTypeCd(EnumCode.KnowledgeGardenLink.TypeCd.KEYWORD.getCode());
                    knowledgeGardenLinkMapper.insertByKnowledgeGardenLinkVO(link);
                }
            }

            // 3-3) 참조 노트 링크 동기화
            if (req.getReferenceNodeIdxList() != null) {
                knowledgeGardenLinkMapper.deleteByStartNodeAndType(
                        node.getIdx(), EnumCode.KnowledgeGardenLink.TypeCd.REF.getCode());

                for (Integer refIdx : req.getReferenceNodeIdxList()) {
                    if (refIdx.equals(node.getIdx())) continue; // 자기 자신 참조 방지

                    KnowledgeGardenNodeVO ref = knowledgeGardenNodeMapper.selectByIdx(refIdx)
                            .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

                    if (!EnumCode.KnowledgeGardenNode.TypeCd.NOTE.getCode().equals(ref.getTypeCd())) {
                        throw new CustomException(ErrorCode.KNOWLEDGE_LINK_TYPE_INVALID);
                    }

                    KnowledgeGardenLinkVO link = new KnowledgeGardenLinkVO();
                    link.setStartNodeIdx(node.getIdx());
                    link.setEndNodeIdx(refIdx);
                    link.setTypeCd(EnumCode.KnowledgeGardenLink.TypeCd.REF.getCode());
                    knowledgeGardenLinkMapper.insertByKnowledgeGardenLinkVO(link);
                }
            }
        } else {
            // NOTE 아닌데 content / 링크가 들어왔으면? → 정책에 따라 에러 or 무시
            if (req.getContent() != null ||
                    req.getKeywordNodeIdxList() != null ||
                    req.getReferenceNodeIdxList() != null) {
                // 무시하거나 에러 던지기 (나는 에러 쪽이 안전하다고 봄)
                throw new CustomException(ErrorCode.KNOWLEDGE_NODE_TYPE_INVALID);
            }
        }

        // 4) 노드 정보 변경사항이 있으면 업데이트
        if (nodeChanged) {
            knowledgeGardenNodeMapper.updateByKnowledgeGardenNodeVO(node);
        }
        knowledgeComponent.rebuildSimilarRelationsForNote(node.getIdx());
    }

    /**
     * 지식 노드 상세 조회
     * @param nodeIdx
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @Transactional
    public ResponseKnowledgeDTO.KnowledgeNode getKnowledgeNodeDetail(Integer nodeIdx, MemberVO memberVO) throws CustomException {
        KnowledgeGardenNodeVO node = knowledgeComponent.selectNotTrashedByIdx(nodeIdx);
        // 파트너십 회원 체크
        PartnershipMemberVO pmVO = partnershipMemberMapper.selectByIdx(node.getPartnershipMemberIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        if (!Objects.equals(memberVO.getIdx(), pmVO.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        Optional<KnowledgeGardenNodeVersionVO> version = knowledgeGardenNodeVersionMapper.selectByIdx(node.getCurrentVersionIdx());

        String label = node.getLabel();
        String content = version.isPresent() ? version.get().getContent() : "";

        List<ResponseKnowledgeDTO.NodeInfo> keywordNodes = knowledgeGardenNodeMapper.selectLinkedNodeByStartNodeIdxAndTypeCd(node.getIdx(), EnumCode.KnowledgeGardenLink.TypeCd.KEYWORD.getCode()).stream()
                .map(n -> ResponseKnowledgeDTO.NodeInfo.builder()
                        .nodeId(n.getIdx())
                        .label(n.getLabel())
                        .type(n.getTypeCd())
                        .build()
                ).toList();
        List<ResponseKnowledgeDTO.NodeInfo> referenceNodes = knowledgeGardenNodeMapper.selectLinkedNodeByStartNodeIdxAndTypeCd(node.getIdx(), EnumCode.KnowledgeGardenLink.TypeCd.REF.getCode()).stream()
                .map(n -> ResponseKnowledgeDTO.NodeInfo.builder()
                        .nodeId(n.getIdx())
                        .label(n.getLabel())
                        .type(n.getTypeCd())
                        .build()
                ).toList();
        // 조회수 증가
        node.setViewCount(node.getViewCount() + 1);
        knowledgeGardenNodeMapper.updateByKnowledgeGardenNodeVO(node);

        List<ResponseKnowledgeDTO.NodeBreadCrumb> pathNodes = knowledgeGardenNodeMapper.selectBreadCrumbByNodeIdx(node.getIdx(), node.getPartnershipMemberIdx()).stream()
                .map(n -> ResponseKnowledgeDTO.NodeBreadCrumb.builder()
                        .nodeIdx(n.getIdx())
                        .label(n.getLabel())
                        .parentNodeIdx(n.getParentNodeIdx())
                        .build()
                ).toList();

        return ResponseKnowledgeDTO.KnowledgeNode.builder()
                .content(content)
                .currentVersionIdx(node.getCurrentVersionIdx())
                .nodeIdx(node.getIdx())
                .label(label)
                .viewCount(node.getViewCount())
                .noteStatusCd(node.getNoteStatusCd())
                .partnershipIdx(pmVO.getPartnershipIdx())
                .keywordNodeList(keywordNodes)
                .referenceNodeList(referenceNodes)
                .pathNodeList(pathNodes)
                .build();
    }

    /**
     * 지식 노드 버전 목록 조회
     * @param nodeIdx
     * @param memberVO
     * @param pageRequest
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getKnowledgeNodeVersions(Integer nodeIdx, MemberVO memberVO, CustomPageRequest pageRequest) throws CustomException {
        KnowledgeGardenNodeVO node = knowledgeGardenNodeMapper.selectByIdx(nodeIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        // 파트너십 회원 체크
        PartnershipMemberVO pmVO = partnershipMemberMapper.selectByIdx(node.getPartnershipMemberIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        if (!Objects.equals(memberVO.getIdx(), pmVO.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        Pageable pageable = pageRequest.of(new String[]{"idx,DESC"});

        List<ResponseKnowledgeDTO.KnowledgeNodeVersion> versions = knowledgeGardenNodeVersionMapper.selectByNodeIdxWithPageable(nodeIdx, pageable).stream()
                .map(v -> ResponseKnowledgeDTO.KnowledgeNodeVersion.builder()
                        .idx(v.getIdx())
                        .nodeIdx(v.getNodeIdx())
                        .label(v.getTitle())
                        .versionNo(v.getVersionNo())
                        .createdAt(v.getCreateDate())
                        .content(v.getContent())
                        .noteStatusCd(v.getNoteStatusCd())
                        .isCurrent(Objects.equals(v.getIdx(), node.getCurrentVersionIdx()))
                        .build()
                ).toList();
        long totalCount = knowledgeGardenNodeVersionMapper.countByNodeIdx(nodeIdx);


        return CustomResponse.builder()
                .data(new PageImpl<>(versions, pageable, totalCount))
                .build();
    }

    /**
     * 지식 노드 버전 복원
     * @param nodeIdx
     * @param versionIdx
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public void restoreKnowledgeNodeVersion(Integer nodeIdx, Integer versionIdx, MemberVO memberVO) throws CustomException {
        KnowledgeGardenNodeVO node = knowledgeGardenNodeMapper.selectByIdx(nodeIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        // 파트너십 회원 체크
        PartnershipMemberVO pmVO = partnershipMemberMapper.selectByIdx(node.getPartnershipMemberIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        if (!Objects.equals(memberVO.getIdx(), pmVO.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        KnowledgeGardenNodeVersionVO version = knowledgeGardenNodeVersionMapper.selectByIdx(versionIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        if (!Objects.equals(version.getNodeIdx(), node.getIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        node.setLabel(version.getTitle());
        node.setCurrentVersionIdx(version.getIdx());
        knowledgeGardenNodeMapper.updateByKnowledgeGardenNodeVO(node);
    }

    /**
     * 지식 노드 삭제 휴지통 이동
     * @param partnershipIdx
     * @param nodeIdx
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public void moveToTrash(Integer partnershipIdx, Integer nodeIdx, MemberVO memberVO) throws CustomException {
        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);

        KnowledgeGardenNodeVO root = knowledgeComponent.selectNotTrashedByIdx(nodeIdx);
        if (!Objects.equals(root.getPartnershipMemberIdx(), pmVO.getIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        // 1) 서브트리 전체 조회 (root 포함)
        List<KnowledgeGardenNodeVO> subtree =
                knowledgeGardenNodeMapper.selectSubtreeNodes(pmVO.getIdx(), nodeIdx);

        if (subtree.isEmpty()) return;

        List<Integer> nodeIds = subtree.stream().map(KnowledgeGardenNodeVO::getIdx).toList();

        // 2) NOTE만 삭제 스냅샷 버전 생성
        for (KnowledgeGardenNodeVO n : subtree) {
            if (!EnumCode.KnowledgeGardenNode.TypeCd.NOTE.getCode().equals(n.getTypeCd())) continue;

            KnowledgeGardenNodeVersionVO cur =
                    knowledgeGardenNodeVersionMapper.selectByIdx(n.getCurrentVersionIdx())
                            .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

            KnowledgeGardenNodeVersionVO v = new KnowledgeGardenNodeVersionVO();
            v.setNodeIdx(n.getIdx());
            v.setTitle(n.getLabel());
            v.setContent(cur.getContent());
            v.setNoteStatusCd(n.getNoteStatusCd());
            v.setStateCd(EnumCode.KnowledgeGardenNode.StateCd.TRASH.getCode());

            knowledgeGardenNodeVersionMapper.insertByKnowledgeGardenNodeVersionVO(v);

            // 현재 버전도 "삭제 스냅샷"으로 이동
            knowledgeGardenNodeMapper.updateCurrentVersionIdx(n.getIdx(), v.getIdx());
        }

        // 3) 노드 전체 TRASH
        knowledgeGardenNodeMapper.updateStateByNodeIds(
                pmVO.getIdx(),
                nodeIds,
                EnumCode.KnowledgeGardenNode.StateCd.TRASH.getCode()
        );

        // 4) 링크 전체 TRASH
        knowledgeGardenLinkMapper.updateStateByNodeIds(
                nodeIds,
                EnumCode.KnowledgeGardenLink.StateCd.TRASH.getCode()
        );
    }

    /**
     * 지식 노드 삭제 복원
     * @param partnershipIdx
     * @param nodeIdx
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public void restoreTrash(Integer partnershipIdx, Integer nodeIdx, MemberVO memberVO) throws CustomException {
        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);

        KnowledgeGardenNodeVO root = knowledgeGardenNodeMapper.selectByIdx(nodeIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        if (!Objects.equals(root.getPartnershipMemberIdx(), pmVO.getIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        // 1) 서브트리 전체 조회 (root 포함)
        List<KnowledgeGardenNodeVO> subtree =
                knowledgeGardenNodeMapper.selectSubtreeNodes(pmVO.getIdx(), nodeIdx);

        if (subtree.isEmpty()) return;

        List<Integer> nodeIds = subtree.stream().map(KnowledgeGardenNodeVO::getIdx).toList();

        // 2) NOTE만 복구 스냅샷 버전 생성
        for (KnowledgeGardenNodeVO n : subtree) {
            if (!EnumCode.KnowledgeGardenNode.TypeCd.NOTE.getCode().equals(n.getTypeCd())) continue;

            KnowledgeGardenNodeVersionVO cur =
                    knowledgeGardenNodeVersionMapper.selectByIdx(n.getCurrentVersionIdx())
                            .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

            KnowledgeGardenNodeVersionVO v = new KnowledgeGardenNodeVersionVO();
            v.setNodeIdx(n.getIdx());
            v.setTitle(n.getLabel());
            v.setContent(cur.getContent());
            v.setNoteStatusCd(n.getNoteStatusCd());
            v.setStateCd(EnumCode.KnowledgeGardenNode.StateCd.ACTIVE.getCode());

            knowledgeGardenNodeVersionMapper.insertByKnowledgeGardenNodeVersionVO(v);

            knowledgeGardenNodeMapper.updateCurrentVersionIdx(n.getIdx(), v.getIdx());
        }

        // 3) 노드 전체 ACTIVE
        knowledgeGardenNodeMapper.updateStateByNodeIds(
                pmVO.getIdx(),
                nodeIds,
                EnumCode.KnowledgeGardenNode.StateCd.ACTIVE.getCode()
        );

        // 4) 링크 전체 ACTIVE
        knowledgeGardenLinkMapper.updateStateByNodeIds(
                nodeIds,
                EnumCode.KnowledgeGardenLink.StateCd.ACTIVE.getCode()
        );
    }

    public List<KnowledgeGardenNodeVO> getTrashNodes(RequestKnowledgeDTO.TrashSearch req, MemberVO memberVO)
            throws CustomException {

        PartnershipMemberVO pmVO = partnershipComponent.checkPartnershipMember(memberVO, req.getPartnershipIdx());

        String searchStr = req.getSearchStr() == null ? "" : req.getSearchStr();
        Integer limit = req.getLimit() == null ? 50 : req.getLimit();

        return knowledgeGardenNodeMapper.selectTrashNodes(
                pmVO.getIdx(),
                req.getIncludeTypes(),
                searchStr,
                limit
        );
    }
}
