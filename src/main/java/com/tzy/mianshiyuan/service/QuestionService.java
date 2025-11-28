package com.tzy.mianshiyuan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tzy.mianshiyuan.model.domain.Question;
import com.tzy.mianshiyuan.model.dto.PageRequest;
import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.vo.QuestionAnswerVO;
import com.tzy.mianshiyuan.model.vo.QuestionCatalogItemVO;
import com.tzy.mianshiyuan.model.vo.QuestionVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
* @author Windows11
* @description 针对表【question(题目表)】的数据库操作Service
* @createDate 2025-11-12 01:12:07
*/
public interface QuestionService extends IService<Question> {

    /**
     * 创建题目
     * @param request 创建请求
     * @param creatorId 创建人ID
     * @param isAdmin 是否管理员
     * @return 题目信息
     */
    QuestionVO createQuestion(QuestionDTOs.QuestionCreateRequest request, Long creatorId, boolean isAdmin);

    /**
     * 更新题目
     * @param id 题目ID
     * @param request 更新请求
     * @param editorId 编辑人ID
     * @param isAdmin 是否管理员
     * @return 更新后的题目信息
     */
    QuestionVO updateQuestion(Long id, QuestionDTOs.QuestionUpdateRequest request, Long editorId, boolean isAdmin);

    /**
     * 根据ID查询题目
     * @param id 题目ID
     * @return 题目信息
     */
    QuestionVO getQuestionById(Long id);

    /**
     * 分页查询题目

     * @return 分页结果
     */
    Page<QuestionVO> listQuestions(PageRequest pageRequest, QuestionDTOs.QuestionListRequest queryRequest,Long userId);

    /**
     * 批量绑定题目到题库
     * @param bankId 题库ID
     * @param questionIdList 题目ID列表
     * @param operatorId 操作者ID
     */
    void bindQuestionsToBank(Long bankId, List<Long> questionIdList, Long operatorId);

    /**
     * 批量解绑题目与题库
     * @param bankId 题库ID
     * @param questionIdList 题目ID列表
     * @return 实际解绑数量
     */
    int unbindQuestionsFromBank(Long bankId, List<Long> questionIdList);

    /**
     * 查询题库下的题目目录
     * @param bankId 题库ID
     * @return 题目目录列表
     */
    List<QuestionCatalogItemVO> listQuestionCatalogByBankId(Long bankId);

    /**
     * 分页查询当前用户创建的题目
     * @param current 当前页码
     * @param size 每页大小
     * @param creatorId 创建人ID
     * @param isPublic 是否公开（可选，用于筛选）
     * @return 分页结果
     */
    Page<QuestionVO> listMyQuestions(long current, long size, Long creatorId, Integer isPublic);

    Page<QuestionAnswerVO> listMyQuestionsAnswer(long current, long size, Long userId);
}
