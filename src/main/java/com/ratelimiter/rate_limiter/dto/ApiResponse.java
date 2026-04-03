package com.ratelimiter.rate_limiter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data                                        // Generates getters, setters, toString
@Builder                                     // Enables ApiResponse.builder() pattern
@JsonInclude(JsonInclude.Include.NON_NULL)   // Hides null fields from JSON output
public class ApiResponse<T> {

    private boolean success;       // true = success, false = error
    private String message;        // Human readable message
    private T data;                // Actual response data (any type)
    private LocalDateTime timestamp; // When response was created
    private int statusCode;        // HTTP status code

    // ✅ Use this for successful responses
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ❌ Use this for error responses
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}