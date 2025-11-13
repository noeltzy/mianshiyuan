package com.tzy.mianshiyuan.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest {
    @Min(value = 1, message = "当前页必须大于等于1")
    private long current = 1;
    
    @Min(value = 1, message = "分页大小必须大于等于1")
    @Max(value = 100, message = "分页大小不能超过100")
    private long size = 10;
}

