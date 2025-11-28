package com.tzy.mianshiyuan.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class BankVO {
    private Long id;
    private String name;
    private String description;
    /**
     * 头图URL
     */
    private String coverImage;

    /**
     * 是否公开 1 公开 0 非公开
     */
    private Integer isPublic;
    private List<String> tagList;
    private Long creatorId;
    /**
     * 状态：0草稿 1待审 2通过 3驳回
     */
    private Integer status;
    private Long reviewId;
    private Date createdAt;
    private Date updatedAt;
}

