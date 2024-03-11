package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *  配置Redisson客户端，实现分布式锁
 * </p>
 *
 * @author Forever Z
 * @since 2024/1/10
 */

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        // 配置类
        Config config = new Config();
        // 添加redis地址，这里添加了单节点地址，也可使用config.useClusterServers()添加集群地址
        config.useSingleServer().setAddress("redis://192.168.145.132:6379").setPassword("123456");
        // config.useSingleServer().setAddress("redis://121.37.155.136:6379").setPassword("223944");
        // config.useSingleServer().setAddress("redis://localhost:6379");
        // 创建客户端
        return Redisson.create(config);


    }

}
