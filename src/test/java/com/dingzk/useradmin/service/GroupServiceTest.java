package com.dingzk.useradmin.service;

import com.dingzk.useradmin.model.domain.User;
import com.dingzk.useradmin.model.qo.GroupQo;
import com.dingzk.useradmin.model.request.UpdateGroupRequest;
import com.dingzk.useradmin.model.vo.GroupVo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
@SpringBootTest
class GroupServiceTest {

    @Resource
    private GroupService groupService;
    @Test
    void listGroups() {
        GroupQo groupQo = new GroupQo();
        groupQo.setName("小队");
        groupQo.setDescription("程序员");
        groupQo.setAccessLevel(0);

        List<GroupVo> groupList = groupService.listGroups(groupQo);

        Assertions.assertNotNull(groupList);
    }

    @Test
    void updateGroup() {
        UpdateGroupRequest request = new UpdateGroupRequest();
        request.setId(7L);
        request.setName("前端小队66");
        request.setAccessLevel(2);
        request.setPassword("");
        User user = new User();
        user.setUserId(2L);
        user.setUserRole(0);

        boolean result = groupService.updateGroup(request, user);

        Assertions.assertTrue(result);
    }
}