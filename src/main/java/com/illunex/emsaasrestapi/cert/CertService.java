package com.illunex.emsaasrestapi.cert;

import com.illunex.emsaasrestapi.cert.dto.RequestCertDTO;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.common.aws.AwsSESComponent;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.mapper.EmailHistoryMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberJoinMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberMapper;
import com.illunex.emsaasrestapi.member.vo.MemberEmailHistoryVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipInviteLinkMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipInvitedMemberMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipInviteLinkVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CertService {
    private final MemberMapper memberMapper;
    private final EmailHistoryMapper emailHistoryMapper;
    private final CertComponent certComponent;

    private final PasswordEncoder passwordEncoder;
    private final PartnershipInviteLinkMapper partnershipInviteLinkMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final PartnershipMapper partnershipMapper;
    private final MemberJoinMapper memberJoinMapper;
    private final PartnershipInvitedMemberMapper partnershipInvitedMemberMapper;

    private final ModelMapper modelMapper;

    @Value("${server.encrypt-key}")
    private String encryptKey;

    /**
     * 회원가입 인증
     * @param certData
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> certificateJoin(String certData) throws Exception {
        String decrypted = Utils.AES256.decrypt(encryptKey, certData);
        JSONObject data = new JSONObject(decrypted);
        ZonedDateTime expireDate = ZonedDateTime.parse(data.getString("expire"));

        // 회원가입용 인증키 확인
        if(!data.getString("type").equals(AwsSESComponent.EmailType.join.getValue())) {
            throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID);
        }

        // 회원 확인
        MemberVO member = memberMapper.selectByEmail(data.getString("email"))
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        if(!member.getStateCd().equals(EnumCode.Member.StateCd.Wait.getCode())) {
            // 인증대기 상태가 아닐 경우
            throw new CustomException(ErrorCode.COMMON_ALREADY_EMAIL_CERTIFICATE);
        }

        // 인증키 유효 체크
        MemberEmailHistoryVO history = emailHistoryMapper.selectTop1ByMemberIdxAndEmailTypeOrderByCreateDateDesc(member.getIdx(), EnumCode.Email.TypeCd.JoinEmail.getCode())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        // 인증 만료 체크
        if(expireDate.isBefore(ZonedDateTime.now())
                || history.getExpireDate().isBefore(ZonedDateTime.now())
                || !history.getCertData().equals(certData)
                || history.isUsed()) {
            throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_EXPIRE);
        }

        // 인증키 사용 처리
        certComponent.markEmailHistoryAsUsed(certData);

        // 회원 상태 변경
        member.setStateCd(EnumCode.Member.StateCd.Approval.getCode());

        memberMapper.updateMemberStateByIdx(member.getIdx(), EnumCode.Member.StateCd.Approval.getCode());

        return CustomResponse.builder()
                .build();
    }

    /**
     * 검증키 검증
     * @param certData
     * @return
     */
    public CustomResponse<?> verify(String certData) throws Exception {
        Optional<PartnershipInviteLinkVO> inviteLinkOpt = partnershipInviteLinkMapper.selectByInviteTokenHash(certData);
        JSONObject data;
        if (inviteLinkOpt.isEmpty()) {
            String decrypted = Utils.AES256.decrypt(encryptKey, certData);
            data = new JSONObject(decrypted);
            ZonedDateTime expireDate = ZonedDateTime.parse(data.getString("expire"));

            MemberEmailHistoryVO historyVO = emailHistoryMapper.selectByCertData(certData)
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));
            // 인증 만료 체크
            if (expireDate.isBefore(ZonedDateTime.now())
                    || historyVO.getExpireDate().isBefore(ZonedDateTime.now())
                    || !historyVO.getCertData().equals(certData)
                    || historyVO.isUsed()) {
                throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_EXPIRE);
            }

            String emailType = data.getString("type");

            // 파트너쉽초대 or 프로젝트 초대 시 회원가입여부 확인
            if (emailType.equals(AwsSESComponent.EmailType.invite.getValue()) ||
                    emailType.equals(AwsSESComponent.EmailType.inviteProject.getValue())) {
                PartnershipMemberVO invitePartnershipMember = partnershipMemberMapper.selectByIdx(data.getInt("partnershipMemberIdx"))
                        .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));
                PartnershipVO partnershipVO = partnershipMapper.selectByIdx(invitePartnershipMember.getPartnershipIdx())
                        .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));
                String receiverEmail = data.getString("receiverEmail");
                MemberVO memberVO = memberMapper.selectByEmail(receiverEmail)
                        .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

                // 정지/탈퇴 상태인 회원인 경우 예외
                if (memberVO.getStateCd().equals(EnumCode.Member.StateCd.Withdrawal.getCode()) ||
                        memberVO.getStateCd().equals(EnumCode.Member.StateCd.Suspend.getCode())) {
                    throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID);
                }

                if (memberVO.getStateCd().equals(EnumCode.Member.StateCd.Wait.getCode())) {
                    data.put("isMemberApproved", false);
                } else if (memberVO.getStateCd().equals(EnumCode.Member.StateCd.Approval.getCode())) {
                    data.put("isMemberApproved", true);
                }
                data.put("partnershipName", partnershipVO.getName());
            }
        } else {
            PartnershipInviteLinkVO linkVO = inviteLinkOpt.get();
            PartnershipVO partnershipVO = partnershipMapper.selectByIdx(linkVO.getPartnershipIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
            data = new JSONObject(linkVO);
            data.put("partnershipName", partnershipVO.getName());
        }

        return CustomResponse.builder()
                .data(data.toMap())
                .build();
    }

    /**
     * 초대 승인을 통한 가입
     * @param request
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> certificateInviteSignup(RequestCertDTO.InviteSignup request) throws Exception {
        Optional<PartnershipInviteLinkVO> inviteLinkOpt = partnershipInviteLinkMapper.selectByInviteTokenHash(request.getCertData());
        // 이메일 링크을 통한 가입
        if (inviteLinkOpt.isEmpty()) {
            final String INVITE_MAIL_TYPE = AwsSESComponent.EmailType.invite.getValue();
            final String INVITE_PROJECT_MAIL_TYPE = AwsSESComponent.EmailType.inviteProject.getValue();
            // 인증키 유효성 체크
            JSONObject data = certComponent.verifyCertData(request.getCertData());

            String password = request.getPassword();
            String emailType = data.getString("type");

            // 초대받은 회원정보 조회
            String email = data.getString("receiverEmail");
            MemberVO memberVO = memberMapper.selectByEmail(email)
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));
            // 대기 상태가 아닌 경우 예외
            if (!memberVO.getStateCd().equals(EnumCode.Member.StateCd.Wait.getCode())) {
                throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID);
            }
            // 회원정보 업데이트(상태, 패스워드)
            memberMapper.updateNameByIdx(request.getName(), memberVO.getIdx());
            memberMapper.updateStateAndPasswordByIdx(memberVO.getIdx(), EnumCode.Member.StateCd.Approval.getCode(), passwordEncoder.encode(password));

            // 파트너쉽 초대 승인 로직
            if (emailType.equals(INVITE_MAIL_TYPE)) {
                certComponent.approvePartnershipMember(data, memberVO);
            }

            // 프로젝트 초대 승인 로직
            if (emailType.equals(INVITE_PROJECT_MAIL_TYPE)) {
                // TODO
            }

            // 이메일 인증 완료 처리
            certComponent.markEmailHistoryAsUsed(request.getCertData());

        // [초대 링크를 통한 가입]
        } else {
            PartnershipInviteLinkVO linkVO = inviteLinkOpt.get();
            if (request.getEmail().isBlank()) {
                throw new CustomException(ErrorCode.COMMON_INVALID);
            }
            // 링크 유효성 체크
            if (linkVO.getExpireDate().isBefore(ZonedDateTime.now())) {
                if (linkVO.getStateCd().equals(EnumCode.PartnershipInviteLink.StateCd.ACTIVE.getCode())) {
                    // 만료된 링크 상태 변경
                    linkVO.setStateCd(EnumCode.PartnershipInviteLink.StateCd.EXPIRE.getCode());
                    partnershipInviteLinkMapper.updateByPartnershipInviteLinkVO(linkVO);
                }
                throw new CustomException(ErrorCode.COMMON_INVITE_LINK_EXPIRE);
            }

            JSONObject inviteInfoJson = new JSONObject(linkVO.getInviteInfoJson());
            JSONArray products = inviteInfoJson.getJSONArray("products");
            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                boolean isValid = Arrays.stream(EnumCode.Product.ProductCd.values())
                        .anyMatch(p -> p.getCode().equals(product.getString("productCode")));

                if (!isValid) {
                    throw new CustomException(ErrorCode.COMMON_INVALID);
                }
            }
            // 초대된 파트너쉽 회원 정보
            String finalAuth = inviteInfoJson.getString("auth");

            PartnershipMemberVO invitePartnershipMember = partnershipMemberMapper.selectByIdx(linkVO.getCreatedByPartnershipMemberIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
            PartnershipVO partnershipVO = partnershipMapper.selectByIdx(invitePartnershipMember.getPartnershipIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

            // 회원가입
            MemberVO member;
            Optional<MemberVO> memberOPT = memberMapper.selectByEmail(request.getEmail());
            if (memberOPT.isPresent()) {
                member = memberOPT.get();
            } else {
                member = modelMapper.map(request, MemberVO.class);
                // 일반 회원
                member.setTypeCd(finalAuth);
                // 승인 완료 상태
                member.setStateCd(EnumCode.Member.StateCd.Wait.getCode());
                member.setPassword(passwordEncoder.encode(member.getPassword()));
                member.setName(request.getName());
                //회원정보 생성
                memberJoinMapper.insertByMemberJoin(member);
            }
            // 파트너쉽 회원 등록
            PartnershipMemberVO partnershipMember = new PartnershipMemberVO();
            partnershipMember.setMemberIdx(member.getIdx());
            partnershipMember.setPartnershipIdx(partnershipVO.getIdx());
            partnershipMember.setManagerCd(finalAuth);
            partnershipMember.setStateCd(EnumCode.PartnershipMember.StateCd.Normal.getCode());

            partnershipMemberMapper.insertByPartnershipMember(partnershipMember);

            // 초대 상태 등록
            PartnershipInvitedMemberVO invitedMemberVO = new PartnershipInvitedMemberVO();
            invitedMemberVO.setPartnershipMemberIdx(partnershipMember.getIdx());
            invitedMemberVO.setPartnershipIdx(partnershipVO.getIdx());
            invitedMemberVO.setEmail(member.getEmail());
            invitedMemberVO.setInvitedDate(ZonedDateTime.now());
            invitedMemberVO.setInvitedByPartnershipMemberIdx(linkVO.getCreatedByPartnershipMemberIdx());
            invitedMemberVO.setJoinedDate(ZonedDateTime.now());
            partnershipInvitedMemberMapper.insertInvitedMember(invitedMemberVO);

            linkVO.setUsedCount(linkVO.getUsedCount() == null ? 1 : linkVO.getUsedCount() + 1);
            partnershipInviteLinkMapper.updateByPartnershipInviteLinkVO(linkVO);
        }


        return CustomResponse.builder()
                .build();
    }

    /**
     * 초대 승인
     * @param certData
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> certificateInviteApprove(String certData) throws Exception {
        final String INVITE_MAIL_TYPE = AwsSESComponent.EmailType.invite.getValue();
        final String INVITE_PROJECT_MAIL_TYPE = AwsSESComponent.EmailType.inviteProject.getValue();
        // 인증키 유효성 체크
        JSONObject data = certComponent.verifyCertData(certData);
        String emailType = data.getString("type");

//        if (emailType.equals(INVITE_MAIL_TYPE)) {
//            certComponent.approvePartnershipMember(data);
//        }

        // 프로젝트 초대 승인 로직
        if (emailType.equals(INVITE_PROJECT_MAIL_TYPE)) {
            // TODO
        }

        // 이메일 인증 완료 처리
        certComponent.markEmailHistoryAsUsed(certData);

        return CustomResponse.builder()
                .build();
    }
}
