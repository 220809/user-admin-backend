package com.dingzk.useradmin.config;

import com.dingzk.useradmin.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RedisTemplateTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedis() {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set("testString", "Hello Redis!", 1, TimeUnit.MINUTES);
        operations.set("testUser", new User(), 1, TimeUnit.MINUTES);
        operations.set("testList", List.of(new User()), 1, TimeUnit.MINUTES);

        System.out.println(operations.get("testString"));
        System.out.println(operations.get("testUser"));
        System.out.println(operations.get("testList"));
    }
}