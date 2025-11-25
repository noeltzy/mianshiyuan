package com.tzy.mianshiyuan.util;

import com.tzy.mianshiyuan.model.domain.User;
import com.tzy.mianshiyuan.model.vo.UserVO;

/**
 * User转换工具类
 * 统一管理User实体类与UserVO之间的转换逻辑
 */
public class UserConverter {

    /**
     * 将User转换为UserVO
     * @param user User实体类
     * @return UserVO视图对象
     */
    public static UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatarUrl(user.getAvatarUrl());
        userVO.setRole(user.getRole());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        
        return userVO;
    }
    
    /**
     * 批量转换User列表为UserVO列表
     * @param users User实体类列表
     * @return UserVO视图对象列表
     */
    public static java.util.List<UserVO> toVOList(java.util.List<User> users) {
        if (users == null || users.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        return users.stream()
                .map(UserConverter::toVO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 将User数组转换为UserVO数组
     * @param userArray User实体类数组
     * @return UserVO视图对象数组
     */
    public static UserVO[] toVOArray(User[] userArray) {
        if (userArray == null) {
            return new UserVO[0];
        }
        
        return java.util.Arrays.stream(userArray)
                .map(UserConverter::toVO)
                .toArray(UserVO[]::new);
    }
}




