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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NetworkService {
    private final MongoTemplate mongoTemplate;
    private final PartnershipComponent partnershipComponent;
    private final ProjectComponent projectComponent;
    private final NetworkComponent networkComponent;


    /**
     * 전체 관계망 조회
     * @param memberVO
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getNetworkAll(MemberVO memberVO, Integer projectIdx) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(projectIdx, partnershipMemberVO.getIdx());

        ResponseNetworkDTO.SearchNetwork response = new ResponseNetworkDTO.SearchNetwork();

        // 모든 노드 조회
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
        networkComponent.networkSearch(response, nodes);

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
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, selectNode.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(selectNode.getProjectIdx(), partnershipMemberVO.getIdx());

        ResponseNetworkDTO.SearchNetwork response = new ResponseNetworkDTO.SearchNetwork();

        // 노드검색
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

        // 관계망 검색
        networkComponent.networkSearch(response, nodes);

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
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, selectNode.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(selectNode.getProjectIdx(), partnershipMemberVO.getIdx());

        List<ResponseNetworkDTO.NodeDetailInfo> data = new ArrayList<>();

        // 1. 노드 검색
        Query query1 = Query.query(Criteria.where("_id.projectIdx").is(selectNode.getProjectIdx())
                .and("_id.nodeIdx").is(selectNode.getNodeIdx())
                .and("label").is(selectNode.getLabel()));
        Node node = mongoTemplate.findOne(query1, Node.class);

        // 노드 정보가 없을 경우 예외처리
        if(node == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        // 2. 사용자가 세팅한 설정 검색
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

        // 세팅한 정보만 추려내기
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
    public CustomResponse<?> getNetworkSearch(MemberVO memberVO, RequestNetworkDTO.Search search) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, search.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(search.getProjectIdx(), partnershipMemberVO.getIdx());

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
        networkComponent.networkSearch(response, nodes);

        //노드, 엣지 개수 세팅
        response.setNodeSize(response.getNodes().size());
        response.setLinkSize(response.getLinks().size());

        return CustomResponse.builder()
                .data(response)
                .build();
    }



    /**
     * 자동완성 조회
     * @param memberVO
     * @param autoCompleteSearch
     */
    public CustomResponse<?> getAutoComplete(MemberVO memberVO,
                                             RequestNetworkDTO.AutoCompleteSearch autoCompleteSearch) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMemberAndProject(memberVO, autoCompleteSearch.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(autoCompleteSearch.getProjectIdx(), partnershipMemberVO.getIdx());

        // 프로젝트 조회
        Project project = mongoTemplate.findOne(new Query(Criteria.where("_id").is(autoCompleteSearch.getProjectIdx())), Project.class);
        if (project == null || project.getProjectNodeContentList().isEmpty()) {
            throw new CustomException(ErrorCode.PROJECT_EMPTY_DATA);
        }

        List<ProjectNodeContent> projectNodeContentList = project.getProjectNodeContentList();

        // 타겟 노드 타입 필터링
        List<ProjectNodeContent> filteredNodeContents = projectNodeContentList.stream()
                .filter(content -> autoCompleteSearch.getNodeType().isEmpty() || autoCompleteSearch.getNodeType().contains(content.getNodeType()))
                .toList();

        if (filteredNodeContents.isEmpty()) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        Map<String, List<ResponseNetworkDTO.AutoComplete>> response = new HashMap<>();

        // 노드 데이터 조회
        for (ProjectNodeContent content : filteredNodeContents) {
            String nodeType = content.getNodeType();
            String labelTitleCellName = content.getLabelTitleCellName();

            Criteria criteria = Criteria.where("_id.projectIdx").is(autoCompleteSearch.getProjectIdx())
                    .and("_id.type").is(nodeType)
                    .and("properties." + labelTitleCellName)
                    .regex(".*" + autoCompleteSearch.getSearchKeyword() + ".*", "i");

            List<Node> nodes = mongoTemplate.find(new Query(criteria).limit(autoCompleteSearch.getLimit()), Node.class, "node");

            List<ResponseNetworkDTO.AutoComplete> result = new ArrayList<>();

            for (Node node : nodes) {
                ResponseNetworkDTO.AutoComplete autoComplete = ResponseNetworkDTO.AutoComplete.builder()
                        .nodeId(node.getNodeId())
                        .nodeLabelTitle(node.getProperties().get(labelTitleCellName).toString())
                        .build();

                result.add(autoComplete);
            }

            response.put(nodeType, result);
        }

        return CustomResponse.builder().data(response).build();
    }
}
