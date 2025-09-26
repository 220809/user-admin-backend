package com.dingzk.useradmin.model.qo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PageParam implements Serializable {
    @Serial
    private static final long serialVersionUID = -8816295030268696622L;

    private int pageNum = 1;
    private int pageSize = 10;
}