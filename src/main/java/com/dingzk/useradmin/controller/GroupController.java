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
import com.dingzk.useradmin.model.request.JoinGroupRequest;
import com.dingzk.useradmin.model.request.UpdateGroupRequest;
import com.dingzk.useradmin.model.vo.GroupVo;
import com.dingzk.useradmin.service.GroupService;
import com.dingzk.useradmin.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
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
    public ResponseEntity<Long> updateGroup(@RequestBody UpdateGroupRequest updateGroupRequest, HttpServletRequest request) {
        if (updateGroupRequest == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean updateResult = groupService.updateGroup(updateGroupRequest, currentUser);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
        }
        return ResponseEntity.success(updateGroupRequest.getId());
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
    public ResponseEntity<List<GroupVo>> listGroups(@ParameterObject GroupQo groupQo) {
        if (groupQo == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }

        List<GroupVo> groupList = groupService.listGroups(groupQo);
        return ResponseEntity.success(groupList);
    }

    @GetMapping("/list/page")
    public ResponseEntity<Page<Group>> pageListGroups(@ParameterObject GroupQo groupQo) {
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
    public ResponseEntity<GroupVo> descGroup(Long groupId, HttpServletRequest request) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        GroupVo group = groupService.listGroupDetails(groupId);
        if (group == null) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "圈子不存在");
        }
        return ResponseEntity.success(group);
    }

    @PostMapping("/join")
    public ResponseEntity<Boolean> joinGroup(@ParameterObject @RequestBody JoinGroupRequest joinGroupRequest,
                                             HttpServletRequest request) {
        if (joinGroupRequest == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }

        User currentUser = userService.getCurrentUser(request);
        boolean result = groupService.joinGroup(joinGroupRequest, currentUser);

        return ResponseEntity.success(result);
    }

    @PostMapping("/quit")
    public ResponseEntity<Boolean> quitGroup(Long groupId, HttpServletRequest request) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = groupService.quitGroup(groupId, currentUser);
        return ResponseEntity.success(result);
    }

    @PostMapping("/dismiss")
    public ResponseEntity<Boolean> dismissGroup(Long groupId, HttpServletRequest request) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = groupService.dismissGroup(groupId, currentUser);
        return ResponseEntity.success(result);
    }

    @GetMapping("/joined")
    public ResponseEntity<List<GroupVo>> listJoinedGroups(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);

        List<GroupVo> groupList = groupService.listJoinedGroups(currentUser);
        return ResponseEntity.success(groupList);
    }

    @GetMapping("/userIds")
    public ResponseEntity<List<Long>> listGroupUsers(Long groupId) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }

        List<Long> groupUserIds = groupService.listGroupUserIds(groupId);
        return ResponseEntity.success(groupUserIds);
    }
}