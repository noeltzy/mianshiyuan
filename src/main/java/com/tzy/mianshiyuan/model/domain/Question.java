package com.tzy.mianshiyuan.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 题目表
 * @TableName question
 */
@TableName(value ="question")
@Data
public class Question implements Serializable {
    /**
     * 题目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 标签列表，JSON格式：["tag1","tag2"]
     */
    private String tagList;

    /**
     * 标准答案
     */
    private String answer;

    /**
     * 难度：0简单 1中等 2困难
     */
    private Integer difficulty;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 状态：0草稿 1待审 2通过 3驳回
     */
    private Integer status;

    /**
     * 是否需要VIP才能查看：0否 1是
     */
    private Integer isVipOnly;

    /**
     * 收藏量
     */
    private Integer favoriteCount;

    /**
     * 查看量
     */
    private Integer viewCount;

    /**
     * 最新审核记录ID
     */
    private Long reviewId;

    /**
     * 逻辑删除
     */
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