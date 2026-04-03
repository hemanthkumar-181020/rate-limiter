package com.ratelimiter.rate_limiter.controller;


//
//import com.ratelimiter.dto.ApiResponse;
//import com.ratelimiter.util.IpExtractorUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ratelimiter.rate_limiter.dto.ApiResponse;
import com.ratelimiter.rate_limiter.util.IpExtractorUtil;
@RestController           // = @Controller + @ResponseBody
@RequestMapping("/api")   // All endpoints start with /api
public class TestController {

    // ✅ PUBLIC — No JWT needed
    // Tests: Is the app running? What is my IP?
    @GetMapping("/public/hello")
    public ResponseEntity<ApiResponse<String>> publicHello(
            HttpServletRequest request) {

        String clientIp = IpExtractorUtil.extractClientIp(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hello! Your IP is: " + clientIp,
                        "Success"
                )
        );
    }

    // 🔒 PROTECTED — Needs JWT token (Stage 4)
    // Tests: Does security correctly block unauthenticated requests?
    @GetMapping("/protected/data")
    public ResponseEntity<ApiResponse<String>> protectedData() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "You accessed protected data!",
                        "Authenticated successfully"
                )
        );
    }

    // ✅ PUBLIC — This is the endpoint we rate limit
    // Tests: Does rate limiting work? (Stage 2 onwards)
    @PostMapping("/public/test-rate-limit")
    public ResponseEntity<ApiResponse<String>> testRateLimit(
            HttpServletRequest request) {

        String clientIp = IpExtractorUtil.extractClientIp(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Request accepted from: " + clientIp,
                        "Success"
                )
        );
    }
}
