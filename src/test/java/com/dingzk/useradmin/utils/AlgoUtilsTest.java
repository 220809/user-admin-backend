package com.dingzk.useradmin.utils;

import com.dingzk.useradmin.model.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class AlgoUtilsTest {

    @Test
    void testMinDistanceForString() {
        String str1 = "Hello";
        String str2 = "Hola";
        String str3 = "ella";

        System.out.println(AlgoUtils.minDistance(str1, str2));
        System.out.println(AlgoUtils.minDistance(str1, str3));
    }

    @Test
    void testMinDistanceForList() {
        final List<String> list1 = Arrays.asList("Java", "C++", "羽毛球", "实习中");
        final List<String> list2 = Arrays.asList("Java", "python", "羽毛球", "实习中");
        final List<String> list3 = Arrays.asList("Java", "go", "羽毛球", "两年");
        final List<String> list4 = Arrays.asList("C", "C++", "篮球", "离职");

        System.out.println(AlgoUtils.minDistance(list1, list2));
        System.out.println(AlgoUtils.minDistance(list1, list3));
        System.out.println(AlgoUtils.minDistance(list1, list4));
    }
}