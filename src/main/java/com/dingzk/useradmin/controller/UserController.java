package com.dingzk.useradmin.controller;

import com.dingzk.useradmin.common.ErrorCode;
import com.dingzk.useradmin.common.ResponseEntity;
import com.dingzk.useradmin.constant.UserConstants;
import com.dingzk.useradmin.exception.BusinessException;
import com.dingzk.useradmin.model.User;
import com.dingzk.useradmin.model.request.UserLoginRequestParam;
import com.dingzk.useradmin.model.request.UserRegisterRequestParam;
import com.dingzk.useradmin.model.search.UserSearchRequestParam;
import com.dingzk.useradmin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController()
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173"})
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Long> userRegister(@RequestBody UserRegisterRequestParam param) {
        if (param == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        String userAccount = param.getUserAccount();
        String password = param.getPassword();
        String checkedPassword = param.getCheckedPassword();
        long result = userService.userRegister(userAccount, password, checkedPassword);
        return ResponseEntity.success(result);
    }

    @PostMapping("/login")
    public ResponseEntity<User> userLogin(@RequestBody UserLoginRequestParam param, HttpServletRequest request) {
        if (param == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        String userAccount = param.getUserAccount();
        String password = param.getPassword();
        User user = userService.userLogin(userAccount, password, request);
        return ResponseEntity.success(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        int result = userService.userLogout(request);
        return ResponseEntity.success(result);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> queryUsers(@RequestBody(required = false) UserSearchRequestParam param,
                                 HttpServletRequest request) {
        userService.checkAuthority(request);

        List<User> users;

        if (param == null) {
            users = userService.queryUsers();
            return ResponseEntity.success(users);
        }
        String username = param.getUsername();
        Integer status = param.getStatus();
        Date beginDate = param.getBeginDate();
        Date endDate = param.getEndDate();
        if (ObjectUtils.allNull(username, status, beginDate, endDate)) {
            users = userService.queryUsers();
        } else {
            users = userService.queryUsersByCondition(username, status, beginDate, endDate);
        }

        return ResponseEntity.success(users);
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<Long> deleteUserByUserId(@PathVariable("user_id") Long userId, HttpServletRequest request) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        userService.checkAuthority(request);
        long result = userService.deleteUserByUserId(userId);
        if (result <= 0L) {
            throw new BusinessException(ErrorCode.USER_STATE_ERROR, "用户不存在");
        }
        return ResponseEntity.success(result);
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstants.USER_LOGIN_DATA);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = user.getUserId();
        User currentUser = userService.getById(userId);

        User result = userService.makeUnsensitiveUser(currentUser);
        return ResponseEntity.success(result);
    }

    @GetMapping("/search/tags")
    public ResponseEntity<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }

        List<User> users = userService.searchUserByAndTags(tagNameList);

        return ResponseEntity.success(users);
    }
}
