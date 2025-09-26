package com.dingzk.useradmin.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dingzk.useradmin.mapper.UserMapper;
import com.dingzk.useradmin.model.domain.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

    @Resource
    private RedissonClient redissonClient;

    /**
     * 定时获取推荐用户列表放入缓存
     */
    @Scheduled(cron = "0 33 15 * * *")
    public void doPreRedisJob() {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        RLock lock = redissonClient.getLock("bitbuddy:preredisjob:scheduledjob:lock");
        try {
            // Lock 默认过期时间为 30s，设置为 -1 将更新过期时间保证任务执行结束
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
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
        } catch (InterruptedException e) {
            System.out.printf("try Lock error: %s%n",e.getMessage());
        } finally {
            // 一定要在 finally 中释放锁资源！！！
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
