package com.tzy.mianshiyuan.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ResultUtils;
import com.tzy.mianshiyuan.model.dto.BankDTOs;
import com.tzy.mianshiyuan.model.dto.PageRequest;
import com.tzy.mianshiyuan.model.vo.BankVO;
import com.tzy.mianshiyuan.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank")
@Tag(name = "题库接口", description = "题库的增、改、查功能")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    @SaCheckLogin
    @Operation(summary = "创建题库（需要登录）",
               description = "支持两种模式：1.保存草稿（submitForReview=false，状态=0） 2.提交审核（submitForReview=true，状态=1）")
    public BaseResponse<BankVO> createBank(@Valid @RequestBody BankDTOs.BankCreateRequest request) {
        Long creatorId = StpUtil.getLoginIdAsLong();
        return ResultUtils.success(bankService.createBank(request, creatorId));
    }

    @PutMapping("/{id}")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "更新题库（需要登录）",
               description = "任何状态的题库都可以更新。支持两种模式：1.保存草稿（submitForReview=false，状态=0） 2.提交审核（submitForReview=true，状态=1）")
    public BaseResponse<BankVO> updateBank(
            @PathVariable Long id,
            @Valid @RequestBody BankDTOs.BankUpdateRequest request) {
        return ResultUtils.success(bankService.updateBank(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询题库（无需登录）")
    public BaseResponse<BankVO> getBankById(@PathVariable Long id) {
        return ResultUtils.success(bankService.getBankById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询题库列表（无需登录）", description = "支持按名称模糊搜索和按标签筛选")
    public BaseResponse<Page<BankVO>> listBanks(
            @Valid @ModelAttribute PageRequest pageRequest,
            @Valid @ModelAttribute BankDTOs.BankListRequest queryRequest) {
        return ResultUtils.success(bankService.listBanks(
                pageRequest.getCurrent(),
                pageRequest.getSize(),
                queryRequest.getName(),
                queryRequest.getTag()));
    }

    @GetMapping("/tags")
    @Operation(summary = "查询所有标签（无需登录）", description = "返回所有题库的标签列表，去重后按包含该标签的题库数量降序排列")
    public BaseResponse<List<String>> getAllTags() {
        return ResultUtils.success(bankService.getAllTags());
    }

    @DeleteMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "删除题库（需要登录）",
            description = "管理员可删除任何题库，普通用户只能删除自己创建的题库。" +
                    "删除时会解绑所有关联的题目（题目本身保留），如果题库处于待审状态会同时删除审核记录。")
    public BaseResponse<Void> deleteBank(@PathVariable Long id) {
        bankService.removeBank(id);
        return ResultUtils.success();
    }


    @GetMapping("/my")
    @SaCheckLogin
    @Operation(summary = "分页查询我创建的题库（需要登录）",
            description = "分页展示当前用户创建的所有题库")
    public BaseResponse<Page<BankVO>> listMyBanks(
            @Valid @ModelAttribute PageRequest pageRequest) {
        Long loginId = StpUtil.getLoginIdAsLong();
        return ResultUtils.success(bankService.listMyBanks(pageRequest.getCurrent(), pageRequest.getSize(), loginId));
    }
}

