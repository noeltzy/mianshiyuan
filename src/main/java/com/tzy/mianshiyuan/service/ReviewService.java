package com.tzy.mianshiyuan.service;

import com.tzy.mianshiyuan.model.domain.Review;
import com.tzy.mianshiyuan.model.dto.ReviewContext;
import com.tzy.mianshiyuan.model.dto.ReviewRequest;
import com.tzy.mianshiyuan.model.dto.ReviewResult;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Windows11
* @description 针对表【review(审核表（逻辑外键版）)】的数据库操作Service
* @createDate 2025-11-12 00:24:49
*/
public interface ReviewService extends IService<Review> {

    /**
     * 创建并保存审核记录
     *
     * @param contentId   内容ID（题库ID或题目ID）
     * @param contentType 内容类型（ReviewConstants.CONTENT_TYPE_BANK 或 CONTENT_TYPE_QUESTION）
     * @param operatorId  操作人ID
     * @param isAdmin     是否管理员（管理员自动审核通过）
     * @return 审核记录ID
     */
    Long createReviewRecord(Long contentId, Integer contentType, Long operatorId, boolean isAdmin);

    /**
     * 处理审核流程
     * 判断是否需要创建审核记录，如果需要则创建并返回结果
     *
     * @param context 审核上下文
     * @return 审核结果，包含是否需要更新实体、reviewId、是否自动通过等信息
     */
    ReviewResult processReview(ReviewContext context);


    /**
     * 人工审核接口
     */

    void review(ReviewRequest request,Long reviewerId);
}
