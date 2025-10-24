package com.illunex.emsaasrestapi.partnership.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
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
        private String email;
        private String name;
    }

    @Builder
    @Getter
    public static class MyInfoPartnershipMember {
        private Integer idx;
        private String phone;
        private String managerCd;
        private String managerCdDesc;
        private PartnershipPositionInfo positionInfo;
        private String profileImageUrl;
        private String profileImagePath;

        public void setManagerCd(String managerCd) {
            this.managerCd = managerCd;
            this.managerCdDesc = EnumCode.getCodeDesc(managerCd);
        }
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

    @Getter
    @Setter
    public static class PartnershipMember {
        private Integer partnershipMemberIdx;
        private String name;
        private String profileImageUrl;
        private String profileImagePath;
        private String email;
        private String stateCd;
        private String stateCdDesc;
        private String managerCd;
        private String managerCdDesc;
        private String teamName;
        private Integer partnershipTeamIdx;
        private String inviteMemberName;
        private Integer invitedByPartnershipMemberIdx;
        private ZonedDateTime invitedDate;
        private ZonedDateTime joinedDate;

        public void setStateCd(String stateCd) {
            this.stateCd = stateCd;
            this.stateCdDesc = EnumCode.getCodeDesc(stateCd);
        }

        public void setManagerCd(String managerCd) {
            this.managerCd = managerCd;
            this.managerCdDesc = EnumCode.getCodeDesc(managerCd);
        }
    }
}

