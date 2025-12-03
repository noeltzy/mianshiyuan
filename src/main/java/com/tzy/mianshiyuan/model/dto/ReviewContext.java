package com.tzy.mianshiyuan.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 审核上下文对象
 * 封装审核流程所需的所有参数
 */
@Data
@Builder
public class ReviewContext {
    
    /**
     * 内容ID（题库ID或题目ID）
     */
    private Long contentId;
    
    /**
     * 内容类型（ReviewConstants.CONTENT_TYPE_BANK 或 CONTENT_TYPE_QUESTION）
     */
    private Integer contentType;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 是否管理员
     */
    private boolean isAdmin;
    
    /**
     * 内容是否公开
     */
    private boolean isPublic;
    
    /**
     * 是否提交审核
     */
    private boolean submitForReview;
}






