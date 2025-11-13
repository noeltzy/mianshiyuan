package com.tzy.mianshiyuan.controller;


import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.common.ResultUtils;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.dto.QuestionGenerationRequest;
import com.tzy.mianshiyuan.service.AgentService;
import com.tzy.mianshiyuan.util.GsonUtils;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@Tag(name = "agent接口", description = "完成系统agent对话功能")
@Slf4j
public class AgentController {

    @Resource
    AgentService agentService;

    @GetMapping("/question")
    public BaseResponse<List<QuestionDTOs.QuestionCreateRequest>> getQuestion(@ModelAttribute QuestionGenerationRequest questionGenerationRequest){
        if(questionGenerationRequest==null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int count = questionGenerationRequest.getCount();
        if(count<=0||count>=10){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int difficulty = questionGenerationRequest.getDifficulty();
        if(difficulty<0 ||difficulty>3){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String topic = questionGenerationRequest.getTopic();
        if(StringUtils.isBlank(topic)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<QuestionDTOs.QuestionCreateRequest> questionList = agentService.generateQuestions(questionGenerationRequest);
        log.info("生成的问题：{}",GsonUtils.toJson(questionList));
        return ResultUtils.success(questionList);
    }


}
