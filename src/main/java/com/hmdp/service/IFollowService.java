package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IFollowService extends IService<Follow> {

    /**
     * 关注或取关该用户
     * @param followUserId 需要关注或取关的用户id
     * @param isFollow 操作类型
     * @return null
     */
    Result follow(Long followUserId, Boolean isFollow);

    /**
     * 查询是否关注该用户
     * @param followUserid 关注用户id
     * @return null
     */
    Result isFollow(Long followUserid);

    /**
     * 查询共同关注
     * @param id 目标用户id
     * @return userDTO
     */
    Result followCommons(Long id);
}
