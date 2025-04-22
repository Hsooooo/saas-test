package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectCategoryMapper {

    List<CategoryVO> findAll();
    List<CategoryVO> findByPartnershipIdx(Integer idx);

}
