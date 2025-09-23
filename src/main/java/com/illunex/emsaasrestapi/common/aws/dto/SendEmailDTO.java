package com.illunex.emsaasrestapi.common.aws.dto;

import com.illunex.emsaasrestapi.common.aws.AwsSESComponent;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import software.amazon.awssdk.services.sesv2.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
@Getter
@Builder
public class SendEmailDTO {
    // 보내는 사람
    private final String senderAddress;
    // 받는 사람
    private final String receiverAddress;
    // 제목
    private final String subject;
    // 인증 페이지 URL
    private final String certUrl;
    // 인증키
    private final String certData;
    // 문의 내용
    private final String content;
    // 이름
    private final String name;

    /**
     * 메일 템플릿 생성
     * @return
     * @throws IOException
     */
    public SendEmailRequest createSendEmailRequest(AwsSESComponent.EmailType emailType) throws IOException {
        ClassPathResource templateResource = new ClassPathResource(emailType.getTemplatePath());
        String bodyHtml = StreamUtils.copyToString(templateResource.getInputStream(), Charset.defaultCharset())
                .replace("${TO}", receiverAddress)
                .replace("${NAME}", name != null ? name : "")
                .replace("${URL}", certUrl + "?certData=" + URLEncoder.encode(certData, StandardCharsets.UTF_8));

        Destination destination = Destination.builder()
                .toAddresses(receiverAddress)
                .build();

        Content content = Content.builder()
                .data(bodyHtml)
                .build();

        Content subject = Content.builder()
                .data(this.subject)
                .build();

        Body body = Body.builder()
                .html(content)
                .build();

        Message message = Message.builder()
                .subject(subject)
                .body(body)
                .build();

        EmailContent emailContent = EmailContent.builder()
                .simple(message)
                .build();

        return SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(senderAddress)
                .build();
    }
}
