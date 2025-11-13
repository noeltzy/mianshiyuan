package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.common.JsonUtils;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.mapper.QuestionMapper;
import com.tzy.mianshiyuan.model.domain.Bank;
import com.tzy.mianshiyuan.model.domain.BankQuestion;
import com.tzy.mianshiyuan.model.domain.Question;
import com.tzy.mianshiyuan.model.domain.Review;
import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.enums.QuestionDifficultyEnum;
import com.tzy.mianshiyuan.model.vo.QuestionVO;
import com.tzy.mianshiyuan.service.BankQuestionService;
import com.tzy.mianshiyuan.service.BankService;
import com.tzy.mianshiyuan.service.QuestionService;
import com.tzy.mianshiyuan.service.ReviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Windows11
* @description 针对表【question(题目表)】的数据库操作Service实现
* @createDate 2025-11-12 01:12:07
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

    private static final int CONTENT_TYPE_QUESTION = 2;
    private static final int REVIEW_RESULT_PENDING = 0;
    private static final int REVIEW_RESULT_PASS = 1;
    private static final int REVIEWER_TYPE_MANUAL = 1;
    private static final String ADMIN_REVIEW_COMMENT = "管理员上传自动审核通过";
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PENDING = 1;
    private static final int STATUS_APPROVED = 2;

    private final ReviewService reviewService;
    private final BankService bankService;
    private final BankQuestionService bankQuestionService;

    public QuestionServiceImpl(ReviewService reviewService,
                               BankService bankService,
                               BankQuestionService bankQuestionService) {
        this.reviewService = reviewService;
        this.bankService = bankService;
        this.bankQuestionService = bankQuestionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO createQuestion(QuestionDTOs.QuestionCreateRequest request, Long creatorId, boolean isAdmin) {
        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setDescription(request.getDescription());
        question.setTagList(JsonUtils.listToString(request.getTagList()));
        question.setAnswer(request.getAnswer());
        question.setDifficulty(resolveDifficulty(request.getDifficulty()));
        question.setCreatorId(creatorId);
        question.setFavoriteCount(0);
        question.setViewCount(0);
        question.setIsVipOnly(0);

        boolean submitForReview = Boolean.TRUE.equals(request.getSubmitForReview());
        question.setStatus(determineStatus(submitForReview, isAdmin));

        boolean saved = this.save(question);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建题目失败");
        }

        if (submitForReview) {
            Review review = buildReviewRecord(question.getId(), creatorId, isAdmin);
            boolean reviewSaved = reviewService.save(review);
            if (!reviewSaved) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建题目审核记录失败");
            }
            question.setReviewId(review.getId());
            // 管理员自动审核通过时同步更新题目状态
            if (isAdmin) {
                question.setStatus(STATUS_APPROVED);
            }
            this.updateById(question);
        }

        return toVO(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO updateQuestion(Long id, QuestionDTOs.QuestionUpdateRequest request, Long editorId, boolean isAdmin) {
        Question question = this.getById(id);
        if (question == null || Objects.equals(question.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题目不存在");
        }
        if (!isAdmin && !Objects.equals(question.getCreatorId(), editorId)) {
            throw new BusinessException(ErrorCode.NO_AUTH.getCode(), "无权编辑该题目");
        }

        question.setTitle(request.getTitle());
        question.setDescription(request.getDescription());
        question.setTagList(JsonUtils.listToString(request.getTagList()));
        question.setAnswer(request.getAnswer());
        question.setDifficulty(resolveDifficulty(request.getDifficulty()));

        boolean submitForReview = Boolean.TRUE.equals(request.getSubmitForReview());
        question.setStatus(determineStatus(submitForReview, isAdmin));
        if (!submitForReview) {
            question.setReviewId(null);
        }

        boolean updated = this.updateById(question);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "更新题目失败");
        }

        if (submitForReview) {
            Review review = buildReviewRecord(question.getId(), editorId, isAdmin);
            boolean reviewSaved = reviewService.save(review);
            if (!reviewSaved) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建题目审核记录失败");
            }
            question.setReviewId(review.getId());
            if (isAdmin) {
                question.setStatus(STATUS_APPROVED);
            }
            this.updateById(question);
        }

        return toVO(question);
    }

    @Override
    public QuestionVO getQuestionById(Long id) {
        Question question = this.getById(id);
        if (question == null || Objects.equals(question.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题目不存在");
        }
        return toVO(question);
    }

    @Override
    public Page<QuestionVO> listQuestions(long current, long size, String title, String tag, Integer difficulty, Long bankId) {
        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        if (title != null && !title.trim().isEmpty()) {
            queryWrapper.like(Question::getTitle, title.trim());
        }
        if (tag != null && !tag.trim().isEmpty()) {
            queryWrapper.like(Question::getTagList, "\"" + tag.trim() + "\"");
        }
        if (difficulty != null) {
            queryWrapper.eq(Question::getDifficulty, difficulty);
        }
        if (bankId != null) {
            Bank bank = bankService.getById(bankId);
            if (bank == null || Objects.equals(bank.getDeleted(), 1)) {
                throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
            }
            List<BankQuestion> relations = bankQuestionService.list(
                    new LambdaQueryWrapper<BankQuestion>().eq(BankQuestion::getBankId, bankId));
            if (relations.isEmpty()) {
                Page<QuestionVO> emptyPage = new Page<>(current, size, 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            Set<Long> questionIds = relations.stream()
                    .map(BankQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            queryWrapper.in(Question::getId, questionIds);
        }
        queryWrapper.eq(Question::getDeleted, 0);
        queryWrapper.orderByDesc(Question::getCreatedAt);

        Page<Question> questionPage = this.page(page, queryWrapper);

        Page<QuestionVO> voPage = new Page<>(current, size, questionPage.getTotal());
        List<QuestionVO> voList = questionPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindQuestionsToBank(Long bankId, List<Long> questionIdList, Long operatorId) {
        if (bankId == null || CollectionUtils.isEmpty(questionIdList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "题库ID或题目列表不能为空");
        }

        Bank bank = bankService.getById(bankId);
        if (bank == null || Objects.equals(bank.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
        }

        if (questionIdList.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "题目ID不能为空");
        }

        List<Long> distinctQuestionIds = questionIdList.stream()
                .distinct()
                .collect(Collectors.toList());

        List<Question> questions = this.list(new LambdaQueryWrapper<Question>()
                .in(Question::getId, distinctQuestionIds)
                .eq(Question::getDeleted, 0));
        if (questions.size() != distinctQuestionIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "部分题目不存在");
        }

        LambdaQueryWrapper<BankQuestion> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(BankQuestion::getBankId, bankId)
                .in(BankQuestion::getQuestionId, distinctQuestionIds);
        List<BankQuestion> exists = bankQuestionService.list(existsWrapper);
        Set<Long> existsQuestionIds = exists.stream()
                .map(BankQuestion::getQuestionId)
                .collect(Collectors.toSet());

        List<BankQuestion> toSave = distinctQuestionIds.stream()
                .filter(id -> !existsQuestionIds.contains(id))
                .map(qid -> {
                    BankQuestion relation = new BankQuestion();
                    relation.setBankId(bankId);
                    relation.setQuestionId(qid);
                    return relation;
                })
                .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            boolean saved = bankQuestionService.saveBatch(toSave);
            if (!saved) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "绑定题目到题库失败");
            }
        }
    }

    private int resolveDifficulty(Integer difficulty) {
        return QuestionDifficultyEnum.fromCode(difficulty).getCode();
    }

    private int determineStatus(boolean submitForReview, boolean isAdmin) {
        if (!submitForReview) {
            return STATUS_DRAFT;
        }
        return isAdmin ? STATUS_APPROVED : STATUS_PENDING;
    }

    private Review buildReviewRecord(Long questionId, Long operatorId, boolean isAdmin) {
        Review review = new Review();
        review.setContentId(questionId);
        review.setContentType(CONTENT_TYPE_QUESTION);
        review.setReviewerType(REVIEWER_TYPE_MANUAL);
        if (isAdmin) {
            review.setReviewerId(operatorId);
            review.setResult(REVIEW_RESULT_PASS);
            review.setComments(ADMIN_REVIEW_COMMENT);
        } else {
            review.setReviewerId(null);
            review.setResult(REVIEW_RESULT_PENDING);
            review.setComments("用户提交待审核");
        }
        return review;
    }

    private QuestionVO toVO(Question question) {
        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        vo.setTagList(JsonUtils.stringToList(question.getTagList()));
        return vo;
    }
}