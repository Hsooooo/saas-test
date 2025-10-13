package com.illunex.emsaasrestapi.partnership;

import com.illunex.emsaasrestapi.common.CustomException;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
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

    private final AwsSESComponent sesComponent;

    private final ModelMapper modelMapper;
    private final PartnershipComponent partnershipComponent;

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
     * @param memberIdx 파트너쉽 관리자 회원 번호
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

        // 제품코드 유효성체크
        for (String product : inviteMember.getProducts()) {
            boolean isValid = Arrays.stream(EnumCode.Product.ProductCd.values())
                    .anyMatch(p -> p.getCode().equals(product));

            if (!isValid) {
                throw new CustomException(ErrorCode.COMMON_INVALID);
            }
        }
        // 초대 제품 및 권한 정보 세팅
        JSONArray productArray = new JSONArray();
        for (String product : inviteMember.getProducts()) {
            JSONObject productJson = new JSONObject()
                    .put("productCode", product)
                    .put("auth", inviteMember.getAuth());   //TODO 권한 정보 확인 필ㄹ요 제품별 권한 설정이 없으면 빼도 될 듯
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
                invitePartnershipMemberVO.setManagerCd(inviteMember.getAuth());
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
        String invitedToken = inviteMember.getInviteToken().isBlank() ? this.createInviteLink(partnershipIdx, memberVO) : inviteMember.getInviteToken();

        // 초대링크정보 조회
        PartnershipInviteLinkVO linkVO = partnershipInviteLinkMapper.selectByInviteTokenHash(invitedToken)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

        // 초대링크 정보 수정
        linkVO.setInviteInfoJson(productArray.toString());
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


        return ResponsePartnershipDTO.MyInfo.builder()
                .partnershipMember(ResponsePartnershipDTO.MyInfoPartnershipMember.builder()
                        .idx(partnershipMember.getIdx())
                        .positionInfo(positionInfo)
                        .phone(partnershipMember.getPhone())
                        .profileImageUrl(partnershipMember.getProfileImageUrl())
                        .profileImagePath(partnershipMember.getProfileImagePath())
                        .build())
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

    public Object getPartnershipMembers(Integer partnershipIdx, MemberVO memberVO) throws CustomException {
        // 파트너쉽 관리자 여부 확인
        PartnershipMemberVO loginPartnershipMemberVO = partnershipMemberMapper
                .selectByPartnershipIdxAndMemberIdx(partnershipIdx, memberVO.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER));
        if (!loginPartnershipMemberVO.getManagerCd().equals(EnumCode.PartnershipMember.ManagerCd.Manager.getCode())) {
            throw new CustomException(ErrorCode.PARTNERSHIP_INVALID_MEMBER);
        }

        List<PartnershipMemberVO> members = partnershipMemberMapper.selectAllByPartnershipIdx(partnershipIdx);
        List<ResponsePartnershipDTO.PartnershipMember> result = new ArrayList<>();
        for (PartnershipMemberVO member : members) {

            MemberVO mv = memberMapper.selectByIdx(member.getMemberIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

            PartnershipInvitedMemberVO invitedMemberVO = partnershipInvitedMemberMapper.selectByPartnershipMemberIdx(member.getIdx()).orElse(null);
            List<PartnershipMemberProductGrantVO> products = partnershipMemberProductGrantMapper.selectByPartnershipMemberIdx(member.getIdx());
            result.add(
                    ResponsePartnershipDTO.PartnershipMember.builder()
                            .partnershipMemberIdx(member.getIdx())
                            .email(mv.getEmail())
                            .name(mv.getName())
                            .profileImageUrl(member.getProfileImageUrl())
                            .profileImagePath(member.getProfileImagePath())
                            .build()
            );
        }
        return result;
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

        String uuid = new UUID(0L, System.currentTimeMillis()).toString();
        byte[] hash = sha256(uuid);
        String hashString = Base64.getEncoder().encodeToString(hash);

        PartnershipInviteLinkVO linkVO = new PartnershipInviteLinkVO();
        linkVO.setPartnershipIdx(partnershipIdx);
        linkVO.setInviteTokenHash(hashString);
        linkVO.setCreatedByPartnershipMemberIdx(ownerMember.getIdx());
        linkVO.setStateCd( EnumCode.PartnershipInviteLink.StateCd.DRAFT.getCode());
        partnershipInviteLinkMapper.insertByPartnershipInviteLinkVO(linkVO);

        return hashString;
    }
}
