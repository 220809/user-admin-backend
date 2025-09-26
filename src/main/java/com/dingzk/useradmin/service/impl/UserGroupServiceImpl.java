package com.dingzk.useradmin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingzk.useradmin.mapper.UserGroupMapper;
import com.dingzk.useradmin.model.domain.UserGroup;
import com.dingzk.useradmin.service.UserGroupService;
import org.springframework.stereotype.Service;

/**
* @author ding
* @description 针对表【user_group】的数据库操作Service实现
* @createDate 2025-09-26 19:27:17
*/
@Service
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup>
    implements UserGroupService {

}