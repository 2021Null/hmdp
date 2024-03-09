package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IBlogService extends IService<Blog> {

    /**
     * 分页查询当前页博客
     * @param current 当前页
     * @return 博客
     */
    Result queryHotBlog(Integer current);

    /**
     * 根据博客id查询博客内容
     * @param id 博客id
     * @return 博客
     */
    Result queryBlogById(Long id);

    /**
     * 点赞博客
     * @param id 博客id
     * @return null
     */
    Result likeBlog(Long id);

    /**
     * 查询点赞前五位
     * @param id 博客id
     * @return 前五位点赞用户dto信息
     */
    Result queryBlogLikes(Long id);

    /**
     * 采用推模式向关注用户推送消息到收件箱
     * @param blog 博客信息
     * @return 博客id
     */
    Result saveBlog(Blog blog);

    /**
     * 我关注的人的博客
     * @param max 上次查询的最小时间，也为本次查询的最大时间
     * @param offset 偏移量，要跳过的元素个数
     * @return 博客列表
     */
    Result queryBlogOfFollow(Long max, Integer offset);
}
