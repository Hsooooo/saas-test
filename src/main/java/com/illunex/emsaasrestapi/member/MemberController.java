package com.illunex.emsaasrestapi.member;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.dto.RequestMemberDTO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("member")
@Slf4j
public class MemberController {
    private final MemberService memberService;

    /**
     * 이메일/도메인 중복 체크
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
     * @param join
     * @return
     */
    @PostMapping("join")
    public CustomResponse<?> join(@RequestBody @Valid RequestMemberDTO.Join join) throws Exception {
        return memberService.join(join);
    }

    /**
     * 로그인
     * @param request
     * @param login
     * @return
     */
    @PostMapping("login")
    public CustomResponse<?> login(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestBody RequestMemberDTO.Login login) throws CustomException {
        return memberService.login(request, response, login);
    }

    /**
     * TODO 약관 목록 조회
     * @return
     */
    @GetMapping("terms")
    public CustomResponse<?> getTermList() {
        return memberService.getTermList();
    }



    /**
     * 엑세스 토큰 갱신
     * @param request
     * @return
     */
    @PostMapping("reissue")
    public CustomResponse<?> reissue(HttpServletRequest request) throws CustomException {
        return memberService.reissue(request);
    }



    /**
     * TODO 회원가입 이메일 재전송
     * @param type
     * @param value
     * @return
     */
    @PostMapping("join/resend")
    public CustomResponse<?> resendJoinEmail(@RequestParam(name = "type") String type,
                                             @RequestParam(name = "value") String value) throws Exception {
        return memberService.resendJoinEmail(type, value);
    }

    /**
     * TODO 인증코드 체크
     * @param certData
     * @return
     * @throws Exception
     */
    @PostMapping("/check/cert")
    public CustomResponse<?> checkCertification(@RequestParam(name = "certData") String certData) throws Exception {
        return memberService.checkCertification(certData);
    }

    /**
     * TODO 비밀번호 찾기
     * @param findPassword
     * @return
     */
    @PostMapping("password")
    public CustomResponse<?> findPassword(@RequestBody RequestMemberDTO.FindPassword findPassword) throws Exception {
        return memberService.findPassword(findPassword);
    }

    /**
     * TODO 비밀번호 변경
     * @param certData
     * @param password
     */
    @PutMapping("password")
    public CustomResponse<?> changePassword(@RequestParam(name = "certData") String certData,
                                            @RequestParam(name = "password") String password) throws Exception {
        return memberService.changePassword(certData, password);
    }
}
