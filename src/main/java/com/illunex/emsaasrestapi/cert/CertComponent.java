package com.illunex.emsaasrestapi.cert;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.mapper.EmailHistoryMapper;
import com.illunex.emsaasrestapi.member.vo.MemberEmailHistoryVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipInvitedMemberMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberProductGrantMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberProductGrantVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class CertComponent {
    private final EmailHistoryMapper emailHistoryMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final PartnershipMemberProductGrantMapper partnershipMemberProductGrantMapper;
    private final PartnershipInvitedMemberMapper partnershipInvitedMemberMapper;

    @Value("${server.encrypt-key}")
    private String encryptKey;

    public JSONObject verifyCertData(String certData) throws Exception {
        String decrypted = "";
        try {
            decrypted = Utils.AES256.decrypt(encryptKey, certData);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID);
        }
        JSONObject data = new JSONObject(decrypted);
        ZonedDateTime expireDate = ZonedDateTime.parse(data.getString("expire"));

        MemberEmailHistoryVO historyVO = emailHistoryMapper.selectByCertData(certData)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));
        // 인증 만료 체크
        if(expireDate.isBefore(ZonedDateTime.now())
                || historyVO.getExpireDate().isBefore(ZonedDateTime.now())
                || !historyVO.getCertData().equals(certData)
                || historyVO.isUsed()) {
            throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_EXPIRE);
        }

        return data;
    }

    /**
     * 초대 승인을 통한 가입
     * @param data certData 복호화 정보
     * @param invitedMember 초대받은 회원 정보
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void approvePartnershipMember(JSONObject data, MemberVO invitedMember) throws Exception {
        int partnershipMemberIdx = data.getInt("partnershipMemberIdx");
        // 초대한 파트너쉽 회원 정보
        PartnershipMemberVO partnershipMember = partnershipMemberMapper.selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        // 초대받은 파트너쉽 회원 정보
        PartnershipMemberVO invitedPartnershipMember = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(partnershipMember.getPartnershipIdx(), invitedMember.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));


        if (!EnumCode.PartnershipMember.StateCd.Wait.getCode().equals(invitedPartnershipMember.getStateCd())) {
            throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID);
        }

        // 제품 권한 정보 저장
        JSONArray products = data.getJSONArray("products");
        if (!products.isEmpty()) {
            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                PartnershipMemberProductGrantVO productGrant = new PartnershipMemberProductGrantVO();
                productGrant.setPartnershipMemberIdx(invitedPartnershipMember.getIdx());
                productGrant.setProductCode(product.getString("productCode"));
                productGrant.setPermissionCode(product.getString("auth"));

                partnershipMemberProductGrantMapper.insertByPartnershipMemberProductGrantVO(productGrant);
            }
        }

        // 파트너쉽 회원 상태 변경
        partnershipMemberMapper.updatePartnershipMemberStateByIdx(
                invitedPartnershipMember.getIdx(),
                EnumCode.PartnershipMember.StateCd.Normal.getCode()
        );
        // 초대 상태 조회
        PartnershipInvitedMemberVO invitedMemberVO = partnershipInvitedMemberMapper.selectByPartnershipMemberIdx(
                invitedPartnershipMember.getIdx()
        ).orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        // 초대 상태 변경
        partnershipInvitedMemberMapper.updateJoinedDateNowByIdx(invitedMemberVO.getIdx());
    }

    public void markEmailHistoryAsUsed(String certData) throws Exception {
        MemberEmailHistoryVO historyVO = emailHistoryMapper.selectByCertData(certData)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        emailHistoryMapper.updateUsedByIdx(true, historyVO.getIdx());
    }
}
