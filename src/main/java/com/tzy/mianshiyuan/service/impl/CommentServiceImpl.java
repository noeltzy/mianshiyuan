package com.tzy.mianshiyuan.service.impl;

import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.mapper.AnswerRatingMapper;
import com.tzy.mianshiyuan.mapper.CommentMapper;
import com.tzy.mianshiyuan.mapper.QuestionMapper;
import com.tzy.mianshiyuan.mapper.UserMapper;
import com.tzy.mianshiyuan.model.domain.AnswerRating;
import com.tzy.mianshiyuan.model.domain.Comment;
import com.tzy.mianshiyuan.model.domain.Question;
import com.tzy.mianshiyuan.model.domain.User;
import com.tzy.mianshiyuan.model.dto.AddCommentRequest;
import com.tzy.mianshiyuan.model.dto.AnswerRatingDTO;
import com.tzy.mianshiyuan.model.enums.CommentTypeEmun;
import com.tzy.mianshiyuan.model.vo.CommentVO;
import com.tzy.mianshiyuan.model.vo.UserVO;
import com.tzy.mianshiyuan.service.AgentService;
import com.tzy.mianshiyuan.service.CommentService;
import com.tzy.mianshiyuan.util.UserConverter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * @author Windows11
 * @description 针对表【comment(评论表（支持嵌套回复，无需审核）)】的数据库操作Service实现
 * @createDate 2025-11-16 15:26:18
 */
@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserMapper userMapper;

    private final QuestionMapper questionMapper;

    private final AgentService agentService;

    private final AnswerRatingMapper answerRatingMapper;

    public CommentServiceImpl(UserMapper userMapper, QuestionMapper questionMapper, AgentService agentService, AnswerRatingMapper answerRatingMapper) {
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.agentService  = agentService;
        this.answerRatingMapper = answerRatingMapper;
    }

    @Override
    public List<CommentVO> getCommentsByQuestionId(Long questionId) {
        // 查询该题目的所有评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getQuestionId, questionId)
                .eq(Comment::getIsDeleted, 0)
                .orderByAsc(Comment::getSortOrder)
                .orderByDesc(Comment::getCreatedAt);
        
        List<Comment> comments = list(wrapper);
        
        if (comments.isEmpty()) {
            return List.of();
        }
        
        // 获取所有用户ID
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // 构建评论树
        return buildCommentTree(comments, userMap);
    }

    @Override
    public void addComment(AddCommentRequest addCommentRequest, Long userId) {
        // 参数验证
        if (addCommentRequest == null || addCommentRequest.getQuestionId() == null || 
            addCommentRequest.getContent() == null || addCommentRequest.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Question question = questionMapper.selectById(addCommentRequest.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //如果有父亲评论
        if(addCommentRequest.getParentId()!=null){
            Comment comment = getById(addCommentRequest.getParentId());
            if(comment==null){
                throw  new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            // 只允许两级评论，有父亲评论的地方，只能是回复,不能是用户回答 包括ai回答
            if(CommentTypeEmun.getByCode(addCommentRequest.getCommentType()).equals(CommentTypeEmun.USER_ANSWER)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }

        // 创建Comment对象
        Comment comment = new Comment();
        comment.setQuestionId(addCommentRequest.getQuestionId());
        comment.setUserId(userId);
        comment.setParentId(addCommentRequest.getParentId());
        comment.setCommentType(addCommentRequest.getCommentType());
        comment.setContent(addCommentRequest.getContent().trim());
        save(comment);
        if(CommentTypeEmun.getByCode(addCommentRequest.getCommentType()).equals(CommentTypeEmun.USER_ANSWER)){
            // 异步执行评分
            CompletableFuture.runAsync(()-> aiRating(comment,question));
        }
    }

    private void aiRating(Comment comment, Question question){
        AnswerRatingDTO answerRatingDTO = agentService.ratingAnswer(comment, question);
        AnswerRating answerRating = new AnswerRating();
        answerRating.setCommentId(comment.getId());
        answerRating.setFeedback(answerRatingDTO.getFeedback());
        answerRating.setScore(answerRatingDTO.getScore());
        answerRating.setRaterType(0);
        answerRating.setRaterId(2L);


        answerRatingMapper.insert(answerRating);

        AddCommentRequest aiReplyRequest = new AddCommentRequest();
        aiReplyRequest.setQuestionId(question.getId());
        aiReplyRequest.setParentId(comment.getId());
        aiReplyRequest.setCommentType(CommentTypeEmun.AI_RATING.getCode());
        aiReplyRequest.setContent(answerRatingDTO.getFeedback()+"所以你的评分为:"+ answerRatingDTO.getScore());
        addComment(aiReplyRequest,2L);
    }
    /**
     * 构建评论树形结构
     */
    private List<CommentVO> buildCommentTree(List<Comment> comments, Map<Long, User> userMap) {
        // 分离顶级评论和子评论
        List<Comment> topLevelComments = comments.stream()
                .filter(comment -> comment.getParentId() == null)
                .toList();
        
        List<Comment> childComments = comments.stream()
                .filter(comment -> comment.getParentId() != null)
                .collect(Collectors.toList());
        
        // 构建顶级评论
        return topLevelComments.stream()
                .map(comment -> buildCommentVO(comment, userMap, childComments))
                .collect(Collectors.toList());
    }

    /**
     * 构建单个评论VO，包含其子评论
     */
    private CommentVO buildCommentVO(Comment comment, Map<Long, User> userMap, List<Comment> allChildComments) {
        CommentVO commentVO = convertToVO(comment, userMap);
        
        // 查找该评论的子评论
        List<Comment> children = allChildComments.stream()
                .filter(child -> child.getParentId().equals(comment.getId()))
                .toList();
        
        if (!children.isEmpty()) {
            // 递归构建子评论
            List<CommentVO> childVOs = children.stream()
                    .map(child -> buildCommentVO(child, userMap, allChildComments))
                    .collect(Collectors.toList());
            commentVO.setChildren(childVOs);
        }
        
        return commentVO;
    }

    /**
     * 将Comment转换为CommentVO
     */
    private CommentVO convertToVO(Comment comment, Map<Long, User> userMap) {
        CommentVO commentVO = new CommentVO();
        commentVO.setId(comment.getId());
        commentVO.setQuestionId(comment.getQuestionId());
        commentVO.setParentId(comment.getParentId());
        commentVO.setCommentType(comment.getCommentType());
        commentVO.setContent(comment.getContent());
        commentVO.setIsPinned(comment.getIsPinned());
        commentVO.setLikeCount(comment.getLikeCount());
        commentVO.setSortOrder(comment.getSortOrder());
        commentVO.setUpdatedAt(comment.getUpdatedAt());
        commentVO.setCreatedAt(comment.getCreatedAt());
        
        // 设置用户信息
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            // 使用UserConverter工具类进行转换
            UserVO userVO = UserConverter.toVO(user);
            commentVO.setUserVO(userVO);
        }
        
        return commentVO;
    }
}
