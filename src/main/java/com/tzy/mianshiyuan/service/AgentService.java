package com.tzy.mianshiyuan.service;

import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.dto.QuestionGenerationRequest;

import java.util.List;

public interface AgentService {

     List<QuestionDTOs.QuestionCreateRequest> generateQuestions( QuestionGenerationRequest questionGenerationRequest);
}
