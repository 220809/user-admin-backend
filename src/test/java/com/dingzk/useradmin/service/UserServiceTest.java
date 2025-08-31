package com.dingzk.useradmin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingzk.useradmin.exception.BusinessException;
import com.dingzk.useradmin.common.ErrorCode;
import com.dingzk.useradmin.mapper.UserMapper;
import com.dingzk.useradmin.model.User;
import com.dingzk.useradmin.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * UserService Test
 * @author ding
 * @date 2025-08-16
 */
@SpringBootTest
public class UserServiceTest {

    @MockitoBean
    private UserMapper userMapper;
    @Autowired
    private UserService userService = new UserServiceImpl();

    private static final String TEST_USER_ACCOUNT1 = "testAccount1";
    private static final String TEST_USER_ACCOUNT2 = "testAccount2";

    private static final String TEST_BLOCKED_ACCOUNT = "blockedAccount";

    private static final String TEST_PASSWORD = "12345678";

    private static List<User> testUserList;
    
    private static User TEST_USER1;
    private static User TEST_USER2;
    private static User TEST_BLOCKED_USER;

    private void initTestUserList() {
        TEST_USER1 = new User();
        TEST_USER1.setUserId(1L);
        TEST_USER1.setUserAccount(TEST_USER_ACCOUNT1);
        TEST_USER1.setUsername("testUser1");
        TEST_USER1.setPassword(TEST_PASSWORD);
        TEST_USER1.setStatus(1);

        TEST_USER2 = new User();
        TEST_USER2.setUserId(2L);
        TEST_USER2.setUserAccount(TEST_USER_ACCOUNT2);
        TEST_USER2.setUsername("testUser2");
        TEST_USER2.setPassword(TEST_PASSWORD);
        TEST_USER2.setStatus(1);

        TEST_BLOCKED_USER = new User();
        TEST_BLOCKED_USER.setUserId(3L);
        TEST_BLOCKED_USER.setUserAccount(TEST_BLOCKED_ACCOUNT);
        TEST_BLOCKED_USER.setUsername("blockedUser");
        TEST_BLOCKED_USER.setPassword(TEST_PASSWORD);
        TEST_BLOCKED_USER.setStatus(2);
        testUserList = List.of(TEST_USER1, TEST_USER2, TEST_BLOCKED_USER);
    }

