package com.tzy.mianshiyuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mianshiyuan.common.ErrUtils;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.model.domain.UserSettings;
import com.tzy.mianshiyuan.model.enums.UserSettingEnum;
import com.tzy.mianshiyuan.model.vo.UserSettingVO;
import com.tzy.mianshiyuan.service.UserSettingsService;
import com.tzy.mianshiyuan.mapper.UserSettingsMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author Windows11
* @description 针对表【user_settings(用户设置表（支持动态扩展设置项）)】的数据库操作Service实现
* @createDate 2025-11-16 21:47:13
*/
@Service
public class UserSettingsServiceImpl extends ServiceImpl<UserSettingsMapper, UserSettings>
    implements UserSettingsService{

    @Resource
    UserSettingsMapper userSettingsMapper;




    @Override
    public List<UserSettingVO> getMySettings(Long userId) {
        LambdaQueryWrapper<UserSettings> uqw= new LambdaQueryWrapper<>();
        uqw.eq(UserSettings::getUserId,userId);
        return this.list(uqw).stream().map(this::toUserSettingVO).toList();
    }

    @Override
    @Transactional
    public void updateSettings(List<UserSettingVO> request, Long userId) {
        request.forEach(this::checkUserSettings);
        List<UserSettings> list = request.stream().map(item -> this.fromVO(item, userId)).toList();
        Map<String, Long> settingKeyIdMap = getSettingKeyIdMap(request, userId);
        // 填入ID
        for(UserSettings settingItem:list){
            settingItem.setId(settingKeyIdMap.get(settingItem.getSettingKey()));
        }
        boolean b = saveOrUpdateBatch(list);
        ErrUtils.errIf(!b,ErrorCode.OPERATION_ERROR);
    }


    private Map<String,Long> getSettingKeyIdMap(List<UserSettingVO> request,Long userId){
        Map<String,Long> keyIdMap = new HashMap<>();
        request.forEach((item)->{
            LambdaQueryWrapper<UserSettings> lqw = new LambdaQueryWrapper<>();
            lqw.eq(UserSettings::getUserId,userId);
            lqw.eq(UserSettings::getSettingKey,item.getSettingKey());
            UserSettings userSettings = userSettingsMapper.selectOne(lqw);
            if(userSettings==null){
                keyIdMap.put(item.getSettingKey(),null);
            }
            else{
                keyIdMap.put(item.getSettingKey(),userSettings.getId());
            }
        });
        return keyIdMap;
    }

    private void checkUserSettings(UserSettingVO vo){
        UserSettingEnum keyEnum =UserSettingEnum.getBySettingKey(vo.getSettingKey());
        ErrUtils.errIf(Objects.isNull(keyEnum),ErrorCode.PARAMS_ERROR,"不支持的配置项");
        // AI 严格等级
        if(UserSettingEnum.AI_REPLAY_STRICTNESS.equals(keyEnum)){
            ErrUtils.paramErrIf(!StringUtils.isNumeric(vo.getSettingValue()),"配置格式错误");

            int value = Integer.parseInt(vo.getSettingValue());
            ErrUtils.paramErrIf(value<=0||value>3,"AI严格度配置错误");
        }
    }


    private UserSettingVO toUserSettingVO(UserSettings userSettings){
        UserSettingVO vo = new UserSettingVO();
        vo.setSettingKey(userSettings.getSettingKey());
        vo.setSettingValue(userSettings.getSettingValue());
        return  vo;
    }

    private UserSettings  fromVO(UserSettingVO userSettings,Long userId){
        UserSettings entity = new UserSettings();
        entity.setUserId(userId);
        entity.setSettingKey(userSettings.getSettingKey());
        entity.setSettingValue(userSettings.getSettingValue());
        return  entity;
    }
}




