package com.illunex.emsaasrestapi.common.code;

import com.illunex.emsaasrestapi.common.code.mapper.CodeMapper;
import com.illunex.emsaasrestapi.common.code.vo.CodeVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static class PartnershipInviteLink {
        /**
         * 파트너쉽 초대 링크 상태
         */
        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            DRAFT("PIS0001", "임시"),
            ACTIVE("PIS0002", "활성"),
            EXPIRE("PIS0003", "만료");

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
            Normal("PMS0001", "활성"),
            Stop("PMS0002", "비활성"),
            Delete("PMS0003", "삭제"),
            Wait("PMS0004", "대기중");

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
            Owner("PMT0001", "소유자"),
            Viewer("PMT0002", "뷰어"),
            Editor("PMT0003", "에디터");

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
            Select("PQT0001", "조회"),
            Update("PQT0002", "수정");

            private final String code;
            private final String value;
        }
    }

    public static class ChatRoom {
        /**
         * 프로젝트 쿼리 구분
         */
        @Getter
        @AllArgsConstructor
        public enum SenderType implements BaseCodeEnum {
            USER("RST0001", "USER"),
            GEMINI("RST0002", "GEMINI"),
            GPT("RST0003", "GPT");

            private final String code;
            private final String value;
        }
    }

    public static class ChatHistory {
        /**
         * 프로젝트 쿼리 구분
         */
        @Getter
        @AllArgsConstructor
        public enum CategoryType implements BaseCodeEnum {
            SIMPLE("CHT0001", "SIMPLE"),
            GENERAL("CHT0002", "GENERAL"),
            PROFESSIONAL("CHT0003", "PROFESSIONAL"),
            USER("CHT0004", "USER"),
            ERROR("CHT0005", "ERROR");

            private final String code;
            private final String value;

            public static String getCodeByValue(String value) {
                return Arrays.stream(CategoryType.values())
                        .filter(e -> e.getValue().equals(value))
                        .map(CategoryType::getCode)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 value: " + value));
            }
        }
    }

    public static class ChatToolResult {
        /**
         * Chat Tool Type
         */
        @Getter
        @AllArgsConstructor
        public enum ToolType implements BaseCodeEnum {
            QUERY_RESULT("CTT0001", "get_search_result_by_query_tool"),
            MCP("CTT0002", "MCP");

            private final String code;
            private final String value;
        }
    }

    /**
     * 채팅 관련 파일
     */
    public static class ChatFile {
        /**
         * 채팅 관련 파일 구분
         */
        @Getter
        @AllArgsConstructor
        public enum FileCd implements BaseCodeEnum {
            PPTX("CFC0001", "PPTX"),
            DOCS("PFC0002", "DOCS");

            private final String code;
            private final String value;
        }
    }

    /**
     * 제품 관련 코드
     */
    public static class Product {
        /**
         * 제품 코드
         */
        @Getter
        @AllArgsConstructor
        public enum ProductCd implements BaseCodeEnum {
            NetworkAnalysis("PPC0001", "Network Analysis"),
            GraphKnowledge("PPC0002", "Graph Knowledge");

            private final String code;
            private final String value;
        }

        /**
         * 제품 권한 코드
         */
        @Getter
        @AllArgsConstructor
        public enum ProductAuthCd implements BaseCodeEnum {
            EDITOR("PPG0001", "EDITOR"),
            VIEWER("PPG0002", "VIEWER");

            private final String code;
            private final String value;
        }
    }

    /**
     * 라이센스 관련 코드
     */
    public static class License {
        /**
         * 플랜 코드
         */
        @Getter
        @AllArgsConstructor
        public enum PlanCd implements BaseCodeEnum {
            BASIC("PLC0001", "Basic Plan"),
            ADVANCED("PLC0002", "Advanced Plan"),
            PREMIUM("PLC0003", "Premium Plan"),
            ENTERPRISE("PLC0004", "Enterprise Plan"),
            ;

            private final String code;
            private final String value;
        }
    }

    /**
     * 파트너쉽 라이센스 관련 코드
     */
    public static class LicensePartnership {
        /**
         * 상태 코드
         */
        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            DRAFT("LPS0001", "임시"),
            ACTIVE("LPS0002", "활성"),
            CHANGE("LPS0003", "변경 예약"),
            CANCEL("LPS0004", "해지 예약"),
            PAUSE("LPS0005", "일시 정지")
            ;

            private final String code;
            private final String value;
        }
    }

    /**
     * 파트너쉽 라이센스 관련 코드
     */
    public static class SubscriptionChangeEvent {
        /**
         * 구독 변경 타입 코드
         */
        @Getter
        @AllArgsConstructor
        public enum TypeCd implements BaseCodeEnum {
            ADD_SEAT("CET0001", "구성원 추가"),
            REMOVE_SEAT("CET0002", "구성원 제거"),
            PLAN_UPGRADE("CET0003", "플랜 업그레이드"),
            PLAN_DOWNGRADE("CET0004", "플랜 다운그레이드");

            private final String code;
            private final String value;
        }
    }

    /**
     * 청구서 관련 코드
     */
    public static class Invoice {
        /**
         * 청구서 상태 코드
         */
        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            DRAFT("ISC0001", "임시"),
            ISSUED("ISC0002", "발행"),
            PAID("ISC0003", "결제완료"),
            CANCELLED("ISC0004", "취소");

            private final String code;
            private final String value;
        }

        @Getter
        @AllArgsConstructor
        public enum TypeCd implements BaseCodeEnum {
            SUBSCRIPTION("IIT0001", "정기"),
            UPGRADE("IIT0002", "업그레이드"),;

            private final String code;
            private final String value;
        }
    }

    /**
     * 청구 항목 관련 코드
     */
    public static class InvoiceItem {
        /**
         * 청구 항목 타입 코드
         */
        @Getter
        @AllArgsConstructor
        public enum ItemTypeCd implements BaseCodeEnum {
            RECURRING("ITC0001", "정기결제액"),
            PRORATION("ITC0002", "일할요금"),
            FIX("ITC0003", "보정액"),
            ADJUST("ITC0004", "조정항목");

            private final String code;
            private final String value;
        }
    }

    public static class PaymentAttempt {
        /**
         * 결제 시도 상태 코드
         */
        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            SUCCESS("PAS0001", "성공"),
            FAILED("PAS0002", "실패"),
            PENDING("PAS0003", "대기중");

            private final String code;
            private final String value;
        }
    }

    public static class PaymentMethod {
        /**
         * 결제 수단 코드
         */
        @Getter
        @AllArgsConstructor
        public enum MethodTypeCd implements BaseCodeEnum {
            CARD("PMC0001", "신용/체크카드"),
            BANK("PMC0002", "계좌이체");

            private final String code;
            private final String value;
        }

        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            ACTIVE("PSC0001", "ACTIVE"),
            INACTIVE("PSC0002", "INACTIVE"),
            DELETED("PSC0003", "DELETED");

            private final String code;
            private final String value;
        }
    }

    public static class PaymentMandate {
        /**
         * PG사 코드
         */
        @Getter
        @AllArgsConstructor
        public enum ProviderCd implements BaseCodeEnum {
            TOSS("PGC0001", "Toss Payments");

            private final String code;
            private final String value;
        }

        @Getter
        @AllArgsConstructor
        public enum StatusCd implements BaseCodeEnum {
            ACTIVE("MDS0001", "ACTIVE"),
            REVOKED("MDS0001", "REVOKED"),
            EXPIRED("MDS0001", "EXPIRED"),;

            private final String code;
            private final String value;
        }
    }

    public static class KnowledgeGardenNode {
        /**
         * 지식정원 노드 타입 코드
         */
        @Getter
        @AllArgsConstructor
        public enum TypeCd implements BaseCodeEnum {
            NOTE("KNT0001", "노트"),
            FOLDER("KNT0002", "폴더"),
            KEYWORD("KNT0003", "키워드");

            private final String code;
            private final String value;
        }

        /**
         * 지식정원 노드 상태 코드
         *  - 초안
         *  - 리뷰중
         *  - 완료
         *  - 보류
         */
        @Getter
        @AllArgsConstructor
        public enum NoteStatusCode implements BaseCodeEnum {
            DRAFT("NSS0001", "초안"),
            REVIEW("NSS0002", "리뷰중"),
            COMPLETE("NSS0003", "완료"),
            HOLD("NSS0004", "보류");

            private final String code;
            private final String value;
        }

        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            ACTIVE("KNS0001", "활성"),
            TRASH("KNS0002", "휴지통");

            private final String code;
            private final String value;
        }
    }

    public static class KnowledgeGardenLink {
        @Getter
        @AllArgsConstructor
        public enum TypeCd implements BaseCodeEnum {
            TREE("KLT0001", "트리"),
            REF("KLT0002", "참조"),
            KEYWORD("KLT0003", "키워드"),
            SIMILARITY("KLT0004", "유사")
            ;

            private final String code;
            private final String value;
        }

        @Getter
        @AllArgsConstructor
        public enum StateCd implements BaseCodeEnum {
            ACTIVE("KLS0001", "활성"),
            TRASH("KLS0002", "휴지통");

            private final String code;
            private final String value;
        }
    }
}