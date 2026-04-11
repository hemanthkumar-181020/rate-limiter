package com.ratelimiter.rate_limiter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratelimiter.rate_limiter.dto.RateLimitResponse;
import com.ratelimiter.rate_limiter.service.RateLimitService;
import com.ratelimiter.rate_limiter.util.IpExtractorUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/actuator",
            "/api/auth"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = IpExtractorUtil.extractClientIp(request);

        RateLimitService.RateLimitResult result =
                rateLimitService.checkRateLimit(clientIp);

        response.setHeader("X-RateLimit-Limit",
                String.valueOf(result.getLimit()));
        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(Math.max(0,
                        result.getLimit() - result.getCurrentCount())));

        if (!result.isAllowed()) {
            response.setHeader("Retry-After",
                    String.valueOf(result.getRetryAfterSeconds()));
            write429Response(response, result);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void write429Response(
            HttpServletResponse response,
            RateLimitService.RateLimitResult result) throws IOException {

        RateLimitResponse body = RateLimitResponse.builder()
                .message("Too many requests. Please slow down.")
                .clientId(result.getClientId())
                .requestLimit(result.getLimit())
                .requestCount(result.getCurrentCount())
                .retryAfterSeconds(result.getRetryAfterSeconds())
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private boolean isExcluded(String path) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(path::startsWith);
    }
}