package com.dingzk.useradmin.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dingzk.useradmin.mapper.UserMapper;
import com.dingzk.useradmin.model.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreRedisJob {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private List<Long> importantUserIdList = List.of(2L);

    /**
     * 定时获取推荐用户列表放入缓存
     */
    @Scheduled(cron = "0 33 15 * * *")
    public void doPreRedisJob() {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        for (Long userId : importantUserIdList) {
            String recommendUsersKey= String.format("bitbuddy:user:recommend:%d", userId);
            Page<User> userPage = userMapper.selectPage(Page.of(1, 10), null);
            List<User> users = userPage.getRecords();
            try {
                operations.set(recommendUsersKey, users, 1, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error("Error creating key for recommend users: ", e);
            }
        }
    }
}
