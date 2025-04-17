package com.illunex.emsaasrestapi.member.dto;


import com.illunex.emsaasrestapi.partnership.dto.PartnershipCreateDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

public class RequestMemberDTO {
    /**
     * 회원가입 정보
     */
    @Getter
    public static class Join {
        private String email;
        private String name;
        private String password;
        @NotNull
        private PartnershipCreateDTO partnership;
        List<MemberTermAgree> memberTermAgreeList;
    }

    /**
     * 비밀번호 변경
     */
    @Getter
    public static class FindPassword {
        private String email;
    }

    /**
     * 회원정보 수정
     */
    @Getter
    public static class UpdateMember {
        private String nickname;
        private String comment;
    }

    /**
     * 로그인 정보
     */
    @Getter
    public static class Login {
        private String email;
        private String password;
    }

    /**
     * 약관 동의
     */
    @Getter
    public static class MemberTermAgree {
        private Integer memberTermIdx;
        private Boolean agree;
    }
}
