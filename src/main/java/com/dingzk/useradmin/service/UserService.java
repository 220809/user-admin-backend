package com.dingzk.useradmin.service;

import com.dingzk.useradmin.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author ding
* @description 针对表【user】的数据库操作Service
* @date 2025-08-16
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 账户名
     * @param password   密码
     * @param checkedPassword  确认密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String password, String checkedPassword);

    /**
     * 用户登录
     *
     * @param userAccount 账户名
     * @param password    密码
     * @param request
     * @return 用户
     */
    User userLogin(String userAccount, String password, HttpServletRequest request);
}