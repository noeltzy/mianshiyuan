package com.tzy.mianshiyuan.model.enums;

import lombok.Getter;

/**
 * 评论类型枚举
 */
@Getter
public enum CommentTypeEmun {
    /**
     * 用户答案
     */
    USER_ANSWER(1, "用户答案"),
    
    /**
     * 用户评论
     */
    USER_COMMENT(2, "用户评论"),
    
    /**
     * AI评分
     */
    AI_RATING(3, "AI评分");

    private final Integer code;
    private final String description;

    CommentTypeEmun(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举值
     * @param code 评论类型代码
     * @return 评论类型枚举
     */
    public static CommentTypeEmun getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (CommentTypeEmun type : CommentTypeEmun.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     * @param code 评论类型代码
     * @return 是否有效
     */
    public static boolean isValidCode(Integer code) {
        return getByCode(code) != null;
    }
}