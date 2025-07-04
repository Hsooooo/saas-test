package com.illunex.emsaasrestapi.database.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
public class EdgeDataDTO {
    private Object id;
    private LinkedHashMap<String, Object> properties;
}
