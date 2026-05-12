package com.ratelimiter.rate_limiter.service;

import com.ratelimiter.rate_limiter.algorithm.RateLimiterAlgorithm;
import com.ratelimiter.rate_limiter.algorithm.RateLimiterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    // Stage 2 removed — no more Lua script
    // Stage 3 — factory picks the algorithm
    private final RateLimiterFactory rateLimiterFactory;

    @Value("${rate.limit.default.requests:5}")
    private int defaultLimit;

    @Value("${rate.limit.default.window:60}")
    private int windowSeconds;

    public RateLimitResult checkRateLimit(String clientId) {
        try {
            // Step 1 — Get algorithm from factory
            // Returns FixedWindowRateLimiter (for now)
            RateLimiterAlgorithm algorithm =
                    rateLimiterFactory.getAlgorithm();

            // Step 2 — Call algorithm
            // FixedWindow stores "fixed:IP:window" key in Redis
            boolean allowed = algorithm.isAllowed(
                    clientId, defaultLimit, windowSeconds);

            // Step 3 — Return result
            if (!allowed) {
                log.warn("[{}] Rate limit exceeded for: {}",
                        algorithm.getAlgorithmName(), clientId);
                return RateLimitResult.blocked(
                        clientId, defaultLimit, windowSeconds);
            }

            return RateLimitResult.allowed(defaultLimit, defaultLimit);

        } catch (Exception e) {
            // Redis down — fail open
            log.error("Rate limit check failed — failing open", e);
            return RateLimitResult.allowed(defaultLimit, defaultLimit);
        }
    }

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
            return new RateLimitResult(
                    true, null, count, limit, 0);
        }

        public static RateLimitResult blocked(String clientId,
                                              int limit,
                                              long retryAfter) {
            return new RateLimitResult(
                    false, clientId, limit, limit, retryAfter);
        }

        public boolean isAllowed()         { return allowed; }
        public String getClientId()        { return clientId; }
        public int getCurrentCount()       { return currentCount; }
        public int getLimit()              { return limit; }
        public long getRetryAfterSeconds() { return retryAfterSeconds; }
    }
}