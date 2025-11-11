package com.tzy.mianshiyuan.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class BankVO {
    private Long id;
    private String name;
    private String description;
    private List<String> tagList;
    private Long creatorId;
    private Integer status;
    private Long reviewId;
    private Date createdAt;
    private Date updatedAt;
}

