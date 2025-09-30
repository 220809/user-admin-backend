package com.dingzk.useradmin.utils;

public final class SqlUtils {
    public static String fullFuzzyValue(String value) {
        return "%" + value + "%";
    }
}