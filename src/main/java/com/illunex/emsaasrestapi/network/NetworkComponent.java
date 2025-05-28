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
    public void networkSearch(ResponseNetworkDTO.SearchNetwork response, List<Node> nodes){
        if(nodes.isEmpty()) return;
        StopWatch stopWatch = new StopWatch();

        //1. 해당 노드들의 엣지 검색
        stopWatch.start("노드들의 엣지조회");
        Map<String, List<Node>> typeNodeList = nodes.stream().collect(Collectors.groupingBy(node -> (String) node.getLabel()));
        List<Criteria> criteriaList = typeNodeList.entrySet().stream()
                .flatMap(entry -> Stream.of(
                        Criteria.where("startType").is(entry.getKey()).and("start").in(entry.getValue().stream().map(Node::getId).toList()),
                        Criteria.where("endType").is(entry.getKey()).and("end").in(entry.getValue().stream().map(Node::getId).toList())
                ))
                .toList();
        Criteria combinedCriteria = new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
        Query combinedQuery = Query.query(combinedCriteria);
        List<Edge> edgeList = mongoTemplate.find(combinedQuery, Edge.class);
        stopWatch.stop();

        //2. 조회된 엣지 response 용으로 변경
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

        //3. 중복제거(override 해놓음) 및 response에 담기(현재 1뎁스만 하기때문에 기존 엣지 가져와서 비교 안해도 됨)
        List<ResponseNetworkDTO.EdgeInfo> mutableLinkList = new ArrayList<>(response.getLinks());
        mutableLinkList.addAll(edgeInfoList);
        mutableLinkList = mutableLinkList.stream().distinct().toList();
        response.setLinks(mutableLinkList);


        //4. 해당 엣지들로 노드 검색
        stopWatch.start("엣지들의 노드조회");
        if(edgeInfoList.isEmpty()) return;

        Map<String, List<Object>> typeEdgeInfoList = new HashMap<>();
        for(ResponseNetworkDTO.EdgeInfo target : edgeInfoList){
            typeEdgeInfoList.computeIfAbsent((String) target.getStartType(), k -> new ArrayList<>()).add(target.getStart());
            typeEdgeInfoList.computeIfAbsent((String) target.getEndType(), k -> new ArrayList<>()).add(target.getEnd());
        }
        List<Criteria> criteriaList2 = typeEdgeInfoList.entrySet().stream()
                .map(entry -> Criteria.where("label").is(entry.getKey()).and("id").in(entry.getValue()))
                .toList();
        Criteria combinedCriteria2 = new Criteria().orOperator(criteriaList2.toArray(new Criteria[0]));
        Query query = Query.query(combinedCriteria2);
        List<Node> nodeList = mongoTemplate.find(query, Node.class);
        stopWatch.stop();

        //5. 조회된 노드 response 용으로 변경
        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodeList.stream().map(target ->
                ResponseNetworkDTO.NodeInfo.builder()
                        .nodeId(target.getNodeId())
                        .label(target.getLabel())
                        .properties(target.getProperties())
                        .build()
        ).toList();


        //6. 중복제거(override 해놓음) 및 response에 담기
        List<ResponseNetworkDTO.NodeInfo> mutableNodeList = new ArrayList<>(response.getNodes());
        mutableNodeList.addAll(nodeInfoList);
        mutableNodeList = mutableNodeList.stream().distinct().toList();
        response.setNodes(mutableNodeList);

        log.info("쿼리별 실행 시간:\n{}", stopWatch.prettyPrint());
    }
}
