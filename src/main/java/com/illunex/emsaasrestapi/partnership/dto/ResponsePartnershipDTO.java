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

    @Builder
    @Getter
    public static class MyInfo {
        private PartnershipInfo partnership;
        private MyInfoMember member;
        private MyInfoPartnershipMember partnershipMember;
    }

    @Builder
    @Getter
    public static class MyInfoMember {
        private Integer idx;
        private String name;
    }

    @Builder
    @Getter
    public static class MyInfoPartnershipMember {
        private Integer idx;
        private String phone;
        private PartnershipPositionInfo positionInfo;
    }

    @Builder
    @Getter
    public static class PartnershipPositionInfo {
        private Integer idx;
        private String name;
        private Integer sortLevel;
    }

    @Builder
    @Getter
    public static class PartnershipInfo {
        private Integer idx;
        private String name;
        private String imageUrl;
        private String imagePath;
        private String domain;
    }
}
