package com.tzy.mianshiyuan.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 题库-题目关联表（多对多逻辑关系）
 * @TableName bank_question
 */
@TableName(value ="bank_question")
@Data
public class BankQuestion implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题库ID（逻辑外键：bank.id）
     */
    private Long bankId;

    /**
     * 题目ID（逻辑外键：question.id）
     */
    private Long questionId;

    /**
     * 排序序号（题库内题目顺序）
     */
    private Integer sortOrder;

    /**
     * 不走逻辑删除 直接物理删除 中间表无需 留存
     */
    private Integer deleted;

    /**
     * 
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}