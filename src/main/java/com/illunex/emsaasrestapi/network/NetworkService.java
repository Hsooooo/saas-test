package com.illunex.emsaasrestapi.network;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.project.ProjectComponent;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectNodeContent;
import com.illunex.emsaasrestapi.project.document.project.ProjectNodeContentCell;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        ResponseProjectDTO.ProjectNetwork response = new ResponseProjectDTO.ProjectNetwork();

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
     * 프로젝트 단일관계망 조회
     * @param projectIdx
     * @return
     */
    public CustomResponse<?> getNetworkOne(Integer projectIdx, Integer nodeIdx) {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        ResponseProjectDTO.ProjectNetwork response = new ResponseProjectDTO.ProjectNetwork();

        //노드검색
        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                                            .and("id").is(nodeIdx));
        List<Node> nodes = mongoTemplate.find(query, Node.class);

        response.setNodes(nodes);

        //엣지검색
        projectComponent.extendRepeatNetworkSearch(response, nodes, 1);

        if(response.getNodes() != null) response.setNodeSize(response.getNodes().size());
        if(response.getLinks() != null) response.setLinkSize(response.getLinks().size());


        return CustomResponse.builder()
                .data(response)
                .build();
    }


    /**
     * 프로젝트 단일노드 상세정보 조회
     * @param projectIdx
     * @param nodeIdx
     * @param label
     * @return
     */
    public CustomResponse<?> getNetworkInfo(Integer projectIdx, Integer nodeIdx, String label) {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        List<ResponseProjectDTO.ProjectNetworkInfo> data = new ArrayList<>();

        //1. 노드 검색
        Query query1 = Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                .and("id").is(nodeIdx)
                .and("label").is(label));
        Node node = mongoTemplate.findOne(query1, Node.class);

        //2. 사용자가 세팅한 설정 검색
        Query query2 = Query.query(Criteria.where("_id").is(projectIdx));
        Project project = mongoTemplate.findOne(query2, Project.class);

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
                    data.add(ResponseProjectDTO.ProjectNetworkInfo.builder()
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
}
