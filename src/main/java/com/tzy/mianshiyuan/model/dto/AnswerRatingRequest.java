package com.tzy.mianshiyuan.model.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnswerRatingRequest {

    Long commentId;

    /**
     * 用户回答
     */
    String answer;

}
