package com.ratelimiter.rate_limiter.util;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class IpExtractorUtil {

    // These headers are checked IN ORDER — first valid one wins
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",       // Most common — set by proxies/load balancers
            "Proxy-Client-IP",       // Apache proxy
            "WL-Proxy-Client-IP",    // WebLogic
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED",
            "REMOTE_ADDR"
    };

    public static String extractClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);

            // StringUtils.hasText = not null, not empty, not just spaces
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {

                // X-Forwarded-For format: "realIP, proxy1, proxy2"
                // We only want the FIRST value — the original client
                return ip.split(",")[0].trim();
            }
        }

        // Last resort — direct connection (no proxy)
        return request.getRemoteAddr();
    }
}