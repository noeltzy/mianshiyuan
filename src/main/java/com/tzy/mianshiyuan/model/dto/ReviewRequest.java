package com.tzy.mianshiyuan.model.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
public class ReviewRequest {
    /**
     * reviewId
     */

    @NotNull(message = "审核内容ID不能为空")
    private Long id;
    /**
     * 结果
     */
    @NotNull(message = "审核结果不能为空")
    private Integer result;
    /**
     * comment 评论，审核不通过一定要填
     */
    private String comment;
}
