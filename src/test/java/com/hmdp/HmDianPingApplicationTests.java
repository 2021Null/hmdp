package com.hmdp;

import cn.hutool.core.lang.UUID;
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

    @Test
    void UUID(){
        // isSimple=true 不带横线
        System.out.println("uuid true:" + UUID.randomUUID().toString(true));
        System.out.println("uuid false:" + UUID.randomUUID().toString(false));
        System.out.println("uuid :" + UUID.randomUUID(true));

    }


}
