package com.ratelimiter.rate_limiter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    // Injected from RedisConfig
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    // Injected from application.properties
    @Value("${rate.limit.default.requests:10}")
    private int defaultLimit;

    @Value("${rate.limit.default.window:60}")
    private int windowSeconds;

    // Key prefix — all rate limit keys start with this
    private static final String KEY_PREFIX = "rate_limit:";

    public RateLimitResult checkRateLimit(String clientId) {

        // Build Redis key — e.g. "rate_limit:192.168.1.1"
        String key = KEY_PREFIX + clientId;

        try {
            // Execute Lua script atomically
            // KEYS = [key], ARGV = [limit, window]
            Long result = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList(key),   // KEYS[1]
                    String.valueOf(defaultLimit),       // ARGV[1]
                    String.valueOf(windowSeconds)       // ARGV[2]
            );

            if (result == null || result == -1L) {
                // Blocked — get TTL to tell client when to retry
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                long retryAfter = (ttl != null && ttl > 0) ? ttl : windowSeconds;

                log.warn("Rate limit exceeded for client: {}", clientId);

                return RateLimitResult.blocked(
                        clientId,
                        defaultLimit,
                        retryAfter
                );
            }

            // Allowed — return current count
            log.debug("Request allowed for: {} count: {}", clientId, result);
            return RateLimitResult.allowed(result.intValue(), defaultLimit);

        } catch (Exception e) {
            // Redis is down — FAIL OPEN (allow request)
            // Better to allow than block all users when Redis fails
            log.error("Redis error for client: {} — failing open", clientId, e);
            return RateLimitResult.allowed(0, defaultLimit);
        }
    }

    // Inner result class — carries decision + metadata
    public static class RateLimitResult {

        private final boolean allowed;
        private final String clientId;
        private final int currentCount;
        private final int limit;
        private final long retryAfterSeconds;

        private RateLimitResult(boolean allowed, String clientId,
                                int currentCount, int limit,
                                long retryAfterSeconds) {
            this.allowed = allowed;
            this.clientId = clientId;
            this.currentCount = currentCount;
            this.limit = limit;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public static RateLimitResult allowed(int count, int limit) {
            return new RateLimitResult(true, null, count, limit, 0);
        }

        public static RateLimitResult blocked(String clientId,
                                              int limit,
                                              long retryAfter) {
            return new RateLimitResult(false, clientId, limit, limit, retryAfter);
        }

        public boolean isAllowed() { return allowed; }
        public String getClientId() { return clientId; }
        public int getCurrentCount() { return currentCount; }
        public int getLimit() { return limit; }
        public long getRetryAfterSeconds() { return retryAfterSeconds; }
    }
}
