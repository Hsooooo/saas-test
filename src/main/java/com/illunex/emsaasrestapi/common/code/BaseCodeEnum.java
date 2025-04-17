package com.illunex.emsaasrestapi.common.code;

public interface BaseCodeEnum {
    String getCode();
    String getValue();

    static <T extends Enum<T> & BaseCodeEnum> T fromCode(Class<T> enumType, String code) {
        for (T constant : enumType.getEnumConstants()) {
            if (constant.getCode().equals(code)) {
                return constant;
            }
        }
        return null;
    }
}
