package com.illunex.emsaasrestapi.common.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class SynologyWebhook {
    @Value("${webhook.synology.url}")
    private String url;

    /**
     * 시놀로지 웹훅 파라미터
     */
    @Value("${webhook.synology.api}")
    private String api;
    @Value("${webhook.synology.method}")
    private String method;
    @Value("${webhook.synology.version}")
    private String version;
    @Value("${webhook.synology.scrap.token}")
    private String token;

    @Value("${webhook.synology.use}")
    private Boolean isSynologyWebHookUse;

    private final ObjectMapper objectMapper;

    private final String mention = "@channel";

    /**
     * 웹훅 발송
     * @param title
     * @param synologyRequestDto
     * @throws JsonProcessingException
     */
    public void sendMessage(String title, SynologyRequestDto synologyRequestDto) throws JsonProcessingException {
        if(isSynologyWebHookUse) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("api", api)
                    .queryParam("method", method)
                    .queryParam("version", version)
                    .queryParam("token", token)
                    .build(true);

            synologyRequestDto.setText(String.format("%s\r\n%s\r\n%s", mention, title, synologyRequestDto.getText()));

            LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("payload", objectMapper.writeValueAsString(synologyRequestDto));

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> ret = restTemplate.exchange(uri.toUri(), HttpMethod.POST, entity, String.class);
            log.info(ret.getBody());
        }
    }
}
