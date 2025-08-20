package com.dingzk.useradmin.controller;

import com.dingzk.useradmin.model.User;
import com.dingzk.useradmin.model.request.UserLoginRequestParam;
import com.dingzk.useradmin.model.request.UserRegisterRequestParam;
import com.dingzk.useradmin.model.search.UserSearchRequestParam;
import com.dingzk.useradmin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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

    @GetMapping("/users")
    public List<User> queryUsers(@RequestBody UserSearchRequestParam param, HttpServletRequest request) {
        userService.checkAuthority(request);

        if (param == null) {
            return userService.queryUsers();
        }
        String username = param.getUsername();
        Integer status = param.getStatus();
        Date beginDate = param.getBeginDate();
        Date endDate = param.getEndDate();
        if (ObjectUtils.allNull(username, status, beginDate, endDate)) {
            return userService.queryUsers();
        }

        return userService.queryUsersByCondition(username, status, beginDate, endDate);
    }

    @DeleteMapping("/user/{user_id}")
    public Long deleteUserByUserId(@PathVariable("user_id") Long userId, HttpServletRequest request) {
        userService.checkAuthority(request);

        if (userId == null) {
            return null;
        }
        return userService.deleteUserByUserId(userId);
    }
}
