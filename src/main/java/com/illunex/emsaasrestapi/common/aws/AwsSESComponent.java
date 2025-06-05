package com.illunex.emsaasrestapi.common.aws;

import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.common.aws.dto.SendEmailDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsSESComponent {
    private final SesV2Client sesV2Client;
    // AES256 암호화 키
    @Value("${server.encrypt-key}")
    private String encryptKey;

    @Value("${spring.cloud.aws.ses.manager-email}")
    private String managerEmail;

    @AllArgsConstructor
    public enum EmailType {
        join("join", "[Em-Stock] 회원가입 메일", "templates/CertificationTemplate.html"),
        findPassword("password", "[Em-Stock] 비밀번호 변경 메일", "templates/FindPWMailTemplate.html"),
        invite("invite", "[Em-SAAS] 초대 메일", "templates/Invite.html"),
        inviteProject("inviteProject", "[Em-SAAS] 초대 메일", "templates/InviteProject.html");

        @Getter
        private final String value;
        @Getter
        private final String subject;
        @Getter
        private final String templatePath;

        public static EmailType stringToEnum(String stringCode) {
            for (EmailType value : values()) {
                if (value.value.equals(stringCode)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 회원가입 인증 메일 발송
     * @param listener
     * @param email
     * @return
     * @throws Exception
     */
    public String sendJoinEmail(AwsSESListener listener, String email) throws Exception {
        JSONObject certJson = new JSONObject()
                .put("type", EmailType.join.getValue())
                .put("email", email)
                .put("expire", ZonedDateTime.now().plusDays(1).toString());

        // 이메일 인증을 위한 암호화 - AES256 -> Base64
        String certData = Utils.AES256.encrypt(encryptKey, certJson.toString());

        final SendEmailDTO senderDto = SendEmailDTO.builder()
                .senderAddress(managerEmail)
                .receiverAddress(email)
                .subject(EmailType.join.getSubject())
//                .certUrl(certJoinUrl)
                .certData(certData)
                .build();

        // 이메일 템플릿 생성
        SendEmailRequest sendEmailRequest = senderDto.createSendEmailRequest(EmailType.join);

        SendEmailResponse sendEmailResponse = sesV2Client.sendEmail(sendEmailRequest);

        sendingResultMustSuccess(listener, sendEmailResponse, sendEmailRequest);

        return certData;
    }

    /**
     * 비밀번호 찾기 메일 전송
     * @param listener
     * @param email
     * @return
     * @throws Exception
     */
    public String sendFindPasswordEmail(AwsSESListener listener, String email) throws Exception {
        JSONObject certJson = new JSONObject()
                .put("type", EmailType.findPassword.getValue())
                .put("email", email)
                .put("expire", ZonedDateTime.now().plusHours(1).toString());

        // 이메일 인증을 위한 암호화 - AES256 -> Base64
        String certData = Utils.AES256.encrypt(encryptKey, certJson.toString());

        final SendEmailDTO senderDto = SendEmailDTO.builder()
                .senderAddress(managerEmail)
                .receiverAddress(email)
                .subject(EmailType.findPassword.getSubject())
//                .certUrl(certPasswordUrl)
                .certData(certData)
                .build();

        SendEmailRequest sendEmailRequest = senderDto.createSendEmailRequest(EmailType.findPassword);

        SendEmailResponse sendEmailResponse = sesV2Client.sendEmail(sendEmailRequest);

        sendingResultMustSuccess(listener, sendEmailResponse, sendEmailRequest);

        return certData;
    }

    /**
     * 회원 초대 메일 발송
     * @param listener
     * @param email
     * @param partnershipMemberIdx
     * @param memberIdx
     * @return
     * @throws Exception
     */
    public String sendInviteMemberEmail(AwsSESListener listener, String email, Integer partnershipMemberIdx, Integer memberIdx) throws Exception {
        JSONObject certJson = new JSONObject()
                .put("type", EmailType.invite.getValue())
                .put("partnershipMemberIdx", partnershipMemberIdx)
                .put("memberIdx", memberIdx)
                .put("expire", ZonedDateTime.now().plusHours(1).toString()); // 1시간?

        // 이메일 인증을 위한 암호화 - AES256 -> Base64
        String certData = Utils.AES256.encrypt(encryptKey, certJson.toString());

        final SendEmailDTO senderDto = SendEmailDTO.builder()
                .senderAddress(managerEmail)
                .receiverAddress(email)
                .subject(EmailType.invite.getSubject())
                .certData(certData)
                .build();

        SendEmailRequest sendEmailRequest = senderDto.createSendEmailRequest(EmailType.invite);

        SendEmailResponse sendEmailResponse = sesV2Client.sendEmail(sendEmailRequest);

        sendingResultMustSuccess(listener, sendEmailResponse, sendEmailRequest);

        return certData;
    }

    /**
     * 결과 처리 체크
     * @param sendEmailResponse
     */
    private void sendingResultMustSuccess(AwsSESListener listener, SendEmailResponse sendEmailResponse, SendEmailRequest sendEmailRequest) {
        if (sendEmailResponse == null || sendEmailResponse.sdkHttpResponse().statusCode() != 200) {
            log.error(Utils.getLogMaker(Utils.eLogType.SYSTEM), "SendEmail", sendEmailResponse.responseMetadata().toString());
            if(listener != null) {
                listener.onFail(sendEmailRequest.destination());
            }
        } else {
            if(listener != null) {
                listener.onSuccess(sendEmailRequest.destination());
            }
        }
    }
}
