package com.tzy.mianshiyuan.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 全局请求日志拦截器
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        request.setAttribute("requestStartTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // 跳过流式响应（SSE）的日志记录，避免干扰流式传输
            String contentType = response.getContentType();
            if (contentType != null && contentType.contains("text/event-stream")) {
                return;
            }

            // 获取请求开始时间
            Object startTimeObj = request.getAttribute("requestStartTime");
            long duration = 0;
            if (startTimeObj != null) {
                long startTime = (Long) startTimeObj;
                duration = System.currentTimeMillis() - startTime;
            }

            // 获取请求URL
            String url = getRequestUrl(request);

            // 输出日志
            log.info("请求日志 - URL: {}, 耗时: {}ms", url, duration);
        } catch (Exception e) {
            log.error("记录请求日志失败", e);
        }
    }

    /**
     * 获取请求URL
     */
    private String getRequestUrl(HttpServletRequest request) {
        String url = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        return url != null ? url : "null";
    }
}

