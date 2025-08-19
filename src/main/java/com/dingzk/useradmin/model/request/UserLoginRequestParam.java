package com.dingzk.useradmin.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestParam {
    private String userAccount;
    private String password;
}
