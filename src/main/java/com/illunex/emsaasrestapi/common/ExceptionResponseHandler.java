package com.illunex.emsaasrestapi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RestControllerAdvice("com.illunex.emsaasrestapi")
public class ExceptionResponseHandler {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({CustomException.class})
    public ResponseEntity<CustomResponse<?>> CustomException(CustomException e) {
        log.error(Utils.getLogMaker(Utils.eLogType.USER), e.getLocalizedMessage());
        Arrays.stream(e.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().startsWith("com.illunex.emsaasrestapi"))
                .forEach(stackTraceElement -> {
                    log.error(Utils.getLogMaker(Utils.eLogType.USER), String.format("%s.%s:%d", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber()));
                });
        return new ResponseEntity<>(CustomResponse.builder()
                .status(e.getErrorCode().getStatus())
                .data(e.getErrorCode().getMessage())
                .message(e.getMessage())
                .build(), HttpStatus.OK);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<CustomResponse<?>> MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(Utils.getLogMaker(Utils.eLogType.USER), Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
        Arrays.stream(e.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().startsWith("com.illunex.emsaasrestapi"))
                .forEach(stackTraceElement -> {
                    log.error(Utils.getLogMaker(Utils.eLogType.USER), String.format("%s.%s:%d", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber()));
                });
        return new ResponseEntity<>(CustomResponse.builder()
                .status(ErrorCode.COMMON_INTERNAL_SERVER_ERROR.getStatus())
                .data(ErrorCode.COMMON_INTERNAL_SERVER_ERROR.getMessage())
                .message(Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage())
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public ResponseEntity<CustomResponse<?>> AccessDeniedException(Exception e) {
        log.error("Access Denied: {}", e.getMessage(), e);
        log.error(Utils.getLogMaker(Utils.eLogType.USER), e.getLocalizedMessage());
        Arrays.stream(e.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().startsWith("com.illunex.emsaasrestapi"))
                .forEach(stackTraceElement -> {
                    log.error(Utils.getLogMaker(Utils.eLogType.USER), String.format("%s.%s:%d", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber()));
                });
        return new ResponseEntity<>(CustomResponse.builder()
                .status(ErrorCode.COMMON_FAIL_AUTHENTICATION.getStatus())
                .data(ErrorCode.COMMON_FAIL_AUTHENTICATION.getMessage())
                .message(e.getMessage())
                .build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<CustomResponse<?>> Exception(Exception e) {
        log.error(Utils.getLogMaker(Utils.eLogType.USER), e.getLocalizedMessage());
        Arrays.stream(e.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().startsWith("com.illunex.emsaasrestapi"))
                .forEach(stackTraceElement -> {
                    log.error(Utils.getLogMaker(Utils.eLogType.USER), String.format("%s.%s:%d", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber()));
                });
        return new ResponseEntity<>(CustomResponse.builder()
                .status(ErrorCode.COMMON_INTERNAL_SERVER_ERROR.getStatus())
                .data(ErrorCode.COMMON_INTERNAL_SERVER_ERROR.getMessage())
                .message(e.getMessage())
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
