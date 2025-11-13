package com.tzy.mianshiyuan.service.impl;

import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.dto.QuestionGenerationRequest;
import com.tzy.mianshiyuan.service.AgentService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {

    @Resource
    ChatClient client;


    public List<QuestionDTOs.QuestionCreateRequest> generateQuestions(
            QuestionGenerationRequest questionGenerationRequest) {

        String prompt = String.format("""
                        你是面试题生成专家。
                        请根据以下要求生成 %d 道面试题，并严格以 JSON 数组格式返回：
                        
                        1. JSON 数组的每个元素格式如下：
                        [
                          {
                            "question": "面试题问题",
                            "answer": "参考答案，必须使用 Markdown 格式输出，包括代码块、列表、标题等，禁止转义换行或引号,制表符请使用HTML标签防止出现转移换行",
                            "difficulty": "难度1-3",
                            "tagList": ["java","后端"]
                          }
                        ]
                        2. 不允许在 JSON 外输出任何额外文字。
                        3. 保证 JSON 完整合法，可直接解析。
                        
                        题目要求: %s
                        整体平均难度: %s
                        """, questionGenerationRequest.getCount(),
                questionGenerationRequest.getTopic(),
                questionGenerationRequest.getDifficulty());


        return client.prompt()
                .system(" 你是面试题生成专家。请根据以下要求生成面试题，")
                .user(prompt)
                .call().entity(new ParameterizedTypeReference<>() {
                });
    }
}
