package com.tzy.mianshiyuan.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
    USER(0, "USER", "普通用户"),
    ADMIN(1, "ADMIN", "管理员"),
    REVIEWER(2, "REVIEWER", "审核员");

    /**
     * 数据库存储值（INT）
     */
    @EnumValue
    private final Integer code;

    /**
     * 角色名称（用于Sa-Token等）
     */
    private final String name;

    /**
     * 角色描述
     */
    private final String description;

    UserRole(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static UserRole fromCode(Integer code) {
        if (code == null) {
            return USER; // 默认返回USER
        }
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return USER;
    }

    /**
     * 根据name获取枚举
     */
    public static UserRole fromName(String name) {
        if (name == null) {
            return USER;
        }
        for (UserRole role : values()) {
            if (role.name.equals(name)) {
                return role;
            }
        }
        return USER;
    }
}

