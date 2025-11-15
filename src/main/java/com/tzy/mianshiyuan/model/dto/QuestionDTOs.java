package com.tzy.mianshiyuan.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTOs {

    @Data
    public static class QuestionCreateRequest {
        @NotBlank(message = "题目标题不能为空")
        private String title;
        private String description;
        private List<String> tagList;
        private String answer;
        @Min(value = 0, message = "难度最小为0")
        @Max(value = 2, message = "难度最大为2")
        private Integer difficulty = 1;
        /**
         * 是否提交审核：true=提交审核（状态=1待审），false=保存草稿（状态=0草稿）
         * 默认为false（保存草稿）
         */
        private Boolean submitForReview = false;
    }

    @Data
    public static class QuestionUpdateRequest {
        @NotBlank(message = "题目标题不能为空")
        private String title;
        private String description;
        private List<String> tagList;
        private String answer;
        @Min(value = 0, message = "难度最小为0")
        @Max(value = 2, message = "难度最大为2")
        private Integer difficulty = 1;
        /**
         * 是否提交审核：true=提交审核（状态=1待审），false=保存草稿（状态=0草稿）
         * 默认为false（保存草稿）
         */
        private Boolean submitForReview = false;
    }

    @Data
    public static class QuestionBatchBindRequest {
        @NotNull(message = "题库ID不能为空")
        private Long bankId;
        @NotEmpty(message = "题目ID列表不能为空")
        private List<Long> questionIdList;
    }

    @Data
    public static class QuestionListRequest {
        private String title;
        private String tag;
        @Min(value = 0, message = "难度最小为0")
        @Max(value = 2, message = "难度最大为2")
        private Integer difficulty;
        private Long bankId;
    }

    @Data
    public static class BatchImportQuestionRequest{
        /**
         * 生成的题目数量
         */
        @Min(value = 1, message = "最少需要导入一题")
        @Max(value = 20, message = "最多导入20题")
        private Integer count;

        @Min(value = 0, message = "难度最小为0")
        @Max(value = 2, message = "难度最大为2")
        private Integer difficulty;

        @NotBlank(message = "请输入正确的题目主题")
        private  String topic;
        /**
         * 是否需要创建题库
         */
        private Boolean createBank = false;
        private List<String> BanktagList;
        private String bankName;
        private String description;
        /**
         * 绑定题库
         */
        private Long bankId;
    }
}

