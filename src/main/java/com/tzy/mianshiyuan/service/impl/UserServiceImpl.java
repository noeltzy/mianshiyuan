package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.model.domain.User;
import com.tzy.mianshiyuan.service.UserService;
import com.tzy.mianshiyuan.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

/**
* @author Zhuanz
* @description 针对表【user(用户表（支持逻辑删除）)】的数据库操作Service实现
* @createDate 2025-11-11 20:00:53
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public User getByUsername(String username) {
        if (username == null) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
    }
}




