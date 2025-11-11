package com.tzy.mianshiyuan.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthDTOs {
    @Data
    public static class RegisterRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        private String email;
        private String phone;
    }
    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }
    @Data
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken; // 预留：当前方案先返回空或同 accessToken
    }
}


