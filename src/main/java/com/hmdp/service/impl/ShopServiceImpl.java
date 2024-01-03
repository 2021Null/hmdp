package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据商户ID查询商户信息
     */
    @Override
    public Result queryById(Long id) {
        // 缓存穿透
        // Shop shop = queryWithPassThrough(id);

        // 互斥锁解决缓存击穿
        Shop shop = queryWithMutex(id);
        if (shop == null) {
            return Result.fail("店铺不存在!");
        }

        return Result.ok(shop);
    }

    /**
     * 封装缓存击穿代码
     * @return 返回shop
     */
    public Shop queryWithMutex(Long id){

        Shop shop;
        String shopCacheKey = RedisConstants.CACHE_SHOP_KEY + id;

        // 1.从redis中查询商户缓存
        String shopJson = stringRedisTemplate.opsForValue().get(shopCacheKey);

        // 2.判断店铺真实数据是否存在
        if (StrUtil.isNotBlank(shopJson)){
            // 3.存在,直接返回商铺信息, 将json串转为shop对象返回
            shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 4.店铺真实信息不存在,继续判断是否为缓存的空对象
        if ("".equals(shopJson)){
            // 如果是缓存的""空对象,则店铺信息不存在
            return null;
        }

        // 5.实现缓存重建
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        try {

            // 5.1 获取互斥锁
            boolean isLock = tryLock(lockKey);
            // 5.2 是否成功获取互斥锁
            if (!isLock){
                // 5.3 失败，休眠并重试
                Thread.sleep(50);
                return queryWithMutex(id);
            }

            // 5.4 若获取锁成功，再次检测redis缓存是否存在
            // 若存在，直接返回，无需重建缓存
            String shopJson2 = stringRedisTemplate.opsForValue().get(shopCacheKey);
            if (StrUtil.isNotBlank(shopJson)){
                shop = JSONUtil.toBean(shopJson, Shop.class);
                return shop;
            }
            // 店铺真实信息不存在,继续判断是否为缓存的空对象
            if ("".equals(shopJson2)){
                // 如果是缓存的""空对象,则店铺信息不存在
                return null;
            }


            // 6.若二次检测缓存 不存在，则再根据id查询数据库
            shop = getById(id);
            // 模拟重建延时
            Thread.sleep(200);
            if (shop == null){
                // 将空值写入redis
                stringRedisTemplate.opsForValue().set(shopCacheKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 返回错误信息
                return null;
            }

            // 7.存在,写入缓存(需要先将shop对象转换为json字符串)
            String shopJsonStr = JSONUtil.toJsonStr(shop);
            // 设置店铺缓存更新超时时间
            stringRedisTemplate.opsForValue().set(shopCacheKey, shopJsonStr, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        } finally {
            // 8. 释放互斥锁
            unLock(lockKey);
        }

        return shop;
    }

    /**
     * 封装缓存穿透代码
     * @return 返回shop
     */
    public Shop queryWithPassThrough(Long id){
        String shopCacheKey = RedisConstants.CACHE_SHOP_KEY + id;

        // 1.从redis中查询商户缓存
        String shopJson = stringRedisTemplate.opsForValue().get(shopCacheKey);

        // 2.判断店铺真实数据是否存在
        if (StrUtil.isNotBlank(shopJson)){
            // 3.存在,直接返回商铺信息, 将json串转为shop对象返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 4.店铺真实信息不存在,继续判断是否为缓存的空对象
        if ("".equals(shopJson)){
            // 如果是缓存的""空对象,则店铺信息不存在
            return null;
        }
        // 5.若缓存的真实数据不存在并且缓存的""空对象也不存在,则再根据id查询数据库
        Shop shop = getById(id);
        // 6.不存在
        if (shop == null){
            // 将空值写入redis
            stringRedisTemplate.opsForValue().set(shopCacheKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        // 7.存在,写入缓存(需要先将shop对象转换为json字符串)
        String shopJsonStr = JSONUtil.toJsonStr(shop);
        // 设置店铺缓存更新超时时间
        stringRedisTemplate.opsForValue().set(shopCacheKey, shopJsonStr, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 8.返回商户信息
        return shop;
    }


    /**
     * 尝试获取锁
     * 锁的值暂定为1
     */
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);

        // 避免直接自动拆箱产生空指针，使用工具类判断是否为true
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     */
    private void unLock(String key){
        stringRedisTemplate.delete(key);

    }


    /**
     * 根据id修改店铺时
     * 先修改数据库，再删除缓存
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long shopId = shop.getId();
        if (shopId == null) {
            return Result.fail("店铺id不能为空");
        }
        String shopCacheKey = RedisConstants.CACHE_SHOP_KEY + shopId;

        // 1.更新数据库
        updateById(shop);

        // 2.删除缓存
        stringRedisTemplate.delete(shopCacheKey);

        return Result.ok();
    }
}
