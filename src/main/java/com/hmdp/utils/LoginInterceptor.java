package com.hmdp.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 登录拦截器,只做拦截功能
 * </p>
 *
 * @author Forever Z
 * @since 2024/1/2
 */

public class LoginInterceptor implements HandlerInterceptor {


    /**
     * 前置拦截，进入controller之前进行登录校验
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        // 1.判断是否需要做拦截
        if (UserHolder.getUser() == null){
            // 没有用户信息，需要拦截，设置状态码
            response.setStatus(401);
            // 拦截
            return false;
        }

        // 有用户信息，直接放行
        return true;
    }


}
