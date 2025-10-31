package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.payment.dto.PgResultDTO;
import com.illunex.emsaasrestapi.payment.dto.ResponseTossPayDTO;
import com.illunex.emsaasrestapi.payment.vo.InvoiceVO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {
    @Value("${toss.secret-key}")
    private String tossSecretKey;
    @Value("${toss.client-key}")
    private String tossClientKey;
    @Value("${toss.url}")
    private String tossUrl;

    /**
     * 토스 빌링키 발급
     * @param customerKey
     * @param authKey
     * @return
     * @throws IOException
     */
    // TossPaymentService.java (핵심 변경)
    public JSONObject issueBillingKey(String customerKey, String authKey) throws IOException {
        JSONObject body = new JSONObject()
                .put("customerKey", customerKey)
                .put("authKey", authKey);
        ResponseTossPayDTO.TossApiResponse<String> res =
                sendHttpPost("/v1/billing/authorizations/issue", "POST", body);

        if (!res.isSuccess()) {
            throw new IOException("Toss issueBillingKey failed: " + res.getStatusCode() + " - " + res.getResponseData());
        }
        return new JSONObject(res.getResponseData());
    }

    /**
     * 토스 자동결제 승인
     * @param requestData
     * @throws IOException
     */
    public JSONObject confirmBilling(JSONObject requestData) throws IOException {
        // 필수값 검증
        confirmBillingValidation(requestData);

        String billingKey = requestData.getString("billingKey");
        // Toss API는 path에 billingKey를 넣고 body에서는 제거
        JSONObject body = new JSONObject(requestData.toString());
        body.remove("billingKey");

        ResponseTossPayDTO.TossApiResponse<String> res =
                sendHttpPost("/v1/billing/" + billingKey, "POST", body);

        if (!res.isSuccess()) {
            throw new IOException("Toss confirmBilling failed: " + res.getStatusCode() + " - " + res.getResponseData());
        }
        return new JSONObject(res.getResponseData());
    }

    private void confirmBillingValidation(JSONObject body) {
        // 최소 검증: Toss 문서 기준 orderId, amount, customerKey, billingKey
        if (!body.has("billingKey") || body.isNull("billingKey"))
            throw new IllegalArgumentException("billingKey required");
        if (!body.has("orderId") || body.isNull("orderId"))
            throw new IllegalArgumentException("orderId required");
        if (!body.has("amount") || body.isNull("amount"))
            throw new IllegalArgumentException("amount required");
        if (!body.has("customerKey") || body.isNull("customerKey"))
            throw new IllegalArgumentException("customerKey required");
        // 선택: 통화 고정
        if (body.has("currency") && !"KRW".equals(body.optString("currency")))
            throw new IllegalArgumentException("currency must be KRW");
    }

    /**
     * 토스페이먼츠 API 요청
     */
    public ResponseTossPayDTO.TossApiResponse<String> sendHttpPost(String getUrl, String method, JSONObject body) {
        String authorizations = "Basic " + Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpURLConnection connection = null;
        try {
            URL url = new URL(tossUrl + getUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            if (body != null) {
                // Body 전송
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }


            int statusCode = connection.getResponseCode();
            InputStream is = (statusCode >= 200 && statusCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            // 응답 본문 읽기
            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }
            return new ResponseTossPayDTO.TossApiResponse<String>() {{
                setSuccess(statusCode == 200);
                setStatusCode(statusCode);
                setResponseData(responseBuilder.toString());
            }};
        } catch (Exception e) {
            // 네트워크 오류 처리
            return new ResponseTossPayDTO.TossApiResponse<String>() {{
                setSuccess(false);
                setStatusCode(500);
                setResponseData(e.getMessage());
            }};
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = connection.getResponseCode(); // 200만 보지 말 것
        try (InputStream responseStream = (200 <= code && code < 300) ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return new JSONObject(new JSONTokener(reader));
        } catch (Exception e) {
            log.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

}
