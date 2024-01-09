package com.hmdp.utils;

/**
 * <p>
 *
 * </p>
 *
 * @author Forever Z
 * @since 2024/1/9
 */


public interface ILock {

    /**
     * 尝试获取锁
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return true 代表获取锁成功
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();



}
