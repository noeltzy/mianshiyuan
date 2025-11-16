package com.tzy.mianshiyuan.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class QuestionAnswerVO {
    /**
     * 问题 id
     */
    Long id;
    /**
     * 问题
     */
    String title;

    List<AnswerRatingVO> answerRatingVOList;
}
