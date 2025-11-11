package com.tzy.mianshiyuan.service;

import com.tzy.mianshiyuan.model.dto.AuthDTOs;
import com.tzy.mianshiyuan.model.vo.UserVO;

/**
 * AuthService 是认证相关的服务接口，定义了用户注册、登录、登出、令牌刷新和获取当前用户等方法。
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @return 注册成功后的用户信息
     */
    UserVO register(AuthDTOs.RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录成功返回 Token 响应
     */
    AuthDTOs.TokenResponse login(AuthDTOs.LoginRequest request);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 刷新令牌
     *
     * @return 刷新后的 Token 响应
     */
    AuthDTOs.TokenResponse refresh();

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    UserVO currentUser();
}


