package com.illunex.emsaasrestapi.common.webhook;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SynologyRequestDto {
    private String text;
}
