package com.dingzk.useradmin.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class GroupVo implements Serializable {
    @Serial
    private static final long serialVersionUID = -2096996363889282619L;

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
     * 队长
     */
    private UserVo leader;

    /**
     * 圈子访问级别 0-公开 1-私有 2-加密
     */
    private Integer accessLevel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 圈子成员（包含队长）
     */
    private List<UserVo> participants;
}