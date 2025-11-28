package com.tzy.mianshiyuan.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.model.domain.User;
import com.tzy.mianshiyuan.model.dto.AuthDTOs;
import com.tzy.mianshiyuan.model.enums.UserRole;
import com.tzy.mianshiyuan.model.vo.UserVO;
import com.tzy.mianshiyuan.service.AuthService;
import com.tzy.mianshiyuan.service.UserService;
import com.tzy.mianshiyuan.util.UserConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserVO register(AuthDTOs.RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User exists = userService.getByUsername(request.getUsername());
        if (exists != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.USER); // 注册时默认设置为普通用户
        userService.save(user);
        return UserConverter.toVO(user);
    }

    @Override
    public AuthDTOs.TokenResponse login(AuthDTOs.LoginRequest request) {
        User user = userService.getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "账号或密码错误");
        }
        StpUtil.login(user.getId());
        AuthDTOs.TokenResponse resp = new AuthDTOs.TokenResponse();
        resp.setAccessToken(StpUtil.getTokenValue());
        resp.setRefreshToken(null);
        return resp;
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Override
    public AuthDTOs.TokenResponse refresh() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Object loginId = StpUtil.getLoginIdDefaultNull();
        StpUtil.logout();
        StpUtil.login(loginId);
        AuthDTOs.TokenResponse resp = new AuthDTOs.TokenResponse();
        resp.setAccessToken(StpUtil.getTokenValue());
        resp.setRefreshToken(null);
        return resp;
    }

    @Override
    public UserVO currentUser() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Long uid = StpUtil.getLoginIdAsLong();
        User user = userService.getById(uid);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return UserConverter.toVO(user);
    }
}


