package com.illunex.emsaasrestapi.common.code.mapper;

import com.illunex.emsaasrestapi.common.code.vo.CodeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CodeMapper {
    List<CodeVO> selectAll();
}
