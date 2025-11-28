package com.tzy.mianshiyuan.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ResultUtils;
import com.tzy.mianshiyuan.model.dto.PageRequest;
import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.vo.CommentVO;
import com.tzy.mianshiyuan.model.vo.QuestionAnswerVO;
import com.tzy.mianshiyuan.model.vo.QuestionCatalogItemVO;
import com.tzy.mianshiyuan.model.vo.QuestionVO;
import com.tzy.mianshiyuan.service.CommentService;
import com.tzy.mianshiyuan.service.QuestionService;
import com.tzy.mianshiyuan.util.GsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question")
@Tag(name = "题目接口", description = "题目的增改查及绑定功能")
@Slf4j
public class QuestionController {

    private final QuestionService questionService;
    private final CommentService commentService;

    public QuestionController(QuestionService questionService, CommentService commentService) {
        this.questionService = questionService;
        this.commentService = commentService;
    }

    @PostMapping
    @SaCheckLogin
    @Operation(summary = "创建题目（需要登录）",
               description = "支持保存草稿与提交审核两种模式，管理员提交将自动审核通过")
    public BaseResponse<QuestionVO> createQuestion(@Valid @RequestBody QuestionDTOs.QuestionCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        return ResultUtils.success(questionService.createQuestion(request, userId, isAdmin));
    }

    @PutMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "更新题目（需要登录）",
               description = "仅题目创建者或管理员可以编辑，支持保存草稿与提交审核")
    public BaseResponse<QuestionVO> updateQuestion(@PathVariable Long id,
                                                   @Valid @RequestBody QuestionDTOs.QuestionUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        return ResultUtils.success(questionService.updateQuestion(id, request, userId, isAdmin));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询题目（无需登录）")
    public BaseResponse<QuestionVO> getQuestionById(@PathVariable Long id) {
        return ResultUtils.success(questionService.getQuestionById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询题目列表（无需登录）",
               description = "支持按标题模糊搜索、按标签筛选、按难度筛选、按照题库id筛选")
    public BaseResponse<Page<QuestionVO>> listQuestions(
            @Valid @ModelAttribute PageRequest pageRequest,
            @Valid @ModelAttribute QuestionDTOs.QuestionListRequest queryRequest) {
        Long userId =null;
        String idString =(String) StpUtil.getLoginIdDefaultNull();
        if(idString !=null){
            userId = Long.parseLong(idString);
        }
        Page<QuestionVO> questionVOPage = questionService.listQuestions(pageRequest, queryRequest, userId);
        return ResultUtils.success(questionVOPage);
    }

    @GetMapping("/catalog")
    @Operation(summary = "查询题库内题目目录（无需登录）",
               description = "传入题库ID，返回该题库下所有题目的ID及标题")
    public BaseResponse<List<QuestionCatalogItemVO>> listQuestionCatalog(@RequestParam("id") Long bankId) {
        return ResultUtils.success(questionService.listQuestionCatalogByBankId(bankId));
    }

    @GetMapping("/{questionId}/comments")
    @SaCheckLogin
    @Operation(summary = "获取题目评论列表（需要登录）",
               description = "根据题目ID获取评论列表，支持嵌套回复结构")
    public BaseResponse<List<CommentVO>> getQuestionComments(@PathVariable Long questionId) {
        List<CommentVO> comments = commentService.getCommentsByQuestionId(questionId);
        return ResultUtils.success(comments);
    }


    @PostMapping("/bind")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "批量绑定题目到题库（管理员）",
               description = "将题目批量加入题库，已存在的绑定会自动忽略")
    public BaseResponse<Boolean> bindQuestionsToBank(
            @Valid @RequestBody QuestionDTOs.QuestionBatchBindRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        questionService.bindQuestionsToBank(request.getBankId(), request.getQuestionIdList(), userId);
        return ResultUtils.success(true);
    }



    @PostMapping("/unbind")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "批量解绑题目与题库（管理员）",
            description = "将题目批量从题库中解绑，不存在的绑定关系会自动忽略，返回实际解绑数量")
    public BaseResponse<Integer> unbindQuestionsFromBank(
            @Valid @RequestBody QuestionDTOs.QuestionBatchUnbindRequest request) {
        int unbindCount = questionService.unbindQuestionsFromBank(request.getBankId(), request.getQuestionIdList());
        return ResultUtils.success(unbindCount);
    }

    @GetMapping("/my")
    @SaCheckLogin
    @Operation(summary = "分页查询我创建的题目（需要登录）",
               description = "分页展示当前用户创建的所有题目，支持按公开状态筛选")
    public BaseResponse<Page<QuestionVO>> listMyQuestions(
            @Valid @ModelAttribute PageRequest pageRequest,
            @Valid @ModelAttribute QuestionDTOs.QuestionListRequest queryRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ResultUtils.success(questionService.listMyQuestions(
                pageRequest.getCurrent(),
                pageRequest.getSize(),
                userId,
                queryRequest.getIsPublic()));
    }


    @GetMapping("/answer")
    @SaCheckLogin
    @Operation(summary = "分页查询我回答的题目以及答案（需要登录）",
            description = "分页展示当前用户回答的所有题目和答案，无需任何参数")
    public BaseResponse<Page<QuestionAnswerVO>> listMyQuestionsAnswer(
            @Valid @ModelAttribute PageRequest pageRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ResultUtils.success(questionService.listMyQuestionsAnswer(
                pageRequest.getCurrent(),
                pageRequest.getSize(),
                userId));
    }
}

