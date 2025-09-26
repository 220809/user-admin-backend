package com.dingzk.useradmin.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName group
 */
@TableName(value ="`group`")
@Data
public class Group {
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 圈子名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 圈子描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 圈子最大人数
     */
    @TableField(value = "max_capacity")
    private Integer maxCapacity;

    /**
     * 队长 ID
     */
    @TableField(value = "leader_id")
    private Long leaderId;

    /**
     * 圈子访问级别 0-公开 1-私有 2-加密
     */
    @TableField(value = "access_level")
    private Integer accessLevel;

    /**
     * 圈子密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableField(value = "deleted")
    @TableLogic
    private Integer deleted;
}