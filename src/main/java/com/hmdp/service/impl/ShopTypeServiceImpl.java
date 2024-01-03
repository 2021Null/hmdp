package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */

@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result queryAllType() {
        String shopTypeCacheKey = RedisConstants.CACHE_SHOP_TYPE_KEY;
        // 1.查询redis店铺类型
        String shopTypeListJson = stringRedisTemplate.opsForValue().get(shopTypeCacheKey);
        // 2.查到 将列表json转为列表返回
        if (shopTypeListJson != null){
            List<ShopType> shopTypes = JSONUtil.toList(shopTypeListJson, ShopType.class);
            log.error("没有去查询数据库，直接从Redis查到数据{}", shopTypes);
            return Result.ok(shopTypes);
        }

        // 3.没查到就去查询数据库
        List<ShopType> shopTypes = getBaseMapper().selectList(null);

        // 4.数据库中没有直接返回404
        if (shopTypes.size() == 0){
            return Result.fail("店铺类型不存在");
        }

        // 5.数据库中有,将其转为json字符串并添加到redis缓存中
        String shopTypeJsonCache = JSONUtil.toJsonStr(shopTypes);
        stringRedisTemplate.opsForValue().set(shopTypeCacheKey, shopTypeJsonCache);
        log.error("查了数据库，并添加到redis中的店铺类型有:{}", shopTypeJsonCache);


        // 6.返回店铺信息
        return Result.ok(shopTypes);
    }
}
