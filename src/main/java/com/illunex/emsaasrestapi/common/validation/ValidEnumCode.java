package com.illunex.emsaasrestapi.common.validation;

import com.illunex.emsaasrestapi.common.code.BaseCodeEnum;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumCodeValidator.class)
@Documented
public @interface ValidEnumCode {
    String message() default "올바르지 않은 코드 값입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    Class<? extends BaseCodeEnum> enumClass();
}
