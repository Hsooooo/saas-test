package com.illunex.emsaasrestapi.license.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

public class ResponseLicenseDTO {

    @Getter
    @Setter
    public static class LicenseList {
        private List<License> licenseList;
    }

    @Getter
    @Setter
    public static class License {
        private Integer idx;
        private String planCd;
        private String planCdDesc;
        private String description;
        private BigDecimal pricePerUser;
        private Integer minUserCount;
        private Integer dataTotalLimit;
        private Integer projectCountLimit;
        private Integer periodMonth;
        private Integer versionNo;
        private Boolean active;

        public void setPlanCd(String planCd) {
            this.planCd = planCd;
            this.planCdDesc = EnumCode.getCodeDesc(planCd);
        }
    }
}
