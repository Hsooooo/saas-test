package com.illunex.emsaasrestapi.member;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.BaseCodeEnum;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.illunex.emsaasrestapi.common.code.EnumCode.Member.StateCd.*;

@Component
public class MemberComponent {
    /**
     * 회원 승인 상태 체크
     * @param stateCd
     * @return
     * @throws CustomException
     */
    public void checkMemberState(String stateCd) throws CustomException {
        switch(Objects.requireNonNull(BaseCodeEnum.fromCode(EnumCode.Member.StateCd.class, stateCd))) {
            // 미인증
            case Wait -> throw new CustomException(ErrorCode.MEMBER_STATE_WAIT);
            // 정지
            case Suspend -> throw new CustomException(ErrorCode.MEMBER_STATE_SUSPEND);
            // 탈퇴
            case Withdrawal -> throw new CustomException(ErrorCode.MEMBER_STATE_WITHDRAWAL);
            // 승인
            case Approval -> {}
            // 그 외에 에러
            default -> throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 요청 클라이언트 아이피 추출
     * @param request
     * @return
     */
    public static String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 요청 클라이언트 사용 브라우저 추출
     * @param request
     * @return
     */
    public static String getClientPlatform(HttpServletRequest request) {
        String browser = "";
        String userBrowser = request.getHeader("User-Agent");

        if(userBrowser.contains("Trident")) {												// IE
            browser = "ie";
        } else if(userBrowser.contains("Edge")) {											// Edge
            browser = "edge";
        } else if(userBrowser.contains("Whale")) { 										// Naver Whale
            browser = "whale";
        } else if(userBrowser.contains("Opera") || userBrowser.contains("OPR")) { 		// Opera
            browser = "opera";
        } else if(userBrowser.contains("Firefox")) { 										 // Firefox
            browser = "firefox";
        } else if(userBrowser.contains("Safari") && !userBrowser.contains("Chrome")) {	 // Safari
            browser = "safari";
        } else if(userBrowser.contains("Chrome")) {										 // Chrome
            browser = "chrome";
        }
        return browser;
    }

    /**
     * 디바이스 환경 추출
     * @param request
     * @return
     */
    public static String getClientDevice(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
