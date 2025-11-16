package com.tzy.mianshiyuan.mapper;

import com.tzy.mianshiyuan.model.domain.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Windows11
* @description 针对表【comment(评论表（支持嵌套回复，无需审核）)】的数据库操作Mapper
* @createDate 2025-11-16 15:26:18
* @Entity com.tzy.mianshiyuan.model.domain.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {

}




