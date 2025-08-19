package com.dingzk.useradmin.exception;

import com.dingzk.useradmin.exception.enums.UserCodeEnum;
import lombok.Getter;

public class UserServiceException extends BussinessException{
    @Getter
    private UserCodeEnum userCodeEnum;

    public UserServiceException(UserCodeEnum userCodeEnum) {
        super(userCodeEnum.getCode(), userCodeEnum.getMessage());
        this.userCodeEnum = userCodeEnum;
    }
}