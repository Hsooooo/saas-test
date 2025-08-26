package com.illunex.emsaasrestapi.network;


import com.illunex.emsaasrestapi.network.dto.ResponseNetworkDTO;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
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
     * 1뎁스로 노드의 관계망 검색
     * @param response
     * @param nodes
     */
    public void networkSearch(ResponseNetworkDTO.SearchNetwork response,
                              List<Node> nodes,
                              Integer projectIdx) {
        if (nodes.isEmpty()) return;
        StopWatch stopWatch = new StopWatch();

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
        newNodes = newNodes.stream().distinct().toList();
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
        StopWatch stopWatch = new StopWatch();

        // 1. 해당 노드들의 엣지 검색
        stopWatch.start("노드들의 엣지조회");

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
        stopWatch.stop();

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
        stopWatch.start("엣지들의 노드조회");
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
        stopWatch.stop();

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

        log.info("쿼리별 실행 시간:\n{}", stopWatch.prettyPrint());

        // 7. 재귀 호출 (projectIdx 전달 필수)
        networkSearch(response, nodeList, projectIdx, depth - 1);
    }
}
