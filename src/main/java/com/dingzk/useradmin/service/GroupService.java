package com.dingzk.useradmin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingzk.useradmin.model.domain.Group;
import com.dingzk.useradmin.model.domain.User;
import com.dingzk.useradmin.model.qo.GroupQo;
import com.dingzk.useradmin.model.request.JoinGroupRequest;
import com.dingzk.useradmin.model.request.UpdateGroupRequest;
import com.dingzk.useradmin.model.vo.GroupVo;

import java.util.List;


/**
* @author ding
* @description 针对表【group】的数据库操作Service
* @createDate 2025-09-26 14:28:00
*/
public interface GroupService extends IService<Group> {
    /**
     * 创建圈子
     * @param groupInfo 圈子信息
     * @param loginUser 登录用户
     * @return 成功创建的圈子 id
     */
    long createGroup(Group groupInfo, User loginUser);

    /**
     * 查询圈子
     * @param groupQo 查询信息
     * @return 圈子信息列表
     */
    List<GroupVo> listGroups(GroupQo groupQo);

    /**
     * 更新圈子信息
     * @param updateGroupRequest 更新内容
     * @param loginUser 当前登录用户
     * @return 更新结果
     */
    boolean updateGroup(UpdateGroupRequest updateGroupRequest, User loginUser);

    /**
     * 加入圈子
     * @param joinGroupRequest 请求参数
     * @param loginUser 当前登录用户
     * @return 加入结果
     */
    boolean joinGroup(JoinGroupRequest joinGroupRequest, User loginUser);

    /**
     * 退出队伍
     * @param groupId 圈子ID
     * @param loginUser 登录用户
     * @return 结果
     */
    boolean quitGroup(Long groupId, User loginUser);

    /**
     * 解散圈子
     * @param groupId 圈子ID
     * @param loginUser 登录用户
     * @return 结果
     */
    boolean dismissGroup(Long groupId, User loginUser);

    /**
     * 获取当前用户已加入的圈子
     * @param loginUser 登录用户
     * @return 加入圈子信息
     */
    List<GroupVo> listJoinedGroups(User loginUser);

    /**
     * 获取圈子详情
     * @param groupId 圈子 ID
     * @return 圈子详情
     */
    GroupVo listGroupDetails(Long groupId);

    List<Long> listGroupUserIds(Long groupId);
}