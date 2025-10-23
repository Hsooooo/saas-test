package com.illunex.emsaasrestapi.payment;

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

    /**
     * 토스 빌링키 발급
     * @param customerKey
     * @param authKey
     * @return
     * @throws IOException
     */
    public String issueBillingKey(String customerKey, String authKey) throws IOException {
        JSONObject requestData = new JSONObject().put("customerKey", customerKey).put("authKey", authKey);
        JSONObject response = sendRequest(requestData, tossSecretKey, "https://api.tosspayments.com/v1/billing/authorizations/issue");
//        if (!response.containsKey("error")) {
//            billingKeyMap.put((String) requestData.get("customerKey"), (String) response.get("billingKey"));
//        }

        return response.getString("billingKey");
    }

    /**
     * 토스 자동결제 승인
     * @param requestData
     * @throws IOException
     */
    public void confirmBilling(JSONObject requestData) throws IOException {
        confirmBillingValidation(requestData);
//        JSONObject response = sendRequest(requestData, tossSecretKey, "https://api.tosspayments.com/v1/billing/" + billingKey);
    }

    private void confirmBillingValidation(JSONObject requestData) {

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
