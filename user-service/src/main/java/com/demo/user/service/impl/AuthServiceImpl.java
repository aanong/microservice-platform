package com.demo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.common.cache.RedisJsonCacheHelper;
import com.demo.user.cache.UserCacheKeys;
import com.demo.user.dto.LoginRequest;
import com.demo.user.dto.LoginResponse;
import com.demo.user.dto.RegisterRequest;
import com.demo.user.dto.ValidateTokenResponse;
import com.demo.user.entity.UserAccount;
import com.demo.user.exception.BizException;
import com.demo.user.mapper.UserAccountMapper;
import com.demo.user.service.AuthService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final long TOKEN_EXPIRE_DAYS = 7L;
    private static final String TOKEN_KEY_PREFIX = "login:token:";

    private final UserAccountMapper userAccountMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisJsonCacheHelper cacheHelper;

    public AuthServiceImpl(UserAccountMapper userAccountMapper,
                           StringRedisTemplate stringRedisTemplate,
                           RedisJsonCacheHelper cacheHelper) {
        this.userAccountMapper = userAccountMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheHelper = cacheHelper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAccount register(RegisterRequest request) {
        UserAccount byUsername = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
            .eq(UserAccount::getUsername, request.getUsername()));
        if (byUsername != null) {
            throw new BizException("Username already exists");
        }

        UserAccount byPhone = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
            .eq(UserAccount::getPhone, request.getPhone()));
        if (byPhone != null) {
            throw new BizException("Phone already exists");
        }

        UserAccount account = new UserAccount();
        account.setUsername(request.getUsername());
        account.setPhone(request.getPhone());
        account.setPasswordHash(sha256(request.getPassword()));
        account.setNickname(request.getNickname() == null || request.getNickname().trim().isEmpty()
            ? request.getUsername() : request.getNickname());
        account.setStatus(1);
        account.setCreateTime(java.time.LocalDateTime.now());
        account.setUpdateTime(java.time.LocalDateTime.now());
        userAccountMapper.insert(account);
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        UserAccount account = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
            .eq(UserAccount::getUsername, request.getUsername()));
        if (account == null) {
            throw new BizException("Username or password is incorrect");
        }
        if (!account.getPasswordHash().equals(sha256(request.getPassword()))) {
            throw new BizException("Username or password is incorrect");
        }
        if (account.getStatus() == null || account.getStatus() != 1) {
            throw new BizException("Account is disabled");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(
            TOKEN_KEY_PREFIX + token,
            String.valueOf(account.getId()),
            Duration.ofDays(TOKEN_EXPIRE_DAYS));

        LoginResponse response = new LoginResponse();
        response.setUserId(account.getId());
        response.setUsername(account.getUsername());
        response.setNickname(account.getNickname());
        response.setToken(token);
        return response;
    }

    @Override
    public ValidateTokenResponse validate(String token) {
        ValidateTokenResponse response = new ValidateTokenResponse();
        if (token == null || token.trim().isEmpty()) {
            response.setValid(false);
            return response;
        }

        String userIdStr = stringRedisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            response.setValid(false);
            return response;
        }
        stringRedisTemplate.expire(TOKEN_KEY_PREFIX + token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        response.setValid(true);
        response.setUserId(Long.valueOf(userIdStr));
        return response;
    }

    @Override
    public UserAccount currentUser(String token) {
        ValidateTokenResponse validate = validate(token);
        if (!validate.isValid()) {
            throw new BizException("Login expired");
        }

        Long userId = validate.getUserId();
        String key = UserCacheKeys.userProfile(userId);

        // 使用公共缓存工具类读取缓存
        UserAccount cached = cacheHelper.getObject(key, UserAccount.class);
        if (cached != null) {
            return cached;
        }

        UserAccount account = userAccountMapper.selectById(userId);
        if (account == null) {
            cacheHelper.setNull(key);
            throw new BizException("User not found");
        }

        // 使用公共缓存工具类写入缓存
        cacheHelper.setDetail(key, account);
        return account;
    }

    private String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new BizException("Hash algorithm unavailable");
        }
    }
}

