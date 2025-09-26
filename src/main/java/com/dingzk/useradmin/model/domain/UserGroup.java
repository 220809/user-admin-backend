package com.dingzk.useradmin.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName user_group
 */
@TableName(value ="user_group")
@Data
public class UserGroup {
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 成员 ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 小组 ID
     */
    @TableField(value = "group_id")
    private Long groupId;

    /**
     * 成员加入时间
     */
    @TableField(value = "join_time")
    private Date joinTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private Date updatedTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableField(value = "deleted")
    private Integer deleted;
}