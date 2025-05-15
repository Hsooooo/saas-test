package com.illunex.emsaasrestapi.cert;

import com.illunex.emsaasrestapi.cert.dto.RequestCertDTO;
import com.illunex.emsaasrestapi.common.CustomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("cert")
@Slf4j
public class CertController {
    private final CertService certService;

    /**
     * 검증키검증
     * @param request
     * @return
     */
    @PostMapping("verify")
    public CustomResponse<?> verify(@RequestBody RequestCertDTO.Cert request) throws Exception {
        return certService.verify(request.getCertData());
    }

    /**
     * 회원가입 인증
     * @param request
     * @throws Exception
     */
    @PostMapping("join")
    public CustomResponse<?> certificateJoin(@RequestBody RequestCertDTO.Cert request) throws Exception {
        return certService.certificateJoin(request.getCertData());
    }

    /**
     * 초대 승인을 통한 가입
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("invite/signup")
    public CustomResponse<?> certificateInviteSignup(@RequestBody RequestCertDTO.InviteSignup request) throws Exception {
        return certService.certificateInviteSignup(request);
    }

    /**
     * 초대 승인
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("invite/approve")
    public CustomResponse<?> certificateInviteApprove(@RequestBody RequestCertDTO.Cert request) throws Exception {
        return certService.certificateInviteApprove(request.getCertData());
    }


}
