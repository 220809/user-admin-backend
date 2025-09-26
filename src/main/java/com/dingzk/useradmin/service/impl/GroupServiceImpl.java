package com.dingzk.useradmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingzk.useradmin.common.ErrorCode;
import com.dingzk.useradmin.exception.BusinessException;
import com.dingzk.useradmin.model.domain.User;
import com.dingzk.useradmin.model.domain.UserGroup;
import com.dingzk.useradmin.model.enums.GroupAccessLevel;
import com.dingzk.useradmin.service.GroupService;
import com.dingzk.useradmin.model.domain.Group;
import com.dingzk.useradmin.mapper.GroupMapper;
import com.dingzk.useradmin.service.UserGroupService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.Optional;

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
        // 2. 用户已登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 3. 圈子名不超过 10 个字符，最少一个字符
        if (StringUtils.isBlank(groupInfo.getName())) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子名称为空");
        }
        if (groupInfo.getName().length() > 10) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子名称不能超过10个字符");
        }
        // 4. 圈子描述不超过100字符
        if (StringUtils.isNotBlank(groupInfo.getDescription()) && groupInfo.getDescription().length() > 100) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子描述不能超过100个字符");
        }
        // 5. 圈子最大人数 >= 1, <= 20
        // 未传递默认为 10
        int maxCapacity = Optional.ofNullable(groupInfo.getMaxCapacity()).orElse(10);
        if (maxCapacity < 1) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "圈子人数最少为1人");
        }
        if (maxCapacity > 20) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "目前仅支持不超过20人的圈子");
        }
        groupInfo.setMaxCapacity(maxCapacity);
        // 6. 圈子 access_level 校验，
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
                throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "密码长度不能超过8位");
            }

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
}