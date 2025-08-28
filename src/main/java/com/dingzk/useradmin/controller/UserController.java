package com.dingzk.useradmin.controller;

import com.dingzk.useradmin.constant.UserConstants;
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

@RestController()
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequestParam param) {
        if (param == null) {
            return null;
        }
        String userAccount = param.getUserAccount();
        String password = param.getPassword();
        String checkedPassword = param.getCheckedPassword();
        return userService.userRegister(userAccount, password, checkedPassword);
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequestParam param, HttpServletRequest request) {
        if (param == null) {
            return null;
        }
        String userAccount = param.getUserAccount();
        String password = param.getPassword();
        return userService.userLogin(userAccount, password, request);
    }

    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return userService.userLogout(request);
    }

    @GetMapping("/list")
    public List<User> queryUsers(@RequestBody(required = false) UserSearchRequestParam param,
                                 HttpServletRequest request) {
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

    @DeleteMapping("/{user_id}")
    public Long deleteUserByUserId(@PathVariable("user_id") Long userId, HttpServletRequest request) {
        userService.checkAuthority(request);

        if (userId == null) {
            return null;
        }
        return userService.deleteUserByUserId(userId);
    }

    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstants.USER_LOGIN_DATA);
        if (user == null) {
            return null;
        }
        long userId = user.getUserId();
        User currentUser = userService.getById(userId);

        return userService.makeUnsensitiveUser(currentUser);
    }
}
