package com.ratelimiter.rate_limiter.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RateLimitResponse {

    private String message;           // "Too many requests"
    private String clientId;          // "192.168.1.1" or "user_123"
    private int requestLimit;         // Max allowed — e.g. 10
    private int requestCount;         // How many they sent — e.g. 11
    private long retryAfterSeconds;   // Wait 45 seconds before retrying
    private LocalDateTime timestamp;  // When this happened
}
