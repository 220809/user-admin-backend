package com.dingzk.useradmin.service;

import com.dingzk.useradmin.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
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
     * @param request request
     * @return 用户
     */
    User userLogin(String userAccount, String password, HttpServletRequest request);

    /**
     * 查询所有用户
     * @param username 用户名
     * @param status 用户状态
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 用户列表
     */
    List<User> queryUsersByCondition(String username, Integer status, Date beginDate, Date endDate);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> queryUsers();

    /**
     * 根据用户id删除用户
     * @param userId 用户id
     * @return 删除结果
     */
    long deleteUserByUserId(long userId);

    /**
     * 检查用户权限
     * @param request 请求
     */
    void checkAuthority(HttpServletRequest request);

    /**
     * 用户数据脱敏
     * @param user 用户
     * @return 脱敏后的用户
     */
    User makeUnsensitiveUser(User user);

    /**
     * 退出登录
     * @param request HttpservletRequest
     * @return 状态
     */
    int userLogout(HttpServletRequest request);

    /**
     * 用户标签系统 - 根据标签查询用户
     * 标签全部存在
     *
     * @param tagNameList 选择的标签列表
     * @return 用户列表
     */
    List<User> searchUserByAndTags(List<String> tagNameList);

    /**
     * 更新用户
     * @param updatedUser 更新用户数据
     * @param request request
     * @return result
     */
    int updateUser(User updatedUser, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request request
     * @return user
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 获取推荐用户
     * @param request request
     * @return userList
     */
    List<User> getRecommendUsers(HttpServletRequest request);
}