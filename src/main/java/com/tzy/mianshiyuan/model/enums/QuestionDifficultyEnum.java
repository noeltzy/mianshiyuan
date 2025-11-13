package com.tzy.mianshiyuan.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 题目难度枚举
 */
@Getter
public enum QuestionDifficultyEnum {
    EASY(0, "简单"),
    MEDIUM(1, "中等"),
    HARD(2, "困难");

    @EnumValue
    private final int code;
    private final String label;

    QuestionDifficultyEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 根据存储值获取枚举，默认返回中等难度
     */
    public static QuestionDifficultyEnum fromCode(Integer code) {
        if (code == null) {
            return MEDIUM;
        }
        for (QuestionDifficultyEnum difficulty : values()) {
            if (difficulty.code == code) {
                return difficulty;
            }
        }
        if (code < EASY.code) {
            return EASY;
        }
        if (code > HARD.code) {
            return HARD;
        }
        return MEDIUM;
    }
}

