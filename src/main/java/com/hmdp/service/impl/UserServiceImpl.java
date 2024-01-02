package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexPatterns;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送验证码业务
     * @param phone 手机号
     * @param session 存到session
     * @return 无
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {

        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号码格式错误!");
        }

        // 3.符合，生成验证码 使用hutool的工具类
        String code = RandomUtil.randomNumbers(6);

        // 4.将验证码保存到redis中  时长两分钟
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone,
                code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5.发送验证码(模拟)
        log.debug("发送验证码成功，验证码：{}", code);

        // 返回ok
        return Result.ok();
    }

    /**
     * 登录注册业务
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     * @return 无
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String formPhone = loginForm.getPhone();
        String formCode = loginForm.getCode();
        String formPassword = loginForm.getPassword();


        // 1.校验手机号(每一个请求都要做独立的校验),格式是否正确
        if (RegexUtils.isPhoneInvalid(formPhone)){
            return Result.fail("手机号码格式错误!");
        }

        // 2. 从redis中取出缓存中的code验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue()
                .get(RedisConstants.LOGIN_CODE_KEY + formPhone);

        // 3.校验验证码
        if (cacheCode == null || !cacheCode.equals(formCode)){
            // 不一致，返回错误信息
            return Result.fail("验证码错误");
        }

        // 4.一致，根据手机号查用户
        User user = query().eq("phone", formPhone).one();

        // 5.判断用户是否存在
        if (user == null){
            // 6.不存在，创建新用户并保存
            user = createUserWithPhone(formPhone);
        }

        // 7.保存用户信息到redis
        // 7.1生成随机token，作为登录令牌         isSimple=true 不带横线
        String token = UUID.randomUUID().toString(true);

        // 7.2保留部分不敏感信息到userDto(选择合适的存储粒度)
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 7.3使用hutool工具类，将userDto转为userMap用来存储到redis的hash结构中
        Map<String, Object> userMap = new HashMap<>();
        // 由于UserDto的id字段为Long类型，所以不能直接插入redis的hash存储结构中(默认支持string类型)
        userMap.put("id", userDTO.getId().toString());
        userMap.put("nickName", userDTO.getNickName());
        userMap.put("icon", userDTO.getIcon());

        // 7.4将UserDto作为Hash存储
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;

        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);



        // 7.5设置30分钟登录状态有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 返回token
        return Result.ok(token);
    }

    private User createUserWithPhone(String formPhone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(formPhone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.保存用户
        save(user);

        return user;
    }
}
