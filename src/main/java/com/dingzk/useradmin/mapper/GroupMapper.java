package com.dingzk.useradmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dingzk.useradmin.model.domain.Group;
import com.dingzk.useradmin.model.qo.GroupQo;
import com.dingzk.useradmin.model.request.UpdateGroupRequest;
import com.dingzk.useradmin.model.vo.GroupVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author ding
* @description 针对表【group】的数据库操作Mapper
* @createDate 2025-09-26 14:28:00
* @Entity com.dingzk.useradmin.model.domain.Group
*/
public interface GroupMapper extends BaseMapper<Group> {

    List<GroupVo> listGroups(@Param("groupQo") GroupQo groupQo);

    long updateGroup(@Param("request")UpdateGroupRequest request);

    long updateGroupLeaderId(@Param("newLeaderId") Long newLeaderId, @Param("groupId") Long groupId);

    List<GroupVo> listJoinedGroups(@Param("groupIdList") List<Long> groupIdList);
}