package com.tzy.mianshiyuan.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Integer isPublic;
    private Integer isVipOnly;
    private Integer favoriteCount;
    private Integer viewCount;
    private Long reviewId;
    /**
     * 预留字段
     */
    private Map<String,String> extMap = new HashMap<>();
    private Date createdAt;
    private Date updatedAt;
}

