package com.dingzk.useradmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingzk.useradmin.exception.UserServiceException;
import com.dingzk.useradmin.exception.enums.UserCodeEnum;
import com.dingzk.useradmin.model.User;
import com.dingzk.useradmin.service.UserService;
import com.dingzk.useradmin.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
* @author ding
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-08-16 20:44:00
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Autowired
    private UserMapper userMapper;

    private static final String SALT = "password";

    private static final String USER_ACCOUNT_REGEX = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$";

    private static final String USER_LOGIN_DATA = "userLoginData";

    @Override
    public long userRegister(String userAccount, String password, String checkedPassword) {
        // 账户名，密码，确认密码不能为空
        if (StringUtils.isAnyBlank(userAccount, password, checkedPassword)) {
            throw new UserServiceException(UserCodeEnum.PARAMETER_BLANK);
        }
        // 账户名不少于4位
        if (userAccount.length() < 4) {
            throw new UserServiceException(UserCodeEnum.USER_ACCOUNT_TOO_SHORT);
        }
        // 密码不少于8位
        if (password.length() < 8) {
            throw new UserServiceException(UserCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 密码和确认密码相同
        if (!password.equals(checkedPassword)) {
            throw new UserServiceException(UserCodeEnum.PASSWORD_MISMATCH);
        }
        // 校验账户名不包含特殊字符
        if (!userAccount.matches(USER_ACCOUNT_REGEX)) {
            throw new UserServiceException(UserCodeEnum.USER_ACCOUNT_INVALID);
        }
        // 用户名不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new UserServiceException(UserCodeEnum.USER_ACCOUNT_EXISTS);
        }

        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new UserServiceException(UserCodeEnum.USER_REGISTRATION_FAILED);
        }

        return user.getUserId();
    }

    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest request) {
        // 账户名，密码，确认密码不能为空
        if (StringUtils.isAnyBlank(userAccount, password)) {
            throw new UserServiceException(UserCodeEnum.PARAMETER_BLANK);
        }
        // 账户名不少于4位
        if (userAccount.length() < 4) {
            throw new UserServiceException(UserCodeEnum.USER_ACCOUNT_TOO_SHORT);
        }
        // 密码不少于8位
        if (password.length() < 8) {
            throw new UserServiceException(UserCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 校验账户名不包含特殊字符
        if (!userAccount.matches(USER_ACCOUNT_REGEX)) {
            throw new UserServiceException(UserCodeEnum.USER_ACCOUNT_INVALID);
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount)
                    .eq("password", encryptPassword);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new UserServiceException(UserCodeEnum.USER_ACCOUNT_PASSWORD_MISMATCH);
        }
        // 检查用户状态
        if (user.getStatus() == 2) {
            throw new UserServiceException(UserCodeEnum.USER_ALREADY_BLOCKED);
        }

        // 用户信息脱敏
        User NonSensitiveUser = new User();
        NonSensitiveUser.setUserId(user.getUserId());
        NonSensitiveUser.setUsername(user.getUsername());
        NonSensitiveUser.setEmail(user.getEmail());
        NonSensitiveUser.setUserAccount(user.getUserAccount());
        NonSensitiveUser.setAvatarUrl(user.getAvatarUrl());
        NonSensitiveUser.setGender(user.getGender());
        NonSensitiveUser.setStatus(user.getStatus());
        NonSensitiveUser.setCreatedAt(user.getCreatedAt());

        if (request != null) {
            HttpSession session = request.getSession();
            session.setAttribute(USER_LOGIN_DATA, NonSensitiveUser);
        }

        return NonSensitiveUser;
    }

    @Override
    public List<User> queryUsersByCondition(String username, Integer status, Date beginDate, Date endDate) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (beginDate != null) {
            queryWrapper.ge("created_at", beginDate);
        }
        if (endDate != null) {
            queryWrapper.le("created_at", endDate);
        }

        return userMapper.selectList(queryWrapper);
    }

    @Override
    public long deleteUserByUserId(long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserServiceException(UserCodeEnum.USER_NOT_FOUND);
        }

        return userMapper.deleteById(userId);
    }
}