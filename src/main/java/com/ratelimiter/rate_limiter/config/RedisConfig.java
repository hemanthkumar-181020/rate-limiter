package com.ratelimiter.rate_limiter.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class RedisConfig {

    // Bean 1 — Connection Factory
    // Lettuce is the Redis client library
    // It manages the actual TCP connection to Redis server
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
        // localhost = Redis running on same machine
        // 6379 = default Redis port
    }

    // Bean 2 — RedisTemplate
    // This is what your Java code uses to talk to Redis
    // Like JdbcTemplate for databases — RedisTemplate for Redis
    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serializers — how Java String becomes Redis bytes
        // Without this, Redis stores binary garbage instead of readable strings
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    // Bean 3 — Lua Script
    // Loads our Lua script file and prepares it for execution
    // We build this script in the next file
    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/rate_limit.lua"));
        script.setResultType(Long.class);
        return script;
    }
}