package com.tzy.mianshiyuan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tzy.mianshiyuan.model.domain.AnswerRating;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;

/**
* @author Windows11
* @description 针对表【answer_rating(回答评分表)】的数据库操作Mapper
* @createDate 2025-11-16 19:37:08
* @Entity com.tzy.mianshiyuan.model.domain.AnswerRating
*/
public interface AnswerRatingMapper extends BaseMapper<AnswerRating> {
    /**
     * 查询某题某用户的最高评分
     *
     * @param questionId 题目ID
     * @param userId     回答者ID
     * @return 最高得分，若无则返回 null
     */
    BigDecimal selectMaxScoreByQuestionAndUser(@Param("questionId") Long questionId,
                                                @Param("userId") Long userId);
}




