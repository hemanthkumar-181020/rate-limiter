package com.ratelimiter.rate_limiter.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiterFactory {

    private final FixedWindowRateLimiter fixedWindow;

    @Value("${rate.limit.algorithm:FIXED_WINDOW}")
    private String algorithm;

    public RateLimiterAlgorithm getAlgorithm() {
        log.info("Using algorithm: {}", algorithm);
        return fixedWindow;
    }
}