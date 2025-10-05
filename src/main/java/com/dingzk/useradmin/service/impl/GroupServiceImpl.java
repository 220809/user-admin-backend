package com.dingzk.useradmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingzk.useradmin.common.ErrorCode;
import com.dingzk.useradmin.constant.UserConstants;
import com.dingzk.useradmin.exception.BusinessException;
import com.dingzk.useradmin.mapper.GroupMapper;
import com.dingzk.useradmin.model.domain.Group;
import com.dingzk.useradmin.model.domain.User;
import com.dingzk.useradmin.model.domain.UserGroup;
import com.dingzk.useradmin.model.enums.GroupAccessLevel;
import com.dingzk.useradmin.model.qo.GroupQo;
import com.dingzk.useradmin.model.request.JoinGroupRequest;
import com.dingzk.useradmin.model.request.UpdateGroupRequest;
import com.dingzk.useradmin.model.vo.GroupVo;
import com.dingzk.useradmin.service.GroupService;
import com.dingzk.useradmin.service.UserGroupService;
import com.dingzk.useradmin.utils.SqlUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
* @author ding
* @description 针对表【group】的数据库操作Service实现
* @createDate 2025-09-26 14:28:00
*/
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group>
    implements GroupService {

    @Resource
    private GroupMapper groupMapper;

    @Resource
    private UserGroupService userGroupService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public long createGroup(Group groupInfo, User loginUser) {
        // 1. 队伍参数校验：队伍参数不为空
        if (groupInfo == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        validateBeforeOperateGroupInfo(loginUser, groupInfo.getName(), groupInfo.getDescription());

        // 5. 圈子最大人数 >= 1, <= 20
        // 未传递默认为 10
        int maxCapacity = Optional.ofNullable(groupInfo.getMaxCapacity()).orElse(10);
        if (maxCapacity < 1 || maxCapacity > 20) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子最大人数目前仅支持1-20人");
        }
        groupInfo.setMaxCapacity(maxCapacity);
        // 6. 圈子 access_level 校验，
        // 没有传入 accessLevel，按搜索公开圈子处理
        Integer accessValue = Optional.ofNullable(groupInfo.getAccessLevel()).orElse(GroupAccessLevel.PUBLIC.getValue());
        GroupAccessLevel accessLevel = GroupAccessLevel.getAccessLevel(accessValue);
        if (accessLevel == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子访问等级错误");
        }
        groupInfo.setAccessLevel(accessValue);
        //   1. 为 加密 等级时，校验密码
        if (accessLevel == GroupAccessLevel.SECRET) {
        //   2. 密码长度 <= 8，数字类型
            if (StringUtils.isNotBlank(groupInfo.getPassword()) && groupInfo.getPassword().length() > 8) {
                throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "密码应为1-8位字符串");
            }

        }
        // 用户已参与的圈子超过10个，则不允许创建圈子
        QueryWrapper<UserGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getUserId());
        long groupJoined = userGroupService.count(queryWrapper);
        if (groupJoined >= 10) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "用户已加入圈子超过10个, 不允许创建圈子");
        }

        // 7. 用户可领队的圈子 <= 6
        QueryWrapper<Group> query = new QueryWrapper<>();
        query.eq("leader_id", loginUser.getUserId());
        long groupsLead = this.count(query);
        if (groupsLead >= 6) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "用户领队的圈子不能超过6个");
        }

        long result = transactionTemplate.execute(status -> {
            try {
                // 8. 插入圈子数据
                groupInfo.setLeaderId(loginUser.getUserId());
                int insertResult = groupMapper.insert(groupInfo);
                if (insertResult < 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                // 9. 插入用户-圈子关系数据
                UserGroup userGroup = new UserGroup();
                userGroup.setUserId(loginUser.getUserId());
                userGroup.setGroupId(groupInfo.getId());
                userGroup.setJoinTime(new Date());
                userGroupService.save(userGroup);
                return groupInfo.getId();
            } catch (Exception e) {
                status.setRollbackOnly();
            }
            return -1L;
        });

        if (result == -1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建圈子失败");
        }
        return result;
    }

    private void validateBeforeOperateGroupInfo(User user, String name, String desc) {
        // 2. 用户已登录
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 3. 圈子名不超过 10 个字符，最少一个字符
        if (StringUtils.isBlank(name) || name.length() > 10) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子名称应为1-10个字符");
        }
        // 4. 圈子描述不超过100字符
        if (StringUtils.isNotBlank(desc) && desc.length() > 100) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子描述不能超过100个字符");
        }
    }

    @Override
    public List<GroupVo> listGroups(GroupQo groupQo) {
        if (groupQo == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        final String name = groupQo.getName();
        final String description = groupQo.getDescription();
        final String keyword = groupQo.getKeyword();
        final Integer accessLevelValue = groupQo.getAccessLevel();
        // 访问级别校验
        // 没有传入 accessLevel，搜索全部
        GroupAccessLevel accessLevel = GroupAccessLevel.getAccessLevel(accessLevelValue);
        if (accessLevelValue != null && accessLevel == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        // todo 如何避免未传递参数时的全量搜索

        if (StringUtils.isNotBlank(name)) {
            groupQo.setName(SqlUtils.fullFuzzyValue(name));
        }

        if (StringUtils.isNotBlank(description)) {
            groupQo.setDescription(SqlUtils.fullFuzzyValue(description));
        }

        if (StringUtils.isNotBlank(keyword)) {
            groupQo.setKeyword(SqlUtils.fullFuzzyValue(keyword));
        }

        return groupMapper.listGroups(groupQo);
    }

    @Override
    public boolean updateGroup(UpdateGroupRequest updateGroupRequest, User loginUser) {
        // 空参校验
        if (updateGroupRequest == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        validateBeforeOperateGroupInfo(loginUser, updateGroupRequest.getName(), updateGroupRequest.getDescription());

        // 圈子是否存在: 查询数据库
        // 修改权限校验: 圈子存在且用户符合权限
        Group groupToUpdate = this.validateAndRetrievedByGroupId(updateGroupRequest.getId());
        Long leaderId = groupToUpdate.getLeaderId();
        // 非管理员/队长不允许修改队伍
        if (!loginUser.getUserId().equals(leaderId) && !loginUser.getUserRole().equals(UserConstants.ROLE_ADMIN)) {
            throw new BusinessException(ErrorCode.NO_AUTHORIZATION_ERROR);
        }

        Integer accessLevelValue = updateGroupRequest.getAccessLevel();
        if (accessLevelValue != null) {
            GroupAccessLevel accessLevel = GroupAccessLevel.getAccessLevel(accessLevelValue);
            if (accessLevel == null) {
                throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "访问级别错误");
            }
            // 访问级别改为非加密时，若用户传入了密码，忽略
            if (!GroupAccessLevel.SECRET.equals(accessLevel)) {
                updateGroupRequest.setPassword(null);
            } else {
                // 访问级别为加密时，若目前圈子为加密状态，可不指定密码
                GroupAccessLevel groupAccessLevel = GroupAccessLevel.getAccessLevel(groupToUpdate.getAccessLevel());
                // 目前圈子不是私密状态，需校验密码
                if (!GroupAccessLevel.SECRET.equals(groupAccessLevel) &&
                        (StringUtils.isBlank(updateGroupRequest.getPassword()) || updateGroupRequest.getPassword().length() > 8)) {
                    throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "密码应为1-8位字符串");
                }
            }
        }

        long result = groupMapper.updateGroup(updateGroupRequest);
        return result == 1;
    }

    /**
     * 校验groupId并获取group
     * @param groupId
     * @return
     */
    private Group validateAndRetrievedByGroupId(Long groupId) {
        if (groupId == null || groupId <= 0) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子 ID 不符合条件");
        }
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "未找到对应圈子");
        }
        return group;
    }

    @Override
    public boolean joinGroup(JoinGroupRequest joinGroupRequest, User loginUser) {
        if (joinGroupRequest == null) {
            throw new BusinessException(ErrorCode.NULL_PARAM_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 圈子存在
        final Long groupId = joinGroupRequest.getGroupId();
        Group group = this.validateAndRetrievedByGroupId(groupId);
        // 不能加入私有圈子
        if (GroupAccessLevel.PRIVATE.getValue() == group.getAccessLevel()) {
            throw new BusinessException(ErrorCode.NO_AUTHORIZATION_ERROR, "不能加入私有圈子");
        }
        // 加入加密圈子需要校验密码
        if (GroupAccessLevel.SECRET.getValue() == group.getAccessLevel()) {
            if (!group.getPassword().equals(joinGroupRequest.getPassword())) {
                throw new BusinessException(ErrorCode.STATE_ERROR, "密码不正确");
            }
        }

        // 用户最多可加入10个圈子
        QueryWrapper<UserGroup> query = new QueryWrapper<>();

        final Long userId = loginUser.getUserId();
        query.eq("user_id", userId);
        long groupsJoined = userGroupService.count(query);
        if (groupsJoined >= 10) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "用户最多可加入10个圈子");
        }

        // 人数未满
        // 查询队伍人数
        query = new QueryWrapper<>();
        query.eq("group_id", groupId);
        long usersJoined = userGroupService.count(query);
        if (usersJoined >= group.getMaxCapacity()) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "圈子人数已满");
        }
        // 用户未加入
        query.eq("user_id", userId);
        boolean hasUserJoined = userGroupService.exists(query);
        if (hasUserJoined) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "用户已加入此圈子");
        }

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(groupId);
        userGroup.setUserId(userId);
        return userGroupService.save(userGroup);
    }

    @Override
    public boolean quitGroup(Long groupId, User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Group group = this.validateAndRetrievedByGroupId(groupId);
        QueryWrapper<UserGroup> query = new QueryWrapper<>();
        query.eq("group_id", groupId);
        long userCount = userGroupService.count(query);

        query.eq("user_id", loginUser.getUserId());
        UserGroup one = userGroupService.getOne(query);
        if (one == null) {
            throw new BusinessException(ErrorCode.STATE_ERROR, "用户不在此圈子中");
        }

        return transactionTemplate.execute((status) -> {
            try {
                if (userCount == 1) {
                    // 圈子中只有最后一人
                    // 删除队伍
                    groupMapper.deleteById(groupId);
                }
                if (userCount > 1) {
                    // 还剩至少两人
                    if (loginUser.getUserId().equals(group.getLeaderId())) {
                        // 退出圈子的是队长
                        // 将队长id置空
                        groupMapper.updateGroupLeaderId(null, groupId);
                    }
                }
                // 删除用户-圈子记录
                return userGroupService.removeById(one.getId());
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
            }
        });
    }

    @Override
    public boolean dismissGroup(Long groupId, User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Group group = this.validateAndRetrievedByGroupId(groupId);
        // 是否是圈子队长
        if (!loginUser.getUserId().equals(group.getLeaderId())) {
            throw new BusinessException(ErrorCode.NO_AUTHORIZATION_ERROR);
        }
        return transactionTemplate.execute(status -> {
            try {
                // 移除圈子与成员的关联关系
                QueryWrapper<UserGroup> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("group_id", groupId);
                userGroupService.remove(queryWrapper);
                // 删除圈子
                return groupMapper.deleteById(groupId) == 1;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散队伍失败");
            }
        });
    }

    @Override
    public List<GroupVo> listJoinedGroups(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        QueryWrapper<UserGroup> queryWrapper = new QueryWrapper<>();
        Long userId = loginUser.getUserId();
        queryWrapper.eq("user_id", userId);
        List<UserGroup> userGroupList = userGroupService.list(queryWrapper);
        if (CollectionUtils.isEmpty(userGroupList)) {
            return new ArrayList<>();
        }

        List<Long> groupIdList = userGroupList.stream().map(UserGroup::getGroupId).toList();
        List<GroupVo> groupList = groupMapper.listJoinedGroups(groupIdList);
        groupList.sort((g1, g2) -> {
            // 将当前用户担任队长的圈子排在前面
            // 为队长/不为队长的圈子中，按id排序
            if (g1.getLeader() != null && g2.getLeader() != null) {
                if (g1.getLeader().getUserId().equals(userId) ^ g2.getLeader().getUserId().equals(userId)) {
                    return g1.getLeader().getUserId().equals(userId) ? -1 : 1;
                }
            }
            if (g1.getLeader() != null ^ g2.getLeader() != null) {
                if (g1.getLeader() != null && g1.getLeader().getUserId().equals(userId)) {
                    return -1;
                }
                if (g2.getLeader() != null && g2.getLeader().getUserId().equals(userId)) {
                    return 1;
                }
            }
            return g1.getId().compareTo(g2.getId());
        });
        return groupList;
    }

    @Override
    public GroupVo listGroupDetails(Long groupId) {
        if (groupId == null || groupId <= 0) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        GroupQo groupQo = new GroupQo();
        groupQo.setId(groupId);
        List<GroupVo> groupList = groupMapper.listGroups(groupQo);
        if (CollectionUtils.isEmpty(groupList)) {
            return null;
        }
        if (groupList.size() > 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取圈子信息失败");
        }

        return groupList.get(0);
    }

    @Override
    public List<Long> listGroupUserIds(Long groupId) {
        Group group = validateAndRetrievedByGroupId(groupId);
        QueryWrapper<UserGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", group.getId());
        List<UserGroup> userGroupList = userGroupService.list(queryWrapper);
        if (CollectionUtils.isEmpty(userGroupList)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                userGroupList.stream()
                        .collect(Collectors.groupingBy(UserGroup::getUserId))
                        .keySet()
        );
    }
}