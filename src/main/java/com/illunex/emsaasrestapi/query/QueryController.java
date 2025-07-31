package com.illunex.emsaasrestapi.query;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.query.dto.RequestQueryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/query")
public class QueryController {
    private final QueryService queryService;

    @PostMapping("/find")
    public CustomResponse<?> queryFind(@CurrentMember MemberVO memberVO,
                                       @RequestBody RequestQueryDTO.ExecuteQuery executeQuery) {
        return CustomResponse.builder()
                .data(queryService.executeQuery(memberVO, executeQuery))
                .message("Query executed successfully")
                .build();
    }

//    @PostMapping("/save")
//    public CustomResponse<?> querySave(@CurrentMember MemberVO memberVO,
//                                       @RequestBody RequestQueryDTO.SaveQuery saveQuery) {
//        return CustomResponse.builder()
//                .data(queryService.saveQuery(memberVO, saveQuery))
//                .message("Query saved successfully")
//                .build();
//    }
}
