package com.ratelimiter.rate_limiter.algorithm;

public interface RateLimiterAlgorithm {


        /**
         * Check if request is allowed
         * @param clientId      — IP address or userId
         * @param limit         — max requests allowed
         * @param windowSeconds — time window in seconds
         * @return true = allowed, false = blocked
         */
        boolean isAllowed(String clientId, int limit, int windowSeconds);

        /**
         * Get algorithm name for logging
         */
        String getAlgorithmName();

}
