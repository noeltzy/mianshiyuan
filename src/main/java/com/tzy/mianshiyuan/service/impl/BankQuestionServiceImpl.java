package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.mapper.CommentMapper;
import com.tzy.mianshiyuan.model.domain.BankQuestion;
import com.tzy.mianshiyuan.model.domain.Comment;
import com.tzy.mianshiyuan.service.BankQuestionService;
import com.tzy.mianshiyuan.mapper.BankQuestionMapper;
import com.tzy.mianshiyuan.service.CommentService;
import org.springframework.stereotype.Service;

/**
* @author Windows11
* @description 针对表【bank_question(题库-题目关联表（多对多逻辑关系）)】的数据库操作Service实现
* @createDate 2025-11-12 00:24:46
*/
@Service
public class BankQuestionServiceImpl extends ServiceImpl<BankQuestionMapper, BankQuestion>
    implements BankQuestionService{



}




