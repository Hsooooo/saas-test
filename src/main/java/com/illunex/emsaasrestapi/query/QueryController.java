package com.illunex.emsaasrestapi.query;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.query.dto.RequestQueryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/query")
public class QueryController {
    private final QueryService queryService;

    @PostMapping("/find")
    public CustomResponse<?> queryFind(@CurrentMember MemberVO memberVO,
                                       @RequestBody RequestQueryDTO.FindQuery executeQuery) {
        return CustomResponse.builder()
                .data(queryService.findQuery(memberVO, executeQuery))
                .message("Query executed successfully")
                .build();
    }

    @PostMapping("/execute")
    public CustomResponse<?> queryExecute(@CurrentMember MemberVO memberVO,
                                          @RequestBody RequestQueryDTO.ExecuteRawQuery executeQuery,
                                          CustomPageRequest pageRequest,
                                          String[] sort) {
        return CustomResponse.builder()
                .data(queryService.executeQuery(memberVO, executeQuery, pageRequest.of(sort)))
                .message("Query executed successfully")
                .build();
    }

    @PostMapping("/ai")
    public CustomResponse<?> queryAI(@CurrentMember MemberVO memberVO,
                                     @RequestBody RequestQueryDTO.AIQuery aiQuery) {
        return CustomResponse.builder()
                .data(queryService.aiQuery(memberVO, aiQuery))
                .message("AI Query executed successfully")
                .build();
    }

    @PostMapping("/save")
    public CustomResponse<?> querySave(@CurrentMember MemberVO memberVO,
                                       @RequestBody RequestQueryDTO.SaveQuery saveQuery) {
        queryService.saveQuery(memberVO, saveQuery);
        return CustomResponse.builder()
                .message("Query saved successfully")
                .build();
    }

    @PutMapping("/edit")
    public CustomResponse<?> queryEdit(@CurrentMember MemberVO memberVO,
                                       @RequestBody RequestQueryDTO.EditQuery editQuery) throws CustomException {
        return CustomResponse.builder()
                .data(queryService.queryEdit(memberVO, editQuery))
                .message("Query edited successfully")
                .build();
    }

    @GetMapping("/categories")
    public CustomResponse<?> getQueryCategories(@CurrentMember MemberVO memberVO,
                                                @RequestParam Integer projectIdx,
                                                @RequestParam Integer partnershipIdx) {
        return CustomResponse.builder()
                .data(queryService.getQueryCategories(memberVO, projectIdx, partnershipIdx))
                .message("Query categories retrieved successfully")
                .build();
    }

    @GetMapping("/category/queries")
    public CustomResponse<?> getQueriesByCategory(@CurrentMember MemberVO memberVO,
                                                  @RequestParam Integer partnershipIdx,
                                                  @RequestParam Integer queryCategoryIdx) {
        return CustomResponse.builder()
                .data(queryService.getQueriesByCategory(memberVO, partnershipIdx, queryCategoryIdx))
                .message("Queries by category retrieved successfully")
                .build();
    }

    @DeleteMapping("/delete")
    public CustomResponse<?> deleteQuery(@CurrentMember MemberVO memberVO,
                                         @RequestParam Integer queryIdx) throws CustomException {
        queryService.deleteQuery(memberVO, queryIdx);
        return CustomResponse.builder()
                .message("Query deleted successfully")
                .build();
    }
}
