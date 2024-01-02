package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * 登录拦截器
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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1.获取sessino
        HttpSession session = request.getSession();
        // 2.获取session中的用户信息
        Object user = session.getAttribute("user");
        // 3.判断用户是否存在
        if (user == null){
            // 4.不存在，拦截 401未授权
            response.setStatus(401);
            return false;
        }
        // 5.存用户信息到ThreadLocal中
        UserHolder.saveUser((UserDTO)user);

        return HandlerInterceptor.super.preHandle(request, response, handler);
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
