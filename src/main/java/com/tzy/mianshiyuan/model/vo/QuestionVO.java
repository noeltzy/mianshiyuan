package com.tzy.mianshiyuan.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class QuestionVO {
    private Long id;
    private String title;
    private String description;
    private List<String> tagList;
    private String answer;
    private Integer difficulty;
    private Long creatorId;
    private Integer status;
    private Integer isVipOnly;
    private Integer favoriteCount;
    private Integer viewCount;
    private Long reviewId;
    private Date createdAt;
    private Date updatedAt;
}

