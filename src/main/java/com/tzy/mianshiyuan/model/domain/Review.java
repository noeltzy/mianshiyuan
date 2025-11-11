package com.tzy.mianshiyuan.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 审核表（逻辑外键版）
 * @TableName review
 */
@TableName(value ="review")
@Data
public class Review implements Serializable {
    /**
     * 审核ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 审核对象ID（逻辑外键：bank.id 或 question.id）
     */
    private Long contentId;

    /**
     * 审核对象类型：1题库 2题目 其他待扩展
     */
    private Integer contentType;

    /**
     * 审核人ID（逻辑外键：user.id）
     */
    private Long reviewerId;

    /**
     * 审核方式：1人工 2AI
     */
    private Integer reviewerType;

    /**
     * 审核结果：0待审 1通过 2驳回
     */
    private Integer result;

    /**
     * 审核意见
     */
    private String comments;

    /**
     * 逻辑删除：0正常 1删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 
     */
    private Date createdAt;

    /**
     * 
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}