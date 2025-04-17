package com.illunex.emsaasrestapi.partnership.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartnershipCreateDTO {
    @NotEmpty
    private String partnershipName;
    @NotEmpty
    private String domain;
}
