package com.illunex.emsaasrestapi.partnership;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.aws.AwsSESComponent;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberJoinMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberMapper;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.dto.PartnershipCreateDTO;
import com.illunex.emsaasrestapi.partnership.dto.RequestPartnershipDTO;
import com.illunex.emsaasrestapi.partnership.dto.ResponsePartnershipDTO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipPositionMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipPositionVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnershipService {
    private final PartnershipMapper partnershipMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final PartnershipPositionMapper partnershipPositionMapper;
    private final LicensePartnershipMapper licensePartnershipMapper;
    private final MemberMapper memberMapper;
    private final MemberJoinMapper memberJoinMapper;

    private final AwsSESComponent sesComponent;

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
        partnershipMapper.insertByPartnershipVO(partnership);

        //파트너쉽회원정보 등록
        PartnershipMemberVO partnershipMember = new PartnershipMemberVO();
        partnershipMember.setMemberIdx(ownerMemberIdx);
        partnershipMember.setPartnershipIdx(partnership.getIdx());
        partnershipMember.setManagerCd(EnumCode.Partnership.ManagerCd.Manager.getValue());
        partnershipMember.setStateCd(EnumCode.Partnership.StateCd.Normal.getCode());
        partnershipMemberMapper.insertByPartnershipMember(partnershipMember);

        //파트너십 기본 라이센스 등록
//        LicensePartnershipVO licensePartnershipVO = new LicensePartnershipVO();
//        licensePartnershipVO.setPartnershipIdx(partnership.getIdx());
//        licensePartnershipVO.setLicenseIdx(1); // 기본 라이센스 ?
//        licensePartnershipVO.setStateCd("LST0001");
//        licensePartnershipMapper.insertByLicensePartnership(licensePartnershipVO);

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
                sesComponent.sendInviteMemberEmail(null, email, partnershipMember.getIdx());

                validList.add(ResponsePartnershipDTO.InviteResult.builder()
                        .email(email)
                        .result("success")
                        .build());


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


    /**
     * 회원정보 조회 (파트너쉽)
     * @param partnershipIdx 파트너쉽 번호
     * @param memberVO 로그인된사용자정보
     * @return
     * @throws CustomException
     */
    public ResponsePartnershipDTO.MyInfo getMyInfo(Integer partnershipIdx, MemberVO memberVO) throws CustomException {
        PartnershipVO partnership = partnershipMapper.selectByIdx(partnershipIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        PartnershipMemberVO partnershipMember = partnershipMemberMapper.selectPartnershipMemberByMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));

        ResponsePartnershipDTO.PartnershipPositionInfo positionInfo = null;
        if (partnershipMember.getPartnershipPositionIdx() != null) {
            PartnershipPositionVO positionVO = partnershipPositionMapper.selectByIdx(partnershipMember.getPartnershipPositionIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
            positionInfo = ResponsePartnershipDTO.PartnershipPositionInfo.builder()
                    .idx(positionVO.getIdx())
                    .name(positionVO.getName())
                    .sortLevel(positionVO.getSortLevel())
                    .build();
        }


        return ResponsePartnershipDTO.MyInfo.builder()
                .partnershipMember(ResponsePartnershipDTO.MyInfoPartnershipMember.builder()
                        .idx(partnershipMember.getIdx())
                        .positionInfo(positionInfo)
                        .phone(partnershipMember.getPhone())
                        .build())
                .partnership(ResponsePartnershipDTO.PartnershipInfo.builder()
                        .idx(partnership.getIdx())
                        .domain(partnership.getDomain())
                        .name(partnership.getName())
                        .imageUrl(partnership.getImageUrl())
                        .imagePath(partnership.getImagePath())
                        .build())
                .member(ResponsePartnershipDTO.MyInfoMember.builder()
                        .idx(memberVO.getIdx())
                        .name(memberVO.getName())
                        .build())
                .build();
    }

    /**
     * 내정보 수정
     * @param partnershipIdx 파트너쉽번호
     * @param memberVO 로그인회원정보
     * @param updateInfo 수정정보
     * @return
     * @throws CustomException
     */
    @Transactional
    public Object updateMyInfo(Integer partnershipIdx, MemberVO memberVO, RequestPartnershipDTO.@Valid UpdateMyInfo updateInfo) throws CustomException {
        PartnershipMemberVO partnershipMember = partnershipMemberMapper.selectPartnershipMemberByMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));

        Integer partnershipPositionIdx = null;
        String phone = updateInfo.getPhone();
        // 직책정보 전달 시
        if (updateInfo.getPosition() != null) {
            // 기존 등록된 직책이 있는경우 해당 직책 Idx / 없는 경우 마지막 sort_level로 등록
            partnershipPositionIdx = partnershipPositionMapper
                    .selectByNameAndPartnershipIdx(partnershipIdx, updateInfo.getName())
                    .map(PartnershipPositionVO::getIdx)
                    .orElseGet(() -> {
                        int maxSortLevel = partnershipPositionMapper.selectMaxSortLevelByPartnershipIdx(partnershipIdx);
                        PartnershipPositionVO newPosition = new PartnershipPositionVO();
                        newPosition.setName(updateInfo.getPosition());
                        newPosition.setSortLevel(maxSortLevel + 1);
                        newPosition.setPartnershipIdx(partnershipIdx);
                        partnershipPositionMapper.insertByPartnershipPositionVO(newPosition);

                        return newPosition.getIdx();
                    });
        }
        // 회원명 변경
        memberMapper.updateNameByIdx(updateInfo.getName(), memberVO.getIdx());
        // 파트너쉽 정보 변경 (직책, 휴대폰번호)
        partnershipMemberMapper.updatePositionIdxAndPhoneByIdx(partnershipPositionIdx, phone, partnershipMember.getIdx());

        return this.getMyInfo(partnershipIdx, memberVO);
    }


}
