package com.illunex.emsaasrestapi.project.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Setter
@Getter
@Alias("ProjectMemberViewVO")
public class ProjectMemberViewVO {
    // 프로젝트 구성원 뷰
    // 프로젝트 구성원 번호 (project_member)
    private Integer projectMemberIdx;
    // 프로젝트 번호 (project_member)
    private Integer projectIdx;
    // 회원 번호 (member)
    private Integer memberIdx;
    // 파트너십 회원 번호 (partnership_member)
    private Integer partnershipMemberIdx;
    // 파트너쉽 회원 상태 (partnership_member)
    private String stateCd;
    // 이메일 (member)
    private String email;
    // 이름 (member)
    private String name;
    // 프로젝트 권한 (project_member)
    private String typeCd;
    // 프로필 이미지 URL (partnership_member)
    private String profileImageUrl;
    // 프로필 이미지 경로 (partnership_member)
    private String profileImagePath;
}
