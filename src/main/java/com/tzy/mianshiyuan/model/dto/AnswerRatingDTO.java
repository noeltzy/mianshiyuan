package com.tzy.mianshiyuan.model.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnswerRatingDTO {

    private String feedback;
    /**
     * 总体得分（0-100分）
     */
    private BigDecimal score;
}
