package com.dingzk.useradmin.service;

import com.dingzk.useradmin.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.util.List;

@SpringBootTest
class UserTagServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void searchUserByAndTags() {
        List<String> inputTagNameList = List.of("Java", "游戏");

        List<User> userList = userService.searchUserByAndTags(inputTagNameList);

        Assertions.assertFalse(CollectionUtils.isEmpty(userList));
    }
}