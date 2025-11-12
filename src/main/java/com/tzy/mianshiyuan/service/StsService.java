package com.tzy.mianshiyuan.service;

import com.tzy.mianshiyuan.model.vo.StsCredentialsVO;

/**
 * STS服务接口
 * 用于生成临时凭证，供前端直传OSS使用
 */
public interface StsService {

    /**
     * 获取STS临时凭证
     * @param folder 文件夹路径（可选，例如：images/、avatars/）
     * @return STS临时凭证
     */
    StsCredentialsVO getStsCredentials(String folder);

    /**
     * 获取STS临时凭证（使用默认路径）
     * @return STS临时凭证
     */
    StsCredentialsVO getStsCredentials();
}

