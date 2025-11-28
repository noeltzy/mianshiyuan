package com.tzy.mianshiyuan.constant;

/**
 * 题目相关常量
 */
public interface QuestionConstants {
    
    /**
     * 公开题目
     */
    Integer PUBLIC = 1;
    
    /**
     * 私有题目
     */
    Integer PRIVATE = 0;
    
    /**
     * 状态：草稿
     */
    Integer STATUS_DRAFT = 0;
    
    /**
     * 状态：待审
     */
    Integer STATUS_PENDING = 1;
    
    /**
     * 状态：通过
     */
    Integer STATUS_APPROVED = 2;
    
    /**
     * 状态：驳回
     */
    Integer STATUS_REJECTED = 3;
}

