package com.dingzk.useradmin.exception.enums;

public enum UserCodeEnum implements BusinessCodeEnum {
    PARAMETER_BLANK(-1, "userAccount, password and checkedPassword cannot be blank"),
    USER_ACCOUNT_TOO_SHORT(-4, "userAccount must be at least 4 characters long"),
    PASSWORD_TOO_SHORT(-5, "password must be at least 8 characters long"),
    PASSWORD_MISMATCH(-6, "password and checkedPassword do not match"),
    USER_ACCOUNT_INVALID(-7, "userAccount contains invalid characters"),
    USER_ACCOUNT_EXISTS(-8, "userAccount already exists"),
    USER_ACCOUNT_PASSWORD_MISMATCH(-9, "userAccount does not match the password"),
    USER_REGISTRATION_FAILED(-10, "user registration failed"),
    USER_NOT_FOUND(-11, "user not found"),
    USER_ALREADY_BLOCKED(-12, "user is already blocked");

    private final Integer code;
    private final String message;

    UserCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
