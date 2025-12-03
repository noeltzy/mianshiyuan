package com.tzy.mianshiyuan.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.common.JsonUtils;
import com.tzy.mianshiyuan.constant.QuestionConstants;
import com.tzy.mianshiyuan.constant.ReviewConstants;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.mapper.AnswerRatingMapper;
import com.tzy.mianshiyuan.mapper.CommentMapper;
import com.tzy.mianshiyuan.mapper.QuestionMapper;
import com.tzy.mianshiyuan.model.domain.AnswerRating;
import com.tzy.mianshiyuan.constant.QuestionExtMapKey;
import com.tzy.mianshiyuan.model.domain.Bank;
import com.tzy.mianshiyuan.model.domain.BankQuestion;
import com.tzy.mianshiyuan.model.domain.Comment;
import com.tzy.mianshiyuan.model.domain.Question;
import com.tzy.mianshiyuan.model.dto.PageRequest;
import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.dto.ReviewContext;
import com.tzy.mianshiyuan.model.dto.ReviewResult;
import com.tzy.mianshiyuan.model.enums.CommentTypeEmun;
import com.tzy.mianshiyuan.model.enums.QuestionDifficultyEnum;
import com.tzy.mianshiyuan.model.vo.AnswerRatingVO;
import com.tzy.mianshiyuan.model.vo.QuestionAnswerVO;
import com.tzy.mianshiyuan.model.vo.QuestionCatalogItemVO;
import com.tzy.mianshiyuan.model.vo.QuestionVO;
import com.tzy.mianshiyuan.service.BankQuestionService;
import com.tzy.mianshiyuan.service.BankService;
import com.tzy.mianshiyuan.service.QuestionService;
import com.tzy.mianshiyuan.service.ReviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Windows11
* @description 针对表【question(题目表)】的数据库操作Service实现
* @createDate 2025-11-12 01:12:07
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

    private final ReviewService reviewService;
    private final BankService bankService;
    private final BankQuestionService bankQuestionService;
    private final CommentMapper commentMapper;
    private final AnswerRatingMapper answerRatingMapper;

    public QuestionServiceImpl(ReviewService reviewService,
                               @Lazy
                               BankService bankService,
                               BankQuestionService bankQuestionService,
                               CommentMapper commentMapper,
                               AnswerRatingMapper answerRatingMapper) {
        this.reviewService = reviewService;
        this.bankService = bankService;
        this.bankQuestionService = bankQuestionService;
        this.commentMapper = commentMapper;
        this.answerRatingMapper = answerRatingMapper;
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

        // 设置公开状态，默认为公开
        question.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : QuestionConstants.PUBLIC);

        // 私有题目强制跳过审核，直接通过
        if (QuestionConstants.PRIVATE.equals(question.getIsPublic())) {
            question.setStatus(QuestionConstants.STATUS_APPROVED);
        } else {
            boolean submitForReview = Boolean.TRUE.equals(request.getSubmitForReview());
            question.setStatus(determineStatus(submitForReview, isAdmin));
        }

        boolean saved = this.save(question);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建题目失败");
        }

        // 处理审核流程
        ReviewResult reviewResult = reviewService.processReview(ReviewContext.builder()
                .contentId(question.getId())
                .contentType(ReviewConstants.CONTENT_TYPE_QUESTION)
                .operatorId(creatorId)
                .isAdmin(isAdmin)
                .isPublic(QuestionConstants.PUBLIC.equals(question.getIsPublic()))
                .submitForReview(Boolean.TRUE.equals(request.getSubmitForReview()))
                .build());
        if (reviewResult.isNeedsUpdate()) {
            question.setReviewId(reviewResult.getReviewId());
            if (reviewResult.isAutoApproved()) {
                question.setStatus(QuestionConstants.STATUS_APPROVED);
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

        // 私有题目强制跳过审核，直接通过
        if (QuestionConstants.PRIVATE.equals(question.getIsPublic())) {
            question.setStatus(QuestionConstants.STATUS_APPROVED);
            question.setReviewId(null);
        } else {
            boolean submitForReview = Boolean.TRUE.equals(request.getSubmitForReview());
            question.setStatus(determineStatus(submitForReview, isAdmin));
            if (!submitForReview) {
                question.setReviewId(null);
            }
        }

        boolean updated = this.updateById(question);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "更新题目失败");
        }

        // 处理审核流程
        ReviewResult reviewResult = reviewService.processReview(ReviewContext.builder()
                .contentId(question.getId())
                .contentType(ReviewConstants.CONTENT_TYPE_QUESTION)
                .operatorId(editorId)
                .isAdmin(isAdmin)
                .isPublic(QuestionConstants.PUBLIC.equals(question.getIsPublic()))
                .submitForReview(Boolean.TRUE.equals(request.getSubmitForReview()))
                .build());
        if (reviewResult.isNeedsUpdate()) {
            question.setReviewId(reviewResult.getReviewId());
            if (reviewResult.isAutoApproved()) {
                question.setStatus(QuestionConstants.STATUS_APPROVED);
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
        
        // 权限校验：问题5-A
        // 公开且审核通过：所有人可见
        if (QuestionConstants.PUBLIC.equals(question.getIsPublic()) && QuestionConstants.STATUS_APPROVED.equals(question.getStatus())) {
            return toVO(question);
        }
        
        // 获取当前登录用户ID
        Long currentUserId = StpUtil.getLoginId(1L);
        
        // 公开但未审核通过：仅创建者和管理员可见
        if (QuestionConstants.PUBLIC.equals(question.getIsPublic())) {
            boolean isCreator = currentUserId != null && Objects.equals(question.getCreatorId(), currentUserId);
            boolean isAdmin = currentUserId != null && StpUtil.hasRole("ADMIN");
            if (isCreator || isAdmin) {
                return toVO(question);
            }
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题目不存在");
        }
        
        // 私有题目：仅创建者可见
        if (QuestionConstants.PRIVATE.equals(question.getIsPublic())) {
            boolean isCreator = currentUserId != null && Objects.equals(question.getCreatorId(), currentUserId);
            if (isCreator) {
                return toVO(question);
            }
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题目不存在");
        }
        
        return toVO(question);
    }

    @Override
    public Page<QuestionVO> listQuestions(PageRequest pageRequest,QuestionDTOs.QuestionListRequest queryRequest,Long userId) {
        long current = pageRequest.getCurrent();
        long size = pageRequest.getSize();
        String title = queryRequest.getTitle();
        String tag = queryRequest.getTag();
        Integer difficulty = queryRequest.getDifficulty();
        Long bankId = queryRequest.getBankId();
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
        
        // 问题4-B：公开列表只显示公开且审核通过的题目
        queryWrapper.eq(Question::getIsPublic, QuestionConstants.PUBLIC);
        queryWrapper.eq(Question::getStatus, QuestionConstants.STATUS_APPROVED);
        queryWrapper.orderByDesc(Question::getCreatedAt);

        Page<Question> questionPage = this.page(page, queryWrapper);

        Page<QuestionVO> voPage = new Page<>(current, size, questionPage.getTotal());
        List<QuestionVO> voList = questionPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        // 如果当前用户登录 也就是 userId 非 null 在VO中的extMap填入bestScore
        List<Long> questionIdList = voList.stream().map(QuestionVO::getId).toList();
        if (userId != null && !questionIdList.isEmpty()) {
            for (QuestionVO vo : voList) {
                BigDecimal bestScore = answerRatingMapper.selectMaxScoreByQuestionAndUser(vo.getId(), userId);
                if (bestScore == null) {
                    continue;
                }
                Map<String, String> extMap = vo.getExtMap();
                if (extMap == null) {
                    extMap = new HashMap<>();
                    vo.setExtMap(extMap);
                }
                extMap.put(QuestionExtMapKey.BEST_SCORE, bestScore.stripTrailingZeros().toPlainString());
            }
        }


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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindQuestionsFromBank(Long bankId, List<Long> questionIdList) {
        // 1. 参数校验
        if (bankId == null || CollectionUtils.isEmpty(questionIdList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "题库ID或题目列表不能为空");
        }

        // 2. 校验题库是否存在
        Bank bank = bankService.getById(bankId);
        if (bank == null || Objects.equals(bank.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
        }

        // 3. 去重题目ID
        List<Long> distinctQuestionIds = questionIdList.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (distinctQuestionIds.isEmpty()) {
            return 0;
        }

        // 4. 物理删除绑定关系（不存在的绑定关系会被忽略）
        LambdaQueryWrapper<BankQuestion> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(BankQuestion::getBankId, bankId)
                .in(BankQuestion::getQuestionId, distinctQuestionIds);

        return bankQuestionService.getBaseMapper().delete(deleteWrapper);
    }

    @Override
    public List<QuestionCatalogItemVO> listQuestionCatalogByBankId(Long bankId) {
        if (bankId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "题库ID不能为空");
        }
        Bank bank = bankService.getById(bankId);
        if (bank == null || Objects.equals(bank.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
        }

        List<BankQuestion> relations = bankQuestionService.list(
                new LambdaQueryWrapper<BankQuestion>()
                        .eq(BankQuestion::getBankId, bankId)
                        .orderByAsc(BankQuestion::getSortOrder)
                        .orderByAsc(BankQuestion::getId));
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }

        Set<Long> orderedQuestionIds = relations.stream()
                .map(BankQuestion::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (orderedQuestionIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 问题7-A：根据题库的公开状态过滤题目
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<Question>()
                .in(Question::getId, orderedQuestionIds)
                .eq(Question::getDeleted, 0);
        
        // 公开题库只显示公开题目
        if (com.tzy.mianshiyuan.constant.BankConstants.PUBLIC.equals(bank.getIsPublic())) {
            questionWrapper.eq(Question::getIsPublic, QuestionConstants.PUBLIC);
        }
        // 私有题库（创建者访问）显示所有题目
        
        List<Question> questions = this.list(questionWrapper);
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        return orderedQuestionIds.stream()
                .map(questionMap::get)
                .filter(Objects::nonNull)
                .map(question -> {
                    QuestionCatalogItemVO vo = new QuestionCatalogItemVO();
                    vo.setId(question.getId());
                    vo.setTitle(question.getTitle());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<QuestionVO> listMyQuestions(long current, long size, Long creatorId, Integer isPublic) {
        if (creatorId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "创建人ID不能为空");
        }
        
        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询指定用户创建的题目
        queryWrapper.eq(Question::getCreatorId, creatorId);
        queryWrapper.eq(Question::getDeleted, 0);
        
        // 问题6-B：支持按 isPublic 筛选
        if (isPublic != null) {
            queryWrapper.eq(Question::getIsPublic, isPublic);
        }
        
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
    public Page<QuestionAnswerVO> listMyQuestionsAnswer(long current, long size, Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "用户ID不能为空");
        }

        // 查询用户的所有答案（不分页，用于按题目分组）
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getUserId, userId)
                .eq(Comment::getCommentType, CommentTypeEmun.USER_ANSWER.getCode())
                .eq(Comment::getIsDeleted, 0)
                .orderByDesc(Comment::getCreatedAt);
        List<Comment> allComments = commentMapper.selectList(commentWrapper);

        if (allComments.isEmpty()) {
            Page<QuestionAnswerVO> emptyPage = new Page<>(current, size, 0);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        // 提取questionId和commentId列表
        List<Long> questionIds = allComments.stream()
                .map(Comment::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> commentIds = allComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // 批量查询题目信息
        List<Question> questions = this.list(new LambdaQueryWrapper<Question>()
                .in(Question::getId, questionIds)
                .eq(Question::getDeleted, 0));
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 批量查询评分信息
        List<AnswerRating> ratings = answerRatingMapper.selectList(
                new LambdaQueryWrapper<AnswerRating>()
                        .in(AnswerRating::getCommentId, commentIds)
                        .orderByDesc(AnswerRating::getCreatedAt));
        Map<Long, List<AnswerRating>> ratingMap = ratings.stream()
                .collect(Collectors.groupingBy(AnswerRating::getCommentId));

        // 按题目分组答案
        Map<Long, List<Comment>> commentsByQuestion = allComments.stream()
                .collect(Collectors.groupingBy(Comment::getQuestionId));

        // 构建所有题目的VO列表（按题目最新答案时间排序）
        List<QuestionAnswerVO> allVoList = questionIds.stream()
                .map(questionId -> {
                    Question question = questionMap.get(questionId);
                    if (question == null) {
                        return null;
                    }

                    QuestionAnswerVO vo = new QuestionAnswerVO();
                    vo.setId(question.getId());
                    vo.setTitle(question.getTitle());

                    // 收集该题目的所有答案和评分
                    List<Comment> questionComments = commentsByQuestion.get(questionId);
                    List<AnswerRatingVO> ratingVOList = new java.util.ArrayList<>();
                    
                    for (Comment comment : questionComments) {
                        List<AnswerRating> commentRatings = ratingMap.getOrDefault(comment.getId(), Collections.emptyList());
                        
                        if (commentRatings.isEmpty()) {
                            // 如果没有评分，也要显示答案
                            AnswerRatingVO ratingVO = new AnswerRatingVO();
                            ratingVO.setAnswer(comment.getContent());
                            ratingVO.setFeedback(null);
                            ratingVO.setScore(null);
                            ratingVO.setCreatedAt(comment.getCreatedAt());
                            ratingVOList.add(ratingVO);
                        } else {
                            // 如果有评分，每个评分一条记录，都包含答案内容
                            for (AnswerRating rating : commentRatings) {
                                AnswerRatingVO ratingVO = new AnswerRatingVO();
                                ratingVO.setAnswer(comment.getContent());
                                ratingVO.setFeedback(rating.getFeedback());
                                ratingVO.setScore(rating.getScore());
                                ratingVO.setCreatedAt(rating.getCreatedAt());
                                ratingVOList.add(ratingVO);
                            }
                        }
                    }

                    // 按创建时间倒序排序
                    ratingVOList.sort((a, b) -> {
                        Date dateA = a.getCreatedAt();
                        Date dateB = b.getCreatedAt();
                        if (dateA == null && dateB == null) return 0;
                        if (dateA == null) return 1;
                        if (dateB == null) return -1;
                        return dateB.compareTo(dateA);
                    });

                    vo.setAnswerRatingVOList(ratingVOList);
                    return vo;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    // 按题目最新答案时间倒序排序
                    List<AnswerRatingVO> listA = a.getAnswerRatingVOList();
                    List<AnswerRatingVO> listB = b.getAnswerRatingVOList();
                    if (listA.isEmpty() && listB.isEmpty()) return 0;
                    if (listA.isEmpty()) return 1;
                    if (listB.isEmpty()) return -1;
                    Date dateA = listA.get(0).getCreatedAt();
                    Date dateB = listB.get(0).getCreatedAt();
                    if (dateA == null && dateB == null) return 0;
                    if (dateA == null) return 1;
                    if (dateB == null) return -1;
                    return dateB.compareTo(dateA);
                })
                .collect(Collectors.toList());

        // 手动分页
        long total = allVoList.size();
        long start = (current - 1) * size;
        long end = Math.min(start + size, total);
        
        List<QuestionAnswerVO> voList = start < total 
                ? allVoList.subList((int)start, (int)end)
                : Collections.emptyList();

        Page<QuestionAnswerVO> voPage = new Page<>(current, size, total);
        voPage.setRecords(voList);
        return voPage;
    }

    private int resolveDifficulty(Integer difficulty) {
        return QuestionDifficultyEnum.fromCode(difficulty).getCode();
    }

    private int determineStatus(boolean submitForReview, boolean isAdmin) {
        if (!submitForReview) {
            return QuestionConstants.STATUS_DRAFT;
        }
        return isAdmin ? QuestionConstants.STATUS_APPROVED : QuestionConstants.STATUS_PENDING;
    }

    private QuestionVO toVO(Question question) {
        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        vo.setTagList(JsonUtils.stringToList(question.getTagList()));
        return vo;
    }
}