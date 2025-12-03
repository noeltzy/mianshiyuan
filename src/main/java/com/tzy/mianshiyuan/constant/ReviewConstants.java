package com.tzy.mianshiyuan.constant;

/**
 * 审核相关常量
 */
public interface ReviewConstants {

    // ============= 内容类型 =============
    /**
     * 内容类型：题库
     */
    Integer CONTENT_TYPE_BANK = 1;
    
    /**
     * 内容类型：题目
     */
    Integer CONTENT_TYPE_QUESTION = 2;

    // ============= 审核结果 =============
    /**
     * 审核结果：待审核
     */
    Integer REVIEW_RESULT_PENDING = 0;
    
    /**
     * 审核结果：通过
     */
    Integer REVIEW_RESULT_PASS = 1;

    /**
     * 审核结果：驳回
     */
    Integer REVIEW_RESULT_REJECT = 2;

    // ============= 审核人类型 =============
    /**
     * 审核人类型：人工审核
     */
    Integer REVIEWER_TYPE_MANUAL = 1;

    // ============= 审核评论 =============
    /**
     * 管理员自动审核通过评论
     */
    String ADMIN_REVIEW_COMMENT = "管理员上传自动审核通过";

    // ============= 内容状态（统一） =============
    /**
     * 状态：草稿
     */
    Integer STATUS_DRAFT = 0;

    /**
     * 状态：待审核
     */
    Integer STATUS_PENDING = 1;

    /**
     * 状态：审核通过
     */
    Integer STATUS_APPROVED = 2;

    /**
     * 状态：审核驳回
     */
    Integer STATUS_REJECTED = 3;
}

