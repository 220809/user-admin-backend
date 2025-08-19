package com.dingzk.useradmin.model.search;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserSearchRequestParam {
    private String username;
    private Integer status;
    private Date beginDate;
    private Date endDate;
}
