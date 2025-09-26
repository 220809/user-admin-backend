package com.dingzk.useradmin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dingzk.useradmin.common.ErrorCode;
import com.dingzk.useradmin.common.ResponseEntity;
import com.dingzk.useradmin.exception.BusinessException;
import com.dingzk.useradmin.model.domain.Group;
import com.dingzk.useradmin.model.domain.User;
import com.dingzk.useradmin.model.qo.GroupQo;
import com.dingzk.useradmin.model.request.CreateGroupRequest;
import com.dingzk.useradmin.model.request.UpdateGroupRequest;
import com.dingzk.useradmin.service.GroupService;
import com.dingzk.useradmin.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class GroupController {

    @Resource
    private GroupService groupService;

    @Resource
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Long> createGroup(@RequestBody CreateGroupRequest createGroupRequest, HttpServletRequest request) {
        if (createGroupRequest == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        Group group = new Group();
        BeanUtils.copyProperties(createGroupRequest, group);
        User currentUser = userService.getCurrentUser(request);
        long groupId = groupService.createGroup(group, currentUser);
        return ResponseEntity.success(groupId);
    }

    @PostMapping("/update")
    public ResponseEntity<Long> updateGroup(@RequestBody UpdateGroupRequest updateGroupRequest) {
        if (updateGroupRequest == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        Group group = new Group();
        BeanUtils.copyProperties(updateGroupRequest, group);
        boolean updateResult = groupService.updateById(group);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
        }
        return ResponseEntity.success(group.getId());
    }

    @PostMapping("/delete")
    public ResponseEntity<Long> deleteGroup(Long groupId) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        boolean deleteResult = groupService.removeById(groupId);
        if (!deleteResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        return ResponseEntity.success(groupId);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Group>> listGroups(GroupQo groupQo) {
        if (groupQo == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        Group group = new Group();
        BeanUtils.copyProperties(groupQo, group);
        QueryWrapper<Group> query = new QueryWrapper<>(group);

        List<Group> groupList = groupService.list(query);
        return ResponseEntity.success(groupList);
    }

    @GetMapping("/list/page")
    public ResponseEntity<Page<Group>> pageListGroups(GroupQo groupQo) {
        if (groupQo == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        Group group = new Group();
        BeanUtils.copyProperties(groupQo, group);
        QueryWrapper<Group> query = new QueryWrapper<>(group);

        Page<Group> groupPage =
                groupService.page(Page.of(groupQo.getPageNum(), groupQo.getPageSize()), query);

        return ResponseEntity.success(groupPage);
    }

    @GetMapping("/details")
    public ResponseEntity<Group> descGroup(Long groupId) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        Group group = groupService.getById(groupId);
        if (group == null) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "圈子不存在");
        }
        return ResponseEntity.success(group);
    }
}