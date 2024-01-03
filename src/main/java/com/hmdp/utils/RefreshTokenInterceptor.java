package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  保存用户到ThreadLocal并刷新token拦截器
 * </p>
 *
 * @author Forever Z
 * @since 2024/1/3
 */


public class RefreshTokenInterceptor implements HandlerInterceptor {

    /**
     * 此处注入只能使用构造函数，因为这个类的对象不是component，不由spring创建，是由我们手动new出来
     * 谁用此对象，在谁那里注入
     */
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 此处注入只能使用构造函数，因为这个类的对象不是component，不由spring创建，是由我们手动new出来
     * 谁用此对象，在谁那里注入
     */
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 前置拦截，进入controller之前进行登录校验
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){

        // 1.获取请求头中token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            // 不存在，直接放行
            return true;
        }
        // 2.基于token获取redis中的用户信息
        String userKey = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(userKey);
        // 3.判断用户是否存在
        if (userMap.isEmpty()) {
            // 4.不存在,放行
            return true;
        }
        // 5.将查询到的Hash数据转为UserDto对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 6.存用户信息到ThreadLocal中
        UserHolder.saveUser(userDTO);

        // 7.刷新token有效期 30分钟
        stringRedisTemplate.expire(userKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8. 放行
        return true;
    }



    /**
     * 渲染之后，返回给用户之前，业务执行完毕，销毁用户信息，避免内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户信息
        UserHolder.removeUser();

    }
}
