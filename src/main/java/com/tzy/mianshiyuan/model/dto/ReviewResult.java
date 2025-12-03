package com.tzy.mianshiyuan.model.dto;

import lombok.Data;

/**
 * 审核处理结果
 */
@Data
public class ReviewResult {
    
    /**
     * 是否需要更新实体（设置 reviewId 和状态）
     */
    private boolean needsUpdate;
    
    /**
     * 审核记录ID，仅当 needsUpdate=true 时有值
     */
    private Long reviewId;
    
    /**
     * 是否自动审核通过（管理员提交时自动通过）
     */
    private boolean autoApproved;
    
    /**
     * 创建无需更新的结果
     */
    public static ReviewResult noUpdate() {
        ReviewResult result = new ReviewResult();
        result.setNeedsUpdate(false);
        return result;
    }
    
    /**
     * 创建需要更新的结果
     */
    public static ReviewResult needsUpdate(Long reviewId, boolean autoApproved) {
        ReviewResult result = new ReviewResult();
        result.setNeedsUpdate(true);
        result.setReviewId(reviewId);
        result.setAutoApproved(autoApproved);
        return result;
    }
}






