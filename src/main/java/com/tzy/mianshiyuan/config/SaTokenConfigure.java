package com.tzy.mianshiyuan.config;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.stp.StpInterface;
import com.tzy.mianshiyuan.model.domain.User;
import com.tzy.mianshiyuan.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

@Configuration
public class SaTokenConfigure {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 通过 Sa-Token 提供统一的角色、权限查询
    @Bean
    public StpInterface stpInterface(UserService userService) {
        return new StpInterface() {
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                if (loginId == null) {
                    return Collections.emptyList();
                }
                Long userId;
                try {
                    userId = Long.parseLong(loginId.toString());
                } catch (NumberFormatException e) {
                    return Collections.emptyList();
                }
                User user = userService.getById(userId);
                if (user == null || user.getRole() == null) {
                    return Collections.emptyList();
                }
                // 使用枚举的 name 字段作为角色标识（用于Sa-Token）
                return List.of(user.getRole().getName());
            }
        };
    }

    /**
     * 放行 Swagger/Knife4j 文档相关路径，避免被拦截导致文档无法渲染
     */
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/doc.html",
                        "/webjars/**",
                        "/favicon.ico"
                )
                .setBeforeAuth(obj -> {
                    // 可在此设置跨域响应头等
                })
                .setAuth(obj -> {
                    // 本项目基于注解鉴权，不在全局过滤器里强制校验
                });
    }
}


