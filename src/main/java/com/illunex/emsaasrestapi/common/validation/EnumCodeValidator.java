package com.illunex.emsaasrestapi.common.validation;

import com.illunex.emsaasrestapi.common.code.BaseCodeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumCodeValidator implements ConstraintValidator<ValidEnumCode, String> {
    private Class<? extends BaseCodeEnum> enumClass;

    @Override
    public void initialize(ValidEnumCode constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;

        try {
            Object[] constants = enumClass.getEnumConstants();
            if (constants == null) return false;

            for (Object constant : constants) {
                BaseCodeEnum codeEnum = (BaseCodeEnum) constant;
                if (codeEnum.getCode().equals(value)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}
