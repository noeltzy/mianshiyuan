package com.tzy.mianshiyuan.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ResultUtils;
import com.tzy.mianshiyuan.model.vo.StsCredentialsVO;
import com.tzy.mianshiyuan.service.StsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文件上传接口
 * 提供STS临时凭证，供前端直传OSS使用
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@Tag(name = "文件上传接口", description = "获取STS临时凭证，前端直传OSS")
public class FileController {

    @Autowired
    private StsService stsService;

    @GetMapping("/sts-credentials")
//    @SaCheckLogin
    @Operation(summary = "获取STS临时凭证（需要登录）", 
               description = "获取STS临时凭证，用于前端直传OSS。返回临时AccessKey、Secret和SecurityToken")
    public BaseResponse<StsCredentialsVO> getStsCredentials(
            @RequestParam(required = false, defaultValue = "") String folder) {
        StsCredentialsVO credentials = stsService.getStsCredentials(folder.isEmpty() ? null : folder);
        return ResultUtils.success(credentials);
    }

    @GetMapping("/sts-credentials/image")
//    @SaCheckLogin
    @Operation(summary = "获取图片上传STS临时凭证（需要登录）", 
               description = "获取STS临时凭证，用于前端直传图片到OSS。自动保存到images文件夹")
    public BaseResponse<StsCredentialsVO> getImageUploadCredentials() {
        StsCredentialsVO credentials = stsService.getStsCredentials("images/");
        return ResultUtils.success(credentials);
    }
}

