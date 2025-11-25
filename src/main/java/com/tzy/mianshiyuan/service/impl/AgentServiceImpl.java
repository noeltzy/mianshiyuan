package com.tzy.mianshiyuan.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.tzy.mianshiyuan.model.domain.AnswerRating;
import com.tzy.mianshiyuan.model.domain.Comment;
import com.tzy.mianshiyuan.model.domain.Question;
import com.tzy.mianshiyuan.model.dto.AddCommentRequest;
import com.tzy.mianshiyuan.model.dto.AnswerRatingDTO;
import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.dto.QuestionGenerationRequest;
import com.tzy.mianshiyuan.service.AgentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
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
                            "title": "面试题问题",
                            "description":"简单描述 or 提示",
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
                .system("你是面试题生成专家。请根据以下要求生成面试题，")
                .user(prompt)
                .options(DashScopeChatOptions.builder().withModel("qwen-flash").build())
                .call().entity(new ParameterizedTypeReference<List<QuestionDTOs.QuestionCreateRequest>>() {
                });
    }

    @Override
    public AnswerRatingDTO ratingAnswer(Comment addCommentRequest, Question question,String strictness) {
        log.info(strictness);

        String prompt = String.format("""
                        请根据以下要求进行评分，并严格以 JSON格式返回：
                        面试题题目：%s
                        我的回答：%s
                        1. JSON 数组的每个元素格式如下：
                          {
                            "feedback": "面试题回答反馈",
                            "score": 99
                          }
                        2. 不允许在 JSON 外输出任何额外文字。
                        3. 保证 JSON 完整合法，可直接解析。
                        4. score范围0~100
                        """, question.getTitle(),
                addCommentRequest.getContent());
        return client.prompt()
                .system("你资深程序员面试官,需要帮助给我的面试题回答打分,你需要按照 一定的严格度来评判与打分 1最轻松同时评价语气非常温和，3最严格同时评价语气最严格和不客气,你的严格度为:"+strictness)
                .user(prompt)
                .options(DashScopeChatOptions.builder().withModel("qwen-flash").build())
                .call().entity(AnswerRatingDTO.class);
    }
}
