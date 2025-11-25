package com.tzy.mianshiyuan.model.dto;


import com.tzy.mianshiyuan.model.enums.UserRole;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String avatarUrl;
    private String email;
    private String phone;
    private String nickname;
}
