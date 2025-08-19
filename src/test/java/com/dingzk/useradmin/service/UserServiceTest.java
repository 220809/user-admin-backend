package com.dingzk.useradmin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingzk.useradmin.exception.UserServiceException;
import com.dingzk.useradmin.exception.enums.UserCodeEnum;
import com.dingzk.useradmin.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * UserService Test
 * @author ding
 * @date 2025-08-16
 */
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private final String TEST_USER_ACCOUNT = "testAccount1";

    private final String TEST_PASSWORD = "12345678";

    @Test
    void testUserRegisterFailure_WhenUserAccountIsBlank() {
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                        () -> userService.userRegister("", TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(UserCodeEnum.PARAMETER_BLANK.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserRegisterFailure_WhenPasswordIsBlank() {
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userRegister(TEST_USER_ACCOUNT, "", TEST_PASSWORD));
        Assertions.assertEquals(UserCodeEnum.PARAMETER_BLANK.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserRegisterFailure_WhenCheckedPasswordIsBlank() {
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userRegister(TEST_USER_ACCOUNT, TEST_PASSWORD, ""));
        Assertions.assertEquals(UserCodeEnum.PARAMETER_BLANK.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserRegisterFailure_WhenUserAccountLessThan4Chars() {
        String userAccount = "abc";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userRegister(userAccount, TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(UserCodeEnum.USER_ACCOUNT_TOO_SHORT.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserRegisterFailure_WhenPasswordLessThan8Chars() {
        String password = "1234567";
        String checkedPassword = "1234567";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userRegister(TEST_USER_ACCOUNT, password, checkedPassword));
        Assertions.assertEquals(UserCodeEnum.PASSWORD_TOO_SHORT.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserRegisterFailure_WhenPasswordNotEqualToCheckedPassword() {
        String checkedPassword = "123456789";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userRegister(TEST_USER_ACCOUNT, TEST_PASSWORD, checkedPassword));
        Assertions.assertEquals(UserCodeEnum.PASSWORD_MISMATCH.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserRegisterFailure_WhenUserAccountContainsSpecialChars() {
        String userAccount = "testAccount@1";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userRegister(userAccount, TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(UserCodeEnum.USER_ACCOUNT_INVALID.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserRegisterFailure_WhenDuplicatedUserAccount() {
        String userAccount = "testAccount";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userRegister(userAccount, TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(UserCodeEnum.USER_ACCOUNT_EXISTS.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserRegisterSuccess() {
        // Ensure the test user account does not already exist
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", TEST_USER_ACCOUNT);
        User user = userService.getOne(queryWrapper);
        if (user != null) {
            userService.removeById(user.getUserId());
        }
        userService.userRegister(TEST_USER_ACCOUNT, TEST_PASSWORD, TEST_PASSWORD);
    }

    @Test
    void testUserLoginFailure_WhenUserAccountIsBlank() {
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userLogin("", TEST_PASSWORD, null));
        Assertions.assertEquals(UserCodeEnum.PARAMETER_BLANK.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserLoginFailure_WhenPasswordIsBlank() {
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userLogin(TEST_USER_ACCOUNT, "", null));
        Assertions.assertEquals(UserCodeEnum.PARAMETER_BLANK.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserLoginFailure_WhenUserAccountLessThan4Chars() {
        String userAccount = "abc";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userLogin(userAccount, TEST_PASSWORD, null));
        Assertions.assertEquals(UserCodeEnum.USER_ACCOUNT_TOO_SHORT.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserLoginFailure_WhenPasswordLessThan8Chars() {
        String password = "1234567";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userLogin(TEST_USER_ACCOUNT, password, null));
        Assertions.assertEquals(UserCodeEnum.PASSWORD_TOO_SHORT.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserLoginFailure_WhenUserAccountContainsSpecialChars() {
        String userAccount = "testAccount@1";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userLogin(userAccount, TEST_PASSWORD, null));
        Assertions.assertEquals(UserCodeEnum.USER_ACCOUNT_INVALID.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserLoginFailure_WhenUserNotFound() {
        String userAccount = "nonExistentUser";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userLogin(userAccount, TEST_PASSWORD, null));
        Assertions.assertEquals(UserCodeEnum.USER_ACCOUNT_PASSWORD_MISMATCH.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserLoginFailure_WhenUserBlocked() {
        String userAccount = "blockedAccount";
        UserServiceException userServiceException = Assertions.assertThrows(UserServiceException.class,
                () -> userService.userLogin(userAccount, TEST_PASSWORD, null));
        Assertions.assertEquals(UserCodeEnum.USER_ALREADY_BLOCKED.name(),
                userServiceException.getUserCodeEnum().name());
    }

    @Test
    void testUserLoginSuccess() {
        User user = userService.userLogin(TEST_USER_ACCOUNT, TEST_PASSWORD, null);
        Assertions.assertEquals(TEST_USER_ACCOUNT, user.getUserAccount());
    }
}