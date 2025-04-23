package com.illunex.emsaasrestapi.partnership.dto;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class ResponsePartnershipDTO {
    @Builder
    @Getter
    public static class InviteMember {
        List<InviteResult> valid;
        List<InviteResult> inValid;
    }

    @Builder
    @Getter
    public static class InviteResult {
        private String email;
        private String result;
        private String reason;
    }

    @Builder
    @Getter
    public static class MemberPreview {
        private Integer memberIdx;
        private String name;
        private String profileImageUrl;
        private String profileImagePath;
    }
}
