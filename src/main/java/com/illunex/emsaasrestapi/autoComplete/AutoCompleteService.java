package com.illunex.emsaasrestapi.autoComplete;

import com.illunex.emsaasrestapi.autoComplete.dto.RequestAutoCompleteDTO;
import com.illunex.emsaasrestapi.autoComplete.dto.ResponseAutoCompleteDTO;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.ProjectComponent;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.document.project.ProjectNodeContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class AutoCompleteService {

    private final MongoTemplate mongoTemplate;
    private final PartnershipComponent partnershipComponent;
    private final ProjectComponent projectComponent;

    public CustomResponse<?> getAutoComplete(MemberVO memberVO,
                                             RequestAutoCompleteDTO.AutoCompleteSearch autoCompleteSearch) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, autoCompleteSearch.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

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

        Map<String, List<ResponseAutoCompleteDTO.AutoComplete>> response = new HashMap<>();

        // 노드 데이터 조회
        for (ProjectNodeContent content : filteredNodeContents) {
            String nodeType = content.getNodeType();
            String labelTitleCellName = content.getLabelTitleCellName();

            Criteria criteria = Criteria.where("_id.projectIdx").is(autoCompleteSearch.getProjectIdx())
                    .and("_id.type").is(nodeType)
                    .and("properties." + labelTitleCellName)
                    .regex(".*" + autoCompleteSearch.getSearchKeyword() + ".*", "i");

            List<Node> nodes = mongoTemplate.find(new Query(criteria).limit(autoCompleteSearch.getLimit()), Node.class, "node");

            List<ResponseAutoCompleteDTO.AutoComplete> result = new ArrayList<>();

            for (Node node : nodes) {
                ResponseAutoCompleteDTO.AutoComplete autoComplete = ResponseAutoCompleteDTO.AutoComplete.builder()
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
