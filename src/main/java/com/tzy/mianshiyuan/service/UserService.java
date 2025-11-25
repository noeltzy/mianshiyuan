package com.tzy.mianshiyuan.service;

import com.tzy.mianshiyuan.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tzy.mianshiyuan.model.dto.UpdateUserRequest;

/**
* @author Zhuanz
* @description 针对表【user(用户表（支持逻辑删除）)】的数据库操作Service
* @createDate 2025-11-11 20:00:53
*/
public interface UserService extends IService<User> {

    User getByUsername(String username);

    void updateUser(UpdateUserRequest request, Long userId);
}
