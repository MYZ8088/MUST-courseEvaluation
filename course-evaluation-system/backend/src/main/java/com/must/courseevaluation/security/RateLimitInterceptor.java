package com.must.courseevaluation.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 每分钟最多60次请求
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int TIME_WINDOW_MINUTES = 1;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);
        String key = "rate_limit:" + clientIp;
        
        try {
            String countStr = (String) redisTemplate.opsForValue().get(key);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;
            
            if (count >= MAX_REQUESTS_PER_MINUTE) {
                logger.warn("Rate limit exceeded for IP: {}", clientIp);
                response.setStatus(429);
                response.getWriter().write("{\"error\":\"Too many requests\"}");
                return false;
            }
            
            // 增加计数
            redisTemplate.opsForValue().increment(key);
            if (count == 0) {
                redisTemplate.expire(key, TIME_WINDOW_MINUTES, TimeUnit.MINUTES);
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Rate limit check failed", e);
            // 如果Redis出错，允许请求通过
            return true;
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}