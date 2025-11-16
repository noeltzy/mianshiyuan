package com.tzy.mianshiyuan.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论表（支持嵌套回复，无需审核）
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment implements Serializable {
    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目ID（逻辑外键：question.id）
     */
    private Long questionId;

    /**
     * 评论者ID（逻辑外键：user.id）
     */
    private Long userId;

    /**
     * 父评论ID（空为顶级评论）
     */
    private Long parentId;

    /**
     * 评论类型：1=用户答案, 2=用户评论, 3=AI评分
     */
    private Integer commentType;

    /**
     * 评论内容（纯文本）
     */
    private String content;

    /**
     * 是否置顶：0否 1是
     */
    private Integer isPinned;

    /**
     * 逻辑删除：0正常 1删除
     */
    private Integer isDeleted;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 排序（置顶评论优先，然后按时间）
     */
    private Integer sortOrder;

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