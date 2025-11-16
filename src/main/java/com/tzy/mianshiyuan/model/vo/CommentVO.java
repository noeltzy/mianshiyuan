package com.tzy.mianshiyuan.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class CommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 题目ID（逻辑外键：question.id）
     */
    private Long questionId;
    /**
     * 发布者用户信息
     */
    private UserVO userVO;
    /**
     * 父评论ID（空为顶级评论）
     */
    private Long parentId;
    /**
     * 子评论
     */
    private List<CommentVO> children;
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
    private Date updatedAt;

    /**
     *
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
