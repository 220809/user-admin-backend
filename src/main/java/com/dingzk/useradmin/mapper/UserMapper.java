package com.dingzk.useradmin.mapper;

import com.dingzk.useradmin.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dingzk.useradmin.model.vo.UserVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author ding
* @description 针对表【user】的数据库操作Mapper
* @createDate 2025-08-16 20:44:00
* @Entity com.dingzk.useradmin.model.domain.User
*/
public interface UserMapper extends BaseMapper<User> {

    UserVo selectByUserId(Long userId);

    @MapKey(value = "user_id")
    Map<Long, Map<String, Object>> selectSimilarUserByTag(String tag);

    @MapKey(value = "user_id")
    Map<Long, User> selectUsersByUserIds(@Param("userIds") List<Long> userIds);

    List<User> selectUsersByAndTags(List<String> tagList);
}