    @Test
    void testUserRegisterFailure_WhenUserAccountIsBlank() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                        () -> userService.userRegister("", TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserRegisterFailure_WhenPasswordIsBlank() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister(TEST_USER_ACCOUNT1, "", TEST_PASSWORD));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserRegisterFailure_WhenCheckedPasswordIsBlank() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister(TEST_USER_ACCOUNT1, TEST_PASSWORD, ""));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserRegisterFailure_WhenUserAccountLessThan4Chars() {
        String userAccount = "abc";
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister(userAccount, TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserRegisterFailure_WhenPasswordLessThan8Chars() {
        String password = "1234567";
        String checkedPassword = "1234567";
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister(TEST_USER_ACCOUNT1, password, checkedPassword));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserRegisterFailure_WhenPasswordNotEqualToCheckedPassword() {
        String checkedPassword = "123456789";
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister(TEST_USER_ACCOUNT1, TEST_PASSWORD, checkedPassword));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserRegisterFailure_WhenUserAccountContainsSpecialChars() {
        String userAccount = "testAccount@1";
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister(userAccount, TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserRegisterFailure_WhenDuplicatedUserAccount() {
        String userAccount = TEST_USER_ACCOUNT1;
        // 模拟用户已存在
        Mockito.when(userMapper.selectCount(Mockito.any(QueryWrapper.class)))
                .thenReturn(1L);
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister(userAccount, TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(ErrorCode.USER_STATE_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserRegisterSuccess() {
        // 模拟用户不存在
        Mockito.when(userMapper.selectCount(Mockito.any(QueryWrapper.class)))
                .thenReturn(0L);

        // 模拟向数据库插入用户
        Mockito.when(userMapper.insert(Mockito.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserId(1L); // 模拟数据库返回的id
                    return 1; // insert 返回值
                });
        long result = userService.userRegister(TEST_USER_ACCOUNT1, TEST_PASSWORD, TEST_PASSWORD);
        Assertions.assertEquals(1L, result);
        Mockito.verify(userMapper, Mockito.times(1)).insert(Mockito.any(User.class));
    }

    @Test
    void testUserLoginFailure_WhenUserAccountIsBlank() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userLogin("", TEST_PASSWORD, null));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserLoginFailure_WhenPasswordIsBlank() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userLogin(TEST_USER_ACCOUNT1, "", null));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserLoginFailure_WhenUserAccountLessThan4Chars() {
        String userAccount = "abc";
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userLogin(userAccount, TEST_PASSWORD, null));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserLoginFailure_WhenPasswordLessThan8Chars() {
        String password = "1234567";
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userLogin(TEST_USER_ACCOUNT1, password, null));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserLoginFailure_WhenUserAccountContainsSpecialChars() {
        String userAccount = "testAccount@1";
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userLogin(userAccount, TEST_PASSWORD, null));
        Assertions.assertEquals(ErrorCode.BAD_PARAM_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserLoginFailure_WhenUserNotFound() {
        String userAccount = "nonExistentUser";
        Mockito.when(userMapper.selectOne(Mockito.any(QueryWrapper.class)))
                .thenReturn(null); // 模拟用户不存在

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userLogin(userAccount, TEST_PASSWORD, null));
        Assertions.assertEquals(ErrorCode.USER_STATE_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserLoginFailure_WhenUserBlocked() {
        String userAccount = "blockedAccount";
        // 模拟用户被封禁
        Mockito.when(userMapper.selectOne(Mockito.any(QueryWrapper.class)))
                .thenReturn(new User() {{
                    setStatus(2);  // 模拟封禁
                }});
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.userLogin(userAccount, TEST_PASSWORD, null));
        Assertions.assertEquals(ErrorCode.USER_STATE_ERROR.getCode(),
                exception.getCode());
    }

    @Test
    void testUserLoginSuccess() {
        initTestUserList();

        Mockito.when(userMapper.selectOne(Mockito.any(QueryWrapper.class)))
                .thenReturn(TEST_USER1); // 模拟用户存在且未封禁
        User user = userService.userLogin(TEST_USER_ACCOUNT1, TEST_PASSWORD, null);
        Assertions.assertEquals(TEST_USER_ACCOUNT1, user.getUserAccount());
    }

    @Test
    void testQueryAllUsers_WhenSearchParamIsNull() {
        initTestUserList();

        Mockito.when(userMapper.selectList(Mockito.any(QueryWrapper.class)))
                .thenReturn(testUserList);

        List<User> users = userService.queryUsersByCondition(null, null, null, null);
        Assertions.assertIterableEquals(testUserList.stream().peek(user -> user.setPassword(null)).toList(), users);
    }

    @Test
    void testQueryUsers_WhenConditionLikeUsergetCode() {
        initTestUserList();

        String likeName = "User2";
        List<User> result = List.of(TEST_USER2);

        Mockito.when(userMapper.selectList(Mockito.any(QueryWrapper.class)))
                .thenReturn(result);
        List<User> users = userService.queryUsersByCondition(likeName, null, null, null);
        Assertions.assertIterableEquals(result.stream().peek(user -> user.setPassword(null)).toList(), users);
    }

    @Test
    void testQueryUsers_WhenConditionUserBlocked() {
        initTestUserList();

        List<User> result = List.of(TEST_BLOCKED_USER);

        Mockito.when(userMapper.selectList(Mockito.any(QueryWrapper.class)))
                .thenReturn(result);

        List<User> users = userService.queryUsersByCondition(null, 2, null, null);
        Assertions.assertIterableEquals(result.stream().peek(user -> user.setPassword(null)).toList(), users);
    }

    @Test
    void testQueryUsers_WhenConditionByDate() {
        initTestUserList();

        List<User> result = List.of(TEST_USER1, TEST_USER2);

        Mockito.when(userMapper.selectList(Mockito.any(QueryWrapper.class)))
                .thenReturn(result);

        List<User> users =
                userService.queryUsersByCondition(null, null,
                        new Date(2025, Calendar.AUGUST,18),
                        new Date(2025, Calendar.AUGUST,19));
        Assertions.assertIterableEquals(result.stream().peek(user -> user.setPassword(null)).toList(), users);
    }

    @Test
    void testDeleteUserByUserId_ThrowsException_WhenUserNotExist() {
        long userId = 4L;
        Mockito.when(userMapper.selectById(userId))
                .thenReturn(null);
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userService.deleteUserByUserId(userId));
        Assertions.assertEquals(ErrorCode.USER_STATE_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testDeleteUserByUserId_Success() {
        initTestUserList();

        long userId = 1L;
        List<User> users = new ArrayList<>(testUserList);
        Mockito.when(userMapper.selectById(userId))
                .thenReturn(TEST_USER1);
        Mockito.when(userMapper.deleteById(userId))
                .thenAnswer(invocation -> {
                    users.removeIf(user -> user.getUserId() == userId);
                    return 1; // 模拟删除成功
                });
        long result = userService.deleteUserByUserId(userId);
        Assertions.assertEquals(1L, result);
        Assertions.assertFalse(users.contains(TEST_USER1));
    }
}