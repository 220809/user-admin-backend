package com.dingzk.useradmin.config;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        RList<String> testList = redissonClient.getList("testList");
//        testList.expire(Duration.ofSeconds(30));
        testList.add("redisson");
        // 不存在的 key，在操作对应数据后设置的过期时间才有效
        testList.expire(Duration.ofSeconds(60));
        testList.add("redisson");

        System.out.println(testList.get(0));
    }

    private final Object LOCK = new Object();

    @Test
    public void testRedissonMultiThreadLock() throws InterruptedException {
        new Thread(this::testRedissonLock).start();
        new Thread(this::testRedissonLock).start();
        new Thread(this::testRedissonLock).start();
        new Thread(this::testRedissonLock).start();

        // 等待线程任务完成
        synchronized (LOCK) {
            LOCK.wait();
        }
    }

    private void testRedissonLock() {
        RLock lock = redissonClient.getLock("bitbuddy:test:testLock");
        try {
            // Lock 默认过期时间为 30s，设置为 -1 将更新过期时间保证任务执行结束
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                System.out.printf("Thread: %d held the lock%n", Thread.currentThread().getId());
                Thread.sleep(40000);  // 模拟耗时操作
                System.out.printf("Thread: %d work completed!", Thread.currentThread().getId());
                synchronized (LOCK) {
                    LOCK.notifyAll();
                }
            } else {
                System.out.printf("Thread: %d failed to get the lock%n", Thread.currentThread().getId());
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