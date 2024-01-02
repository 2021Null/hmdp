package com.hmdp.dto;

import lombok.Data;

@Data
public class LoginFormDTO {
    private String phone;
    private String code;

    /**
     * 既支持密码登录，又支持验证码登录
     */
    private String password;
}
