package com.illunex.emsaasrestapi.cert;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.mapper.EmailHistoryMapper;
import com.illunex.emsaasrestapi.member.vo.MemberEmailHistoryVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class CertComponent {
    private final EmailHistoryMapper emailHistoryMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;

    @Value("${server.encrypt-key}")
    private String encryptKey;

    public JSONObject verifyCertData(String certData) throws Exception {
        String decrypted = Utils.AES256.decrypt(encryptKey, certData);
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

    public void approvePartnershipMember(JSONObject data) throws Exception {
        int partnershipMemberIdx = data.getInt("partnershipMemberIdx");
        PartnershipMemberVO partnershipMember = partnershipMemberMapper.selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        if (!EnumCode.PartnershipMember.StateCd.Wait.getCode().equals(partnershipMember.getStateCd())) {
            throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID);
        }

        partnershipMemberMapper.updatePartnershipMemberStateByIdx(
                partnershipMemberIdx,
                EnumCode.PartnershipMember.StateCd.Normal.getCode()
        );
    }

    public void markEmailHistoryAsUsed(String certData) throws Exception {
        MemberEmailHistoryVO historyVO = emailHistoryMapper.selectByCertData(certData)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        emailHistoryMapper.updateUsedByIdx(true, historyVO.getIdx());
    }
}
