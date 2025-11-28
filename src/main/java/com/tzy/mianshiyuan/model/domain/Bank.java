package com.tzy.mianshiyuan.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 题库表
 * @TableName bank
 */
@TableName(value ="bank")
@Data
public class Bank implements Serializable {
    /**
     * 题库ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题库名称
     */
    private String name;

    /**
     * 题库简介
     */
    private String description;
    /**
     * 是否公开 0 私有 1公开
     */
    private Integer isPublic;

    /**
     * 头图URL
     */
    private String coverImage;

    /**
     * 标签列表（JSON格式：["tag1","tag2"]）
     */
    private String tagList;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 状态：0草稿 1待审 2通过 3驳回
     */
    private Integer status;

    /**
     * 最新审核记录ID
     */
    private Long reviewId;

    /**
     * 逻辑删除
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