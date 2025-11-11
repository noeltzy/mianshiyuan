package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.model.domain.Review;
import com.tzy.mianshiyuan.service.ReviewService;
import com.tzy.mianshiyuan.mapper.ReviewMapper;
import org.springframework.stereotype.Service;

/**
* @author Windows11
* @description 针对表【review(审核表（逻辑外键版）)】的数据库操作Service实现
* @createDate 2025-11-12 00:24:49
*/
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review>
    implements ReviewService{

}




