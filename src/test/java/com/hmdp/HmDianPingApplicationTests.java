package com.hmdp;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.StreamSupport;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private RedisIdWorker redisIdWorker;
    private ExecutorService es = Executors.newFixedThreadPool(500);


    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () ->{
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("irder");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }

    @Test
    void testRandomString(){

        System.out.println(RandomUtil.randomString("user_", 5));
        System.out.println(RandomUtil.randomString("user_", 5));
        System.out.println(RandomUtil.randomString("user_", 5));
        System.out.println(RandomUtil.randomString("user_", 5));

    }

    @Test
    void UUID(){
        // isSimple=true 不带横线
        System.out.println("uuid true:" + UUID.randomUUID().toString(true));
        System.out.println("uuid false:" + UUID.randomUUID().toString(false));
        System.out.println("uuid :" + UUID.randomUUID(true));

    }


}
