package com.rockranger.chat.rate_limit;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> rateLimitScript;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        String script = """
            local count = redis.call("INCR", KEYS[1])
            if count == 1 then
                redis.call("PEXPIRE", KEYS[1], ARGV[2])
            end
            local ttl = redis.call("PTTL", KEYS[1])
            if count > tonumber(ARGV[1]) then
                return {0, count, ttl}
            end
            return {1, count, ttl}
        """;
        this.rateLimitScript = new DefaultRedisScript<>(script, List.class);
    }

    public boolean isAllowed(String key, int maxRequests, Duration window) {
        try {
            List result = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList(key),
                    String.valueOf(maxRequests),
                    String.valueOf(window.toMillis())
            );
            if (result != null && !result.isEmpty()) {
                Number allowed = (Number) result.get(0);
                return allowed.intValue() == 1;
            }
            return false;
        } catch (Exception e) {
            log.error("Redis rate limit error for key {}. Failing closed for security.", key, e);
            // Fail closed on security-sensitive rate limit failure
            return false;
        }
    }

    public void enforce(String key, int maxRequests, Duration window) {
        if (!isAllowed(key, maxRequests, window)) {
            throw new ApiException(ErrorCode.TOO_MANY_REQUESTS, "Rate limit exceeded. Please wait before trying again.");
        }
    }

    public boolean setCooldownIfNotPresent(String key, Duration cooldown) {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", cooldown);
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error("Redis error setting cooldown for key {}", key, e);
            return false;
        }
    }

    public boolean hasCooldown(String key) {
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.error("Redis error checking cooldown for key {}", key, e);
            return false;
        }
    }
}
