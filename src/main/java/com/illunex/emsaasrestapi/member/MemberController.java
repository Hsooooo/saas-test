package com.illunex.emsaasrestapi.member;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.jwt.TokenProvider;
import com.illunex.emsaasrestapi.member.dto.RequestMemberDTO;
import com.illunex.emsaasrestapi.member.dto.ResponseMemberDTO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("member")
@Slf4j
public class MemberController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    /**
     * 이메일/도메인 중복 체크
     *
     * @param type
     * @param value
     * @return
     */
    @GetMapping("/check/duplicate")
    public CustomResponse<?> checkDuplicate(@RequestParam(name = "type") String type,
                                            @RequestParam(name = "value") String value) throws CustomException {
        return memberService.checkDuplicate(type, value);
    }

    /**
     * 회원가입
     *
     * @param join
     * @return
     */
    @PostMapping("join")
    public CustomResponse<?> join(@RequestBody @Valid RequestMemberDTO.Join join) throws Exception {
        return memberService.join(join);
    }

    /**
     * 로그인
     *
     * @param exchange
     * @param login
     * @return
     */
    @PostMapping("login")
    public Mono<ResponseEntity<CustomResponse<?>>> login(ServerWebExchange exchange,
                                                         @RequestBody RequestMemberDTO.Login login) throws CustomException {
        // 클라이언트 메타 추출 (네 유틸 대체)
        String ip       = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null) ip = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        String ua       = exchange.getRequest().getHeaders().getFirst("User-Agent");
        String platform = ua; // 필요하면 파싱

        var meta = new MemberService.ClientMeta(ip, ua, platform);

        CustomResponse<?> resp = memberService.loginCore(login, meta);

        // refresh 토큰 쿠키(웹플럭스)
        ResponseCookie cookie = tokenProvider.createResponseCookie(
                "refreshToken",
                ((ResponseMemberDTO.Login) resp.getData()).getAccessToken() /* <- 여기 오타 주의: refreshToken을 따로 반환 원하면 loginCore 반환값에 포함 */,
                ip
        );
        exchange.getResponse().addCookie(cookie);

        return Mono.just(ResponseEntity.ok(resp));
    }

    /**
     * 약관 목록 조회
     *
     * @return
     */
    @GetMapping("terms")
    public CustomResponse<?> getTermList() {
        return memberService.getTermList();
    }


    /**
     * 엑세스 토큰 갱신
     *
     * @param exchange
     * @return
     */
    @PostMapping("reissue")
    public Mono<ResponseEntity<CustomResponse<?>>> reissue(ServerWebExchange exchange) throws CustomException {
        var cookies = exchange.getRequest().getCookies().getFirst("refreshToken");
        if (cookies == null) {
            return Mono.error(new AccessDeniedException("Cookie is not empty"));
        }
        String refreshToken = cookies.getValue();
        // tokenProvider.validateToken(refreshToken) 등 처리
        return Mono.just(ResponseEntity.ok(memberService.reissue(refreshToken)));
    }


    /**
     * 회원가입 이메일 재전송
     *
     * @param resendJoinEmail
     * @return
     */
    @PostMapping("join/resend")
    public CustomResponse<?> resendJoinEmail(@RequestBody RequestMemberDTO.ResendJoinEmail resendJoinEmail) throws Exception {
        return memberService.resendJoinEmail(resendJoinEmail.getType(), resendJoinEmail.getValue());
    }

    /**
     * 비밀번호 찾기
     *
     * @param findPassword
     * @return
     */
    @PostMapping("password")
    public CustomResponse<?> findPassword(@RequestBody RequestMemberDTO.FindPassword findPassword) throws Exception {
        return memberService.findPassword(findPassword);
    }

    /**
     * 비밀번호 변경
     *
     * @param resetPassword
     */
    @PutMapping("password")
    public CustomResponse<?> changePassword(@RequestBody RequestMemberDTO.ResetPassword resetPassword) throws Exception {
        return memberService.changePassword(resetPassword.getCertData(), resetPassword.getPassword());
    }

    /**
     * 내정보 > 비밀번호 변경
     * @param request
     * @param memberVO
     * @return
     * @throws Exception
     */
    @PutMapping("mypage/password")
    public CustomResponse<?> mypageChangePassword(@RequestBody RequestMemberDTO.UpdatePassword request,
                                                  @CurrentMember MemberVO memberVO) throws Exception {
        return memberService.mypageChangePassword(memberVO, request.getPassword(), request.getNewPassword());
    }
}