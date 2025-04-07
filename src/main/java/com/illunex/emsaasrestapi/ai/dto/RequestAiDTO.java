package com.illunex.emsaasrestapi.ai.dto;

public class RequestAiDTO {
    /**
     * 안정성 혁신성 그래프 타입
     */
    public enum InnovationType {
        all,                    // 전종목
        kospi,                  // 코스피
        kosdaq,                 // 코스닥
        industry,               // 동일업종
        theme                   // 동일테마
    }
}
