package com.illunex.emsaasrestapi.member;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.common.aws.AwsSESComponent;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.common.jwt.TokenProvider;
import com.illunex.emsaasrestapi.config.SecurityConfig;
import com.illunex.emsaasrestapi.member.dto.RequestMemberDTO;
import com.illunex.emsaasrestapi.member.dto.ResponseMemberDTO;
import com.illunex.emsaasrestapi.member.mapper.EmailHistoryMapper;
import com.illunex.emsaasrestapi.member.mapper.LoginHistoryMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberJoinMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberMapper;
import com.illunex.emsaasrestapi.member.vo.MemberEmailHistoryVO;
import com.illunex.emsaasrestapi.member.vo.MemberLoginHistoryVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipService;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    private final PartnershipService partnershipService;

//    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final MemberJoinMapper memberJoinMapper;
    private final PartnershipMapper partnershipMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final LoginHistoryMapper loginHistoryMapper;
    private final EmailHistoryMapper emailHistoryMapper;

    private final AwsSESComponent awsSESComponent;
    private final AwsS3Component awsS3Component;
    private final MemberComponent memberComponent;

    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    // AES256 암호화 키
    @Value("${server.encrypt-key}")
    private String encryptKey;

    /**
     * 약관 목록 조회
     */
    public CustomResponse<?> getTermList() {
//        List<MemberTerm> memberTermList = memberTermRepository.findAllByActiveTrue();
//        return CustomResponse.builder()
//                .data(modelMapper.map(memberTermList, new TypeToken<List<ResponseMemberDTO.Term>>(){}.getType()))
//                .build();
        return CustomResponse.builder()
                .data(null)
                .build();
    }

    /**
     * 로그인
     * @param login
     * @return
     */
    public CustomResponse<?> login(HttpServletRequest request, HttpServletResponse response, RequestMemberDTO.Login login) throws CustomException {
        // 아이디 확인
        MemberVO member = memberMapper.selectByEmail(login.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_EMPTY_ACCOUNT));

        // 회원 상태 체크
        memberComponent.checkMemberState(member.getStateCd());

        if(passwordEncoder.matches(login.getPassword(), member.getPassword())) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(SecurityConfig.MEMBER));
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(member.getEmail(), member.getPassword(), authorities);

            // 토큰 생성 후 응답 값에 추가
            ResponseMemberDTO.Login responseLoginDto = modelMapper.map(member, ResponseMemberDTO.Login.class);
            responseLoginDto.setAccessToken(tokenProvider.generateAccessToken(auth));

            // 로그인 이력 저장
            MemberLoginHistoryVO loginHistoryVO = new MemberLoginHistoryVO();
            loginHistoryVO.setMemberIdx(member.getIdx());
            loginHistoryVO.setBrowser(MemberComponent.getClientPlatform(request));
            loginHistoryVO.setPlatform(MemberComponent.getClientDevice(request));
            loginHistoryVO.setIp(MemberComponent.getClientIpAddr(request));
            loginHistoryMapper.insertLoginHistory(loginHistoryVO);

            // 리프레시 토큰 쿠키 등록
            response.addCookie(tokenProvider.createCookie("refreshToken", tokenProvider.generateRefreshToken(auth), MemberComponent.getClientIpAddr(request)));

            return CustomResponse.builder()
                    .data(responseLoginDto)
                    .build();
        } else {
            throw new CustomException(ErrorCode.MEMBER_NOT_MATCH_PASSWORD);
        }
    }

    /**
     * 엑세스 토큰 갱신
     * @param request
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> reissue(HttpServletRequest request) throws CustomException {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            throw new AccessDeniedException("Cookie is not empty");
        }
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
            }
        }

        // 토큰 유효성 체크
        if(refreshToken != null && !tokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.COMMON_FAIL_AUTHENTICATION);
        }

        Authentication authentication = tokenProvider.getAuthentication(refreshToken);

        // 토큰 생성 후 응답 값에 추가
        ResponseMemberDTO.Login response = new ResponseMemberDTO.Login();
        response.setEmail(authentication.getName());
        response.setAccessToken(tokenProvider.generateAccessToken(authentication));

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 회원가입
     * @param joinData
     * @return
     */
    @Transactional
    public CustomResponse<?> join(RequestMemberDTO.Join joinData) throws Exception {
        // 가입 체크
        if(memberMapper.selectByEmail(joinData.getEmail()).isPresent()) {
            // 가입된 회원 있음.
            throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        // 약관 체크 TODO
//        List<MemberTerm> memberTermList = memberTermRepository.findAllByActiveTrue();
//        for (MemberTerm memberTerm : memberTermList) {
//            joinData.getMemberTermAgreeList().stream()
//                    .filter(memberTermAgree -> memberTermAgree.getMemberTermIdx().equals(memberTerm.getIdx()))
//                    .findFirst()
//                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
//        }

        // 회원 등록
        MemberVO member = modelMapper.map(joinData, MemberVO.class);
        // 일반 회원
        member.setTypeCd(EnumCode.Member.TypeCd.Normal.getCode());
        // 승인 대기 상태
        member.setStateCd(EnumCode.Member.StateCd.Wait.getCode());
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        //회원정보 생성
        memberJoinMapper.insertByMemberJoin(member);

        //파트너십 생성
        partnershipService.createPartnership(joinData.getPartnership(), member.getIdx());

        // 약관 동의 저장
//        List<MemberTermAgree> memberMemberTermAgreeList = new ArrayList<>();
//        for (RequestMemberDTO.MemberTermAgree inputMemberTermAgree : joinData.getMemberTermAgreeList()) {
//            memberMemberTermAgreeList.add(com.illunex.emsaasrestapi.member.entity.MemberTermAgree.builder()
//                    .agree(inputMemberTermAgree.getAgree())
//                    .memberTerm(memberTermRepository.findById(inputMemberTermAgree.getMemberTermIdx()).orElseThrow())
//                    .member(member)
//                    .build());
//        }
//        memberTermAgreeRepository.saveAll(memberMemberTermAgreeList);
//
//        // 회원가입 이메일 발송
        String certData = awsSESComponent.sendJoinEmail(
                null,
                joinData.getEmail());
//
//        // 회원가입 인증 메일 이력 저장
        MemberEmailHistoryVO emailHistoryVO = new MemberEmailHistoryVO();
        emailHistoryVO.setMemberIdx(member.getIdx());
        emailHistoryVO.setCertData(certData);
        emailHistoryVO.setUsed(false);
        emailHistoryVO.setEmailType(EnumCode.Email.TypeCd.JoinEmail.getCode());
        emailHistoryVO.setExpireDate(ZonedDateTime.now().plusHours(1)); //1시간?

        emailHistoryMapper.insertByMemberEmailHistoryVO(emailHistoryVO);

        return CustomResponse.builder()
                .data(null)
                .build();
    }

    /**
     * 회원가입 인증 메일 재전송
     * @param type
     * @param value
     * @return
     */
    public CustomResponse<?> resendJoinEmail(String type, String value) throws Exception {
//        String email;
//        switch (type) {
//            case "email" -> email = value;
//            case "expire" -> {
//                JSONObject data = new JSONObject(Utils.AES256.decrypt(encryptKey, URLDecoder.decode(value, StandardCharsets.UTF_8)));
//                email = data.getString("email");
//            }
//            default -> throw new CustomException(ErrorCode.COMMON_INVALID);
//        }
//
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_FAIL_AUTHENTICATION));
//
//        if(!member.getStateCd().equals(EnumCode.Member.StateCd.Wait.getCode())) {
//            // 인증대기 상태가 아닐 경우
//            throw new CustomException(ErrorCode.MEMBER_ALREADY_EMAIL_CERTIFICATE);
//        }
//
//        // 회원가입 인증 이메일 발송
//        String certData = awsSESComponent.sendJoinEmail(
//                null,
//                member.getEmail());
//
//        // 비밀번호 찾기 메일 이력 저장
//        MemberEmailHistory memberEmailHistory = new MemberEmailHistory();
//        memberEmailHistory.setMemberIdx(member.getIdx());
//        memberEmailHistory.setCertData(certData);
//        memberEmailHistory.setUsed(false);
//        memberEmailHistory.setEmailType(EnumCode.Email.TypeCd.JoinEmail.getCode());
//        memberEmailHistory.setExpireDate(ZonedDateTime.now().plusHours(1));
//        memberPasswordHistoryRepository.save(memberEmailHistory);

        return CustomResponse.builder()
                .data(null)
                .build();
    }

    /**
     * 회원가입 인증
     * @param certData
     * @return
     */
    @Transactional
    public CustomResponse<?> certificateJoin(String certData) throws Exception {
        String decrypted = Utils.AES256.decrypt(encryptKey, certData);
        JSONObject data = new JSONObject(decrypted);
        ZonedDateTime expireDate = ZonedDateTime.parse(data.getString("expire"));

        // 회원가입용 인증키 확인
        if(!data.getString("type").equals(AwsSESComponent.EmailType.join.getValue())) {
            throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID);
        }

        // 회원 확인
        MemberVO member = memberMapper.selectByEmail(data.getString("email"))
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        if(!member.getStateCd().equals(EnumCode.Member.StateCd.Wait.getCode())) {
            // 인증대기 상태가 아닐 경우
            throw new CustomException(ErrorCode.COMMON_ALREADY_EMAIL_CERTIFICATE);
        }

        // 인증키 유효 체크
        MemberEmailHistoryVO history = emailHistoryMapper.selectTop1ByMemberIdxAndEmailTypeOrderByCreateDateDesc(member.getIdx(), EnumCode.Email.TypeCd.JoinEmail.getCode())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_INVALID));

        // 인증 만료 체크
        if(expireDate.isBefore(ZonedDateTime.now())
                || history.getExpireDate().isBefore(ZonedDateTime.now())
                || !history.getCertData().equals(certData)
                || history.isUsed()) {
            throw new CustomException(ErrorCode.COMMON_EMAIL_CERTIFICATE_EXPIRE);
        }

        // 인증키 사용 처리
        emailHistoryMapper.updateUsedByIdx(true, history.getIdx());

        // 회원 상태 변경
        member.setStateCd(EnumCode.Member.StateCd.Approval.getCode());

        memberMapper.updateMemberStateByIdx(member.getIdx(), EnumCode.Member.StateCd.Approval.getCode());

        return CustomResponse.builder()
                .build();
    }


    /**
     * 이메일/도메인 중복 체크
     * @param type
     * @param value
     * @return
     */
    public CustomResponse<?> checkDuplicate(String type, String value) throws CustomException {
        switch(type) {
            case "email" :
                if(memberMapper.selectByEmail(value).isPresent()) {
                    throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS);
                }
                break;
            case "domain" :
                if(partnershipMapper.selectByDomain(value).isPresent()) {
                    throw new CustomException(ErrorCode.PARTNERSHIP_DOMAIN_DUPLICATE);
                }
                break;
            default:
                throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        return CustomResponse.builder()
                .build();
    }

    /**
     * 인증코드 유효 체크
     */
    public CustomResponse<?> checkCertification(String certData) throws Exception {
        JSONObject data = new JSONObject(Utils.AES256.decrypt(encryptKey, URLDecoder.decode(certData, StandardCharsets.UTF_8)));
//        AwsSESComponent.EmailType emailType = AwsSESComponent.EmailType.stringToEnum(data.getString("type"));
//        ZonedDateTime expireDate = ZonedDateTime.parse(data.getString("expire"));
//
//        // 회원 확인
//        Member member = memberRepository.findByEmail(data.getString("email"))
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_EMAIL_CERTIFICATE_INVALID));
//
//        String typeCd;
//        switch (emailType) {
//            case join -> {
//                typeCd = EnumCode.Email.TypeCd.JoinEmail.getCode();
//            }
//            case findPassword -> {
//                typeCd = EnumCode.Email.TypeCd.FindPasswordEmail.getCode();
//            }
//            default -> throw new CustomException(ErrorCode.MEMBER_EMAIL_CERTIFICATE_INVALID);
//        }
//
//        // 인증키 유효 체크
//        MemberEmailHistory history = memberPasswordHistoryRepository.findTop1ByMemberIdxAndEmailTypeOrderByCreateDateDesc(member.getIdx(), typeCd)
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_EMAIL_CERTIFICATE_INVALID));
//
//        // 인증 만료 체크
//        if(expireDate.isBefore(ZonedDateTime.now())
//                || history.getExpireDate().isBefore(ZonedDateTime.now())
//                || !history.getCertData().equals(certData)
//                || history.getUsed().equals(true)) {
//            throw new CustomException(ErrorCode.MEMBER_EMAIL_CERTIFICATE_EXPIRE);
//        }

        return CustomResponse.builder()
                .build();
    }

    /**
     * 비밀번호 찾기
     * @param findPasswordData
     * @return
     */
    public CustomResponse<?> findPassword(RequestMemberDTO.FindPassword findPasswordData) throws Exception {
//        Member member = memberRepository.findByEmail(findPasswordData.getEmail())
//                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_FAIL_AUTHENTICATION));
//
//        // 비밀번호 변경 이메일 발송
//        String certData = awsSESComponent.sendFindPasswordEmail(
//                null,
//                member.getEmail());
//
//        // 비밀번호 찾기 메일 이력 저장
//        MemberEmailHistory memberEmailHistory = new MemberEmailHistory();
//        memberEmailHistory.setMemberIdx(member.getIdx());
//        memberEmailHistory.setCertData(certData);
//        memberEmailHistory.setUsed(false);
//        memberEmailHistory.setEmailType(EnumCode.Email.TypeCd.FindPasswordEmail.getCode());
//        memberEmailHistory.setExpireDate(ZonedDateTime.now().plusHours(1));
//        memberPasswordHistoryRepository.save(memberEmailHistory);

        return CustomResponse.builder()
                .data(null)
                .build();
    }

    /**
     * 비밀번호 변경
     * @param certData
     * @param password
     * @throws Exception
     */
    @Transactional
    public CustomResponse<?> changePassword(String certData, String password) throws Exception {
//        JSONObject data = new JSONObject(Utils.AES256.decrypt(encryptKey, URLDecoder.decode(certData, StandardCharsets.UTF_8)));
//        ZonedDateTime expireDate = ZonedDateTime.parse(data.getString("expire"));
//
//        // 비밀번호 변경용 인증키 확인
//        if(!data.getString("type").equals(AwsSESComponent.EmailType.findPassword.getValue())) {
//            throw new CustomException(ErrorCode.MEMBER_EMAIL_CERTIFICATE_INVALID);
//        }
//
//        // 회원 확인
//        Member member = memberRepository.findByEmail(data.getString("email"))
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_EMAIL_CERTIFICATE_INVALID));
//
//        // 인증키 유효 체크
//        MemberEmailHistory history = memberPasswordHistoryRepository.findTop1ByMemberIdxAndEmailTypeOrderByCreateDateDesc(member.getIdx(), EnumCode.Email.TypeCd.FindPasswordEmail.getCode())
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_EMAIL_CERTIFICATE_INVALID));
//
//        // 인증 만료 체크
//        if(expireDate.isBefore(ZonedDateTime.now())
//                || history.getExpireDate().isBefore(ZonedDateTime.now())
//                || !history.getCertData().equals(certData)
//                || history.getUsed().equals(true)) {
//            throw new CustomException(ErrorCode.MEMBER_EMAIL_CERTIFICATE_EXPIRE);
//        }
//
//        // 인증키 사용 처리
//        history.setUsed(true);
//
//        // 비밀번호 변경
//        member.setPassword(passwordEncoder.encode(password));

        return CustomResponse.builder()
                .build();
    }

    public MemberVO findByEmail(String email) throws CustomException {
        return memberMapper.selectByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
    }
}
