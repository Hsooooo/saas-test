package com.illunex.emsaasrestapi.partnership;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.dto.RequestMemberDTO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.dto.PartnershipCreateDTO;
import com.illunex.emsaasrestapi.partnership.dto.RequestPartnershipDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("partnership")
@Slf4j
public class PartnershipController {
    private final PartnershipService partnershipService;

    /**
     * 신규 파트너쉽 생성
     * @param createDTO 파트너쉽 생성 정보
     * @param memberVO 로그인사용자정보
     * @return
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> createPartnership(@RequestBody PartnershipCreateDTO createDTO,
                                               @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(partnershipService.createPartnership(createDTO, memberVO.getIdx()))
                .build();
    }

    /**
     * 소속 파트너쉽 목록 조회
     * @param memberVO 로그인사용자정보
     * @return
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getPartnerships(@CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(partnershipService.getPartnerships(memberVO.getIdx()))
                .build();
    }

    /**
     * 파트너쉽 회원 초대
     * @param partnershipIdx 파트너쉽번호
     * @param memberVO 로그인사용자정보
     * @param request 초대회원정보
     * @return
     * @throws CustomException
     */
    @PostMapping("/{partnershipIdx}/invite")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> invitePartnershipMember(@PathVariable("partnershipIdx") Integer partnershipIdx,
                                                     @CurrentMember MemberVO memberVO,
                                                     @RequestBody @Valid RequestPartnershipDTO.InviteMember request) throws CustomException {
        return CustomResponse.builder()
                .data(partnershipService.invitePartnershipMember(partnershipIdx, memberVO.getIdx(), request))
                .build();
    }

    /**
     * 내정보 조회 (파트너쉽)
     * @param partnershipIdx 파트너쉽 번호
     * @param memberVO 로그인사용자정보
     * @return
     * @throws CustomException
     */
    @GetMapping("/{partnershipIdx}/my-info")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> getMyInfo(@PathVariable("partnershipIdx") Integer partnershipIdx,
                                       @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(partnershipService.getMyInfo(partnershipIdx, memberVO))
                .build();
    }

    /**
     * 회원정보 수정
     * @param partnershipIdx 파트너쉽 번호
     * @param memberVO 로그인사용자정보
     * @param updateInfo 수정데이터
     * @return
     * @throws CustomException
     */
    @PutMapping("/{partnershipIdx}/my-info")
    @PreAuthorize("isAuthenticated()")
    public CustomResponse<?> updateMyInfo(@PathVariable("partnershipIdx") Integer partnershipIdx,
                                          @CurrentMember MemberVO memberVO,
                                          @RequestBody @Valid RequestPartnershipDTO.UpdateMyInfo updateInfo) throws CustomException {
        return CustomResponse.builder()
                .data(partnershipService.updateMyInfo(partnershipIdx, memberVO, updateInfo))
                .build();
    }
}
