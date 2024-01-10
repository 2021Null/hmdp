-- KEYS[1]就是锁的key，ARGV[1]就是当前线程标识
-- 比较线程标识与锁中的标识是否一致
if (redis.call('get', KEYS[1] == ARGV[1])) then
    -- 若一致，释放锁 del key
    return redis.call('del', KEYS[1])
end
-- 不一致则直接返回
return 0