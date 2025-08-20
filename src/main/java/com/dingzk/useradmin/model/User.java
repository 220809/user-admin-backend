package com.dingzk.useradmin.model;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName user
 */
@Data
@TableName(value ="user")
public class User {
    /**
     * 用户 ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 用户账号
     */
    @TableField(value = "user_account")
    private String userAccount;

    /**
     * 用户密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 头像地址
     */
    @TableField(value = "avatar_url")
    private String avatarUrl;

    /**
     * 用户性别 0-未知 1-男 2-女
     */
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 账户状态 0-禁用 1-正常 2-锁定
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableField(value = "deleted")
    @TableLogic
    private Integer deleted;

    /**
     * 最后登录时间
     */
    @TableField(value = "last_login_at")
    private Date lastLoginAt;

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    @TableField(value = "user_role")
    Integer userRole;
}