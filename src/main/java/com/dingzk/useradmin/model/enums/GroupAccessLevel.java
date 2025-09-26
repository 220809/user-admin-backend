package com.dingzk.useradmin.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GroupAccessLevel {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private final int value;
    private final String text;

    public static GroupAccessLevel getAccessLevel(Integer value) {
        if (value == null) {
            return null;
        }
        for (GroupAccessLevel level : GroupAccessLevel.values()) {
            if (level.getValue() == value) {
                return level;
            }
        }
        return null;
    }
}