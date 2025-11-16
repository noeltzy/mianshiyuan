package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.model.domain.UserSettings;
import com.tzy.mianshiyuan.service.UserSettingsService;
import com.tzy.mianshiyuan.mapper.UserSettingsMapper;
import org.springframework.stereotype.Service;

/**
* @author Windows11
* @description 针对表【user_settings(用户设置表（支持动态扩展设置项）)】的数据库操作Service实现
* @createDate 2025-11-16 21:47:13
*/
@Service
public class UserSettingsServiceImpl extends ServiceImpl<UserSettingsMapper, UserSettings>
    implements UserSettingsService{

}




