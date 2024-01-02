package com.hmdp;

import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HmDianPingApplicationTests {


    @Test
    void testRandomString(){

        System.out.println(RandomUtil.randomString("user_", 5));
        System.out.println(RandomUtil.randomString("user_", 5));
        System.out.println(RandomUtil.randomString("user_", 5));
        System.out.println(RandomUtil.randomString("user_", 5));

    }


}
