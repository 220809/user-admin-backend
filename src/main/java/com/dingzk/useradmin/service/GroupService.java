package com.dingzk.useradmin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingzk.useradmin.model.domain.Group;
import com.dingzk.useradmin.model.domain.User;


/**
* @author ding
* @description 针对表【group】的数据库操作Service
* @createDate 2025-09-26 14:28:00
*/
public interface GroupService extends IService<Group> {
    long createGroup(Group groupInfo, User loginUser);
}