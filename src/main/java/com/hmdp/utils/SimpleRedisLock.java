package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author Forever Z
 * @since 2024/1/9
 */


public class SimpleRedisLock implements ILock{

    /**
     * 不同业务锁的名称不同
     */
    private String name;
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        // 采用构造函数可接受字符串型脚本，不推荐硬编码，将脚本编写到文件中单独管理
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        // 设置脚本位置，由于lua脚本就在resources目录下，所以可以直接使用名字
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        // 设置返回值类型
        UNLOCK_SCRIPT.setResultType(Long.class);
    }



    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 尝试获取锁
     *
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return true 代表获取锁成功
     */
    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取当前线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();

        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);

        return BooleanUtil.isTrue(success);
    }

    /**
     * 释放锁，调用Lua脚本
     * 锁的键是代表哪个业务的锁，锁的值是线程id
     */
    @Override
    public void unlock(){
        // 调用lua脚本
        stringRedisTemplate.execute(UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId()
                );
    }

    // @Override
    // public void unlock() {
    //     // 获取线程标识
    //     String threadId = ID_PREFIX + Thread.currentThread().getId();
    //     // 获取锁中标识
    //     String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
    //     // 判断线程标识与锁标识是否一致
    //     if (threadId.equals(id)) {
    //         // 一致就释放锁
    //         stringRedisTemplate.delete(KEY_PREFIX + name);
    //     }
    //     // 不一致不管
    // }
}
