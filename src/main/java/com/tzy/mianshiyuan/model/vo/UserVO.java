package com.tzy.mianshiyuan.model.vo;

import com.tzy.mianshiyuan.model.enums.UserRole;
import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String avatarUrl;
    private UserRole role;
    private String email;
    private String phone;
}


