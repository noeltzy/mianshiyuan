package com.tzy.mianshiyuan.model.vo;

import lombok.Data;

/**
 * STS临时凭证VO
 * 用于前端直传OSS
 */
@Data
public class StsCredentialsVO {
    /**
     * 临时AccessKey ID
     */
    private String accessKeyId;

    /**
     * 临时AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 安全令牌（SecurityToken）
     */
    private String securityToken;

    /**
     * 过期时间
     */
    private String expiration;

    /**
     * OSS访问域名
     */
    private String ossHost;

    /**
     * OSS区域（用于OSS SDK，例如：oss-cn-hangzhou）
     */
    private String region;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 文件上传路径前缀
     */
    private String pathPrefix;

    /**
     * 文件夹路径
     */
    private String folder;
}

