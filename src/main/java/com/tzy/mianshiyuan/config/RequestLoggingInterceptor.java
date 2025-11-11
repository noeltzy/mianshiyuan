package com.tzy.mianshiyuan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局请求日志拦截器
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_RESPONSE_LENGTH = 2000; // 最大响应体长度

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        request.setAttribute("requestStartTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // 获取请求开始时间
            Object startTimeObj = request.getAttribute("requestStartTime");
            long duration = 0;
            if (startTimeObj != null) {
                long startTime = (Long) startTimeObj;
                duration = System.currentTimeMillis() - startTime;
            }

            // 获取请求URL
            String url = getRequestUrl(request);
            
            // 获取请求参数
            String params = getRequestParams(request);
            
            // 获取返回值
            String responseBody = getResponseBody(response);

            // 输出日志
            log.info("请求日志 - URL: {}, 参数: {}, 返回值: {}, 耗时: {}ms", 
                    url, params, responseBody, duration);
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

    /**
     * 获取请求参数
     */
    private String getRequestParams(HttpServletRequest request) {
        try {
            Map<String, Object> params = new HashMap<>();
            
            // 获取Query参数
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap != null && !parameterMap.isEmpty()) {
                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String[] values = entry.getValue();
                    if (values != null && values.length > 0) {
                        params.put(entry.getKey(), values.length == 1 ? values[0] : values);
                    }
                }
            }
            
            // 获取RequestBody（如果是POST/PUT等）
            String method = request.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || 
                "PATCH".equalsIgnoreCase(method)) {
                String body = getRequestBody(request);
                if (body != null && !body.isEmpty()) {
                    params.put("body", body);
                }
            }
            
            if (params.isEmpty()) {
                return "null";
            }
            
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.warn("解析请求参数失败: {}", e.getMessage());
            return "null";
        }
    }

    /**
     * 获取RequestBody
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            } else {
                // 尝试从InputStream读取（注意：流只能读取一次）
                String contentType = request.getContentType();
                if (contentType != null && contentType.contains("application/json")) {
                    // 如果已经被读取过，返回null
                    return null;
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }

    /**
     * 获取响应体
     */
    private String getResponseBody(HttpServletResponse response) {
        try {
            if (response instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
                byte[] content = wrapper.getContentAsByteArray();
                if (content != null && content.length > 0) {
                    String body = new String(content, StandardCharsets.UTF_8);
                    // 限制响应体长度，避免日志过长
                    if (body.length() > MAX_RESPONSE_LENGTH) {
                        return body.substring(0, MAX_RESPONSE_LENGTH) + "...(已截断，总长度:" + body.length() + ")";
                    }
                    return body;
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "null";
    }
}

