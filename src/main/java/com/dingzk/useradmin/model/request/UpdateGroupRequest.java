package com.dingzk.useradmin.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UpdateGroupRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 190945691329004024L;
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 圈子名称
     */
    private String name;

    /**
     * 圈子描述
     */
    private String description;

    /**
     * 圈子最大人数
     */
    private Integer maxCapacity;

    /**
     * 队长 ID
     */
    private Long leaderId;

    /**
     * 圈子访问级别 0-公开 1-私有 2-加密
     */
    private Integer accessLevel;

    /**
     * 圈子密码
     */
    private String password;
}