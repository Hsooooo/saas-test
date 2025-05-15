package com.illunex.emsaasrestapi.common.code.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("CodeVO")
public class CodeVO {
    private String code;
    private String firstCode;
    private String secondCode;
    private String thirdCode;
    private String codeValue;
    private Integer seq;
}
