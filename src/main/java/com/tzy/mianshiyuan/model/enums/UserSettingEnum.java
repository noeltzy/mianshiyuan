package com.tzy.mianshiyuan.model.enums;

import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.exception.BusinessException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户设置枚举
 * 定义所有支持的设置项及其默认值
 */
@Getter
public enum UserSettingEnum {


    /**
     * 是否默认展示答案
     */
    AI_REPLAY_STRICTNESS("aiReplayStrictness", "3",  "回复严格度3最严格"),
    /**
     * 是否默认展示答案
     */
    SHOW_ANSWER("showAnswer", "false",  "是否直接显示答案");

    /**
     * 设置键名
     */
    private final String settingKey;

    /**
     * 默认值
     */
    private final String defaultValue;



    /**
     * 设置项描述
     */
    private final String description;

    UserSettingEnum(String settingKey, String defaultValue,  String description) {
        this.settingKey = settingKey;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    /**
     * 根据设置键名获取枚举值
     * @param settingKey 设置键名
     * @return 用户设置枚举
     */
    public static UserSettingEnum getBySettingKey(String settingKey) {
        if (settingKey == null || settingKey.isEmpty()) {
            return null;
        }
        
        for (UserSettingEnum setting : UserSettingEnum.values()) {
            if (setting.getSettingKey().equals(settingKey)) {
                return setting;
            }
        }
        return null;
    }

    /**
     * 获取默认值
     * @param en 枚举
     * @param value 值
     * @return 结果
     */
    public static String getDefaultValue(UserSettingEnum en, String value) {
        if(en==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        if(StringUtils.isBlank(value)){
            return en.defaultValue;
        }
        return value;
    }
    /**
     * 判断设置键名是否有效
     * @param settingKey 设置键名
     * @return 是否有效
     */
    public static boolean isValidSettingKey(String settingKey) {
        return getBySettingKey(settingKey) != null;
    }
}