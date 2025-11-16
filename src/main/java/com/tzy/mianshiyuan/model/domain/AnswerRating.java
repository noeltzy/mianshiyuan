package com.tzy.mianshiyuan.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 回答评分表
 * @TableName answer_rating
 */
@TableName(value ="answer_rating")
@Data
public class AnswerRating implements Serializable {
    /**
     * 评分ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评论ID（逻辑外键：comment.id）
     */
    private Long commentId;

    /**
     * 评价内容
     */
    private String feedback;

    /**
     * 总体得分（0-100分）
     */
    private BigDecimal score;

    /**
     * 评分者类型：0=AI 1=人工
     */
    private Integer raterType;

    /**
     * 评分者ID（逻辑外键：user.id，AI评分时为NULL）
     */
    private Long raterId;

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