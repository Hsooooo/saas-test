package com.illunex.emsaasrestapi.autoComplete;

import com.illunex.emsaasrestapi.autoComplete.dto.RequestAutoCompleteDTO;
import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/autoComplete")
public class AutoCompleteController {

    private final AutoCompleteService autoCompleteService;

    @PatchMapping
    public CustomResponse<?> getAutoComplete(@CurrentMember MemberVO memberVO,
                                             @RequestBody RequestAutoCompleteDTO.AutoCompleteSearch autoCompleteSearch) throws CustomException {
        return autoCompleteService.getAutoComplete(memberVO,autoCompleteSearch);
    }
}
