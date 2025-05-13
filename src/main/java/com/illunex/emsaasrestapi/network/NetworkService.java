package com.illunex.emsaasrestapi.network;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.network.dto.RequestNetworkDTO;
import com.illunex.emsaasrestapi.network.dto.ResponseNetworkDTO;
import com.illunex.emsaasrestapi.project.ProjectComponent;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectNodeContent;
import com.illunex.emsaasrestapi.project.document.project.ProjectNodeContentCell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class NetworkService {
    private final MongoTemplate mongoTemplate;
    private final ProjectComponent projectComponent;

    /**
     * 전체 관계망 조회
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getNetworkAll(Integer projectIdx) {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크

        ResponseNetworkDTO.Network response = new ResponseNetworkDTO.Network();

        //TODO [PYJ] 노드검색
        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectIdx));
        List<Node> nodes = mongoTemplate.find(query, Node.class);

        response.setNodes(nodes);

        //TODO [PYJ] 엣지검색
        projectComponent.extendRepeatNetworkSearch(response, nodes, 1);

        if(response.getNodes() != null) response.setNodeSize(response.getNodes().size());
        if(response.getLinks() != null) response.setLinkSize(response.getLinks().size());

        return CustomResponse.builder()
                .data(response)
                .build();
    }


    /**
     * 단일 노드 확장 조회
     * @param extend
     * @return
     */
    public CustomResponse<?> getNetworkSingleExtend(RequestNetworkDTO.Extend extend) {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        ResponseNetworkDTO.Network response = new ResponseNetworkDTO.Network();

        //노드검색
        Query query = Query.query(Criteria.where("_id.projectIdx").is(extend.getProjectIdx())
                        .and("_id.nodeIdx").is(extend.getNodeIdx())
                        .and("label").is(extend.getLabel()));
        List<Node> nodes = mongoTemplate.find(query, Node.class);

        response.getNodes().addAll(nodes);

        //엣지검색
        projectComponent.extendRepeatNetworkSearch(response, nodes, 1);

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 노드 상세정보 조회
     * @param extend
     * @return
     */
    public CustomResponse<?> getNetworkInfo(RequestNetworkDTO.Extend extend) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        List<ResponseNetworkDTO.NodeDetailInfo> data = new ArrayList<>();

        //1. 노드 검색
        Query query1 = Query.query(Criteria.where("_id.projectIdx").is(extend.getProjectIdx())
                .and("_id.nodeIdx").is(extend.getNodeIdx())
                .and("label").is(extend.getLabel()));
        Node node = mongoTemplate.findOne(query1, Node.class);

        // 노드 정보가 없을 경우 예외처리
        if(node == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        //2. 사용자가 세팅한 설정 검색
        Query query2 = Query.query(Criteria.where("_id").is(extend.getProjectIdx()));
        Project project = mongoTemplate.findOne(query2, Project.class);

        // 프로젝트 정보가 없을 경우 예외처리
        if(project == null) {
            throw new CustomException(ErrorCode.PROJECT_EMPTY_DATA);
        }

        List <ProjectNodeContent> nodeContentList = project.getProjectNodeContentList();
        ProjectNodeContent findNodeInfo = null;

        for(ProjectNodeContent nodeContent : nodeContentList) {
            if(nodeContent.getNodeType().equals(node.getLabel())){
                findNodeInfo = nodeContent;
            }
        }

        //세팅한 정보만 추려내기
        List<ProjectNodeContentCell> cellList = findNodeInfo.getProjectNodeContentCellList();
        for(ProjectNodeContentCell cell : cellList) {
            node.getProperties().forEach((key, value) -> {
                if(cell.getCellName().equals(key)){
                    data.add(ResponseNetworkDTO.NodeDetailInfo.builder()
                            .label(cell.getLabel())
                            .cellName(key)
                            .cellType(cell.getCellType())
                            .value(value)
                            .build());
                }
            });
        }

        return CustomResponse.builder()
                .data(data)
                .build();
    }


    /**
     * 검색한 다중노드 관계망 조회
     * @param search
     * @return
     */
    public CustomResponse<?> getNetworkSearch(RequestNetworkDTO.Search search) {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        ResponseNetworkDTO.SearchNetwork response = new ResponseNetworkDTO.SearchNetwork();

        //검색할 컬럼명 조회
        Query query = Query.query(Criteria.where("_id").is(search.getProjectIdx()));
        Project project = mongoTemplate.findOne(query, Project.class);

        //노드검색
        String labelTitleCellName = project.getProjectNodeContentList().get(0).getLabelTitleCellName();
        String nodeType = project.getProjectNodeContentList().get(0).getNodeType();
        Query query2 = Query.query(
                new Criteria().andOperator(
                        Criteria.where("properties." + labelTitleCellName).regex(".*" + search.getKeyword() + ".*"),
                        Criteria.where("label").is(nodeType)
                ));
        List<Node> nodes = mongoTemplate.find(query2, Node.class);
        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodes.stream().map(target ->
                        ResponseNetworkDTO.NodeInfo.builder()
                                .nodeId(target.getNodeId())
                                .properties(target.getProperties())
                                .build()
                        ).toList();

        response.getNodes().addAll(nodeInfoList);

        //엣지검색
        networkSearch(response, nodes);

        //노드, 엣지 개수 세팅
        response.setNodeSize(response.getNodes().size());
        response.setLinkSize(response.getLinks().size());

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 1뎁스로 노드의 관계망 검색
     * @param response
     * @param nodes
     */
    public void networkSearch(ResponseNetworkDTO.SearchNetwork response, List<Node> nodes){
        if(nodes.isEmpty()) return;

        //1. 해당 노드들의 엣지 검색
        List<Criteria> criteriaList = nodes.stream()
                .map(node -> new Criteria().orOperator(
                        Criteria.where("start").is(node.getId()).and("startType").is(node.getLabel()),
                        Criteria.where("end").is(node.getId()).and("endType").is(node.getLabel())
                ))
                .toList();
        Criteria combinedCriteria = new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
        Query combinedQuery = Query.query(combinedCriteria);
        List<Edge> edgeList = mongoTemplate.find(combinedQuery, Edge.class);

        //2. 조회된 엣지 response 용으로 변경
        List<ResponseNetworkDTO.EdgeInfo> edgeInfoList = edgeList.stream().map(target ->
                ResponseNetworkDTO.EdgeInfo.builder()
                        .edgeId(target.getEdgeId())
                        .startType(target.getStartType())
                        .start(target.getStart())
                        .endType(target.getEndType())
                        .end(target.getEnd())
                        .build()
        ).toList();

        //3. 중복제거(override 해놓음) 및 response에 담기
        edgeInfoList = edgeInfoList.stream().distinct().toList();
        response.getLinks().addAll(edgeInfoList);

        //4. 해당 엣지들로 노드 검색
        List<Criteria> criteriaList2 = edgeInfoList.stream()
                .flatMap(edge -> Stream.of(
                        Criteria.where("type").is(edge.getStartType()).and("id").is(edge.getStart()),
                        Criteria.where("type").is(edge.getEndType()).and("id").is(edge.getEnd())
                ))
                .toList();
        Criteria combinedCriteria2 = new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
        Query query = Query.query(combinedCriteria2);
        List<Node> nodeList = mongoTemplate.find(query, Node.class);


        //5. 조회된 노드 response 용으로 변경
        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodeList.stream().map(target ->
                ResponseNetworkDTO.NodeInfo.builder()
                        .nodeId(target.getNodeId())
                        .properties(target.getProperties())
                        .build()
        ).toList();


        //6. 중복제거(override 해놓음) 및 response에 담기
        nodeInfoList = nodeInfoList.stream().distinct().toList();
        response.getNodes().addAll(nodeInfoList);
    }

}
