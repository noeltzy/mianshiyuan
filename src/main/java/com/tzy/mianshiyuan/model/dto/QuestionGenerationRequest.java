package com.tzy.mianshiyuan.model.dto;

import lombok.Data;



@Data
public class QuestionGenerationRequest {
    public String topic;

    public String desc;

    public int difficulty;

    public int count;
}
