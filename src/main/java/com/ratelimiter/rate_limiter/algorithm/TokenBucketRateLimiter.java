package com.ratelimiter.rate_limiter.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimiterAlgorithm {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isAllowed(String clientId, int capacity, int refillRate) {

        String key = "token_bucket:" + clientId;

        try {

            HashOperations<String, Object, Object> hashOps =
                    redisTemplate.opsForHash();

            long now = Instant.now().getEpochSecond();

            // Get current bucket state
            String tokenStr = (String) hashOps.get(key, "tokens");
            String lastRefillStr = (String) hashOps.get(key, "lastRefill");

            double tokens;
            long lastRefill;

            // First request
            if (tokenStr == null || lastRefillStr == null) {
                tokens = capacity;
                lastRefill = now;
            } else {
                tokens = Double.parseDouble(tokenStr);
                lastRefill = Long.parseLong(lastRefillStr);
            }

            // Calculate elapsed time
            long elapsedTime = now - lastRefill;

            // Refill tokens
            double refillTokens = elapsedTime * refillRate;

            tokens = Math.min(capacity, tokens + refillTokens);

            // Check request allowed
            boolean allowed = tokens >= 1;

            if (allowed) {
                tokens -= 1;
            }

            // Save updated state
            hashOps.put(key, "tokens", String.valueOf(tokens));
            hashOps.put(key, "lastRefill", String.valueOf(now));

            // Auto expire inactive buckets
            redisTemplate.expire(key, 1, TimeUnit.HOURS);

            log.debug(
                    "[TokenBucket] client={} tokens={} capacity={} allowed={}",
                    clientId,
                    tokens,
                    capacity,
                    allowed
            );

            return allowed;

        } catch (Exception e) {

            log.error("[TokenBucket] Redis error for client={}", clientId, e);

            // fail open
            return true;
        }
    }

    @Override
    public String getAlgorithmName() {
        return "TOKEN_BUCKET";
    }
}