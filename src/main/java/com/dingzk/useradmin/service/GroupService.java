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
     * 更新队伍信息
     * @param updateGroupRequest 更新内容
     * @param loginUser 当前登录用户
     * @return 更新结果
     */
    boolean updateGroup(UpdateGroupRequest updateGroupRequest, User loginUser);

    boolean joinGroup(JoinGroupRequest joinGroupRequest, User loginUser);
}