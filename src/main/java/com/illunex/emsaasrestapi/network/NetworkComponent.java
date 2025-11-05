package com.illunex.emsaasrestapi.network;


import com.illunex.emsaasrestapi.network.dto.ResponseNetworkDTO;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.project.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class NetworkComponent {

    private final MongoTemplate mongoTemplate;

    /**
     * 전체 관계망 조회
     * @param projectIdx
     * @param limit
     * @return
     */
    public ResponseNetworkDTO.SearchNetwork networkSearchAll(Integer projectIdx, Integer limit) {
        long startAll = System.currentTimeMillis();
        log.info("전체 관계망 조회 시작 - projectIdx: {}, limit: {}", projectIdx, limit);

        ResponseNetworkDTO.SearchNetwork response = new ResponseNetworkDTO.SearchNetwork();

        // 프로젝트 조회
        long t1 = System.currentTimeMillis();
        Project projectDoc = mongoTemplate.findById(projectIdx, Project.class);
        long t2 = System.currentTimeMillis();
        log.info("프로젝트 문서 조회 완료 - 소요시간: {}ms", (t2 - t1));

        if (projectDoc == null) return response;

        limit = projectDoc.getMaxNodeSize() != null ? projectDoc.getMaxNodeSize() : limit;

        // 노드 조회
        t1 = System.currentTimeMillis();
        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectIdx)).limit(limit);
        List<Node> nodes = mongoTemplate.find(query, Node.class);
        t2 = System.currentTimeMillis();
        log.info("노드 조회 완료 - 소요시간: {}ms, 노드개수: {}", (t2 - t1), nodes.size());

        if (nodes.isEmpty()) return response;

        String mainLabel = projectDoc.getProjectNodeContentList().get(0).getLabelContentCellName();

        // 엣지 조건 생성
        t1 = System.currentTimeMillis();
        Map<String, List<Object>> typeToIds = nodes.stream()
                .collect(Collectors.groupingBy(
                        n -> (String) n.getLabel(),
                        Collectors.mapping(Node::getId, Collectors.toList())
                ));
        List<Criteria> orEdge = typeToIds.entrySet().stream()
                .flatMap(e -> Stream.of(
                        new Criteria().andOperator(
                                Criteria.where("startType").is(e.getKey()),
                                Criteria.where("start").in(e.getValue())
                        ),
                        new Criteria().andOperator(
                                Criteria.where("endType").is(e.getKey()),
                                Criteria.where("end").in(e.getValue())
                        )
                ))
                .toList();
        t2 = System.currentTimeMillis();
        log.info("엣지 조건 생성 완료 - 소요시간: {}ms, 조건개수: {}", (t2 - t1), orEdge.size());

        if (orEdge.isEmpty()) return response;

        // 엣지 조회
        t1 = System.currentTimeMillis();
        Criteria project = Criteria.where("_id.projectIdx").is(projectIdx);
        Query edgeQuery = new Query(project).addCriteria(new Criteria().orOperator(orEdge.toArray(new Criteria[0])));
        List<Edge> edgeList = mongoTemplate.find(edgeQuery, Edge.class);
        t2 = System.currentTimeMillis();
        log.info("엣지 조회 완료 - 소요시간: {}ms, 엣지개수: {}", (t2 - t1), edgeList.size());

        // DTO 변환 및 중복 제거
        t1 = System.currentTimeMillis();
        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodes.stream().map(target ->
                ResponseNetworkDTO.NodeInfo.builder()
                        .nodeId(target.getNodeId())
                        .label(target.getLabel())
                        .properties(target.getProperties())
                        .build()
        ).toList();
        List<ResponseNetworkDTO.EdgeInfo> edgeInfoList = edgeList.stream().map(t ->
                ResponseNetworkDTO.EdgeInfo.builder()
                        .edgeId(t.getEdgeId())
                        .startType(t.getStartType())
                        .start(t.getStart())
                        .endType(t.getEndType())
                        .end(t.getEnd())
                        .properties(t.getProperties())
                        .build()
        ).toList();
        t2 = System.currentTimeMillis();
        log.info("DTO 변환 완료 - 소요시간: {}ms", (t2 - t1));

        // 정렬 및 최종 세팅
        t1 = System.currentTimeMillis();
        nodeInfoList = nodeInfoList.stream()
                .distinct()
                .sorted(Comparator.comparing(
                        n -> {
                            if (n.getProperties() == null) return null;
                            Object v = n.getProperties().get(mainLabel);
                            if (v == null) return null;
                            String s = v.toString().trim();
                            return s.isEmpty() ? null : s;
                        },
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();
        t2 = System.currentTimeMillis();
        log.info("노드 정렬 및 중복제거 완료 - 소요시간: {}ms", (t2 - t1));

        response.setNodes(nodeInfoList);
        response.setLinks(edgeInfoList.stream().distinct().toList());

        long endAll = System.currentTimeMillis();
        log.info("전체 관계망 조회 완료 - 총 소요시간: {}ms, 노드: {}, 엣지: {}",
                (endAll - startAll),
                nodeInfoList.size(),
                edgeInfoList.size()
        );

        return response;
    }

    /**
     * 1뎁스로 노드의 관계망 검색
     * @param response
     * @param nodes
     */
    public void networkSearch(ResponseNetworkDTO.SearchNetwork response,
                              List<Node> nodes,
                              Integer projectIdx) {
        if (nodes.isEmpty()) return;
        StopWatch stopWatch = new StopWatch();
        Project projectDoc = mongoTemplate.findById(projectIdx, Project.class);
        if (projectDoc == null) return;
        String mainLabel = projectDoc.getProjectNodeContentList().get(0).getLabelContentCellName();

        // 1) 노드들의 엣지 조회 (projectIdx 필수)
        stopWatch.start("노드들의 엣지조회");

        Map<String, List<Object>> typeToIds = nodes.stream()
                .collect(Collectors.groupingBy(
                        n -> (String) n.getLabel(),
                        Collectors.mapping(Node::getId, Collectors.toList())
                ));

        // (startType=label AND start in ids) OR (endType=label AND end in ids)
        List<Criteria> orEdge = typeToIds.entrySet().stream()
                .flatMap(e -> Stream.of(
                        Criteria.where("startType").is(e.getKey()).and("start").in(e.getValue()),
                        Criteria.where("endType").is(e.getKey()).and("end").in(e.getValue())
                ))
                .toList();

        if (orEdge.isEmpty()) {
            stopWatch.stop();
            log.info("쿼리별 실행 시간:\n{}", stopWatch.prettyPrint());
            return;
        }

        // AND projectIdx
        Criteria project = Criteria.where("_id.projectIdx").is(projectIdx);
        Query edgeQuery = new Query(project).addCriteria(new Criteria().orOperator(orEdge.toArray(new Criteria[0])));

        List<Edge> edgeList = mongoTemplate.find(edgeQuery, Edge.class);
        stopWatch.stop();

        // 2) 엣지 -> response 변환
        List<ResponseNetworkDTO.EdgeInfo> edgeInfoList = edgeList.stream().map(t ->
                ResponseNetworkDTO.EdgeInfo.builder()
                        .edgeId(t.getEdgeId())
                        .startType(t.getStartType())
                        .start(t.getStart())
                        .endType(t.getEndType())
                        .end(t.getEnd())
                        .properties(t.getProperties())
                        .build()
        ).toList();

        // 3) 링크 중복 제거
        List<ResponseNetworkDTO.EdgeInfo> newLinks = new ArrayList<>(response.getLinks());
        newLinks.addAll(edgeInfoList);
        newLinks = newLinks.stream().distinct().toList();
        response.setLinks(newLinks);

        // 4) 엣지들의 노드 조회 (projectIdx 필수)
        stopWatch.start("엣지들의 노드조회");
        if (edgeInfoList.isEmpty()) {
            stopWatch.stop();
            log.info("쿼리별 실행 시간:\n{}", stopWatch.prettyPrint());
            return;
        }

        Map<String, List<Object>> typeToNodeIds = new HashMap<>();
        for (ResponseNetworkDTO.EdgeInfo e : edgeInfoList) {
            typeToNodeIds.computeIfAbsent((String) e.getStartType(), k -> new ArrayList<>()).add(e.getStart());
            typeToNodeIds.computeIfAbsent((String) e.getEndType(), k -> new ArrayList<>()).add(e.getEnd());
        }

        List<Criteria> orNode = typeToNodeIds.entrySet().stream()
                .map(en -> Criteria.where("label").is(en.getKey()).and("id").in(en.getValue()))
                .toList();

        Query nodeQuery = new Query(project).addCriteria(new Criteria().orOperator(orNode.toArray(new Criteria[0])));
        List<Node> nodeList = mongoTemplate.find(nodeQuery, Node.class);
        stopWatch.stop();

        // 5) 노드 -> response 변환
        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodeList.stream().map(t ->
                ResponseNetworkDTO.NodeInfo.builder()
                        .nodeId(t.getNodeId())
                        .label(t.getLabel())
                        .properties(t.getProperties())
                        .build()
        ).toList();

        // 6) 노드 중복 제거
        List<ResponseNetworkDTO.NodeInfo> newNodes = new ArrayList<>(response.getNodes());
        newNodes.addAll(nodeInfoList);
        // 메인 노드 컨텐츠 기준 정렬 + 1만개 제한
        int maxNodeSize = projectDoc.getMaxNodeSize() != null ? projectDoc.getMaxNodeSize() : 10000;
        newNodes = newNodes.stream()
                .distinct()
                .sorted(Comparator.comparing(
                        n -> {
                            if (n.getProperties() == null) return null;
                            Object v = n.getProperties().get(mainLabel);
                            if (v == null) return null;
                            String s = v.toString().trim();
                            return s.isEmpty() ? null : s; // 공백 문자열도 null 취급
                        },
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .limit(maxNodeSize).toList();
        response.setNodes(newNodes);

        log.info("쿼리별 실행 시간:\n{}", stopWatch.prettyPrint());
    }

    /**
     * 다중 뎁스로 노드의 관계망 검색
     * @param response
     * @param nodes
     * @param depth
     */
    public void networkSearch(ResponseNetworkDTO.SearchNetwork response,
                              List<Node> nodes,
                              int projectIdx,
                              int depth) {
        if (nodes.isEmpty() || depth <= 0) return;

        Map<String, List<Node>> typeNodeList = nodes.stream()
                .collect(Collectors.groupingBy(node -> (String) node.getLabel()));

        List<Criteria> criteriaList = typeNodeList.entrySet().stream()
                .flatMap(entry -> Stream.of(
                        Criteria.where("startType").is(entry.getKey())
                                .and("start").in(entry.getValue().stream().map(Node::getId).toList()),
                        Criteria.where("endType").is(entry.getKey())
                                .and("end").in(entry.getValue().stream().map(Node::getId).toList())
                ))
                .toList();

        Criteria edgeOr = new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
        Criteria projectScope = Criteria.where("_id.projectIdx").is(projectIdx);
        Query edgeQuery = new Query(projectScope).addCriteria(edgeOr);

        List<Edge> edgeList = mongoTemplate.find(edgeQuery, Edge.class);

        // 2. 엣지 → response
        List<ResponseNetworkDTO.EdgeInfo> edgeInfoList = edgeList.stream().map(target ->
                ResponseNetworkDTO.EdgeInfo.builder()
                        .edgeId(target.getEdgeId())
                        .startType(target.getStartType())
                        .start(target.getStart())
                        .endType(target.getEndType())
                        .end(target.getEnd())
                        .properties(target.getProperties())
                        .build()
        ).toList();

        // 3. 중복 제거 후 response에 담기
        List<ResponseNetworkDTO.EdgeInfo> mutableLinkList = new ArrayList<>(response.getLinks());
        mutableLinkList.addAll(edgeInfoList);
        mutableLinkList = mutableLinkList.stream().distinct().toList();
        response.setLinks(mutableLinkList);

        // 4. 해당 엣지들로 노드 검색
        if (mutableLinkList.isEmpty()) return;

        Map<String, List<Object>> typeEdgeInfoList = new HashMap<>();
        for (ResponseNetworkDTO.EdgeInfo target : mutableLinkList) {
            typeEdgeInfoList.computeIfAbsent((String) target.getStartType(), k -> new ArrayList<>()).add(target.getStart());
            typeEdgeInfoList.computeIfAbsent((String) target.getEndType(), k -> new ArrayList<>()).add(target.getEnd());
        }

        List<Criteria> criteriaList2 = typeEdgeInfoList.entrySet().stream()
                .map(entry -> Criteria.where("label").is(entry.getKey()).and("id").in(entry.getValue()))
                .toList();

        Criteria nodeOr = new Criteria().orOperator(criteriaList2.toArray(new Criteria[0]));
        Query nodeQuery = new Query(projectScope).addCriteria(nodeOr);

        List<Node> nodeList = mongoTemplate.find(nodeQuery, Node.class);

        // 5. 노드 → response
        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodeList.stream().map(target ->
                ResponseNetworkDTO.NodeInfo.builder()
                        .nodeId(target.getNodeId())
                        .label(target.getLabel())
                        .properties(target.getProperties())
                        .build()
        ).toList();

        // 6. 중복 제거 후 response에 담기
        List<ResponseNetworkDTO.NodeInfo> mutableNodeList = new ArrayList<>(response.getNodes());
        mutableNodeList.addAll(nodeInfoList);
        mutableNodeList = mutableNodeList.stream().distinct().toList();
        response.setNodes(mutableNodeList);

        // 7. 재귀 호출 (projectIdx 전달 필수)
        networkSearch(response, nodeList, projectIdx, depth - 1);
    }
}
