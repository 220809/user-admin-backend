package com.dingzk.useradmin.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequestParam {
    private String userAccount;
    private String password;
    private String checkedPassword;
}
