package com.tzy.mianshiyuan.service;

import com.tzy.mianshiyuan.model.domain.UserSettings;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tzy.mianshiyuan.model.vo.UserSettingVO;

import java.util.List;

/**
* @author Windows11
* @description 针对表【user_settings(用户设置表（支持动态扩展设置项）)】的数据库操作Service
* @createDate 2025-11-16 21:47:13
*/
public interface UserSettingsService extends IService<UserSettings> {

    List<UserSettingVO> getMySettings(Long userId);

    void updateSettings(List<UserSettingVO> request, Long userId);
}
