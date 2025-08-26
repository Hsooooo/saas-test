package com.illunex.emsaasrestapi.database;

import com.illunex.emsaasrestapi.database.dto.SaveResultRecord;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.EdgeId;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.network.NodeId;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectEdge;
import com.illunex.emsaasrestapi.project.document.project.ProjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
    public SaveResultRecord handleNodeSave(Project project, Integer projectIdx, String type, LinkedHashMap<String, Object> data) {
        // 1) 해당 타입 템플릿 노드(스키마용) 찾기
        ProjectNode projectNode = project.getProjectNodeList().stream()
                .filter(n -> n.getNodeType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 Node 타입이 존재하지 않습니다: " + type));

        Node schemaNode = mongoTemplate.findOne(
                Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)),
                Node.class
        );
        if (schemaNode == null) {
            throw new IllegalArgumentException("Node 스키마 템플릿을 찾을 수 없습니다: " + type);
        }

        // 2) 필드 검증
        Map<String, Object> baseProperties = schemaNode.getProperties();
        validateFields(baseProperties.keySet(), data.keySet(), "Node");

        // 3) ID 추출 및 변환
        Object rawId = data.get(projectNode.getUniqueCellName());
        Object id = convertIdType(schemaNode.getId(), rawId);

        // 4) 실제 존재 여부 확인
        Query nodeQuery = Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                .and("_id.type").is(type)
                .and("_id.nodeIdx").is(id));
        Node existing = mongoTemplate.findOne(nodeQuery, Node.class);
        boolean isCreated = (existing == null);

        // 5) 데이터 변환
        LinkedHashMap<String, Object> transformedData = transformData(schemaNode.getProperties(), data);

        // 6) NodeId 새로 생성 (기존 객체 재사용 X)
        NodeId nodeId = new NodeId(projectIdx, type, id);

        Node newNode = Node.builder()
                .nodeId(nodeId)
                .id(id)
                .label(schemaNode.getLabel())
                .properties(transformedData)
                .build();

        // 7) 저장
        mongoTemplate.save(newNode);
        return isCreated ? SaveResultRecord.created(id, type) : SaveResultRecord.updated(id, type);
    }

    public SaveResultRecord handleNodeInsert(Project project, Integer projectIdx, String type, LinkedHashMap<String, Object> data) {
        // 1) 해당 타입 템플릿 노드(스키마용) 찾기
        ProjectNode projectNode = project.getProjectNodeList().stream()
                .filter(n -> n.getNodeType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 Node 타입이 존재하지 않습니다: " + type));

        Node schemaNode = mongoTemplate.findOne(
                Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)),
                Node.class
        );
        if (schemaNode == null) {
            throw new IllegalArgumentException("Node 스키마 템플릿을 찾을 수 없습니다: " + type);
        }

        // 2) 필드 검증
        Map<String, Object> baseProperties = schemaNode.getProperties();
        validateFields(baseProperties.keySet(), data.keySet(), "Node");

        // 3) ID 추출 및 변환
        Object rawId = data.get(projectNode.getUniqueCellName());
        Object id = convertIdType(schemaNode.getId(), rawId);

        // 4) 실제 존재 여부 확인
        Query nodeQuery = Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                .and("_id.type").is(type)
                .and("_id.nodeIdx").is(id));
        Node existing = mongoTemplate.findOne(nodeQuery, Node.class);
        boolean isCreated = (existing == null);

        // 5) 데이터 변환
        LinkedHashMap<String, Object> transformedData = transformData(schemaNode.getProperties(), data);

        // 6) NodeId 새로 생성 (기존 객체 재사용 X)
        NodeId nodeId = new NodeId(projectIdx, type, id);

        Node newNode = Node.builder()
                .nodeId(nodeId)
                .id(id)
                .label(schemaNode.getLabel())
                .properties(transformedData)
                .build();

        try {
            mongoTemplate.insert(newNode);
            return SaveResultRecord.created(id, type);
        } catch (DuplicateKeyException e) {
            log.error("Node with ID {} already exists for type {}", id, type, e);
            return SaveResultRecord.skipped(id, type);
        } catch (Exception e) {
            log.error("Error occurred while inserting Node with ID {} for type {}", id, type, e);
            return SaveResultRecord.failed(id, type);
        }
    }

    public SaveResultRecord handleNodeUpdate(Project project, Integer projectIdx, String type, Object id, LinkedHashMap<String, Object> data) {
        Node schemaNode = mongoTemplate.findOne(
                Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)),
                Node.class
        );
        if (schemaNode == null) throw new IllegalArgumentException("Node 스키마 템플릿이 존재하지 않습니다.");

        Map<String, Object> baseProperties = schemaNode.getProperties();
        validateFields(baseProperties.keySet(), data.keySet(), "Node");

        LinkedHashMap<String, Object> transformedData = transformData(schemaNode.getProperties(), data);

        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                .and("_id.type").is(type)
                .and("_id.nodeIdx").is(id));

        Update update = new Update();
        for (Map.Entry<String, Object> entry : transformedData.entrySet()) {
            update.set("properties." + entry.getKey(), entry.getValue());
        }

        UpdateResult result = mongoTemplate.updateFirst(query, update, Node.class);

        if (result.getMatchedCount() > 0) {
            return SaveResultRecord.updated(id, type);
        } else {
            return SaveResultRecord.failed(id, type);
        }
    }

    public SaveResultRecord handleEdgeInsert(Project project, Integer projectIdx, String type, LinkedHashMap<String, Object> data) {
        Object id;
        ProjectEdge projectEdge = project.getProjectEdgeList().stream()
                .filter(e -> e.getEdgeType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 Edge 타입이 존재하지 않습니다: " + type));

        Edge existEdge = mongoTemplate.findOne(Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)), Edge.class);
        if (existEdge == null) throw new IllegalArgumentException("Edge가 존재하지 않습니다: " + type);

        Map<String, Object> baseProperties = existEdge.getProperties();
        validateFields(baseProperties.keySet(), data.keySet(), "Edge");
        // ID가 null인 경우, 최대 Edge 인덱스를 조회하여 새 ID를 생성
        int maxEdgeIdx = getMaxEdgeIdx(projectIdx, type);
        id = convertIdType(existEdge.getId(), maxEdgeIdx + 1);

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

        try {
            mongoTemplate.insert(newEdge);
            return SaveResultRecord.created(id, type);
        } catch (DuplicateKeyException e) {
            log.warn("Edge 중복: {} ({})", id, type, e);
            return SaveResultRecord.skipped(id, type);
        } catch (Exception e) {
            log.error("Edge 삽입 오류: {} ({})", id, type, e);
            return SaveResultRecord.failed(id, type);
        }
    }

    public SaveResultRecord handleEdgeUpdate(Project project, Integer projectIdx, String type, Object id, LinkedHashMap<String, Object> data) {
        ProjectEdge projectEdge = project.getProjectEdgeList().stream()
                .filter(e -> e.getEdgeType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 Edge 타입이 존재하지 않습니다: " + type));

        Edge existEdge = mongoTemplate.findOne(Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)), Edge.class);
        if (existEdge == null) throw new IllegalArgumentException("Edge가 존재하지 않습니다: " + type);

        Map<String, Object> baseProperties = existEdge.getProperties();
        validateFields(baseProperties.keySet(), data.keySet(), "Edge");
        // ID가 null인 경우, 최대 Edge 인덱스를 조회하여 새 ID를 생성
        id = convertIdType(existEdge.getId(), id);
        LinkedHashMap<String, Object> transformedData = transformData(existEdge.getProperties(), data);

        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectIdx)
                .and("_id.type").is(type)
                .and("_id.edgeIdx").is(id));

        Update update = new Update();
        for (Map.Entry<String, Object> entry : transformedData.entrySet()) {
            update.set("properties." + entry.getKey(), entry.getValue());
        }

        try {
            UpdateResult result = mongoTemplate.updateFirst(query, update, Edge.class);
            if (result.getMatchedCount() > 0) {
                return SaveResultRecord.updated(id, type);
            } else {
                log.warn("Edge 업데이트 실패 (대상 없음): {} ({})", id, type);
                return SaveResultRecord.failed(id, type);
            }
        } catch (Exception e) {
            log.error("Edge 업데이트 중 오류: {} ({})", id, type, e);
            return SaveResultRecord.failed(id, type);
        }
    }




    /**
     * Edge 저장 처리
     *
     * @param project     프로젝트 정보
     * @param projectIdx  프로젝트 인덱스
     * @param type        Edge 타입
     * @param data        저장할 데이터
     */
    public SaveResultRecord handleEdgeSave(Project project, Integer projectIdx, String type, LinkedHashMap<String, Object> data, Object id) {
        ProjectEdge projectEdge = project.getProjectEdgeList().stream()
                .filter(e -> e.getEdgeType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 Edge 타입이 존재하지 않습니다: " + type));

        Edge existEdge = mongoTemplate.findOne(Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.type").is(type)), Edge.class);
        if (existEdge == null) throw new IllegalArgumentException("Edge가 존재하지 않습니다: " + type);

        Map<String, Object> baseProperties = existEdge.getProperties();
        validateFields(baseProperties.keySet(), data.keySet(), "Edge");
        // ID가 null인 경우, 최대 Edge 인덱스를 조회하여 새 ID를 생성
        if (id == null) {
            int maxEdgeIdx = getMaxEdgeIdx(projectIdx, type);
            id = convertIdType(existEdge.getId(), maxEdgeIdx + 1);
        } else {
            id = convertIdType(existEdge.getId(), id);
        }
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
        return (id != null) ? SaveResultRecord.created(id, type) : SaveResultRecord.updated(id, type);
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
        if (referenceId instanceof Long) return Long.parseLong(rawValue.toString());   // 추가 권장
        if (referenceId instanceof String) return rawValue.toString();
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
