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
                        请根据以下题库要求生成 %d 道面试题，并严格以 JSON 数组格式返回：
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
                        题目标签主题：%s
                        整体平均难度: %s
                        """, questionGenerationRequest.getCount(),
                questionGenerationRequest.getDesc(),
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

        // *** 优化后的 User Prompt ***
        String prompt = String.format("""
            请根据上述严格度定义，对以下面试题回答进行专业评分，并严格以 **JSON** 格式返回：
            
            ### 待评估内容
            - **面试题题目:** %s
            - **我的回答:** %s
            
            ### 评分与反馈要求
            1. **结构格式:** 必须返回一个 **JSON**（如 `{...}`）。
            2. **对象格式:** JSON必须包含 `feedback` 和 `score` 两个字段：
               ```json
               {
                 "feedback": "请给出详细的反馈，解释为什么得分/失分，以及如何改进。",
                 "score": 0-100之间的整数
               }
               ```
            3. **内容要求:** - **feedback:** 必须提供**建设性**的、**针对性强**的评价。
               - **score:** 必须是**0到100**范围内的整数。
            4. **禁止输出:** 绝对不允许在 JSON 的**外部**包含任何额外的文字、解释、 Markdown标识符（如 ````json`）或注释。
            5. **合法性:** 保证 JSON 完整、合法，可直接被解析。
            """, question.getTitle(), addCommentRequest.getContent());

        // *** 优化后的 System Prompt ***
        return client.prompt()
                .system("""
                你是一位**资深技术面试官**，精通Java、Spring Boot、多线程等后端技术。
                你的任务是**严格、专业**地评估候选人对面试题的回答，被面试者与面试官之间的关系是口头对话形式。
            
                **评分严格度定义 (基于用户输入的 strictness: %s)：**
                - **1 (宽松/温和):** 侧重于回答的**核心正确性**，对细节错误和表达瑕疵容忍度高。反馈语气**鼓励、温和**。
                - **2 (标准/专业):** 侧重于回答的**准确性和完整性**，要求技术点覆盖全面，表达清晰。反馈语气**专业、中立**。
                - **3 (严格/不客气):** 要求回答**精准无误、深入底层原理、考虑边缘情况**。对细节错误、概念模糊处**非常挑剔**。反馈语气**直接、严厉**。
            
                **输出要求：** 必须且只能返回有效的 JSON 格式。
                """.formatted(strictness))
                .user(prompt)
                .options(DashScopeChatOptions.builder().withModel("qwen-flash").build())
                .call().entity(AnswerRatingDTO.class);
    }
}
