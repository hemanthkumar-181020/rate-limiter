package com.ratelimiter.rate_limiter.exception;

import lombok.Getter;

@Getter   // Only getters — no setters, exception fields never change
public class RateLimitExceededException extends RuntimeException {

    private final String clientId;   // Who exceeded the limit
    private final int limit;         // What the limit was
    private final long retryAfter;   // When they can retry (seconds)

    public RateLimitExceededException(String clientId,
                                      int limit,
                                      long retryAfter) {
        // Call parent RuntimeException with a message
        super("Rate limit exceeded for: " + clientId);
        this.clientId = clientId;
        this.limit = limit;
        this.retryAfter = retryAfter;
    }
}
