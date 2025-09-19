package com.dingzk.useradmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingzk.useradmin.common.ErrorCode;
import com.dingzk.useradmin.constant.UserConstants;
import com.dingzk.useradmin.exception.BusinessException;
import com.dingzk.useradmin.model.User;
import com.dingzk.useradmin.service.UserService;
import com.dingzk.useradmin.mapper.UserMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

    @Override
    public long userRegister(String userAccount, String password, String checkedPassword) {
        // 账户名，密码，确认密码不能为空
        if (StringUtils.isAnyBlank(userAccount, password, checkedPassword)) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "账户名，密码，确认密码不能为空");
        }
        // 账户名不少于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "账户名少于4位");
        }
        // 密码不少于8位
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "密码少于8位");
        }
        // 密码和确认密码相同
        if (!password.equals(checkedPassword)) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "密码和确认密码不同");
        }
        // 校验账户名不包含特殊字符
        if (!userAccount.matches(USER_ACCOUNT_REGEX)) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "账户名包含特殊字符");
        }
        // 用户名不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_STATE_ERROR, "账户已存在");
        }

        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);
        // 用户名默认为账户名
        user.setUsername(userAccount);
        // 默认头像
        user.setAvatarUrl("https://robohash.org/example123");
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return user.getUserId();
    }

    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest request) {
        // 账户名，密码，确认密码不能为空
        if (StringUtils.isAnyBlank(userAccount, password)) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "账户名，密码，确认密码不能为空");
        }
        // 账户名不少于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "账户名少于4位");
        }
        // 密码不少于8位
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "密码少于8位");
        }
        // 校验账户名不包含特殊字符
        if (!userAccount.matches(USER_ACCOUNT_REGEX)) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "账户名包含特殊字符");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount)
                    .eq("password", encryptPassword);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_STATE_ERROR, "账户名密码不正确");
        }
        // 检查用户状态
        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.USER_STATE_ERROR, "用户已封禁");
        }

        // 记录最后登录时间
        user.setLastLoginAt(new Date());
        userMapper.updateById(user);

        // 用户信息脱敏
        User unsensitiveUser = makeUnsensitiveUser(user);

        if (request != null) {
            HttpSession session = request.getSession();
            session.setAttribute(UserConstants.USER_LOGIN_DATA, unsensitiveUser);
        }

        return unsensitiveUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstants.USER_LOGIN_DATA) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().removeAttribute(UserConstants.USER_LOGIN_DATA);
        return 0;
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

        // 脱敏
        return userMapper.selectList(queryWrapper).stream().map(this::makeUnsensitiveUser).toList();
    }

    @Override
    public List<User> queryUsers() {
        return userMapper.selectList(null).stream().map( this::makeUnsensitiveUser).toList();
    }

    @Override
    public long deleteUserByUserId(long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_STATE_ERROR, "用户不存在");
        }

        return userMapper.deleteById(userId);
    }

    @Override
    public void checkAuthority(HttpServletRequest request) {
        // 获取当前登录用户
        if (!hasAuthority(request)) {
            throw new BusinessException(ErrorCode.NO_AUTHORIZATION_ERROR);
        }
    }

    private boolean hasAuthority(HttpServletRequest request) {
        // 获取当前登录用户
        User user = this.getCurrentUser(request);
        return user.getUserRole() == UserConstants.ROLE_ADMIN;
    }

    private boolean hasAuthority(User currentUser) {
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        return currentUser.getUserRole() == UserConstants.ROLE_ADMIN;
    }

    @Override
    public User makeUnsensitiveUser(User user) {
        if (user == null) {
            return null;
        }
        User unsensitiveUser = new User();
        unsensitiveUser.setUserId(user.getUserId());
        unsensitiveUser.setUsername(user.getUsername());
        unsensitiveUser.setEmail(user.getEmail());
        unsensitiveUser.setUserAccount(user.getUserAccount());
        unsensitiveUser.setAvatarUrl(user.getAvatarUrl());
        unsensitiveUser.setGender(user.getGender());
        unsensitiveUser.setSlogan(user.getSlogan());
        unsensitiveUser.setStatus(user.getStatus());
        unsensitiveUser.setCreatedAt(user.getCreatedAt());
        unsensitiveUser.setUserRole(user.getUserRole());
        unsensitiveUser.setTags(user.getTags());
        return unsensitiveUser;
    }

    @Override
    public List<User> searchUserByAndTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }

        return searchUserByAndTags_usingPureSql(tagNameList);
    }

    /**
     * SQL 实现标签查询
     *
     * @param tagNameList 标签列表
     * @return 用户列表
     */
    private List<User> searchUserByAndTags_usingPureSql(List<String> tagNameList) {
        QueryWrapper<User> query = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            query = query.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(query);

        return userList.stream().map(this::makeUnsensitiveUser).toList();
    }

    /**
     * 内存 实现标签查询
     *
     * @param tagNameList 标签列表
     * @return 用户列表
     */
    private List<User> searchUserByAndTags_usingMemory(List<String> tagNameList) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.like("tags", tagNameList.get(0));
        List<User> userListBeforeFilter = userMapper.selectList(query);

        if (tagNameList.size() == 1) {
            return userListBeforeFilter.stream().map(this::makeUnsensitiveUser).toList();
        }

        Gson gson = new Gson();
        Type tagsType = new TypeToken<Set<String>>() {}.getType();
        List<User> userList = userListBeforeFilter.stream().filter(
                user -> {
                    Set<String> tagNameSet = gson.fromJson(user.getTags(), tagsType);
                    for (int i = 1; i < tagNameList.size(); ++i) {
                        if (!tagNameSet.contains(tagNameList.get(i))) {
                            return false;
                        }
                    }
                    return true;
                }
        ).toList();

        return userList.stream().map(this::makeUnsensitiveUser).toList();
    }

    @Override
    public int updateUser(User updatedUser, HttpServletRequest request) {
        if (updatedUser == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        // 校验权限，非管理员且非当前用户，不允许修改
        Long userId = updatedUser.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        // 当前登录用户
        User currentUser = getCurrentUser(request);
        // 获取要更新的用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            // 用户不存在
            throw new BusinessException(ErrorCode.USER_STATE_ERROR, "用户不存在");
        }
        if (!hasAuthority(currentUser) && !currentUser.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTHORIZATION_ERROR);
        }

        // 修改用户
        int result = userMapper.updateById(updatedUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstants.USER_LOGIN_DATA);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return (User) userObj;
    }

    @Override
    public List<User> getRecommendUsers(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);

        // 从前 50 条记录中，获取随机 8 条用户数据
        QueryWrapper<User> query = new QueryWrapper<>();
        Page<User> page = new Page<>(0, 50);
        Page<User> userPage = userMapper.selectPage(page, query);
        List<User> users = userPage.getRecords();
        Collections.shuffle(users);
        return users.stream().limit(8).toList();
    }
}