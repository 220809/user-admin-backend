package com.dingzk.useradmin.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 通用响应实体类
 * @param <T> data type
 */
@Getter
@AllArgsConstructor
public class ResponseEntity<T> implements Serializable {
    private int code;

    private String message;

    private T data;

    private String description;

    public static <T> ResponseEntity<T> success(T data) {
        return new ResponseEntity<>(200, "ok", data, "");
    }

    public static ResponseEntity<?> error(int code, String message, String description) {
        return new ResponseEntity<>(code, message, null, description);
    }
}
