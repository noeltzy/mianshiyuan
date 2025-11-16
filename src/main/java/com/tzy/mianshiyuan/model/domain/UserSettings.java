package com.tzy.mianshiyuan.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户设置表（支持动态扩展设置项）
 * @TableName user_settings
 */
@TableName(value ="user_settings")
@Data
public class UserSettings implements Serializable {
    /**
     * 设置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（逻辑外键：user.id）
     */
    private Long userId;

    /**
     * 设置键名（如：email_notification, theme, language等）
     */
    private String settingKey;

    /**
     * 设置值（支持字符串、数字、布尔值、JSON等）
     */
    private String settingValue;

    /**
     * 设置值类型：STRING, NUMBER, BOOLEAN, JSON
     */
    private String settingType;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}