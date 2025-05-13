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

import java.util.ArrayList;
import java.util.List;

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
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, projectIdx);
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

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
     * @param memberVO
     * @param selectNode
     * @return
     */
    public CustomResponse<?> getNetworkSingleExtend(MemberVO memberVO, RequestNetworkDTO.SelectNode selectNode) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, selectNode.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

        ResponseNetworkDTO.Network response = new ResponseNetworkDTO.Network();

        //노드검색
        Query query = Query.query(Criteria.where("_id.projectIdx").is(selectNode.getProjectIdx())
                        .and("_id.nodeIdx").is(selectNode.getNodeIdx())
                        .and("label").is(selectNode.getLabel()));
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
     * @param memberVO
     * @param selectNode
     * @return
     */
    public CustomResponse<?> getNetworkInfo(MemberVO memberVO, RequestNetworkDTO.SelectNode selectNode) throws CustomException {
        // 파트너쉽 회원 여부 체크
        PartnershipMemberVO partnershipMemberVO = partnershipComponent.checkPartnershipMember(memberVO, selectNode.getProjectIdx());
        // 프로젝트 구성원 여부 체크
        projectComponent.checkProjectMember(memberVO.getIdx(), partnershipMemberVO.getIdx());

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
}
