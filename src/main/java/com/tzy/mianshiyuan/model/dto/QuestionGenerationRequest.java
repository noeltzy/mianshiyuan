package com.tzy.mianshiyuan.model.dto;

import lombok.Data;



@Data
public class QuestionGenerationRequest {
    public String topic;

    public int difficulty;

    public int count;
}
