package com.illunex.emsaasrestapi.partnership;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.member.mapper.MemberJoinMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberMapper;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.dto.PartnershipCreateDTO;
import com.illunex.emsaasrestapi.partnership.dto.RequestPartnershipDTO;
import com.illunex.emsaasrestapi.partnership.dto.ResponsePartnershipDTO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnershipService {
    private final PartnershipMapper partnershipMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final LicensePartnershipMapper licensePartnershipMapper;
    private final MemberMapper memberMapper;
    private final MemberJoinMapper memberJoinMapper;

    private final ModelMapper modelMapper;

    /**
     * 파트너십 생성 (기본 라이센스)
     * @param partnershipDTO 파트너십 정보
     * @param ownerMemberIdx 파트너십 생성자 IDX
     * @return partnershipIdx 파트너십 IDX
     */
    @Transactional
    public PartnershipVO createPartnership(PartnershipCreateDTO partnershipDTO, Integer ownerMemberIdx) throws CustomException {
        //파트너쉽 도메인 중복체크
        if(partnershipMapper.selectByDomain(partnershipDTO.getDomain()).isPresent()) {
            throw new CustomException(ErrorCode.PARTNERSHIP_DOMAIN_DUPLICATE);
        }
        //파트너쉽 정보 등록
        PartnershipVO partnership = new PartnershipVO();
        partnership.setName(partnershipDTO.getPartnershipName());
        partnership.setDomain(partnershipDTO.getDomain());
        partnershipMapper.insertByPartnerJoin(partnership);

        //파트너쉽회원정보 등록
        PartnershipMemberVO partnershipMember = new PartnershipMemberVO();
        partnershipMember.setMemberIdx(ownerMemberIdx);
        partnershipMember.setPartnershipIdx(partnership.getIdx());
        partnershipMember.setManagerCd(EnumCode.Partnership.ManagerCd.Manager.getValue());
        partnershipMember.setStateCd(EnumCode.Partnership.StateCd.Normal.getCode());
        partnershipMemberMapper.insertByPartnershipMember(partnershipMember);

        //파트너십 기본 라이센스 등록
        LicensePartnershipVO licensePartnershipVO = new LicensePartnershipVO();
        licensePartnershipVO.setPartnershipIdx(partnership.getIdx());
        licensePartnershipVO.setLicenseIdx(1); // 기본 라이센스 ?
        licensePartnershipVO.setStateCd("LST0001");
        licensePartnershipMapper.insertByLicensePartnership(licensePartnershipVO);

        return partnership;
    }

    public List<PartnershipVO> getPartnerships(Integer memberIdx) throws CustomException {
        return partnershipMapper.selectByMember(memberIdx);
    }

    /**
     * 파트너쉽 멤버 초대
     * @param partnershipIdx 파트너쉽 번호
     * @param memberIdx 파트너쉽 관리자 회원 번호
     * @param inviteMember 초대 회원 정보
     * @return
     * @throws CustomException
     */
    @Transactional
    public ResponsePartnershipDTO.InviteMember invitePartnershipMember(Integer partnershipIdx, Integer memberIdx, RequestPartnershipDTO.InviteMember inviteMember) throws CustomException {
        // 파트너쉽 관리자 여부 확인
        Optional<PartnershipMemberVO> partnershipMemberVO = partnershipMemberMapper.selectPartnershipMemberByMemberIdx(partnershipIdx, memberIdx);
        if (partnershipMemberVO.isEmpty() || !partnershipMemberVO.get().getManagerCd().equals(EnumCode.Partnership.ManagerCd.Manager.getCode())) {
            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
        }

        List<ResponsePartnershipDTO.InviteResult> validList = new ArrayList<>();
        List<ResponsePartnershipDTO.InviteResult> invalidList = new ArrayList<>();

        for (RequestPartnershipDTO.InviteMemberInfo info : inviteMember.getInviteMembers()) {
            String email = info.getEmail().toLowerCase().trim();

            try {
                // 1. 이미 초대되었는지 확인
                if (partnershipMemberMapper.existsInvitedMember(partnershipIdx, email)) {
                    continue;
                }

                // 2. 이미 가입된 사용자 여부
                MemberVO member = memberMapper.selectByEmail(email)
                        .filter(e -> e.getStateCd().equals(EnumCode.Partnership.StateCd.Normal.getCode()))
                        .orElseGet(() -> {
                            MemberVO memberVO = new MemberVO();
                            memberVO.setEmail(email);
                            memberJoinMapper.insertByMemberJoin(memberVO);
                            return memberVO;
                        });

                // 3. 초대 정보 저장
                PartnershipInvitedMemberVO invitedMemberVO = new PartnershipInvitedMemberVO();
                invitedMemberVO.setEmail(email);
                invitedMemberVO.setPartnershipIdx(partnershipIdx);
                invitedMemberVO.setInvitedByPartnershipMemberIdx(partnershipMemberVO.get().getIdx());
                invitedMemberVO.setMemberIdx(member.getIdx());

                // 4. partnership_member 생성
                PartnershipMemberVO partnershipMember = new PartnershipMemberVO();
                partnershipMember.setMemberIdx(member.getIdx());
                partnershipMember.setPartnershipIdx(partnershipIdx);
                partnershipMember.setManagerCd(info.getAuth());
                partnershipMember.setStateCd(EnumCode.Partnership.StateCd.Wait.getCode());
                partnershipMemberMapper.insertByPartnershipMember(partnershipMember);

                invitedMemberVO.setPartnershipMemberIdx(partnershipMember.getIdx());


                partnershipMemberMapper.insertInvitedMember(invitedMemberVO);
                validList.add(ResponsePartnershipDTO.InviteResult.builder()
                        .email(email)
                        .result("success")
                        .build());

                // TODO 이메일 발송 로직, 발송 이력 저장 로직 필요
            } catch (Exception e) {
                log.error(e.getMessage());
                invalidList.add(ResponsePartnershipDTO.InviteResult.builder()
                                .email(email)
                                .result("error")
                                .reason(e.getMessage())
                                .build()
                );
            }
        }

        return ResponsePartnershipDTO.InviteMember.builder()
                .valid(validList)
                .inValid(invalidList)
                .build();
    }
}
