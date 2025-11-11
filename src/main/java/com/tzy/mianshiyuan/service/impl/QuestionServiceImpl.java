package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.model.domain.Question;
import com.tzy.mianshiyuan.mapper.QuestionMapper;
import com.tzy.mianshiyuan.service.QuestionService;
import org.springframework.stereotype.Service;

/**
* @author Windows11
* @description 针对表【question(题目表)】的数据库操作Service实现
* @createDate 2025-11-12 01:12:07
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

}




