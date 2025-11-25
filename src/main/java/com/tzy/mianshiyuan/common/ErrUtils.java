package com.tzy.mianshiyuan.common;

import com.tzy.mianshiyuan.exception.BusinessException;

public class ErrUtils {
    public static void err(ErrorCode errorCode,String message){
        throw new BusinessException(errorCode.getCode(),message);
    }

    public static void err(ErrorCode errorCode){
        throw new BusinessException(errorCode);
    }

    public static void errIf(boolean condition,ErrorCode errorCode){
        if(condition){
            throw new BusinessException(errorCode);
        }
    }

    public static void errIf(boolean condition,ErrorCode errorCode,String message){
        if(condition){
            throw new BusinessException(errorCode.getCode(),message);
        }
    }

    public static void paramErrIf(boolean condition,String message){
        if(condition){
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(),message);
        }
    }
}
