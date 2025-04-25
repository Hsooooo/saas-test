package com.illunex.emsaasrestapi.partnership.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.common.validation.ValidEnumCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

public class RequestPartnershipDTO {

    @Getter
    public static class InviteMember {
        @Valid
        List<InviteMemberInfo> inviteMembers;
    }

    @Getter
    public static class UpdateMyInfo {
        private String name;
        private String position;
        private String phone;
    }

    @Valid
    @Getter
    public static class InviteMemberInfo {
        @NotNull
        private String email;
        @ValidEnumCode(enumClass = EnumCode.Partnership.ManagerCd.class, message = "유효하지 않은 권한 코드입니다.")
        private String auth;
    }
}
