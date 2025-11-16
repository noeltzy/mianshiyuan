package com.tzy.mianshiyuan.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 用户设置表（支持动态扩展设置项）
 * @TableName user_settings
 */
@TableName(value ="user_settings")
@Data
public class UserSettingVO implements Serializable {
    /**
     * 设置键名（如：email_notification, theme, language等）
     */
    private String settingKey;

    /**
     * 设置值（支持字符串、数字、布尔值、JSON等）
     */
    private Objects settingValue;

}