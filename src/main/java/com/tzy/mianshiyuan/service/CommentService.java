package com.tzy.mianshiyuan.service;

import com.tzy.mianshiyuan.model.domain.Comment;
import com.tzy.mianshiyuan.model.dto.AddCommentRequest;
import com.tzy.mianshiyuan.model.vo.CommentVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Windows11
* @description 针对表【comment(评论表（支持嵌套回复，无需审核）)】的数据库操作Service
* @createDate 2025-11-16 15:26:18
*/
public interface CommentService extends IService<Comment> {

    /**
     * 根据题目ID获取评论列表（支持树形结构）
     * @param questionId 题目ID
     * @return 评论列表
     */
    List<CommentVO> getCommentsByQuestionId(Long questionId);

    /**
     * 新增评论
     * @param addCommentRequest 评论请求DTO
     * @param userId 当前用户ID
     * @return 是否成功
     */
    void addComment(AddCommentRequest addCommentRequest, Long userId);
}
