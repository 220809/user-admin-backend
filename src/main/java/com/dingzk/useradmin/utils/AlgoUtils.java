package com.dingzk.useradmin.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public final class AlgoUtils {

    /**
     * 字符串最小编辑距离算法
     * @param str1
     * @param str2
     * @return 最小距离
     */
    public static int minDistance(String str1, String str2) {
        if (str1 == null) {
            return str2 != null ? str2.length() : 0;
        }
        if (str2 == null) {
            return str1.length();
        }
        // 交换保证 str1 长度最短
        if (str1.length() > str2.length()) {
            String tmp = str1;
            str1 = str2;
            str2 = tmp;
        }

        int str1Len = str1.length();
        int str2Len = str2.length();

        int[] dp = new int[str1Len + 1];
        for (int i = 0; i <= str1Len; i++) {
            dp[i] = i;
        }

        for (int i = 1; i <= str2Len; i++) {
            int dp_i = dp[0];
            dp[0] = i;
            for (int j = 1; j <= str1Len; j++) {
                int tmp = dp[j];
                if (str2.charAt(i - 1) == str1.charAt(j - 1)) {
                    dp[j] = dp_i;
                } else {
                    dp[j] = 1 + NumberUtils.min(dp_i, dp[j - 1], dp[j]);
                }
                dp_i = tmp;
            }
        }

        return dp[str1Len];
    }

    /**
     * List<String>最小编辑距离算法
     * @param list1
     * @param list2
     * @return 最小距离
     */
    public static int minDistance(List<String> list1, List<String> list2) {
        if (CollectionUtils.isEmpty(list1)) {
            return list2 != null ? list2.size() : 0;
        }
        if (CollectionUtils.isEmpty(list2)) {
            return list1.size();
        }
        // 交换保证 str1 长度最短
        if (list1.size() > list2.size()) {
            List<String> tmp = list1;
            list1 = list2;
            list2 = tmp;
        }

        int list1Len = list1.size();
        int list2Len = list2.size();

        int[] dp = new int[list1Len + 1];
        for (int i = 0; i <= list1Len; i++) {
            dp[i] = i;
        }

        for (int i = 1; i <= list2Len; i++) {
            int dp_i = dp[0];
            dp[0] = i;
            for (int j = 1; j <= list1Len; j++) {
                int tmp = dp[j];
                if (Objects.equals(list2.get(i - 1), list1.get(j - 1))) {
                    dp[j] = dp_i;
                } else {
                    dp[j] = 1 + NumberUtils.min(dp_i, dp[j - 1], dp[j]);
                }
                dp_i = tmp;
            }
        }

        return dp[list1Len];
    }
}
