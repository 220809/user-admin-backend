package com.dingzk.useradmin.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 圈子用户 VO
 */
@Data
public class UserVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 6422568407131982701L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 用户性别 0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 用户签名
     */
    private String slogan;

    /**
     * 账户状态 0-禁用 1-正常 2-锁定
     */
    private Integer status;

    /**
     * json格式 用户标签
     */
    private String tags;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后登录时间
     */
    private Date lastLoginAt;

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    private Integer userRole;
}
