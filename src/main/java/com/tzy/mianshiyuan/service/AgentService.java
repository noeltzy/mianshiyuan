package com.tzy.mianshiyuan.service;

import com.tzy.mianshiyuan.model.domain.Comment;
import com.tzy.mianshiyuan.model.domain.Question;
import com.tzy.mianshiyuan.model.dto.AnswerRatingDTO;
import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.dto.QuestionGenerationRequest;

import java.util.List;

public interface AgentService {

     List<QuestionDTOs.QuestionCreateRequest> generateQuestions(QuestionGenerationRequest questionGenerationRequest);

     AnswerRatingDTO ratingAnswer(Comment addCommentRequest, Question question);

}
