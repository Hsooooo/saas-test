package com.illunex.emsaasrestapi.common.code;

import com.illunex.emsaasrestapi.common.code.mapper.CodeMapper;
import com.illunex.emsaasrestapi.common.code.vo.CodeVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class EnumCode {
    private static LinkedHashMap<String, String> codeMap;
    private static CodeMapper codeMapper;

    @Bean
    public EnumCode init(CodeMapper mapper){
        if(codeMap == null || codeMap.size() == 0){
            codeMap = new LinkedHashMap<>();
            codeMapper = mapper;
        }
        return this;
    }

    private static void setCodeMap(){
        if(codeMap.size() == 0) {
            List<CodeVO> codeList = codeMapper.selectAll();
            codeList.forEach(code -> codeMap.put(code.getCode(), code.getCodeValue()));
        }
    }

    public static String getCodeDesc(String code){
        setCodeMap();
        return codeMap.get(code);
    }

    public static String[] getCodeValues(String[] codes){
        List<String> codeList = new ArrayList<>();
        for (String s : codes) {
            if(!s.equals("")) {
                codeList.add(codeMap.get(s.trim()));
            }
        }
        return codeList.toArray(new String[codeList.size()]);
    }

    public static void reloadCodeMap(){
        if(codeMap != null){
            codeMap.clear();
        }
        setCodeMap();
    }

    /**
     * 회원
     */
    public static class Member {
        /**
         * 회원 구분
         */
        @Getter
        @AllArgsConstructor
        public enum TypeCd implements BaseCodeEnum {
            Normal("MTP0001", "일반회원");

            private final String code;
            private final String value;
        }

        /**
         * 회원 상태
         */
        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            Wait("MST0001", "인증대기"),
            Approval("MST0002", "인증완료"),
            Suspend("MST0003", "정지"),
            Withdrawal("MST0004", "탈퇴");

            private final String code;
            private final String value;
        }
    }

    /**
     * 파트너쉽
     */
    public static class PartnershipMember {
        /**
         * 파트너쉽 회원 구분
         */
        @Getter
        @AllArgsConstructor
        public enum ManagerCd implements BaseCodeEnum {
            Manager("PST0001", "관리자"),
            Normal("PST0002", "일반");

            private final String code;
            private final String value;

        }

        /**
         * 파트너쉽 회원 상태
         */
        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            Normal("PMS0001", "정상"),
            Stop("PMS0002", "정지"),
            Delete("PMS0003", "삭제"),
            Wait("PMS0004", "대기");

            private final String code;
            private final String value;
        }
    }

    /**
     * 프로젝트
     */
    public static class Project {
        /**
         * 프로젝트 상태
         */
        @Getter
        @AllArgsConstructor
        public enum StatusCd implements BaseCodeEnum {
            Created("PJS0001", "생성/기본정보 입력"),
            Step1("PJS0002", "엑셀 업로드"),
            Step2("PJS0003", "노드/엣지 정의"),
            Step3("PJS0004", "기능 정의"),
            Step4("PJS0005", "속성 정의"),
            Step5("PJS0006", "노드/엣지 정제중"),
            Fail("PJS0007", "정제 오류"),
            Complete("PJS0008", "설정 완료");

            private final String code;
            private final String value;
        }
    }

    /**
     * 프로젝트 구성원
     */
    public static class ProjectMember {
        /**
         * 프로젝트 사용자 구분
         */
        @Getter
        @AllArgsConstructor
        public enum TypeCd implements BaseCodeEnum {
            Manager("PMT0001", "관리자"),
            Normal("PMT0002", "구성원");

            private final String code;
            private final String value;
        }
    }

    /**
     * 이메일
     */
    public static class Email {
        /**
         * 이메일 구분
         */
        @Getter
        @AllArgsConstructor
        public enum TypeCd {
            JoinEmail("ETP0001", "회원가입 인증"),
            FindPasswordEmail("ETP0002", "비밀번호 찾기"),
            InviteProject("ETP0003", "프로젝트 초대"),
            InvitePartnership("ETP0004", "파트너쉽 초대");


            private final String code;
            private final String value;
        }
    }

    /**
     * 프로젝트 업로드 파일
     */
    public static class ProjectFile {
        /**
         * 프로젝트 업로드 파일 구분
         */
        @Getter
        @AllArgsConstructor
        public enum FileCd implements BaseCodeEnum {
            Single("PFC0001", "단일 파일"),
            MultipleNode("PFC0002", "멀티 파일(노드)"),
            MultipleEdge("PFC0003", "멀티 파일(엣지)");

            private final String code;
            private final String value;
        }
    }

    public static class ProjectTable {
        /**
         * 프로젝트 테이블 구분
         */
        @Getter
        @AllArgsConstructor
        public enum TypeCd implements BaseCodeEnum {
            Node("PTT0001", "node"),
            Edge("PTT0002", "edge");

            private final String code;
            private final String value;
        }
    }

    public static class ProjectTableAuth {
        /**
         * 프로젝트 테이블 권한
         */
        @Getter
        @AllArgsConstructor
        public enum AuthCd implements BaseCodeEnum {
            Read("PTA0001", "읽기"),
            Write("PTA0002", "쓰기");

            private final String code;
            private final String value;
        }
    }

    public static class ProjectQuery {
        /**
         * 프로젝트 쿼리 구분
         */
        @Getter
        @AllArgsConstructor
        public enum TypeCd implements BaseCodeEnum {
            Mongo_Shell("PQT0001", "mongo-shell"),
            Mongo_JSON("PQT0002", "mongo-JSON");

            private final String code;
            private final String value;
        }
    }
}