package com.tzy.mianshiyuan.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 请求日志过滤器
 * 用于包装Request和Response，使其可以多次读取
 * 优先级设置为最高，确保在其他过滤器之前执行
 */
@Order(1)
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 排除静态资源
        String path = request.getRequestURI();
        if (path.startsWith("/v3/api-docs") || 
            path.startsWith("/swagger-ui") || 
            path.startsWith("/swagger-resources") ||
            path.startsWith("/doc.html") ||
            path.startsWith("/webjars") ||
            path.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 包装Request和Response，使其可以多次读取
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // 确保响应内容被写入到客户端
            wrappedResponse.copyBodyToResponse();
        }
    }
}

