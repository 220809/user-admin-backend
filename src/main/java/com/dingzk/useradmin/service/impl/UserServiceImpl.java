package com.dingzk.useradmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingzk.useradmin.common.ErrorCode;
import com.dingzk.useradmin.constant.UserConstants;
import com.dingzk.useradmin.exception.BusinessException;
import com.dingzk.useradmin.mapper.UserMapper;
import com.dingzk.useradmin.model.domain.User;
import com.dingzk.useradmin.model.vo.UserVo;
import com.dingzk.useradmin.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
* @author ding
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-08-16 20:44:00
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Autowired
    private UserMapper userMapper;

    private static final String SALT = "password";

    private static final String USER_ACCOUNT_REGEX = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
            throw new BusinessException(ErrorCode.STATE_ERROR, "账户已存在");
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
            throw new BusinessException(ErrorCode.STATE_ERROR, "账户名密码不正确");
        }
        // 检查用户状态
        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "用户已封禁");
        }

        // 记录最后登录时间
        user.setLastLoginAt(new Date());
        userMapper.updateById(user);

        if (request != null) {
            // 脱敏
            User nonSensitiveUser = makeNonSensitive(user);
            HttpSession session = request.getSession();
            session.setAttribute(UserConstants.USER_LOGIN_DATA, nonSensitiveUser);
        }

        return user;
    }

    private User makeNonSensitive(User user) {
        User nonSensitiveUser = new User();
        BeanUtils.copyProperties(user, nonSensitiveUser);
        nonSensitiveUser.setPassword(null);
        return nonSensitiveUser;
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
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public List<User> queryUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public long deleteUserByUserId(long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "用户不存在");
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
    public UserVo convertToUserVo(User user) {
        if (user == null) {
            return null;
        }
        UserVo userVo = new UserVo();
        userVo.setUserId(user.getUserId());
        userVo.setUsername(user.getUsername());
        userVo.setEmail(user.getEmail());
        userVo.setUserAccount(user.getUserAccount());
        userVo.setAvatarUrl(user.getAvatarUrl());
        userVo.setGender(user.getGender());
        userVo.setSlogan(user.getSlogan());
        userVo.setStatus(user.getStatus());
        userVo.setCreatedAt(user.getCreatedAt());
        userVo.setUserRole(user.getUserRole());
        userVo.setTags(user.getTags());
        return userVo;
    }

    @Override
    public List<UserVo> convertToUserVoList(List<User> users) {
        if (CollectionUtils.isEmpty(users)) {
            return new ArrayList<>();
        }

        return users.stream().map(this::convertToUserVo).toList();
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

        return userList;
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
            return userListBeforeFilter;
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

        return userList;
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
            throw new BusinessException(ErrorCode.STATE_ERROR, "用户不存在");
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

        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        final String recommendUsersKey = "bitbuddy:user:recommend:" + currentUser.getUserId();
        List<User> cachedRecommendUsers = (List<User>) operations.get(recommendUsersKey);
        // 缓存中有数据
        if (cachedRecommendUsers != null) {
            return cachedRecommendUsers;
        }

        // 缓存中无数据
        Page<User> userPage = userMapper.selectPage(Page.of(1, 10), null);
        List<User> users = userPage.getRecords();
        // 脱敏
        List<User> nonSensitiveUsers = users.stream()
                .map(this::makeNonSensitive)
                .toList();

        try {
            operations.set(recommendUsersKey, nonSensitiveUsers, 60, TimeUnit.SECONDS);  // 设置 60 秒过期时间用于测试
        } catch (Exception e) {
            log.error("Error creating key for recommend users: ", e);
        }
        return nonSensitiveUsers;
    }
}