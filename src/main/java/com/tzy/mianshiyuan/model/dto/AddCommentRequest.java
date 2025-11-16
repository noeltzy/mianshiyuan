package com.tzy.mianshiyuan.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tzy.mianshiyuan.model.vo.UserVO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class AddCommentRequest {
    /**
     * 题目ID（逻辑外键：question.id）
     */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /**
     * 父评论ID（空为顶级评论）
     */
    private Long parentId;

    /**
     * 评论类型：1=用户答案, 2=用户评论, 3=AI评分
     */
    @NotNull(message = "评论类型不能为空")
    private Integer commentType;

    /**
     * 评论内容（纯文本）
     */
    @NotNull(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论内容长度必须在1-1000字符之间")
    private String content;
}
