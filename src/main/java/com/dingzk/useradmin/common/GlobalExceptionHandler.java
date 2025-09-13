package com.dingzk.useradmin.common;

import com.dingzk.useradmin.exception.BusinessException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden  // knife4j 500 NoSuchMethodError
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = {BusinessException.class})
    public ResponseEntity<?> businessExceptionHandler(BusinessException e) {
        log.error("Business Exception Message: {}\t Cause: {}", e.getMessage(), e.getStackTrace()[0]);
        return ResponseEntity.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(exception = {RuntimeException.class})
    public ResponseEntity<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("Runtime Exception Message: {}\t Cause: {}", e.getMessage(), e.getStackTrace()[0]);
        return ResponseEntity.error(500, e.getMessage(), "系统异常");
    }
}
