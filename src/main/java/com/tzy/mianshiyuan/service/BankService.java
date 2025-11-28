package com.tzy.mianshiyuan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzy.mianshiyuan.common.BaseResponse;
import com.tzy.mianshiyuan.model.domain.Bank;
import com.tzy.mianshiyuan.model.dto.BankDTOs;
import com.tzy.mianshiyuan.model.vo.BankVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Windows11
* @description 针对表【bank(题库表)】的数据库操作Service
* @createDate 2025-11-12 00:24:39
*/
public interface BankService extends IService<Bank> {

    /**
     * 创建题库
     * @param request 创建请求
     * @param creatorId 创建人ID
     * @return 创建的题库信息
     */
    BankVO createBank(BankDTOs.BankCreateRequest request, Long creatorId);

    /**
     * 更新题库
     * @param id 题库ID
     * @param request 更新请求
     * @return 更新后的题库信息
     */
    BankVO updateBank(Long id, BankDTOs.BankUpdateRequest request);

    /**
     * 根据ID查询题库
     * @param id 题库ID
     * @return 题库信息
     */
    BankVO getBankById(Long id);

    /**
     * 分页查询题库列表
     * @param current 当前页
     * @param size 每页大小
     * @param name 题库名称（模糊搜索，可选）
     * @param tag 标签（精确匹配，可选）
     * @return 分页结果
     */
    Page<BankVO> listBanks(long current, long size, String name, String tag);

    /**
     * 查询所有标签（去重并按题库数量排序）
     * @return 标签列表，按包含该标签的题库数量降序排列
     */
    List<String> getAllTags();

    /**
     * 删除题库
     * @param id 题库ID
     */
    void removeBank(Long id);

    /**
     * 分页查询用户创建的题库
     * @param current 当前页
     * @param size 每页大小
     * @param creatorId 创建人ID
     * @return 分页结果
     */
    Page<BankVO> listMyBanks(long current, long size, Long creatorId);
}
