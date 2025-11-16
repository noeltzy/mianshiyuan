package com.tzy.mianshiyuan.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ResultUtils;
import com.tzy.mianshiyuan.model.dto.AddCommentRequest;
import com.tzy.mianshiyuan.model.vo.CommentVO;
import com.tzy.mianshiyuan.service.CommentService;
import com.tzy.mianshiyuan.util.UserConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.stp.StpUtil;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论接口", description = "评论相关接口")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 新增评论
     * @param addCommentRequest 评论请求
     */
    @PostMapping
    @SaCheckLogin
    @Operation(summary = "新增评论", description = "新增用户评论或回复")
    public BaseResponse<String> addComment(@Valid @RequestBody AddCommentRequest addCommentRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        commentService.addComment(addCommentRequest, userId);
        return ResultUtils.success();
    }
}
