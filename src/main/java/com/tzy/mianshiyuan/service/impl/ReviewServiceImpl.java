package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.common.ErrUtils;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.constant.BankConstants;
import com.tzy.mianshiyuan.constant.QuestionConstants;
import com.tzy.mianshiyuan.constant.ReviewConstants;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.mapper.BankMapper;
import com.tzy.mianshiyuan.mapper.QuestionMapper;
import com.tzy.mianshiyuan.model.domain.Bank;
import com.tzy.mianshiyuan.model.domain.Question;
import com.tzy.mianshiyuan.model.domain.Review;
import com.tzy.mianshiyuan.model.dto.ReviewContext;
import com.tzy.mianshiyuan.model.dto.ReviewRequest;
import com.tzy.mianshiyuan.model.dto.ReviewResult;
import com.tzy.mianshiyuan.service.ReviewService;
import com.tzy.mianshiyuan.mapper.ReviewMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
* @author Windows11
* @description 针对表【review(审核表（逻辑外键版）)】的数据库操作Service实现
* @createDate 2025-11-12 00:24:49
*/
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review>
    implements ReviewService{

    @Resource
    BankMapper bankMapper;


    @Resource
    QuestionMapper questionMapper;

    @Override
    public Long createReviewRecord(Long contentId, Integer contentType, Long operatorId, boolean isAdmin) {
        Review review = new Review();
        review.setContentId(contentId);
        review.setContentType(contentType);
        review.setReviewerType(ReviewConstants.REVIEWER_TYPE_MANUAL);

        if (isAdmin) {
            review.setReviewerId(operatorId);
            review.setResult(ReviewConstants.REVIEW_RESULT_PASS);
            review.setComments(ReviewConstants.ADMIN_REVIEW_COMMENT);
        } else {
            review.setReviewerId(null);
            review.setResult(ReviewConstants.REVIEW_RESULT_PENDING);
        }

        boolean saved = this.save(review);
        if (!saved) {
            String contentName = ReviewConstants.CONTENT_TYPE_BANK.equals(contentType) ? "题库" : "题目";
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建" + contentName + "审核记录失败");
        }

        return review.getId();
    }

    @Override
    public ReviewResult processReview(ReviewContext context) {
        // 只有公开内容且提交审核时才需要创建审核记录
        if (!context.isPublic() || !context.isSubmitForReview()) {
            return ReviewResult.noUpdate();
        }

        Long reviewId = createReviewRecord(
                context.getContentId(),
                context.getContentType(),
                context.getOperatorId(),
                context.isAdmin()
        );

        return ReviewResult.needsUpdate(reviewId, context.isAdmin());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(ReviewRequest request, Long reviewerId) {
        Integer result = request.getResult();
        String comment = request.getComment();

        // 1. 参数校验
        // 校验 result 是否合法（只能是 1=通过 或 2=驳回）
        ErrUtils.errIf(!ReviewConstants.REVIEW_RESULT_PASS.equals(result) 
                && !ReviewConstants.REVIEW_RESULT_REJECT.equals(result), 
                ErrorCode.PARAMS_ERROR, "审核结果不合法");
        
        // 如果是驳回，comment 必填
        ErrUtils.errIf(ReviewConstants.REVIEW_RESULT_REJECT.equals(result) 
                && (comment == null || comment.trim().isEmpty()), 
                ErrorCode.PARAMS_ERROR, "驳回原因不能为空");

        // 2. 查询审核记录
        Review review = getById(request.getId());
        ErrUtils.errIf(Objects.isNull(review), ErrorCode.NOT_FOUND, "审核记录不存在");

        // 校验是否已审核（防止重复审核）
        ErrUtils.errIf(!ReviewConstants.REVIEW_RESULT_PENDING.equals(review.getResult()), 
                ErrorCode.OPERATION_ERROR, "该内容已审核，请勿重复操作");

        Integer contentType = review.getContentType();
        Long contentId = review.getContentId();

        // 3. 根据 contentType 更新内容状态
        // 计算目标状态：通过->PASS(2)，驳回->REJECT(3)
        Integer targetStatus = ReviewConstants.REVIEW_RESULT_PASS.equals(result) 
                ? ReviewConstants.STATUS_APPROVED 
                : ReviewConstants.STATUS_REJECTED;

        if (ReviewConstants.CONTENT_TYPE_BANK.equals(contentType)) {
            // 题库审核：校验存在性
            Bank existBank = bankMapper.selectById(contentId);
            ErrUtils.errIf(Objects.isNull(existBank), ErrorCode.NOT_FOUND, "审核对象不存在");
            
            // 只更新需要修改的字段
            Bank updateBank = new Bank();
            updateBank.setId(contentId);
            updateBank.setStatus(targetStatus);
            bankMapper.updateById(updateBank);

        } else if (ReviewConstants.CONTENT_TYPE_QUESTION.equals(contentType)) {
            // 题目审核：校验存在性
            Question existQuestion = questionMapper.selectById(contentId);
            ErrUtils.errIf(Objects.isNull(existQuestion), ErrorCode.NOT_FOUND, "审核对象不存在");
            
            // 只更新需要修改的字段
            Question updateQuestion = new Question();
            updateQuestion.setId(contentId);
            updateQuestion.setStatus(targetStatus);
            questionMapper.updateById(updateQuestion);
        }

        // 4. 更新审核记录（只更新需要修改的字段）
        Review updateReview = new Review();
        updateReview.setId(review.getId());
        updateReview.setResult(result);
        updateReview.setReviewerId(reviewerId);
        updateReview.setComments(comment);
        this.updateById(updateReview);
    }
}




