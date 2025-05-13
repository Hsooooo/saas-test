package com.illunex.emsaasrestapi.partnership;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartnershipComponent {
    private final ProjectMapper projectMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;

    /**
     * 프로젝트 및 파트너쉽 회원 여부 체크
     * @param memberVO
     * @param projectIdx
     * @return
     * @throws CustomException
     */
    public PartnershipMemberVO checkPartnershipMember(MemberVO memberVO, Integer projectIdx) throws CustomException {
        // 프로젝트 조회
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        // 파트너쉽 회원 조회
        PartnershipMemberVO partnershipMemberVO = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(projectVO.getPartnershipIdx(), memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));

        return partnershipMemberVO;
    }
}
