package com.illunex.emsaasrestapi.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponse<T> {
    @Builder.Default
    private int status = 0;
    private T data;
    private String message;
}
