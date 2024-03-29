package com.hmdp.utils;

/**
 * redis常量
 */
public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    // 验证码时长 两分钟
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    // 登录token有效期，30分钟
    public static final Long LOGIN_USER_TTL = 30L;

    public static final Long CACHE_NULL_TTL = 2L;

    // 缓存店铺时间30分钟
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    // 缓存店铺类型
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop-type";

    // 店铺互斥锁key前缀
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    // 互斥锁时间 10s
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
