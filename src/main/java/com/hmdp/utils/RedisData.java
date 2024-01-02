package com.hmdp.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * redis过期时间
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
