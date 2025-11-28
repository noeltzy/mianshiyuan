package com.tzy.mianshiyuan.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.common.ErrUtils;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.common.JsonUtils;
import com.tzy.mianshiyuan.constant.BankConstants;
import com.tzy.mianshiyuan.constant.QuestionConstants;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.model.domain.Bank;
import com.tzy.mianshiyuan.model.domain.Review;
import com.tzy.mianshiyuan.model.dto.BankDTOs;
import com.tzy.mianshiyuan.model.dto.QuestionDTOs;
import com.tzy.mianshiyuan.model.dto.QuestionGenerationRequest;
import com.tzy.mianshiyuan.model.vo.BankVO;
import com.tzy.mianshiyuan.model.vo.QuestionVO;
import com.tzy.mianshiyuan.model.domain.BankQuestion;
import com.tzy.mianshiyuan.service.AgentService;
import com.tzy.mianshiyuan.service.BankQuestionService;
import com.tzy.mianshiyuan.service.BankService;
import com.tzy.mianshiyuan.service.QuestionService;
import com.tzy.mianshiyuan.service.ReviewService;
import com.tzy.mianshiyuan.mapper.BankMapper;
import com.tzy.mianshiyuan.util.GsonUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author Windows11
* @description 针对表【bank(题库表)】的数据库操作Service实现
* @createDate 2025-11-12 00:24:39
*/
@Slf4j
@Service
public class BankServiceImpl extends ServiceImpl<BankMapper, Bank>
    implements BankService{

    private static final String REDIS_KEY_BANK_TAGS = "bank:tags:all";
    private static final long CACHE_EXPIRE_HOURS = 1; // 缓存过期时间：1小时

    // 审核相关常量
    private static final int CONTENT_TYPE_BANK = 1;
    private static final int REVIEW_RESULT_PENDING = 0;
    private static final int REVIEW_RESULT_PASS = 1;
    private static final int REVIEWER_TYPE_MANUAL = 1;
    private static final String ADMIN_REVIEW_COMMENT = "管理员上传自动审核通过";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AgentService agentService;
    private final QuestionService questionService;
    private final ReviewService reviewService;
    private final BankQuestionService bankQuestionService;

    public BankServiceImpl(RedisTemplate<String, Object> redisTemplate,
                          AgentService agentService,
                          QuestionService questionService,
                          ReviewService reviewService,
                          BankQuestionService bankQuestionService) {
        this.redisTemplate = redisTemplate;
        this.agentService = agentService;
        this.questionService = questionService;
        this.reviewService = reviewService;
        this.bankQuestionService = bankQuestionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BankVO createBank(BankDTOs.BankCreateRequest request, Long creatorId) {

        // 填写
        Bank bank = new Bank();
        bank.setName(request.getName());
        bank.setDescription(request.getDescription());
        bank.setCoverImage(request.getCoverImage());
        // 将List<String>转换为JSON字符串存储，默认值为"[]"
        bank.setTagList(JsonUtils.listToString(request.getTagList()));
        bank.setCreatorId(creatorId);
        // 设置公开状态，默认为公开
        bank.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : BankConstants.PUBLIC);

        // 权限  1. public只能Admin 2. private 需要 判断数量
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        ErrUtils.errIf(BankConstants.PUBLIC.equals(request.getIsPublic()) && (!isAdmin), ErrorCode.NO_AUTH);

        // 私有题库强制跳过审核，直接通过
        if (BankConstants.PRIVATE.equals(bank.getIsPublic())) {
            bank.setStatus(BankConstants.PASS);
        } else {
            boolean submitForReview = Boolean.TRUE.equals(request.getSubmitForReview());
            bank.setStatus(determineStatus(submitForReview, isAdmin));
        }

        // 查询当前用户私有题库已经有多少个了：
        if(BankConstants.PRIVATE.equals(request.getIsPublic())){
            LambdaQueryWrapper<Bank> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Bank::getIsPublic,BankConstants.PRIVATE);
            lqw.eq(Bank::getCreatorId,creatorId);
            long count = this.count(lqw);
            // 能创建五个题库
            if(count>5){
                ErrUtils.err(ErrorCode.NO_AUTH);
            }
            // 如果需要生成题目 需要检验参数
            if(Boolean.TRUE.equals(request.getGenerateByAI())){
                Integer c = request.getQuestionGenerateParam().getCount();
                ErrUtils.errIf(c>=10||c<0,ErrorCode.PARAMS_ERROR);
            }
        }

        boolean saved = this.save(bank);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建题库失败");
        }
        Long newBankId = bank.getId();

        // 公开题库且提交审核时创建审核记录
        if (BankConstants.PUBLIC.equals(bank.getIsPublic()) && Boolean.TRUE.equals(request.getSubmitForReview())) {
            Review review = buildReviewRecord(bank.getId(), creatorId, isAdmin);
            boolean reviewSaved = reviewService.save(review);
            if (!reviewSaved) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建题库审核记录失败");
            }
            bank.setReviewId(review.getId());
            // 管理员自动审核通过时同步更新题库状态
            if (isAdmin) {
                bank.setStatus(BankConstants.PASS);
            }
            this.updateById(bank);
        }

        // 需要AI 生成 题目
        if(BankConstants.PRIVATE.equals(request.getIsPublic()) && Boolean.TRUE.equals(request.getGenerateByAI())){

            QuestionGenerationRequest generationRequest = new QuestionGenerationRequest();
            generationRequest.setCount(request.getQuestionGenerateParam().getCount());
            generationRequest.setDesc(request.getDescription());
            generationRequest.setDifficulty(request.getQuestionGenerateParam().getDifferent());
            generationRequest.setTopic(GsonUtils.toJson(request.getTagList()));

            // TODO 发送消息队列消息,暂时先直接异步
            CompletableFuture.runAsync(()->runGenerateAndBind(newBankId,creatorId,generationRequest));
        }
        
        // 清除标签缓存（因为新增题库可能包含新标签）
        if(BankConstants.PUBLIC.equals(request.getIsPublic())){
            clearTagsCache();
        }
        return toVO(bank);
    }

    /**
     * AI 生成题目并绑定到题库（异步执行）
     * 失败不影响题库创建，仅记录日志
     */
    public void runGenerateAndBind(Long newBankId, Long creatorId, QuestionGenerationRequest request) {
        try {
            log.info("开始 AI 生成题目，题库ID: {}, 创建者: {}, 数量: {}", 
                     newBankId, creatorId, request.getCount());
            
            // 1. 调用 AI 生成题目
            List<QuestionDTOs.QuestionCreateRequest> questionCreateRequests = 
                agentService.generateQuestions(request);
            
            if (questionCreateRequests == null || questionCreateRequests.isEmpty()) {
                log.warn("AI 生成题目为空，题库ID: {}", newBankId);
                return;
            }
            
            log.info("AI 生成题目成功，数量: {}", questionCreateRequests.size());
            
            // 2. 批量创建题目（私有 + 跳过审核）
            List<Long> createdQuestionIds = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (QuestionDTOs.QuestionCreateRequest questionRequest : questionCreateRequests) {
                try {
                    // 强制设置为私有题目，跳过审核
                    questionRequest.setIsPublic(QuestionConstants.PRIVATE);
                    questionRequest.setSubmitForReview(false);
                    
                    // 调用现有的 createQuestion 方法创建题目
                    QuestionVO createdQuestion = questionService.createQuestion(
                        questionRequest, 
                        creatorId, 
                        false  // 非管理员
                    );
                    
                    createdQuestionIds.add(createdQuestion.getId());
                    successCount++;
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("创建单个题目失败，题目标题: {}, 错误: {}", 
                             questionRequest.getTitle(), e.getMessage());
                    // 继续创建下一个题目，不中断流程
                }
            }
            
            log.info("题目创建完成，成功: {}, 失败: {}", successCount, failCount);
            
            // 3. 批量绑定到题库
            if (!createdQuestionIds.isEmpty()) {
                try {
                    questionService.bindQuestionsToBank(newBankId, createdQuestionIds, creatorId);
                    log.info("题目绑定成功，题库ID: {}, 绑定数量: {}", 
                             newBankId, createdQuestionIds.size());
                } catch (Exception e) {
                    log.error("绑定题目到题库失败，题库ID: {}, 题目ID: {}, 错误: {}", 
                             newBankId, createdQuestionIds, e.getMessage(), e);
                    // 绑定失败时保留孤儿题目（问题3-A），仅记录日志
                }
            } else {
                log.warn("没有成功创建任何题目，跳过绑定，题库ID: {}", newBankId);
            }
            
        } catch (Exception e) {
            // AI 生成失败或其他未预期异常，静默失败（问题1-A）
            log.error("AI 生成题目流程失败，题库ID: {}, 错误: {}", newBankId, e.getMessage(), e);
            // 不抛出异常，不影响题库创建
        }
    }





    @Override
    @Transactional(rollbackFor = Exception.class)
    public BankVO updateBank(Long id, BankDTOs.BankUpdateRequest request) {
        Bank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
        }
        
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        Long editorId = StpUtil.getLoginId(-1L);
        
        // 权限校验：非管理员只能编辑自己的题库
        if (!isAdmin && !Objects.equals(bank.getCreatorId(), editorId)) {
            throw new BusinessException(ErrorCode.NO_AUTH.getCode(), "无权编辑该题库");
        }
        
        bank.setName(request.getName());
        bank.setDescription(request.getDescription());
        bank.setCoverImage(request.getCoverImage());
        // 将List<String>转换为JSON字符串存储，默认值为"[]"
        bank.setTagList(JsonUtils.listToString(request.getTagList()));
        
        // 私有题库强制跳过审核，直接通过
        if (BankConstants.PRIVATE.equals(bank.getIsPublic())) {
            bank.setStatus(BankConstants.PASS);
            bank.setReviewId(null);
        } else {
            boolean submitForReview = Boolean.TRUE.equals(request.getSubmitForReview());
            bank.setStatus(determineStatus(submitForReview, isAdmin));
            if (!submitForReview) {
                bank.setReviewId(null);
            }
        }
        
        bank.setUpdatedAt(new Date());
        
        boolean updated = this.updateById(bank);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "更新题库失败");
        }
        
        // 公开题库且提交审核时创建审核记录
        if (BankConstants.PUBLIC.equals(bank.getIsPublic()) && Boolean.TRUE.equals(request.getSubmitForReview())) {
            Review review = buildReviewRecord(bank.getId(), editorId, isAdmin);
            boolean reviewSaved = reviewService.save(review);
            if (!reviewSaved) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建题库审核记录失败");
            }
            bank.setReviewId(review.getId());
            if (isAdmin) {
                bank.setStatus(BankConstants.PASS);
            }
            this.updateById(bank);
        }
        
        // 清除标签缓存（因为更新题库可能修改了标签）
        clearTagsCache();
        return toVO(bank);
    }

    @Override
    public BankVO getBankById(Long id) {
        Bank bank = this.getById(id);

        if (bank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
        }

        Long creatorId = bank.getCreatorId();
        Integer isPublic = bank.getIsPublic();
        Long loginId = StpUtil.getLoginId(-1L);
        boolean admin = StpUtil.hasRole("ADMIN");
        // 未审核通过只能自己看 or 管理员看
        Integer status = bank.getStatus();
        if(!BankConstants.PASS.equals(status)){
            ErrUtils.errIf(!admin || !Objects.equals(loginId, creatorId), ErrorCode.NO_AUTH);
        }
        if(!BankConstants.PUBLIC.equals(isPublic)){
            ErrUtils.errIf(!creatorId.equals(loginId),ErrorCode.NOT_FOUND);
        }
        return toVO(bank);
    }


    @Override
    public Page<BankVO> listBanks(long current, long size, String name, String tag) {
        Page<Bank> page = new Page<>(current, size);
        LambdaQueryWrapper<Bank> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按名称模糊搜索
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(Bank::getName, name.trim());
        }
        
        // 按标签筛选（JSON字符串中包含指定标签）
        if (tag != null && !tag.trim().isEmpty()) {
            // 使用LIKE查询，匹配JSON数组中的标签
            // JSON格式：["tag1","tag2"]，需要匹配 "tag"
            queryWrapper.like(Bank::getTagList, "\"" + tag.trim() + "\"");
        }
        queryWrapper.eq(Bank::getIsPublic,BankConstants.PUBLIC);
        // 普通用户 只能查询通过审核的
        if( !StpUtil.hasRole("ADMIN")){
            queryWrapper.eq(Bank::getStatus,BankConstants.PASS);
        }
        queryWrapper.orderByDesc(Bank::getCreatedAt);
        Page<Bank> bankPage = this.page(page, queryWrapper);
        
        // 转换为VO
        Page<BankVO> voPage = new Page<>(current, size, bankPage.getTotal());
        List<BankVO> voList = bankPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public List<String> getAllTags() {
        // 先从Redis缓存中获取
        try {
            Object cachedTags = redisTemplate.opsForValue().get(REDIS_KEY_BANK_TAGS);
            if (cachedTags != null) {
                // 缓存命中，直接返回
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) cachedTags;
                log.debug("从Redis缓存获取标签列表，数量: {}", tags.size());
                return tags;
            }
        } catch (Exception e) {
            log.warn("从Redis读取缓存失败，降级到数据库查询: {}", e.getMessage());
        }
        
        // 缓存未命中或读取失败，查询数据库
        List<String> tags = loadTagsFromDatabase();
        
        // 尝试将结果存入缓存，设置过期时间为1小时
        // 如果Redis是只读的或写入失败，不影响业务逻辑，只记录警告日志
        if (!tags.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(REDIS_KEY_BANK_TAGS, tags, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                log.debug("标签列表已存入Redis缓存，数量: {}", tags.size());
            } catch (Exception e) {
                log.warn("写入Redis缓存失败（可能是只读模式），但不影响业务: {}", e.getMessage());
            }
        }
        
        return tags;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBank(Long id) {
        // 1. 参数校验
        ErrUtils.errIf(id == null, ErrorCode.PARAMS_ERROR);

        // 2. 获取题库信息
        Bank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
        }

        // 3. 权限校验：admin可删除任何题库，其他用户只能删除自己的
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        Long loginId = StpUtil.getLoginIdAsLong();
        if (!isAdmin && !Objects.equals(bank.getCreatorId(), loginId)) {
            throw new BusinessException(ErrorCode.NO_AUTH.getCode(), "无权删除该题库");
        }

        // 4. 如果题库处于待审状态（status=1），物理删除最新的审核记录
        if (BankConstants.SUBMIT.equals(bank.getStatus()) && bank.getReviewId() != null) {
            reviewService.getBaseMapper().deleteById(bank.getReviewId());
            log.info("已物理删除题库审核记录，题库ID: {}, 审核ID: {}", id, bank.getReviewId());
        }

        // 5. 物理删除题库-题目关系（解绑题目）
        LambdaQueryWrapper<BankQuestion> bqWrapper = new LambdaQueryWrapper<>();
        bqWrapper.eq(BankQuestion::getBankId, id);
        int deletedRelations = bankQuestionService.getBaseMapper().delete(bqWrapper);
        log.info("已解绑题目，题库ID: {}, 解绑数量: {}", id, deletedRelations);

        // 6. 软删除题库
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "删除题库失败");
        }

        // 7. 清除标签缓存（如果是公开题库）
        if (BankConstants.PUBLIC.equals(bank.getIsPublic())) {
            clearTagsCache();
        }

        log.info("题库删除成功，题库ID: {}, 操作人: {}", id, loginId);
    }

    @Override
    public Page<BankVO> listMyBanks(long current, long size, Long creatorId) {
        Page<Bank> page = new Page<>(current, size);
        LambdaQueryWrapper<Bank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Bank::getCreatorId, creatorId);
        queryWrapper.orderByDesc(Bank::getCreatedAt);
        
        Page<Bank> bankPage = this.page(page, queryWrapper);
        
        Page<BankVO> voPage = new Page<>(current, size, bankPage.getTotal());
        List<BankVO> voList = bankPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 从数据库加载标签列表
     */
    private List<String> loadTagsFromDatabase() {
        // 查询所有公开题库
        LambdaQueryWrapper<Bank> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Bank::getIsPublic,BankConstants.PUBLIC);
        List<Bank> allBanks = this.list(lqw);
        // 统计每个标签出现的次数
        Map<String, Integer> tagCountMap = new HashMap<>();
        for (Bank bank : allBanks) {
            // 将JSON字符串转换为标签列表
            List<String> tags = JsonUtils.stringToList(bank.getTagList());
            // 统计每个标签出现的次数
            for (String tag : tags) {
                if (tag != null && !tag.trim().isEmpty()) {
                    tagCountMap.put(tag, tagCountMap.getOrDefault(tag, 0) + 1);
                }
            }
        }
        
        // 按出现次数降序排序，然后返回标签列表
        return tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 清除标签缓存
     * 使用异步删除，避免阻塞
     */
    private void clearTagsCache() {
        try {
        redisTemplate.delete(REDIS_KEY_BANK_TAGS);
        } catch (Exception e) {
            log.warn("清除Redis标签缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 根据提交审核状态和是否管理员确定状态
     */
    private Integer determineStatus(boolean submitForReview, boolean isAdmin) {
        if (!submitForReview) {
            return BankConstants.DRAFT;
        }
        return isAdmin ? BankConstants.PASS : BankConstants.SUBMIT;
    }

    /**
     * 构建审核记录
     */
    private Review buildReviewRecord(Long bankId, Long operatorId, boolean isAdmin) {
        Review review = new Review();
        review.setContentId(bankId);
        review.setContentType(CONTENT_TYPE_BANK);
        review.setReviewerType(REVIEWER_TYPE_MANUAL);
        if (isAdmin) {
            review.setReviewerId(operatorId);
            review.setResult(REVIEW_RESULT_PASS);
            review.setComments(ADMIN_REVIEW_COMMENT);
        } else {
            review.setReviewerId(null);
            review.setResult(REVIEW_RESULT_PENDING);
            review.setComments("用户提交待审核");
        }
        return review;
    }

    private BankVO toVO(Bank bank) {
        BankVO vo = new BankVO();
        BeanUtils.copyProperties(bank, vo);
        // 将JSON字符串转换为List<String>返回给前端
        vo.setTagList(JsonUtils.stringToList(bank.getTagList()));
        return vo;
    }
}




