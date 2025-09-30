package com.dingzk.useradmin.model.qo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupQo extends PageParam{
    @Serial
    private static final long serialVersionUID = 3892240524923733261L;

    /**
     * 圈子名称
     */
    private String name;

    /**
     * 圈子描述
     */
    private String description;

    /**
     * 队长名称
     */
    private String leaderName;

    /**
     * 圈子访问级别 0-公开 1-私有 2-加密
     */
    private Integer accessLevel;
}