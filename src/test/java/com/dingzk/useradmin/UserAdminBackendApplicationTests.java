package com.dingzk.useradmin;

import com.dingzk.useradmin.model.User;
import com.dingzk.useradmin.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
class UserAdminBackendApplicationTests {

    @Autowired
    private UserService userService;

    private final ExecutorService executor = new ThreadPoolExecutor(20, 50,
            20, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000));

    @Test
    void contextLoads() {
    }

    /**
     * 插入测试数据
     * 一次性使用，使用后将 @Test 注释，避免打包后重复执行
     */
//    @Test
    void insertTestData() {
        final int INSERT_TOTAL = 500_000;
        final int BATCH_SIZE = 10_000;
        int j = 500001;

        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < INSERT_TOTAL / BATCH_SIZE; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                String userName = String.format("user_%d", j);
                user.setUsername(userName);
                user.setEmail(userName + "@163.com");
                user.setUserAccount(userName);
                user.setPassword("12345678");
                user.setAvatarUrl("https://images.pexels.com/photos/33934951/pexels-photo-33934951.jpeg");
                user.setGender(j % 2 + 1);
                user.setStatus(1);
                user.setTags("[]");
                user.setUserRole(0);
                userList.add(user);
                if (j % BATCH_SIZE == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> userService.saveBatch(userList), executor);
            futureList.add(future);
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println("Inserting test data costs: " + stopWatch.getTotalTimeMillis() + "ms");
    }

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
