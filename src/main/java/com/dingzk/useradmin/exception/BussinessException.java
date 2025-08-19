package com.dingzk.useradmin.exception;

public class BussinessException extends RuntimeException{
    private final Integer code;

    public BussinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BussinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
