package com.knowledgehub.framework.jwt;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT 黑名单工具：登出后把 token 加入 Redis，过期自动清除
 */
@Slf4j
@Component
public class TokenBlacklistUtil {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 把 token 加入黑名单，过期时间 = token 剩余有效期（token 过期后自动从 Redis 删除，不占空间）
     */
    public void addToBlacklist(String token, Date expiration) {
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl <= 0) {
            // token 已过期，不用加黑名单
            return;
        }
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "1",
                ttl,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 判断 token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        Boolean exists = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
        return Boolean.TRUE.equals(exists);
    }
}
