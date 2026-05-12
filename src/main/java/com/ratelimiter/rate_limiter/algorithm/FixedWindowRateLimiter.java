package com.ratelimiter.rate_limiter.algorithm;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixedWindowRateLimiter implements RateLimiterAlgorithm {

  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public boolean isAllowed(String clientId, int limit, int windowSeconds) {

    // Step 1 — Build Redis key using current window
    // currentWindow changes every 60 seconds automatically
    // e.g. "fixed:192.168.1.1:28333350"
    long currentWindow = Instant.now().getEpochSecond() / windowSeconds;
    String key = "fixed:" + clientId + ":" + currentWindow;

    try {
      // Step 2 — Increment counter in Redis
      // If key doesn't exist → Redis creates it with value 1
      // If key exists → Redis adds 1 to existing value
      Long count = redisTemplate.opsForValue().increment(key);

      // Step 3 — If first request in window, set expiry
      // So key auto-deletes after window ends
      if (count != null && count == 1) {
        redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
      }

      // Step 4 — Decide allow or block
      boolean allowed = count != null && count <= limit;

      // Step 5 — Log for debugging
      log.debug("[FixedWindow] client={} count={} limit={} allowed={}",
              clientId, count, limit, allowed);

      return allowed;

    } catch (Exception e) {
      // Redis is down — fail open (allow request)
      log.error("[FixedWindow] Redis error for client: {}", clientId, e);
      return true;
    }
  }

  @Override
  public String getAlgorithmName() {
    return "FIXED_WINDOW";
  }
}