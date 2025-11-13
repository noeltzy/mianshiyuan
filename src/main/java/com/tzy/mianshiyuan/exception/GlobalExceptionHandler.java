package com.tzy.mianshiyuan.exception;

import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.common.ResultUtils;
import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;



@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录访问: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.NO_AUTH);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public BaseResponse<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        // 静默处理静态资源未找到异常（如 favicon.ico），避免产生错误日志
        log.debug("静态资源未找到: {}", e.getResourcePath());
        return ResultUtils.error(ErrorCode.NOT_FOUND.getCode(), "资源不存在");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse(ErrorCode.PARAMS_ERROR.getMessage());
        log.warn("参数校验异常: {}", message);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}


