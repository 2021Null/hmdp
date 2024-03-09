package com.hmdp.dto;

import lombok.Data;

@Data
/**
 * UserDTO去掉了用户敏感信息进行返回
 */
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;


}
