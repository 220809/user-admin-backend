package com.dingzk.useradmin.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class JoinGroupRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5459459705240195877L;

    private Long groupId;
    private String password;
}