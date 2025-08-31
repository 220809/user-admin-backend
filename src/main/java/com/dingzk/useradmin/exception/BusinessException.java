package com.dingzk.useradmin.exception;

import com.dingzk.useradmin.common.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局业务异常类
 */
@AllArgsConstructor
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    private final String description;

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, "");
    }
}
