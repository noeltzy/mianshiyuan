package com.tzy.mianshiyuan.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.common.ResultUtils;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.model.dto.UpdateUserRequest;
import com.tzy.mianshiyuan.model.vo.UserSettingVO;
import com.tzy.mianshiyuan.service.UserService;
import com.tzy.mianshiyuan.service.UserSettingsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户接口", description = "用户更新信息")
public class UserController {
    private final UserService userService;

    private final UserSettingsService userSettingsService;

    public UserController(UserService userService, UserSettingsService userSettingsService){
        this.userService = userService;
        this.userSettingsService = userSettingsService;
    }

    @SaCheckLogin
    @PostMapping("/update")
    BaseResponse<Boolean> update(@RequestBody UpdateUserRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateUser(request,userId);
        return ResultUtils.success();
    }
    @SaCheckLogin
    @GetMapping("/settings")
    BaseResponse<List<UserSettingVO>> getSettings(){
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserSettingVO> mySettings = userSettingsService.getMySettings(userId);
        return ResultUtils.success(mySettings);
    }

    @SaCheckLogin
    @PostMapping("/settings")
    BaseResponse<Boolean>  updateSettings(@RequestBody List<UserSettingVO> request){
        if(request == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Long userId = StpUtil.getLoginIdAsLong();
        userSettingsService.updateSettings(request,userId);
        return ResultUtils.success();
    }
}
