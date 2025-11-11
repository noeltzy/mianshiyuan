package com.tzy.mianshiyuan.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class BankDTOs {
    @Data
    public static class BankCreateRequest {
        @NotBlank(message = "题库名称不能为空")
        private String name;
        private String description;
        private List<String> tagList;
        /**
         * 是否提交审核：true=提交审核（状态=1待审），false=保存草稿（状态=0草稿）
         * 默认为false（保存草稿）
         */
        private Boolean submitForReview = false;
    }

    @Data
    public static class BankUpdateRequest {
        @NotBlank(message = "题库名称不能为空")
        private String name;
        private String description;
        private List<String> tagList;
        /**
         * 是否提交审核：true=提交审核（状态=1待审），false=保存草稿（状态=0草稿）
         * 默认为false（保存草稿）
         * 任何状态的题库都可以更新，更新后状态会根据此字段设置
         */
        private Boolean submitForReview = false;
    }
}

