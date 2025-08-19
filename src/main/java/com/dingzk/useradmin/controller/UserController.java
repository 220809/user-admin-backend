package com.dingzk.useradmin.controller;

import com.dingzk.useradmin.model.User;
import com.dingzk.useradmin.model.request.UserLoginRequestParam;
import com.dingzk.useradmin.model.request.UserRegisterRequestParam;
import com.dingzk.useradmin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/user/register")
    public Long userRegister(@RequestBody UserRegisterRequestParam param) {
        if (param == null) {
            return null;
        }
        String userAccount = param.getUserAccount();
        String password = param.getPassword();
        String checkedPassword = param.getCheckedPassword();
        return userService.userRegister(userAccount, password, checkedPassword);
    }

    @PostMapping("/user/login")
    public User userLogin(@RequestBody UserLoginRequestParam param, HttpServletRequest request) {
        if (param == null) {
            return null;
        }
        String userAccount = param.getUserAccount();
        String password = param.getPassword();
        return userService.userLogin(userAccount, password, request);
    }
}
