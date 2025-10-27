package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.chat.dto.ResponseAIDTO;
import com.illunex.emsaasrestapi.chat.dto.ResponseChatDTO;
import com.illunex.emsaasrestapi.chat.mapper.*;
import com.illunex.emsaasrestapi.chat.vo.*;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatHistoryMapper chatHistoryMapper;
    private final ChatToolResultMapper chatToolResultMapper;
    private final ChatFileMapper chatFileMapper;
    private final ChatFileSlideMapper chatFileSlideMapper;
    private final ChatNetworkMapper chatNetworkMapper;
    private final ChatNodeMapper chatNodeMapper;
    private final ChatLinkMapper chatLinkMapper;
    private final ChatMcpMapper chatMcpMapper;
    private final ObjectMapper om;
    @Value("${ai.url}") String aiGptBase;
    private final WebClient webClient;

    public int resolveChatRoom(int partnershipMemberIdx, String title) {
        ChatRoomVO room = new ChatRoomVO();
        room.setPartnershipMemberIdx(partnershipMemberIdx);
        room.setTitle(title != null ? title : "새 대화");
        chatRoomMapper.insertByChatRoomVO(room);
        return room.getIdx();
    }

    public void saveHistoryAsync(int chatRoomIdx, String senderType, String categoryType, String message) {
        Mono.fromRunnable(() -> {
            ChatHistoryVO h = new ChatHistoryVO();
            h.setChatRoomIdx(chatRoomIdx);
            h.setSenderType(senderType);
            h.setMessage(message);
            h.setCategoryType(categoryType);
            chatHistoryMapper.insertByChatHistoryVO(h);
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    public CustomResponse<?> getChatRoomList(MemberVO memberVO, Integer partnershipMemberIdx, CustomPageRequest page, String[] sort) throws CustomException {
        PartnershipMemberVO pm = partnershipMemberMapper.selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new IllegalArgumentException("Partnership member not found"));
        if (!memberVO.getIdx().equals(pm.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }
        Pageable pageable = page.of(sort);
        return CustomResponse.builder()
                .data(chatRoomMapper.selectByPartnershipMemberIdx(partnershipMemberIdx))
                .build();
    }

    public CustomResponse<?> getChatHistoryList(
            MemberVO memberVO, Integer partnershipMemberIdx, Integer chatRoomIdx,
            CustomPageRequest page, String[] sort) throws CustomException {

        var pm = partnershipMemberMapper.selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID)); // not found도 통일
        if (!Objects.equals(memberVO.getIdx(), pm.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        chatRoomMapper.selectByPartnershipMemberIdxAndChatRoomIdx(partnershipMemberIdx, chatRoomIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID)); // 메시지/코드 통일

        Pageable pageable = page.ofWithStableSort(sort, List.of("create_date,DESC", "idx,DESC")); // 타이브레이커 강제

        List<ChatHistoryVO> historyList =
                chatHistoryMapper.selectByChatRoomIdxAndPageable(chatRoomIdx, pageable);

        if (historyList.isEmpty()) {
            return CustomResponse.builder()
                    .data(new PageImpl<>(List.of(), pageable, 0))
                    .build();
        }

        // === 1) 배치 조회 준비
        List<Integer> historyIds = historyList.stream().map(ChatHistoryVO::getIdx).toList();

        // === 2) 도구결과 배치 조회 후 groupBy
        Map<Integer, List<ChatToolResultVO>> toolMap =
                chatToolResultMapper.selectByChatHistoryIdxIn(historyIds).stream()
                        .collect(Collectors.groupingBy(ChatToolResultVO::getChatHistoryIdx));

        // === 3) 파일 배치 조회 → 슬라이드도 한 번에
        List<ChatFileVO> allFiles = chatFileMapper.selectByChatHistoryIdxIn(historyIds);
        Map<Integer, List<ChatFileVO>> fileMap =
                allFiles.stream().collect(Collectors.groupingBy(ChatFileVO::getChatHistoryIdx));

        // PPTX 파일들의 id만 뽑아서 슬라이드 배치 조회
        List<Long> pptxFileIds = allFiles.stream()
                .filter(f -> Objects.equals(f.getFileCd(), EnumCode.ChatFile.FileCd.PPTX.getCode()))
                .map(ChatFileVO::getIdx)
                .toList();

        Map<Long, List<ChatFileSlideVO>> slideMap = pptxFileIds.isEmpty()
                ? Map.of()
                : chatFileSlideMapper.selectByChatFileIdxIn(pptxFileIds).stream()
                .collect(Collectors.groupingBy(ChatFileSlideVO::getChatFileIdx));
//
//        List<ChatNetworkVO> networks = chatNetworkMapper.selectByChatHistoryIdxIn(historyIds);
//        Map<Integer, List<ChatNetworkVO>> networkMap =
//                networks.stream().collect(Collectors.groupingBy(ChatNetworkVO::getChatHistoryIdx));
        List<ChatNetworkVO> networks = chatNetworkMapper.selectByChatHistoryIdxIn(historyIds);
        Map<Integer, List<ChatNetworkVO>> networkMap =
                networks.stream().collect(Collectors.groupingBy(ChatNetworkVO::getChatHistoryIdx));

        List<Integer> networkIds = networks.stream().map(ChatNetworkVO::getIdx).toList();

// full 모드: 노드/링크까지 한 번에 당겨오기
        List<ChatNodeVO> allNodes = networkIds.isEmpty() ? List.of()
                : chatNodeMapper.selectByChatNetworkIdxIn(networkIds);
        List<ChatLinkVO> allLinks = networkIds.isEmpty() ? List.of()
                : chatLinkMapper.selectByChatNetworkIdxIn(networkIds);

        // groupBy
        Map<Integer, List<ChatNodeVO>> nodeMap =
                allNodes.stream().collect(Collectors.groupingBy(ChatNodeVO::getChatNetworkIdx));
        Map<Integer, List<ChatLinkVO>> linkMap =
                allLinks.stream().collect(Collectors.groupingBy(ChatLinkVO::getChatNetworkIdx));

        // === 4) DTO 매핑
        List<ResponseChatDTO.History> response = historyList.stream().map(h -> {
            // tools
            List<ResponseChatDTO.ToolResult> toolResults = toolMap.getOrDefault(h.getIdx(), List.of())
                    .stream()
                    .map(t -> ResponseChatDTO.ToolResult.builder()
                            .idx(t.getIdx())
                            .chatHistoryIdx(t.getChatHistoryIdx())
                            .toolType(t.getToolType())
                            .title(t.getTitle())
                            .url(t.getUrl())
                            .createDate(t.getCreateDate())
                            .updateDate(t.getUpdateDate())
                            .build())
                    .toList();

            // files (+slides)
            List<ResponseChatDTO.ChatFileResult> chatFiles = fileMap.getOrDefault(h.getIdx(), List.of())
                    .stream()
                    .map(f -> {
                        List<String> slides = slideMap.getOrDefault(f.getIdx(), List.of())
                                .stream()
                                .filter(Objects::nonNull)
                                .map(s -> s.getPage() + ":" + s.getContent())
                                .toList();
                        return ResponseChatDTO.ChatFileResult.builder()
                                .idx(f.getIdx())
                                .chatHistoryIdx(f.getChatHistoryIdx())
                                .fileName(f.getFileName())
                                .fileUrl(f.getFileUrl())
                                .filePath(f.getFilePath())
                                .fileSize(f.getFileSize())
                                .fileCd(f.getFileCd())
                                .slides(slides) // 빌더에서 바로
                                .createDate(f.getCreateDate())
                                .updateDate(f.getUpdateDate())
                                .build();
                    })
                    .toList();

            // networks (full)
            List<ResponseChatDTO.ChatNetwork> chatNetworks = networkMap.getOrDefault(h.getIdx(), List.of())
                    .stream()
                    .map(n -> {
                        // 노드/링크 붙이기
                        List<ResponseChatDTO.ChatNode> dtoNodes =
                                nodeMap.getOrDefault(n.getIdx(), List.of()).stream()
                                        .map(v -> ResponseChatDTO.ChatNode.builder()
                                                .idx(v.getIdx())
                                                .id(v.getId())
                                                .labels(parseLabels(v.getLabels()))
                                                .properties(parseProps(v.getProperties()))
                                                .build())
                                        .toList();

                        List<ResponseChatDTO.ChatLink> dtoLinks =
                                linkMap.getOrDefault(n.getIdx(), List.of()).stream()
                                        .map(v -> ResponseChatDTO.ChatLink.builder()
                                                .idx(v.getIdx())
                                                .type(v.getType())
                                                .start(v.getStart())
                                                .end(v.getEnd())
                                                .properties(parseProps(v.getProperties()))
                                                .build())
                                        .toList();

                        return ResponseChatDTO.ChatNetwork.builder()
                                .idx(n.getIdx())
                                .chatHistoryIdx(n.getChatHistoryIdx())
                                .title(n.getTitle())
                                .nodes(dtoNodes)
                                .links(dtoLinks)
                                .createDate(n.getCreateDate())
                                .updateDate(n.getUpdateDate())
                                .build();
                    })
                    .toList();

            List<ChatMcpVO> chatMcpVOs = chatMcpMapper.selectByChatHistoryIdx(h.getIdx());

            List<String> mcpNames = chatMcpVOs.stream()
                    .map(ChatMcpVO::getName)
                    .toList();

            return ResponseChatDTO.History.builder()
                    .idx(h.getIdx())
                    .chatRoomIdx(h.getChatRoomIdx())
                    .message(h.getMessage())
                    .senderType(h.getSenderType())
                    .categoryType(h.getCategoryType())
                    .categoryTypeDesc(
                            h.getCategoryType() == null ? null : EnumCode.getCodeDesc(h.getCategoryType())
                    )
                    .createDate(h.getCreateDate())
                    .updateDate(h.getUpdateDate())
                    .toolResults(toolResults)
                    .chatMcpNames(mcpNames)
                    .chatFiles(chatFiles) // ← 누락 보완
                    .chatNetworks(chatNetworks)
                    .build();
        }).toList();

        int totalCount = chatHistoryMapper.countAllByChatRoomIdx(chatRoomIdx);

        return CustomResponse.builder()
                .data(new PageImpl<>(response, pageable, totalCount))
                .build();
    }

    public List<ChatHistoryVO> getRecentHistories(Integer chatRoomIdx, int count) {
        return chatHistoryMapper.selectRecentByChatRoomIdx(chatRoomIdx, count);
    }

    /** 어시스턴트/유저 메시지를 '동기' 저장하고 PK를 반환 */
    int saveHistoryAndReturnIdx(int chatRoomIdx, String senderTypeCode, String message) {
        ChatHistoryVO h = new ChatHistoryVO();
        h.setChatRoomIdx(chatRoomIdx);
        h.setSenderType(senderTypeCode);
        h.setMessage(message);
        chatHistoryMapper.insertByChatHistoryVO(h);
        return h.getIdx();
    };

    /** 기존 히스토리 본문 교체 (최종 응답으로 덮어쓰기) */
    void updateHistoryContent(int historyIdx, String content, String category) {
        ChatHistoryVO h = new ChatHistoryVO();
        h.setIdx(historyIdx);
        h.setMessage(content);
        h.setCategoryType(category);
        chatHistoryMapper.updateMessageAndCategoryTypeByIdx(h);
    };

    public List<Long> insertChatTool(String payloadJson) throws JsonProcessingException {
        List<Long> insertedIds = new ArrayList<>();
        // Jackson으로 파싱
        JsonNode root = om.readTree(payloadJson);

        JsonNode resultsNode = root.path("results");


        if (resultsNode.isArray()) {
            for (JsonNode item : resultsNode) {
                String title = item.path("title").asText("");
                String url = item.path("url").asText("");
                ChatToolResultVO vo = new ChatToolResultVO();
                vo.setToolType(EnumCode.ChatToolResult.ToolType.QUERY_RESULT.getCode());
                vo.setTitle(title);
                vo.setUrl(url);
                chatToolResultMapper.insertByChatToolResultVO(vo);
                Long newId = vo.getIdx();
                if (newId != null) {
                    insertedIds.add(newId);
                }
            }
        }

        return insertedIds;
    }

    public void insertChatToolByHistoryIdx(String payloadJson, int historyIdx) throws JsonProcessingException {
        // Jackson으로 파싱
        JsonNode root = om.readTree(payloadJson);

        JsonNode resultsNode = root.path("results");

        if (resultsNode.isArray()) {
            for (JsonNode item : resultsNode) {
                String title = item.path("title").asText("");
                String url = item.path("url").asText("");
                ChatToolResultVO vo = new ChatToolResultVO();
                vo.setChatHistoryIdx(historyIdx);
                vo.setToolType(EnumCode.ChatToolResult.ToolType.QUERY_RESULT.getCode());
                vo.setTitle(title);
                vo.setUrl(url);
                chatToolResultMapper.insertByChatToolResultVO(vo);
            }
        }
    }

    public Long insertChatMCP(JsonNode mcpNode) {
        ChatToolResultVO vo = new ChatToolResultVO();
        vo.setToolType(EnumCode.ChatToolResult.ToolType.MCP.getCode());
        vo.setTitle(mcpNode.get("tool").asText());
        chatToolResultMapper.insertByChatToolResultVO(vo);
        return vo.getIdx();
    }

    int saveHistory(int chatRoomIdx, String senderCode, String categoryCode, String message) {
        ChatHistoryVO h = new ChatHistoryVO();
        h.setChatRoomIdx(chatRoomIdx);
        h.setSenderType(senderCode);
        h.setCategoryType(categoryCode);
        h.setMessage(message);
        chatHistoryMapper.insertByChatHistoryVO(h);
        return h.getIdx();
    };

    public ChatHistoryVO getChatHistory(MemberVO memberVO, Integer pmIdx, Integer chatHistoryIdx) throws CustomException {
        var pm = partnershipMemberMapper.selectByIdx(pmIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        if (!Objects.equals(memberVO.getIdx(), pm.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        ChatHistoryVO history = chatHistoryMapper.selectByIdx(chatHistoryIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

        chatRoomMapper.selectByPartnershipMemberIdxAndChatRoomIdx(pmIdx, history.getChatRoomIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

        return history;
    }

    public void saveGraph(ChatHistoryVO history, String graphResponse) throws JsonProcessingException {
        ChatRoomVO room = chatRoomMapper.selectByIdx(history.getChatRoomIdx())
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        String payload = normalizeGraphJson(graphResponse, om);
        ResponseAIDTO.Graph graph = om.readValue(payload, ResponseAIDTO.Graph.class);
        ChatNetworkVO networkVO = new ChatNetworkVO();
        networkVO.setChatHistoryIdx(history.getIdx());
        networkVO.setTitle(room.getTitle());

        chatNetworkMapper.insertByChatNetworkVO(networkVO);

        List<ChatNodeVO> nodes = new ArrayList<>();
        List<ChatLinkVO> links = new ArrayList<>();
        if (graph.getGraphData().getNodes() !=  null) {
            for (ResponseAIDTO.Graph.GraphNode n : graph.getGraphData().getNodes()) {
                ChatNodeVO nodeVO = new ChatNodeVO();
                nodeVO.setChatNetworkIdx(networkVO.getIdx());
                nodeVO.setId(n.getId());
                nodeVO.setProperties(toJson(n.getProperties()));
                nodeVO.setLabels(n.getLabels());
                nodes.add(nodeVO);
            }
        }

        if (graph.getGraphData().getRelationships() != null) {
            for (ResponseAIDTO.Graph.GraphRelationship r : graph.getGraphData().getRelationships()) {
                ChatLinkVO linkVO = new ChatLinkVO();
                linkVO.setChatNetworkIdx(networkVO.getIdx());
                linkVO.setType(r.getType());
                linkVO.setStart(r.getStart());
                linkVO.setEnd(r.getEnd());
                linkVO.setProperties(toJson(r.getProperties()));
                links.add(linkVO);
            }
        }

        chatNodeMapper.insertBulkNode(nodes);
        chatLinkMapper.insertBulkLink(links);
    }

    private String toJson(Object o) throws JsonProcessingException {
        return om.writeValueAsString(o == null ? new Object() : o);
    }

    public String normalizeGraphJson(String raw, ObjectMapper om) throws JsonProcessingException {
        if (raw == null) throw new IllegalArgumentException("graphResponse is null");
        String s = raw.strip();

        // BOM 제거
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') s = s.substring(1);

        // 서버가 전체를 "문자열"로 감싸 보낸 경우: 1~2회까지 언쿼트 시도
        for (int i = 0; i < 2; i++) {
            if (s.startsWith("\"") && s.endsWith("\"")) {
                s = om.readValue(s, String.class); // 언이스케이프
                s = s.strip();
            } else {
                break;
            }
        }
        // 최종적으로 객체/배열 형태여야 함
        if (!(s.startsWith("{") || s.startsWith("["))) {
            throw new IllegalStateException("Invalid JSON payload after normalize: " +
                    (s.length() > 200 ? s.substring(0, 200) + "..." : s));
        }
        return s;
    }

    private List<String> parseLabels(String json) {
        try { return json == null ? List.of() : om.readValue(json, new TypeReference<List<String>>(){}); }
        catch (Exception e) { return List.of(); }
    }
    private Map<String,Object> parseProps(String json) {
        try { return json == null ? Map.of() : om.readValue(json, new TypeReference<Map<String,Object>>(){}); }
        catch (Exception e) { return Map.of(); }
    }

    public RequestProjectDTO.Project convertExcelProject(String s3Url) {
        final String graphUrl = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                .path("/v2/api/convert-excel-graph").toUriString();
        // 1) 전체 응답(Map) 받기
        Map<String, Object> respMap = webClient.post().uri(graphUrl)
                .bodyValue(Map.of("url", s3Url))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (respMap == null) throw new IllegalStateException("Null response from convert-excel-graph API");

        // (옵션) status 확인
        Object status = respMap.get("status");

        // 2) results 래퍼 꺼내기
        Object resultsObj = respMap.get("results");
        if (resultsObj == null) {
            throw new IllegalStateException("No 'results' field in response");
        }

        // 2-1) JsonNode로 구조/키 미리 확인(디버깅 편함)
        com.fasterxml.jackson.databind.JsonNode resultsNode = om.valueToTree(resultsObj);

        return om.convertValue(resultsObj, RequestProjectDTO.Project.class);
    }

    public CustomResponse<?> deleteChatRoom(MemberVO memberVO, Integer chatRoomIdx) {
        ChatRoomVO room = chatRoomMapper.selectByIdx(chatRoomIdx)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        PartnershipMemberVO pm = partnershipMemberMapper.selectByIdx(room.getPartnershipMemberIdx())
                .orElseThrow(() -> new IllegalArgumentException("Partnership member not found"));
        if (!memberVO.getIdx().equals(pm.getMemberIdx())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        chatRoomMapper.softDeleteByIdx(chatRoomIdx);
        return CustomResponse.builder().build();
    }
}
