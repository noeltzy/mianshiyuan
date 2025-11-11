package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.common.JsonUtils;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.model.domain.Bank;
import com.tzy.mianshiyuan.model.dto.BankDTOs;
import com.tzy.mianshiyuan.model.vo.BankVO;
import com.tzy.mianshiyuan.service.BankService;
import com.tzy.mianshiyuan.mapper.BankMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

    private final RedisTemplate<String, Object> redisTemplate;

    public BankServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public BankVO createBank(BankDTOs.BankCreateRequest request, Long creatorId) {
        Bank bank = new Bank();
        bank.setName(request.getName());
        bank.setDescription(request.getDescription());
        // 将List<String>转换为JSON字符串存储，默认值为"[]"
        bank.setTagList(JsonUtils.listToString(request.getTagList()));
        bank.setCreatorId(creatorId);
        // 根据submitForReview设置状态：true=1待审，false=0草稿
        bank.setStatus(Boolean.TRUE.equals(request.getSubmitForReview()) ? 1 : 0);
        bank.setCreatedAt(new Date());
        bank.setUpdatedAt(new Date());
        
        boolean saved = this.save(bank);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "创建题库失败");
        }
        
        // 清除标签缓存（因为新增题库可能包含新标签）
        try {
            clearTagsCache();
        } catch (Exception e) {
            log.warn("清除Redis缓存失败，但不影响业务: {}", e.getMessage());
        }
        
        return toVO(bank);
    }

    @Override
    public BankVO updateBank(Long id, BankDTOs.BankUpdateRequest request) {
        Bank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
        }
        
        bank.setName(request.getName());
        bank.setDescription(request.getDescription());
        // 将List<String>转换为JSON字符串存储，默认值为"[]"
        bank.setTagList(JsonUtils.listToString(request.getTagList()));
        // 根据submitForReview设置状态：true=1待审，false=0草稿
        // 任何状态的题库都可以更新，更新后状态会根据此字段设置
        bank.setStatus(Boolean.TRUE.equals(request.getSubmitForReview()) ? 1 : 0);
        bank.setUpdatedAt(new Date());
        
        boolean updated = this.updateById(bank);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR.getCode(), "更新题库失败");
        }
        
        // 清除标签缓存（因为更新题库可能修改了标签）
        try {
            clearTagsCache();
        } catch (Exception e) {
            log.warn("清除Redis缓存失败，但不影响业务: {}", e.getMessage());
        }
        
        return toVO(bank);
    }

    @Override
    public BankVO getBankById(Long id) {
        Bank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "题库不存在");
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

    /**
     * 从数据库加载标签列表
     */
    private List<String> loadTagsFromDatabase() {
        // 查询所有题库
        List<Bank> allBanks = this.list();
        
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
     */
    private void clearTagsCache() {
        redisTemplate.delete(REDIS_KEY_BANK_TAGS);
    }

    private BankVO toVO(Bank bank) {
        BankVO vo = new BankVO();
        BeanUtils.copyProperties(bank, vo);
        // 将JSON字符串转换为List<String>返回给前端
        vo.setTagList(JsonUtils.stringToList(bank.getTagList()));
        return vo;
    }
}




