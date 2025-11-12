package com.tzy.mianshiyuan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 用于前端直传OSS的配置
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class OssConfig {

    /**
     * OSS endpoint（地域节点）
     * 固定为：https://oss-cn-shanghai.aliyuncs.com
     */
    private String endpoint = "https://oss-cn-shanghai.aliyuncs.com";

    /**
     * AccessKey ID（用于STS）
     */
    private String accessKeyId;

    /**
     * AccessKey Secret（用于STS）
     */
    private String accessKeySecret;

    /**
     * Bucket名称
     * 固定为：teng-oss
     */
    private String bucketName = "teng-oss";

    /**
     * 自定义域名（可选，如果有配置CDN或自定义域名）
     * 如果未配置，将使用默认的OSS域名
     */
    private String customDomain;

    /**
     * 文件上传路径前缀
     * 例如：images/ 或 uploads/
     */
    private String pathPrefix = "uploads/";

    /**
     * STS角色ARN（用于前端直传）
     * 格式：acs:ram::账户ID:role/角色名称
     * 例如：acs:ram::1234567890123456:role/oss-upload-role
     */
    private String roleArn;

    /**
     * STS会话名称（可选）
     * 用于标识STS会话
     */
    private String roleSessionName = "oss-upload-session";

    /**
     * STS临时凭证过期时间（秒）
     * 默认1小时（3600秒），最大12小时（43200秒）
     */
    private Long stsDurationSeconds = 3600L;

    /**
     * STS区域（固定为cn-shanghai）
     * 与OSS区域oss-cn-shanghai对应
     */
    private String stsRegion = "cn-shanghai";

    /**
     * 获取OSS访问域名
     * 如果配置了自定义域名，使用自定义域名；否则使用OSS默认域名
     * @return OSS访问域名
     */
    public String getOssHost() {
        if (customDomain != null && !customDomain.trim().isEmpty()) {
            // 使用自定义域名
            String domain = customDomain.trim();
            if (!domain.endsWith("/")) {
                domain = domain + "/";
            }
            return domain;
        } else {
            // 使用OSS默认域名
            // endpoint格式：https://oss-cn-shanghai.aliyuncs.com 或 oss-cn-shanghai.aliyuncs.com
            String endpointWithoutProtocol = endpoint;
            if (endpoint.startsWith("https://")) {
                endpointWithoutProtocol = endpoint.substring(8);
            } else if (endpoint.startsWith("http://")) {
                endpointWithoutProtocol = endpoint.substring(7);
            }
            return "https://" + bucketName + "." + endpointWithoutProtocol;
        }
    }

    /**
     * 获取文件访问URL
     * @param objectName 对象名称（文件路径）
     * @return 文件访问URL
     */
    public String getFileUrl(String objectName) {
        String host = getOssHost();
        if (host.endsWith("/")) {
            return host + objectName;
        } else {
            return host + "/" + objectName;
        }
    }
}

