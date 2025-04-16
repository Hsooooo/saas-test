package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.project.document.Edge;
import com.illunex.emsaasrestapi.project.document.Node;
import com.illunex.emsaasrestapi.project.document.Project;
import com.illunex.emsaasrestapi.project.document.ProjectId;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final MongoTemplate mongoTemplate;

    public CustomResponse<?> testMongoDB() {
//        mongoTemplate.find(Query.query(Criteria.where("idx").is(1)), RequestProjectDTO.Project.class);
        Project findProject = mongoTemplate.findById(ProjectId.builder()
                        .projectIdx(1L)
                        .partnershipIdx(1L)
                        .build(),
                Project.class);
        DeleteResult deleteResult = null;
        if(findProject != null) {
             deleteResult = mongoTemplate.remove(findProject);
            log.info(Utils.getLogMaker(Utils.eLogType.USER), "delete count : " + deleteResult.getDeletedCount());
        }
        // 프로젝트 생성
        Project project = Project.builder()
                .projectId(ProjectId.builder()
                        .projectIdx(1L)
                        .partnershipIdx(1L)
                        .build())
                .nodeList(new ArrayList<>())
                .edgeList(new ArrayList<>())
                .build();
        // 노드 생성
        Node node1 = Node.builder()
                .nodeType("Company")
                .fieldName("CompanyName")
                .fieldType("String")
                .build();
        Node node2 = Node.builder()
                .nodeType("Company")
                .fieldName("CompanyName")
                .fieldType("String")
                .build();
        // 노드 추가
        project.getNodeList().add(node1);
        project.getNodeList().add(node2);

        // 엣지 생성
        Edge edge1 = Edge.builder()
                .edgeType("CompanyLink")
                .srcNodeType("Company")
                .srcFieldName("cpIdx_1")
                .destNodeType("Company")
                .destFieldName("cpIdx_2")
                .labelFieldName("buy")
                .labelFieldType("Integer")
                .unit("$")
                .color("FF0000")
                .useDirection(true)
                .weight(false)
                .build();
        Edge edge2 = Edge.builder()
                .edgeType("CompanyLink")
                .srcNodeType("Company")
                .srcFieldName("cpIdx_2")
                .destNodeType("Company")
                .destFieldName("cpIdx_1")
                .labelFieldName("sell")
                .labelFieldType("Integer")
                .unit("$")
                .color("FF0000")
                .useDirection(true)
                .weight(false)
                .build();
        // 엣지 추가
        project.getEdgeList().add(edge1);
        project.getEdgeList().add(edge2);

        mongoTemplate.insert(project);

        List<Project> result = mongoTemplate.find(Query.query(Criteria.where("nodeList").elemMatch(Criteria.where("nodeType").is("Company"))), Project.class);

        return CustomResponse.builder()
                .data(result)
                .build();
    }
}
