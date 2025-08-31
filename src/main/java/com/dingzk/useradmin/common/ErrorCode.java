package com.dingzk.useradmin.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    NULL_PARAM_ERROR(40000, "请求参数为空"),
    BAD_PARAM_ERROR(40001, "请求参数错误"),
    NO_AUTHORIZATION_ERROR(40100, "没有权限"),
    NOT_LOGIN_ERROR(40101, "未登录"),
    USER_STATE_ERROR(40300, "用户状态错误"),
    SYSTEM_ERROR(50000, "系统错误");


    private final int code;

    private final String message;
}
