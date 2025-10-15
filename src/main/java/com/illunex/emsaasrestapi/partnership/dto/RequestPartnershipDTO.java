package com.illunex.emsaasrestapi.partnership.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.common.validation.ValidEnumCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RequestPartnershipDTO {

    @Getter
    public static class InviteMember {
        @NotNull(message = "이메일은 필수 입력값입니다.")
        private String emails;
        private InviteInfo inviteInfo;
    }

    @Getter
    public static class UpdateMyInfo {
        private String name;
        private String position;
        private String phone;
    }

    @Getter
    public static class AdditionalInfo {
        // 방문 목적(안녕하세요? 무슨 일로 찾아오셨나요?)
        private String purpose;
        // 직무(직무를 선택해주세요.)
        private String position;
        // 팀규모(팀의 규모를 선택해주세요.)
        private String teamSize;
        // 회사규모(회사의 규모를 선택해주세요.)
        private String companySize;
        //우선관리사항 (어떤 관리 사항을 우선적으로 하고 싶은지 알려주세요)
        private String managementItem;
        //집중분야 (집중하실 분야를 선택해주세요)
        private String focusTopic;
        // 유입경로 (마지막으로, 저희를 어떻게 알게 되었나요?)
        private String referrer;
    }

    @Getter
    public static class ApproveInvite {
        private String inviteToken;
    }

    @Getter
    public static class InviteInfo {
        @NotNull
        private String inviteToken;
        @ValidEnumCode(enumClass = EnumCode.PartnershipMember.ManagerCd.class, message = "유효하지 않은 권한 코드입니다.")
        private String auth;
        private List<String> products;
    }

    @Setter
    @Getter
    public static class SearchMember {
        private Integer partnershipIdx;
        private String searchString;
    }
}
