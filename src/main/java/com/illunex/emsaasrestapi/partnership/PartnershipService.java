package com.illunex.emsaasrestapi.partnership;

import com.illunex.emsaasrestapi.cert.CertComponent;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.common.aws.AwsSESComponent;
import com.illunex.emsaasrestapi.common.aws.dto.AwsS3ResourceDTO;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.member.mapper.EmailHistoryMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberJoinMapper;
import com.illunex.emsaasrestapi.member.mapper.MemberMapper;
import com.illunex.emsaasrestapi.member.vo.MemberEmailHistoryVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.dto.PartnershipCreateDTO;
import com.illunex.emsaasrestapi.partnership.dto.RequestPartnershipDTO;
import com.illunex.emsaasrestapi.partnership.dto.ResponsePartnershipDTO;
import com.illunex.emsaasrestapi.partnership.mapper.*;
import com.illunex.emsaasrestapi.partnership.vo.*;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMemberMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.*;

import static org.apache.commons.codec.digest.DigestUtils.sha256;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnershipService {
    private final PartnershipMapper partnershipMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final PartnershipPositionMapper partnershipPositionMapper;
    private final LicensePartnershipMapper licensePartnershipMapper;
    private final PartnershipAdditionalMapper partnershipAdditionalMapper;
    private final EmailHistoryMapper emailHistoryMapper;
    private final MemberMapper memberMapper;
    private final MemberJoinMapper memberJoinMapper;
    private final AwsS3Component awsS3Component;
    private final PartnershipInvitedMemberMapper partnershipInvitedMemberMapper;
    private final PartnershipMemberProductGrantMapper partnershipMemberProductGrantMapper;
    private final PartnershipInviteLinkMapper partnershipInviteLinkMapper;
    private final PartnershipMemberViewMapper partnershipMemberViewMapper;

    private final PasswordEncoder passwordEncoder;

    private final AwsSESComponent sesComponent;
    private final CertComponent certComponent;

    private final ModelMapper modelMapper;
    private final PartnershipComponent partnershipComponent;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;

    /**
     * 파트너십 생성 (기본 라이센스)
     * @param partnershipDTO 파트너십 정보
     * @param ownerMemberIdx 파트너십 생성자 IDX
     * @return partnershipIdx 파트너십 IDX
     */
    @Transactional(rollbackFor = Exception.class)
    public PartnershipVO createPartnership(PartnershipCreateDTO partnershipDTO, Integer ownerMemberIdx) throws CustomException {
        //파트너쉽 도메인 중복체크
        if(partnershipMapper.selectByDomain(partnershipDTO.getDomain()).isPresent()) {
            throw new CustomException(ErrorCode.PARTNERSHIP_DOMAIN_DUPLICATE);
        }
        //파트너쉽 정보 등록
        PartnershipVO partnership = new PartnershipVO();
        partnership.setName(partnershipDTO.getPartnershipName());
        partnership.setDomain(partnershipDTO.getDomain());
        partnershipMapper.insertByPartnershipVO(partnership);

        //파트너쉽회원정보 등록
        PartnershipMemberVO partnershipMember = new PartnershipMemberVO();
        partnershipMember.setMemberIdx(ownerMemberIdx);
        partnershipMember.setPartnershipIdx(partnership.getIdx());
        partnershipMember.setManagerCd(EnumCode.PartnershipMember.ManagerCd.Manager.getCode());
        partnershipMember.setStateCd(EnumCode.PartnershipMember.StateCd.Normal.getCode());
        partnershipMemberMapper.insertByPartnershipMember(partnershipMember);

        //파트너십 기본 라이센스 등록
//        LicensePartnershipVO licensePartnershipVO = new LicensePartnershipVO();
//        licensePartnershipVO.setPartnershipIdx(partnership.getIdx());
//        licensePartnershipVO.setLicenseIdx(1); // 기본 라이센스 ?
//        licensePartnershipVO.setStateCd("LST0001");
//        licensePartnershipMapper.insertByLicensePartnership(licensePartnershipVO);

        return partnership;
    }

    public List<PartnershipVO> getPartnerships(Integer memberIdx) throws CustomException {
        return partnershipMapper.selectByMember(memberIdx);
    }

    /**
     * 파트너쉽 멤버 초대
     * @param partnershipIdx 파트너쉽 번호
     * @param memberVO 로그인한 회원 정보
     * @param inviteMember 초대 회원 정보
     * @return
     * @throws CustomException
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponsePartnershipDTO.InviteMember invitePartnershipMember(Integer partnershipIdx, MemberVO memberVO, RequestPartnershipDTO.InviteMember inviteMember) throws CustomException {
        // 파트너쉽 관리자 여부 확인
        PartnershipMemberVO loginPartnershipMemberVO = partnershipMemberMapper
                .selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));

        if (!loginPartnershipMemberVO.getManagerCd().equals(EnumCode.PartnershipMember.ManagerCd.Manager.getCode())) {
            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
        }

        List<ResponsePartnershipDTO.InviteResult> validList = new ArrayList<>();
        List<ResponsePartnershipDTO.InviteResult> invalidList = new ArrayList<>();
        RequestPartnershipDTO.InviteInfo inviteInfo = inviteMember.getInviteInfo();

        // 제품코드 유효성체크
        for (String product : inviteInfo.getProducts()) {
            boolean isValid = Arrays.stream(EnumCode.Product.ProductCd.values())
                    .anyMatch(p -> p.getCode().equals(product));

            if (!isValid) {
                throw new CustomException(ErrorCode.COMMON_INVALID);
            }
        }
        // 초대 제품 및 권한 정보 세팅
        JSONArray productArray = new JSONArray();
        for (String product : inviteInfo.getProducts()) {
            JSONObject productJson = new JSONObject()
                    .put("productCode", product)
                    .put("productAuth", EnumCode.Product.ProductAuthCd.EDITOR.getCode());   //TODO 권한 정보 확인 필ㄹ요 제품별 권한 설정이 없으면 빼도 될 듯
            productArray.put(productJson);
        }

        for (String email : inviteMember.getEmails().split("[,;\\s]+")) {
            try {
                // 1. 이미 초대되었는지 확인
                if (partnershipInvitedMemberMapper.existsInvitedMember(partnershipIdx, email)) {
                    invalidList.add(ResponsePartnershipDTO.InviteResult.builder()
                            .email(email)
                            .result("error")
                            .reason("이미 초대된 회원입니다.")
                            .build()
                    );
                    continue;
                }

                // 2. 이미 가입된 사용자 여부
                MemberVO inviteTargetMember = memberMapper.selectByEmail(email)
                        .orElseGet(() -> {
                            MemberVO newMemberVO = new MemberVO();
                            newMemberVO.setEmail(email);
                            newMemberVO.setStateCd(EnumCode.Member.StateCd.Wait.getCode());
                            newMemberVO.setTypeCd(EnumCode.Member.TypeCd.Normal.getCode());
                            memberJoinMapper.insertByMemberJoin(newMemberVO);
                            return newMemberVO;
                        });

                if (inviteTargetMember.getStateCd().equals(EnumCode.Member.StateCd.Suspend.getCode())) {
                    throw new CustomException(ErrorCode.MEMBER_STATE_SUSPEND);
                }

                if (inviteTargetMember.getStateCd().equals(EnumCode.Member.StateCd.Withdrawal.getCode())) {
                    throw new CustomException(ErrorCode.MEMBER_STATE_WITHDRAWAL);
                }

                // 3. 초대 정보 저장
                PartnershipInvitedMemberVO invitedMemberVO = new PartnershipInvitedMemberVO();
                invitedMemberVO.setEmail(email);
                invitedMemberVO.setPartnershipIdx(partnershipIdx);
                invitedMemberVO.setInvitedByPartnershipMemberIdx(loginPartnershipMemberVO.getIdx());
                invitedMemberVO.setMemberIdx(inviteTargetMember.getIdx());

                // 4. partnership_member 생성
                PartnershipMemberVO invitePartnershipMemberVO = new PartnershipMemberVO();
                invitePartnershipMemberVO.setMemberIdx(inviteTargetMember.getIdx());
                invitePartnershipMemberVO.setPartnershipIdx(partnershipIdx);
                invitePartnershipMemberVO.setManagerCd(inviteInfo.getAuth());
                invitePartnershipMemberVO.setStateCd(EnumCode.PartnershipMember.StateCd.Wait.getCode());
                partnershipMemberMapper.insertByPartnershipMember(invitePartnershipMemberVO);

                invitedMemberVO.setPartnershipMemberIdx(invitePartnershipMemberVO.getIdx());

                partnershipInvitedMemberMapper.insertInvitedMember(invitedMemberVO);

                // 메일 발송
                String certData = sesComponent.sendInviteMemberEmail(null, email, loginPartnershipMemberVO.getIdx(), memberVO.getIdx(), productArray);

                MemberEmailHistoryVO emailHistoryVO = new MemberEmailHistoryVO();
                emailHistoryVO.setMemberIdx(inviteTargetMember.getIdx());
                emailHistoryVO.setCertData(certData);
                emailHistoryVO.setUsed(false);
                emailHistoryVO.setEmailType(EnumCode.Email.TypeCd.InvitePartnership.getCode());
                emailHistoryVO.setExpireDate(ZonedDateTime.now().plusHours(1)); //1시간?

                emailHistoryMapper.insertByMemberEmailHistoryVO(emailHistoryVO);

                validList.add(ResponsePartnershipDTO.InviteResult.builder()
                        .email(email)
                        .result("success")
                        .build());
            } catch (Exception e) {
                log.error(e.getMessage());
                invalidList.add(ResponsePartnershipDTO.InviteResult.builder()
                        .email(email)
                        .result("error")
                        .reason(e.getMessage())
                        .build()
                );
            }
        }
        // 초대 링크 토큰
        String invitedToken = inviteInfo.getInviteToken().isBlank() ? this.createInviteLink(partnershipIdx, memberVO) : inviteInfo.getInviteToken();

        // 초대링크정보 조회
        PartnershipInviteLinkVO linkVO = partnershipInviteLinkMapper.selectByInviteTokenHash(invitedToken)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

        JSONObject inviteInfoJson = new JSONObject();
        inviteInfoJson.put("products", productArray);
        inviteInfoJson.put("auth", inviteInfo.getAuth());
        // 초대링크 정보 수정
        linkVO.setInviteInfoJson(inviteInfoJson.toString());
        linkVO.setStateCd(EnumCode.PartnershipInviteLink.StateCd.ACTIVE.getCode());
        linkVO.setExpireDate(linkVO.getCreateDate().plusDays(7));
        partnershipInviteLinkMapper.updateByPartnershipInviteLinkVO(linkVO);

        return ResponsePartnershipDTO.InviteMember.builder()
                .valid(validList)
                .inValid(invalidList)
                .build();
    }


    /**
     * 회원정보 조회 (파트너쉽)
     * @param partnershipIdx 파트너쉽 번호
     * @param memberVO 로그인된사용자정보
     * @return
     * @throws CustomException
     */
    public ResponsePartnershipDTO.MyInfo getMyInfo(Integer partnershipIdx, MemberVO memberVO) throws CustomException {
        PartnershipVO partnership = partnershipMapper.selectByIdx(partnershipIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        PartnershipMemberVO partnershipMember = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));

        ResponsePartnershipDTO.PartnershipPositionInfo positionInfo = null;
        if (partnershipMember.getPartnershipPositionIdx() != null) {
            PartnershipPositionVO positionVO = partnershipPositionMapper.selectByIdx(partnershipMember.getPartnershipPositionIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
            positionInfo = ResponsePartnershipDTO.PartnershipPositionInfo.builder()
                    .idx(positionVO.getIdx())
                    .name(positionVO.getName())
                    .sortLevel(positionVO.getSortLevel())
                    .build();
        }
        ResponsePartnershipDTO.MyInfoPartnershipMember pm = ResponsePartnershipDTO.MyInfoPartnershipMember.builder()
                .idx(partnershipMember.getIdx())
                .positionInfo(positionInfo)
                .phone(partnershipMember.getPhone())
                .profileImageUrl(partnershipMember.getProfileImageUrl())
                .profileImagePath(partnershipMember.getProfileImagePath())
                .build();
        pm.setManagerCd(partnershipMember.getManagerCd());

        return ResponsePartnershipDTO.MyInfo.builder()
                .partnershipMember(pm)
                .partnership(ResponsePartnershipDTO.PartnershipInfo.builder()
                        .idx(partnership.getIdx())
                        .domain(partnership.getDomain())
                        .name(partnership.getName())
                        .imageUrl(partnership.getImageUrl())
                        .imagePath(partnership.getImagePath())
                        .build())
                .member(ResponsePartnershipDTO.MyInfoMember.builder()
                        .idx(memberVO.getIdx())
                        .email(memberVO.getEmail())
                        .name(memberVO.getName())
                        .build())
                .build();
    }

    /**
     * 내정보 수정
     * @param partnershipIdx 파트너쉽번호
     * @param memberVO 로그인회원정보
     * @param updateInfo 수정정보
     * @return
     * @throws CustomException
     */
    @Transactional(rollbackFor = Exception.class)
    public Object updateMyInfo(Integer partnershipIdx, MemberVO memberVO, RequestPartnershipDTO.@Valid UpdateMyInfo updateInfo) throws CustomException {
        PartnershipMemberVO partnershipMember = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));

        Integer partnershipPositionIdx = null;
        String phone = updateInfo.getPhone();
        // 직책정보 전달 시
        if (updateInfo.getPosition() != null) {
            // 기존 등록된 직책이 있는경우 해당 직책 Idx / 없는 경우 마지막 sort_level로 등록
            partnershipPositionIdx = partnershipPositionMapper
                    .selectByNameAndPartnershipIdx(partnershipIdx, updateInfo.getPosition())
                    .map(PartnershipPositionVO::getIdx)
                    .orElseGet(() -> {
                        int maxSortLevel = partnershipPositionMapper.selectMaxSortLevelByPartnershipIdx(partnershipIdx);
                        PartnershipPositionVO newPosition = new PartnershipPositionVO();
                        newPosition.setName(updateInfo.getPosition());
                        newPosition.setSortLevel(maxSortLevel + 1);
                        newPosition.setPartnershipIdx(partnershipIdx);
                        partnershipPositionMapper.insertByPartnershipPositionVO(newPosition);

                        return newPosition.getIdx();
                    });
        }
        // 회원명 변경
        memberMapper.updateNameByIdx(updateInfo.getName(), memberVO.getIdx());
        // 파트너쉽 정보 변경 (직책, 휴대폰번호)
        partnershipMemberMapper.updatePositionIdxAndPhoneByIdx(partnershipPositionIdx, phone, partnershipMember.getIdx());

        return this.getMyInfo(partnershipIdx, memberVO);
    }

    /**
     * 파트너쉽 멤버 프로필 이미지 변경
     * @param partnershipIdx
     * @param memberVO
     * @param file
     * @return
     * @throws CustomException
     * @throws IOException
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> updateProfileImage(Integer partnershipIdx, MemberVO memberVO, MultipartFile file) throws CustomException, IOException {
        PartnershipMemberVO partnershipMember = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));

        AwsS3ResourceDTO response = AwsS3ResourceDTO.builder()
                .fileName(file.getOriginalFilename())
                .s3Resource(awsS3Component.upload(file, AwsS3Component.FolderType.PartnershipMember, partnershipMember.getIdx().toString()))
                .build();
//
//        // 기존 프로필 이미지가 있을 경우 삭제
        if(partnershipMember.getProfileImagePath() != null && !partnershipMember.getProfileImagePath().isEmpty()) {
            awsS3Component.delete(partnershipMember.getProfileImagePath());
        }
//
        partnershipMember.setProfileImageUrl(response.getUrl());
        partnershipMember.setProfileImagePath(response.getFileName());

        partnershipMemberMapper.updateProfileImageByIdx(partnershipMember);

        return CustomResponse.builder()
                .data(null)
                .build();
    }


    /**
     * 파트너쉽 부가정보 저장
     * @param partnershipIdx
     * @param request
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> updatePartnershipAdditionalInfo(Integer partnershipIdx, RequestPartnershipDTO.AdditionalInfo request) {
        partnershipAdditionalMapper.deleteByPartnershipIdx(partnershipIdx);

        Field[] fields = RequestPartnershipDTO.AdditionalInfo.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                String key = field.getName();
                String value = (String) field.get(request);

                if (value != null && !value.isBlank()) {
                    PartnershipAdditionalVO additionalVO = new PartnershipAdditionalVO();
                    additionalVO.setPartnershipIdx(partnershipIdx);
                    additionalVO.setAttrKey(key);
                    additionalVO.setAttrValue(value);
                    partnershipAdditionalMapper.insertByPartnershipAdditionalVO(additionalVO);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("속성 접근 실패", e);
            }
        }

        return CustomResponse.builder().build();
    }

    /**
     * 파트너쉽 회원 목록 조회
     * @param partnershipIdx 파트너쉽 번호
     * @param memberVO 로그인회원정보
     * @param request 검색조건
     * @param pageRequest 페이지요청정보
     * @param sort 정렬정보
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getPartnershipMembers(Integer partnershipIdx, MemberVO memberVO, RequestPartnershipDTO.SearchMember request, CustomPageRequest pageRequest, String[] sort) throws CustomException {
        // 파트너쉽 관리자 여부 확인
        PartnershipMemberVO loginPartnershipMemberVO = partnershipMemberMapper
                .selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));
        if (!loginPartnershipMemberVO.getManagerCd().equals(EnumCode.PartnershipMember.ManagerCd.Manager.getCode())) {
            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
        }

        request.setPartnershipIdx(partnershipIdx);
        if (sort == null) {
            sort = new String[]{"partnership_member_idx,ASC"};
        }
        Pageable pageable = pageRequest.of(sort);
        List<PartnershipMemberViewVO> members = partnershipMemberViewMapper.selectAllBySearchMemberAndPageable(request, pageable);
        long totalCount = partnershipMemberViewMapper.countAllBySearchMember(request);

        List<ResponsePartnershipDTO.PartnershipMember> result = new ArrayList<>();
        for (PartnershipMemberViewVO member : members) {
            ResponsePartnershipDTO.PartnershipMember pm = modelMapper.map(member, ResponsePartnershipDTO.PartnershipMember.class);
            pm.setStateCd(member.getStateCd());
            pm.setManagerCd(member.getManagerCd());
            result.add(pm);
        }

        return CustomResponse.builder()
                .data(new PageImpl<>(result, pageable, totalCount))
                .build();
    }

    /**
     * 파트너쉽 초대링크 임시 생성
     * @param partnershipIdx
     * @param memberVO
     * @return
     */
    public String createInviteLink(Integer partnershipIdx, MemberVO memberVO) throws CustomException {
        PartnershipMemberVO ownerMember = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);
        if (!ownerMember.getManagerCd().equals(EnumCode.PartnershipMember.ManagerCd.Manager.getCode())) {
            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
        }

        String uuid = UUID.randomUUID().toString();
        byte[] hash = sha256(uuid);
        String hashString = Base64.getEncoder().encodeToString(hash);

        // 중복 확인
        if (partnershipInviteLinkMapper.selectByInviteTokenHash(hashString).isPresent()) {
            uuid = UUID.randomUUID().toString();
            hash = sha256(uuid);
            hashString = Base64.getEncoder().encodeToString(hash);
        }

        PartnershipInviteLinkVO linkVO = new PartnershipInviteLinkVO();
        linkVO.setPartnershipIdx(partnershipIdx);
        linkVO.setInviteTokenHash(hashString);
        linkVO.setCreatedByPartnershipMemberIdx(ownerMember.getIdx());
        linkVO.setStateCd( EnumCode.PartnershipInviteLink.StateCd.DRAFT.getCode());
        partnershipInviteLinkMapper.insertByPartnershipInviteLinkVO(linkVO);

        return hashString;
    }

    /**
     * 초대 승인
     * @param request
     * @param memberVO
     * @return
     */
    public CustomResponse<?> approveInvite(RequestPartnershipDTO.ApproveInvite request, MemberVO memberVO) throws Exception {
        // 초대링크 유효성 체크
        Optional<PartnershipInviteLinkVO> inviteLinkOpt = partnershipInviteLinkMapper.selectByInviteTokenHash(request.getInviteToken());

        if (inviteLinkOpt.isEmpty()) {
//            final String INVITE_MAIL_TYPE = AwsSESComponent.EmailType.invite.getValue();
//            final String INVITE_PROJECT_MAIL_TYPE = AwsSESComponent.EmailType.inviteProject.getValue();
            // 인증키 유효성 체크
            JSONObject data = certComponent.verifyCertData(request.getInviteToken());

            certComponent.approvePartnershipMember(data, memberVO);
            // 파트너쉽 초대 승인 로직
//            if (emailType.equals(INVITE_MAIL_TYPE)) {
//                certComponent.approvePartnershipMember(data, memberVO);
//            }
//
//            // 프로젝트 초대 승인 로직
//            if (emailType.equals(INVITE_PROJECT_MAIL_TYPE)) {
//                // TODO
//            }

            // 이메일 인증 완료 처리
            certComponent.markEmailHistoryAsUsed(request.getInviteToken());

            return null;
        } else {
            PartnershipInviteLinkVO linkVO = inviteLinkOpt.get();
            if (linkVO.getExpireDate() != null && linkVO.getExpireDate().isBefore(ZonedDateTime.now())) {
                throw new CustomException(ErrorCode.COMMON_INVITE_LINK_EXPIRE);
            }

            if (!linkVO.getStateCd().equals(EnumCode.PartnershipInviteLink.StateCd.ACTIVE.getCode())) {
                throw new CustomException(ErrorCode.COMMON_INVALID);
            }

            String infoJsonString = linkVO.getInviteInfoJson();
            if (infoJsonString == null || infoJsonString.isBlank()) {
                throw new CustomException(ErrorCode.COMMON_INVALID);
            }
            JSONObject infoJson = new JSONObject(infoJsonString);
            JSONArray products = infoJson.getJSONArray("products");
            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                boolean isValid = Arrays.stream(EnumCode.Product.ProductCd.values())
                        .anyMatch(p -> p.getCode().equals(product.getString("productCode")));

                if (!isValid) {
                    throw new CustomException(ErrorCode.COMMON_INVALID);
                }
            }
            // 초대된 파트너쉽 회원 정보
            String finalAuth = infoJson.getString("auth");;
            PartnershipMemberVO invitedPartnershipMember = partnershipMemberMapper.selectByPartnershipIdxAndMemberIdx(linkVO.getPartnershipIdx(), memberVO.getIdx())
                    .orElseGet(() -> {
                        PartnershipMemberVO newMember = new PartnershipMemberVO();
                        newMember.setMemberIdx(memberVO.getIdx());
                        newMember.setPartnershipIdx(linkVO.getPartnershipIdx());
                        newMember.setManagerCd(finalAuth);
                        newMember.setStateCd(EnumCode.PartnershipMember.StateCd.Wait.getCode());
                        partnershipMemberMapper.insertByPartnershipMember(newMember);

                        return newMember;
                    });

            if (invitedPartnershipMember.getStateCd().equals(EnumCode.PartnershipMember.StateCd.Normal.getCode())) {
                throw new CustomException(ErrorCode.PARTNERSHIP_MEMBER_ALREADY_JOINED);
            }
            Optional<PartnershipInvitedMemberVO> invitedOpt = partnershipInvitedMemberMapper.selectByPartnershipMemberIdx(invitedPartnershipMember.getIdx());

            if (invitedOpt.isPresent()) {
                PartnershipInvitedMemberVO invitedMemberVO = invitedOpt.get();
                invitedMemberVO.setJoinedDate(ZonedDateTime.now());
                partnershipInvitedMemberMapper.updateJoinedDateNowByIdx(invitedMemberVO.getIdx());
            } else {
                PartnershipInvitedMemberVO invitedMemberVO = new PartnershipInvitedMemberVO();


                invitedMemberVO.setEmail(memberVO.getEmail());
                invitedMemberVO.setPartnershipIdx(linkVO.getPartnershipIdx());
                invitedMemberVO.setInvitedByPartnershipMemberIdx(linkVO.getCreatedByPartnershipMemberIdx());
                invitedMemberVO.setMemberIdx(memberVO.getIdx());
                invitedMemberVO.setPartnershipMemberIdx(invitedPartnershipMember.getIdx());
                invitedMemberVO.setInvitedDate(linkVO.getCreateDate()); //TODO 초대일시 확인필요
                invitedMemberVO.setJoinedDate(ZonedDateTime.now());
                partnershipInvitedMemberMapper.insertInvitedMember(invitedMemberVO);
            }
//
//        if (!invitedPartnershipMember.getStateCd().equals(EnumCode.PartnershipMember.StateCd.Wait.getCode())) {
//            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
//        }

            // 제품 권한 정보 저장
            if (!products.isEmpty()) {
                for (int i = 0; i < products.length(); i++) {
                    JSONObject product = products.getJSONObject(i);
                    PartnershipMemberProductGrantVO productGrant = new PartnershipMemberProductGrantVO();
                    productGrant.setPartnershipMemberIdx(invitedPartnershipMember.getIdx());
                    productGrant.setProductCode(product.getString("productCode"));
                    productGrant.setPermissionCode(product.getString("productAuth"));

                    partnershipMemberProductGrantMapper.insertByPartnershipMemberProductGrantVO(productGrant);
                }
            }

            // 파트너쉽 회원 상태 변경
            partnershipMemberMapper.updatePartnershipMemberStateByIdx(
                    invitedPartnershipMember.getIdx(),
                    EnumCode.PartnershipMember.StateCd.Normal.getCode()
            );
            linkVO.setUsedCount(linkVO.getUsedCount() == null ? 1 : linkVO.getUsedCount() + 1);
            partnershipInviteLinkMapper.updateByPartnershipInviteLinkVO(linkVO);

            return null;
        }

    }

    /**
     * 초대링크 정보 수정
     * @param partnershipIdx
     * @param memberVO
     * @param inviteInfo
     * @return
     */
    public Object updateInviteLink(Integer partnershipIdx, MemberVO memberVO, RequestPartnershipDTO.InviteInfo inviteInfo) throws CustomException {
        PartnershipMemberVO ownerMember = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);
        if (!ownerMember.getManagerCd().equals(EnumCode.PartnershipMember.ManagerCd.Manager.getCode())) {
            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
        }
        PartnershipInviteLinkVO linkVO = partnershipInviteLinkMapper.selectByInviteTokenHash(inviteInfo.getInviteToken())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        // 제품코드 유효성체크
        for (String product : inviteInfo.getProducts()) {
            boolean isValid = Arrays.stream(EnumCode.Product.ProductCd.values())
                    .anyMatch(p -> p.getCode().equals(product));

            if (!isValid) {
                throw new CustomException(ErrorCode.COMMON_INVALID);
            }
        }
        // 초대 제품 및 권한 정보 세팅
        JSONArray productArray = new JSONArray();
        for (String product : inviteInfo.getProducts()) {
            JSONObject productJson = new JSONObject()
                    .put("productCode", product)
                    .put("productAuth", EnumCode.Product.ProductAuthCd.EDITOR.getCode());   //TODO 권한 정보 확인 필ㄹ요 제품별 권한 설정이 없으면 빼도 될 듯
            productArray.put(productJson);
        }

        JSONObject inviteInfoJson = new JSONObject();
        inviteInfoJson.put("products", productArray);
        inviteInfoJson.put("auth", inviteInfo.getAuth());
        // 초대링크 정보 수정
        linkVO.setInviteInfoJson(inviteInfoJson.toString());
        linkVO.setStateCd(EnumCode.PartnershipInviteLink.StateCd.ACTIVE.getCode());
        linkVO.setExpireDate(linkVO.getCreateDate().plusDays(7));
        linkVO.setUsedCount(0);
        partnershipInviteLinkMapper.updateByPartnershipInviteLinkVO(linkVO);

        return null;
    }

    /**
     * 파트너쉽 멤버 자동완성 조회
     * @param partnershipIdx
     * @param memberVO
     * @param searchString
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getPartnershipMembersAutoComplete(Integer partnershipIdx, MemberVO memberVO, String searchString) throws CustomException {
        partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);

        List<PartnershipMemberViewVO> members = partnershipMemberViewMapper.selectAllByPartnershipIdxAndSearchString(partnershipIdx, searchString);

        List<ResponsePartnershipDTO.PartnershipMember> result = new ArrayList<>();
        for (PartnershipMemberViewVO member : members) {
            ResponsePartnershipDTO.PartnershipMember pm = modelMapper.map(member, ResponsePartnershipDTO.PartnershipMember.class);
            pm.setStateCd(member.getStateCd());
            pm.setManagerCd(member.getManagerCd());
            result.add(pm);
        }

        return CustomResponse.builder()
                .data(result)
                .build();
    }

    /**
     * 파트너쉽 회원 비활성화 (TODO)
     * @param partnershipIdx 파트너쉽 번호
     * @param memberVO 로그인회원정보
     * @param partnershipMemberIdx 비활성화할 파트너쉽회원번호
     * @return
     */
    public Object deactivatePartnershipMembers(Integer partnershipIdx, MemberVO memberVO, Integer partnershipMemberIdx) throws CustomException {
        // 파트너쉽 관리자 여부 확인
        PartnershipMemberVO loginPartnershipMemberVO = partnershipMemberMapper
                .selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));
        if (!loginPartnershipMemberVO.getManagerCd().equals(EnumCode.PartnershipMember.ManagerCd.Manager.getCode())) {
            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
        }

        PartnershipMemberVO targetMember = partnershipMemberMapper
                .selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
        if (!targetMember.getPartnershipIdx().equals(partnershipIdx)) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }
        return null;
    }

    public Object updatePartnershipMember(Integer partnershipIdx, MemberVO memberVO, RequestPartnershipDTO.PatchPartnershipMember request) throws CustomException {
        // 파트너쉽 관리자 여부 확인
        PartnershipMemberVO loginPartnershipMemberVO = partnershipMemberMapper
                .selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));
        if (!loginPartnershipMemberVO.getManagerCd().equals(EnumCode.PartnershipMember.ManagerCd.Manager.getCode())) {
            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
        }

        // 변경하고자 하는 회원 정보 조회
        PartnershipMemberVO targetMember = partnershipMemberMapper
                .selectByIdx(request.getPartnershipMemberIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
        // 파트너쉽 일치 여부 확인
        if (!targetMember.getPartnershipIdx().equals(partnershipIdx)) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        if (request.getStateCd() != null && !request.getStateCd().isEmpty()) {
            // 회원 상태가 대기인 경우 상태 변경 불가
            if (targetMember.getStateCd().equals(EnumCode.PartnershipMember.StateCd.Wait.getCode())) {
                throw new CustomException(ErrorCode.PARTNERSHIP_MEMBER_INVALID_STATE_CHANGE);
            }
            // 삭제 요청인 경우
            if (request.getStateCd().equals(EnumCode.PartnershipMember.StateCd.Delete.getCode())) {
                if (request.getTransferPartnershipMemberIdx() == null) {
                    throw new CustomException(ErrorCode.PARTNERSHIP_MEMBER_TRANSFER_REQUIRED);
                }
                PartnershipMemberVO transferMember = partnershipMemberMapper
                        .selectByIdx(request.getPartnershipMemberIdx())
                        .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_MEMBER_TRANSFER_REQUIRED));

                if (!transferMember.getPartnershipIdx().equals(partnershipIdx)) {
                    throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
                }

                if (transferMember.getStateCd().equals(EnumCode.PartnershipMember.StateCd.Delete.getCode())) {
                    throw new CustomException(ErrorCode.PARTNERSHIP_MEMBER_TRANSFER_INVALID);
                }

                projectMemberMapper.updatePartnershipMemberIdxByPartnershipIdxAndPartnershipMemberIdx(
                        partnershipIdx,
                        targetMember.getIdx(),
                        transferMember.getIdx()
                );
            }
            // 상태코드 변경
            targetMember.setStateCd(request.getStateCd());
        }

        if (request.getManagerCd() != null && !request.getManagerCd().isEmpty()) {
            // 관리자 권한 변경
            targetMember.setManagerCd(request.getManagerCd());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            MemberVO targetUser = memberMapper.selectByIdx(request.getPartnershipMemberIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            targetUser.setPassword(encodedPassword);
            memberMapper.updateStateAndPasswordByIdx(targetUser.getIdx(), targetUser.getStateCd(), targetUser.getPassword());
        }


        partnershipMemberMapper.updatePartnershipMemberStateByIdx(targetMember.getIdx(), targetMember.getStateCd());
        partnershipMemberMapper.updatePartnershipMemberManagerCdByIdx(targetMember.getIdx(), targetMember.getManagerCd());

        return null;
    }
}
