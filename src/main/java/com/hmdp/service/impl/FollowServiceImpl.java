package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;

    /**
     * 关注或取关该用户
     *
     * @param followUserId 需要关注或取关的用户id
     * @param isFollow     操作类型
     * @return null
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        // 1、获取当前登录用户
        Long userId = UserHolder.getUser().getId();

        // 2、判断操作类型
        String key = "follows:" + userId;
        if (isFollow) {
            // 关注用户，新增当前登录用户与操作用户的关系
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // 把关注用户的id放入redis的set集合中， sadd userId followUserId

                // 添加记录到redis中
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }


        } else {
            // 取关用户，删除数据  delete from tb_follow where userId = ? and follow_user_id = ?
            boolean isSuccess = remove(new QueryWrapper<Follow>().eq("user_id", userId)
                    .eq("follow_user_id", followUserId));
            if (isSuccess) {
                // 移除redis中的数据
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    /**
     * 查询是否关注该用户
     *
     * @param followUserid 关注用户id
     * @return null
     */
    @Override
    public Result isFollow(Long followUserid) {
        // 1、获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        // 2、查询当前用户与所关注用户id关系是否存在
        Integer count = query().eq("user_id", userId)
                .eq("follow_user_id", followUserid).count();
        // 3、返回结果
        return Result.ok(count > 0);
    }

    /**
     * 查询共同关注
     *
     * @param id 目标用户id
     * @return userDTO
     */
    @Override
    public Result followCommons(Long id) {
        // 1、获取当前登录用户
        Long userId = UserHolder.getUser().getId();

        // 2、求交集
        String key1 = "follows:" + userId;
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);
        if (intersect == null || intersect.isEmpty()) {
            // 无交集
            return Result.ok(Collections.emptyList());
        }
        // 3、 解析id集合
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        // 4、查询用户
        List<UserDTO> userDTOList = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOList);
    }
}
