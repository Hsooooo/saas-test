package com.illunex.emsaasrestapi.common;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomPageRequest {
    private final int DEFAULT_SIZE = 10;
    private final int MAX_SIZE = 10000;
    private int page;
    private int size;
    private Sort.Direction direction = Sort.Direction.DESC;

    public void setPage(int page){
        this.page = page <= 0 ? 1 : page;
    }

    public void setSize(int size){
        this.size = size > MAX_SIZE ? DEFAULT_SIZE : size;
    }

    public void setDirection(Sort.Direction direction){
        if(direction != null){
            this.direction = direction;
        }
    }

    public org.springframework.data.domain.PageRequest of(String... properties){
        if(properties == null){
            return of();
        }
        return org.springframework.data.domain.PageRequest.of(
                page > 0 ? page - 1 : page,
                size > 0 ? size : DEFAULT_SIZE,
                parseSort(properties));
    }

    public org.springframework.data.domain.PageRequest of(){
        return org.springframework.data.domain.PageRequest.of(
                page > 0 ? page - 1 : page,
                size > 0 ? size : DEFAULT_SIZE,
                direction,
                "create_date");
    }

    /**
     * pageable로 넘어온 sort를 Sort클래스로 변환하는 함수
     * @param properties
     * @return
     */
    private Sort parseSort(String... properties){
        List<String> propertyArray = new ArrayList<String>();
        if(properties[0].indexOf(',') <= 0){
            // 단일 정렬
            // 문자열값에 ','가 없으면 단일 정렬이므로 문자 배열을 변경 해주는 처리
            propertyArray.add(String.format("%s,%s", properties[0], properties[1]));
        }else{
            // 다중 정렬
            for(String value : properties){
                propertyArray.add(value);
            }
        }
        List<Sort.Order> orders = new ArrayList<>();
        for(String property : propertyArray){
            String[] sortValue = property.split(",");
            switch(sortValue[1].trim().toUpperCase(Locale.ROOT)){
                case "ASC":
                    orders.add(new Sort.Order(Sort.Direction.ASC, sortValue[0].trim()));
                    break;
                case "DESC":
                    orders.add(new Sort.Order(Sort.Direction.DESC, sortValue[0].trim()));
                    break;
            }
        }
        return Sort.by(orders);
    }
}
