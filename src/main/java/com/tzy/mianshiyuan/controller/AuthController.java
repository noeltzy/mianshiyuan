package com.tzy.mianshiyuan.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ResultUtils;
import com.tzy.mianshiyuan.model.dto.AuthDTOs;
import com.tzy.mianshiyuan.model.vo.UserVO;
import com.tzy.mianshiyuan.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证接口", description = "注册、登录、刷新、退出、当前用户")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public BaseResponse<UserVO> register(@Valid @RequestBody AuthDTOs.RegisterRequest request) {
        return ResultUtils.success(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录（返回JWT）")
    public BaseResponse<AuthDTOs.TokenResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        return ResultUtils.success(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public BaseResponse<Void> logout() {
        authService.logout();
        return ResultUtils.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌")
    public BaseResponse<AuthDTOs.TokenResponse> refresh() {
        return ResultUtils.success(authService.refresh());
    }

    @GetMapping("/me")
    @SaCheckLogin
    @Operation(summary = "获取当前登录用户信息")
    public BaseResponse<UserVO> me() {
        return ResultUtils.success(authService.currentUser());
    }
}


