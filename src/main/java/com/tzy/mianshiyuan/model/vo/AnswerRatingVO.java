package com.tzy.mianshiyuan.model.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AnswerRatingVO {


    /**
     * 用户答案
     */
    private  String answer;

    /**
     * 评价内容
     */
    private String feedback;

    /**
     * 总体得分（0-100分）
     */
    private BigDecimal score;
    /**
     * 评价时间
     */
    private Date createdAt;
}
