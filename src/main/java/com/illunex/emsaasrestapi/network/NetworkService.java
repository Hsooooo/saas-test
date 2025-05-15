package com.illunex.emsaasrestapi.network;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.network.dto.RequestNetworkDTO;
import com.illunex.emsaasrestapi.network.dto.ResponseNetworkDTO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.ProjectComponent;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.EdgeId;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectNodeContent;
import com.illunex.emsaasrestapi.project.document.project.ProjectNodeContentCell;
import com.mongodb.client.MongoCursor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class NetworkService {
    private final MongoTemplate mongoTemplate;
    private final PartnershipComponent partnershipComponent;
    private final ProjectComponent projectComponent;

    /**
     * 전체 관계망 조회
     * @param memberVO
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getNetworkAll(MemberVO memberVO, Integer projectIdx) throws CustomException {
        // 파트너쉽 회원 여부 체크
//        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
//        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        ResponseNetworkDTO.SearchNetwork response = new ResponseNetworkDTO.SearchNetwork();

        StopWatch stopWatch = new StopWatch();

        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectIdx));
        List<Node> nodes = mongoTemplate.find(query, Node.class);
        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodes.stream().map(target ->
                ResponseNetworkDTO.NodeInfo.builder()
                        .nodeId(target.getNodeId())
                        .label(target.getLabel())
                        .properties(target.getProperties())
                        .build()
        ).toList();

        response.setNodes(nodeInfoList);


        //관계망 검색
        networkSearch(response, nodes);

        if(response.getNodes() != null) response.setNodeSize(response.getNodes().size());
        if(response.getLinks() != null) response.setLinkSize(response.getLinks().size());

        return CustomResponse.builder()
                .data(response)
                .build();
    }


    /**
     * 단일 노드 확장 조회
     * @param memberVO
     * @param selectNode
     * @return
     */
    public CustomResponse<?> getNetworkSingleExtend(MemberVO memberVO, RequestNetworkDTO.SelectNode selectNode) throws CustomException {
        // 파트너쉽 회원 여부 체크
//        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, selectNode.getProjectIdx());
        // 프로젝트 구성원 여부 체크
//        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        ResponseNetworkDTO.SearchNetwork response = new ResponseNetworkDTO.SearchNetwork();

        //노드검색
        Query query = Query.query(Criteria.where("_id.projectIdx").is(selectNode.getProjectIdx())
                        .and("_id.nodeIdx").is(selectNode.getNodeIdx())
                        .and("label").is(selectNode.getLabel()));
        List<Node> nodes = mongoTemplate.find(query, Node.class);
        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodes.stream().map(target ->
                ResponseNetworkDTO.NodeInfo.builder()
                        .nodeId(target.getNodeId())
                        .label(target.getLabel())
                        .properties(target.getProperties())
                        .build()
        ).toList();

        response.setNodes(nodeInfoList);

        //관계망 검색
        networkSearch(response, nodes);

        if(response.getNodes() != null) response.setNodeSize(response.getNodes().size());
        if(response.getLinks() != null) response.setLinkSize(response.getLinks().size());

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 노드 상세정보 조회
     * @param memberVO
     * @param selectNode
     * @return
     */
    public CustomResponse<?> getNetworkInfo(MemberVO memberVO, RequestNetworkDTO.SelectNode selectNode) throws CustomException {
        // 파트너쉽 회원 여부 체크
//        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, selectNode.getProjectIdx());
        // 프로젝트 구성원 여부 체크
//        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        List<ResponseNetworkDTO.NodeDetailInfo> data = new ArrayList<>();

        //1. 노드 검색
        Query query1 = Query.query(Criteria.where("_id.projectIdx").is(selectNode.getProjectIdx())
                .and("_id.nodeIdx").is(selectNode.getNodeIdx())
                .and("label").is(selectNode.getLabel()));
        Node node = mongoTemplate.findOne(query1, Node.class);

        // 노드 정보가 없을 경우 예외처리
        if(node == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        //2. 사용자가 세팅한 설정 검색
        Query query2 = Query.query(Criteria.where("_id").is(selectNode.getProjectIdx()));
        Project project = mongoTemplate.findOne(query2, Project.class);

        // 프로젝트 정보가 없을 경우 예외처리
        if(project == null) {
            throw new CustomException(ErrorCode.PROJECT_EMPTY_DATA);
        }

        List <ProjectNodeContent> nodeContentList = project.getProjectNodeContentList();
        ProjectNodeContent findNodeInfo = null;

        // 프로젝트 필드 정보가 없을 경우 예외처리
        if(nodeContentList == null) {
            throw new CustomException(ErrorCode.PROJECT_CONTENT_EMPTY_DATA);
        }

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
     * 관계망 조회 API
     * @param search
     * @return
     */
    public CustomResponse<?> getNetworkSearch(RequestNetworkDTO.Search search) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        ResponseNetworkDTO.SearchNetwork response = new ResponseNetworkDTO.SearchNetwork();

        //검색할 컬럼명 조회
        Query query = Query.query(Criteria.where("_id").is(search.getProjectIdx()));
        Project project = mongoTemplate.findOne(query, Project.class);

        // 프로젝트 정보가 없을 경우 예외처리
        if(project == null) {
            throw new CustomException(ErrorCode.PROJECT_EMPTY_DATA);
        }

        //노드검색
        List<ProjectNodeContent> projectNodeContentList = project.getProjectNodeContentList();
        if(projectNodeContentList == null) {
            throw new CustomException(ErrorCode.PROJECT_CONTENT_EMPTY_DATA);
        }
        List<Criteria> criteriaList = projectNodeContentList.stream().map(
                target -> new Criteria().andOperator(
                        Criteria.where("properties." + target.getLabelTitleCellName()).regex(".*" + search.getKeyword() + ".*"),
                        Criteria.where("label").is(target.getNodeType())
                )
        ).toList();
        Criteria combinedCriteria = new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
        Query combinedQuery = Query.query(combinedCriteria);
        List<Node> nodes = mongoTemplate.find(combinedQuery, Node.class);

        List<ResponseNetworkDTO.NodeInfo> nodeInfoList = nodes.stream().map(target ->
                        ResponseNetworkDTO.NodeInfo.builder()
                                .nodeId(target.getNodeId())
                                .label(target.getLabel())
                                .properties(target.getProperties())
                                .build()
                        ).toList();

        response.setNodes(nodeInfoList);

        //관계망 검색
        networkSearch(response, nodes);

        //node 정보 사용자가 지정한 정보만 보이도록 필터링
        Map<String, ProjectNodeContent> projectNodeContentMap = project.getProjectNodeContentList().stream()
                .collect(Collectors.toMap(
                        ProjectNodeContent::getNodeType,
                        nodeContent -> nodeContent
                ));

        nodeInfoList = response.getNodes();
        for(ResponseNetworkDTO.NodeInfo nodeInfo : nodeInfoList){
            ProjectNodeContent currentOption = projectNodeContentMap.get(nodeInfo.getLabel());
            List<String> cellList = currentOption.getProjectNodeContentCellList().stream()
                    .map(ProjectNodeContentCell::getCellName).toList();
            List<ResponseNetworkDTO.NodeDetailInfo> props = new ArrayList<>();
            for(ProjectNodeContentCell cell : currentOption.getProjectNodeContentCellList()){
                ResponseNetworkDTO.NodeDetailInfo nodeDetailInfo = ResponseNetworkDTO.NodeDetailInfo.builder()
                        .label((String) cell.getLabel())
                        .cellName(cell.getCellName())
                        .cellType(cell.getCellType())
                        .value(nodeInfo.getProperties().get(cell.getCellName()))
                        .build();
                props.add(nodeDetailInfo);
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("data", props);
            nodeInfo.setProperties(map);
            nodeInfo.setDelimiter(currentOption.getKeywordSplitUnit());
        }
        response.setNodes(nodeInfoList);


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
