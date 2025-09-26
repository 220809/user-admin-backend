package com.dingzk.useradmin.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CreateGroupRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5110683877441703133L;
    /**
     * 圈子名称
     */
    private String name = null;

    /**
     * 圈子描述
     */
    private String description = null;

    /**
     * 圈子最大人数
     */
    private Integer maxCapacity = null;

    /**
     * 圈子访问级别 0-公开 1-私有 2-加密
     */
    private Integer accessLevel = null;

    /**
     * 圈子密码
     */
    private String password = null;
}