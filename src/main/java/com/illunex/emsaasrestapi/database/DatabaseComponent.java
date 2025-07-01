package com.illunex.emsaasrestapi.database;

import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.EdgeId;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.network.NodeId;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectEdge;
import com.illunex.emsaasrestapi.project.document.project.ProjectNode;
import com.mongodb.BasicDBObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseComponent {
    private final MongoTemplate mongoTemplate;

    /**
     * Node 저장 처리
     *
     * @param project     프로젝트 정보
     * @param projectIdx  프로젝트 인덱스
     * @param type        Node 타입
     * @param data        저장할 데이터
     */
    public void handleNodeSave(Project project, Integer projectIdx, String type, LinkedHashMap<String, Object> data) {
        ProjectNode projectNode = project.getProjectNodeList().stream()
                .filter(n -> n.getNodeType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 Node 타입이 존재하지 않습니다: " + type));

        Node existNode = mongoTemplate.findOne(Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)), Node.class);
        if (existNode == null) throw new IllegalArgumentException("Node가 존재하지 않습니다: " + type);


        Map<String, Object> baseProperties = existNode.getProperties();
        validateFields(baseProperties.keySet(), data.keySet(), "Node");
        Object rawId = data.get(projectNode.getUniqueCellName());
        Object id = convertIdType(existNode.getId(), rawId);
        LinkedHashMap<String, Object> transformedData = transformData(existNode.getProperties(), data);

        NodeId nodeId = existNode.getNodeId();
        nodeId.setNodeIdx(id);

        Node newNode = Node.builder()
                .nodeId(nodeId)
                .id(id)
                .label(existNode.getLabel())
                .properties(transformedData)
                .build();

        mongoTemplate.save(newNode);
    }

    /**
     * Edge 저장 처리
     *
     * @param project     프로젝트 정보
     * @param projectIdx  프로젝트 인덱스
     * @param type        Edge 타입
     * @param data        저장할 데이터
     */
    public void handleEdgeSave(Project project, Integer projectIdx, String type, LinkedHashMap<String, Object> data) {
        ProjectEdge projectEdge = project.getProjectEdgeList().stream()
                .filter(e -> e.getEdgeType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 Edge 타입이 존재하지 않습니다: " + type));

        Edge existEdge = mongoTemplate.findOne(Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)), Edge.class);
        if (existEdge == null) throw new IllegalArgumentException("Edge가 존재하지 않습니다: " + type);

        Map<String, Object> baseProperties = existEdge.getProperties();
        validateFields(baseProperties.keySet(), data.keySet(), "Edge");
        int maxEdgeIdx = getMaxEdgeIdx(projectIdx, type);
        Object id = convertIdType(existEdge.getId(), maxEdgeIdx + 1);
        LinkedHashMap<String, Object> transformedData = transformData(existEdge.getProperties(), data);

        EdgeId edgeId = existEdge.getEdgeId();
        edgeId.setEdgeIdx(id);

        Edge newEdge = Edge.builder()
                .edgeId(edgeId)
                .id(id)
                .startType(existEdge.getStartType())
                .start(transformedData.get(projectEdge.getSrcEdgeCellName()))
                .endType(existEdge.getEndType())
                .end(transformedData.get(projectEdge.getDestEdgeCellName()))
                .type(existEdge.getType())
                .properties(transformedData)
                .build();

        mongoTemplate.save(newEdge);
    }

    /**
     * ID 타입 변환
     *
     * @param referenceId  참조 ID 타입
     * @param rawValue     변환할 값
     * @return 변환된 ID 값
     */
    private Object convertIdType(Object referenceId, Object rawValue) {
        if (referenceId instanceof Integer) return Integer.parseInt(rawValue.toString());
        if (referenceId instanceof Double) return Double.parseDouble(rawValue.toString());
        throw new IllegalArgumentException("Unsupported ID type: " + referenceId.getClass());
    }

    /**
     * 입력 데이터를 기반 속성에 맞게 변환
     *
     * @param baseProperties 기본 속성 맵
     * @param inputData      입력 데이터 맵
     * @return 변환된 데이터 맵
     */
    private LinkedHashMap<String, Object> transformData(Map<String, Object> baseProperties, Map<String, Object> inputData) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (baseProperties.containsKey(key)) {
                Object refValue = baseProperties.get(key);
                try {
                    result.put(key, convertType(refValue, value));
                } catch (Exception e) {
                    result.put(key, null);
                }
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 참조 타입에 따라 값을 변환
     *
     * @param reference 참조 타입
     * @param value     변환할 값
     * @return 변환된 값
     */
    private Object convertType(Object reference, Object value) {
        if (reference instanceof Integer) return Integer.parseInt(value.toString());
        if (reference instanceof Double) return Double.parseDouble(value.toString());
        if (reference instanceof Boolean) return Boolean.parseBoolean(value.toString());
        if (reference instanceof String) return value.toString();
        return value;
    }

    /**
     * Edge의 최대 인덱스 조회
     *
     * @param projectIdx 프로젝트 인덱스
     * @param type       Edge 타입
     * @return 최대 Edge 인덱스
     */
    private int getMaxEdgeIdx(Integer projectIdx, String type) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)),
                Aggregation.group().max("_id.edgeIdx").as("maxEdgeIdx")
        );

        AggregationResults<BasicDBObject> result = mongoTemplate.aggregate(agg, "edge", BasicDBObject.class);
        return result.getUniqueMappedResult() != null ? result.getUniqueMappedResult().getInt("maxEdgeIdx") : 0;
    }

    private void validateFields(Set<String> expectedKeys, Set<String> actualKeys, String label) {
        Set<String> missingKeys = new HashSet<>(expectedKeys);
        missingKeys.removeAll(actualKeys);

        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException(label + " 데이터에 다음 필드가 누락되었습니다: " + missingKeys);
        }
    }

}
