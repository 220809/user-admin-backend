package com.dingzk.useradmin.service.impl;

import com.dingzk.useradmin.model.domain.User;
import com.dingzk.useradmin.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserService userService;

    @Test
    void getMatchUsers() {
        User user = new User();
        user.setTags("[\"Java\", \"游戏\"]");
        final List<User> matchUsers = userService.getMatchUsers(user);

        Assertions.assertNotNull(matchUsers);
    }
}