package com.tzy.mianshiyuan.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.tzy.mianshiyuan.config.OssConfig;
import com.tzy.mianshiyuan.common.ErrorCode;
import com.tzy.mianshiyuan.exception.BusinessException;
import com.tzy.mianshiyuan.model.vo.StsCredentialsVO;
import com.tzy.mianshiyuan.service.StsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * STS服务实现类
 */
@Slf4j
@Service
public class StsServiceImpl implements StsService {

    @Autowired
    private OssConfig ossConfig;

    @Override
    public StsCredentialsVO getStsCredentials(String folder) {
        try {
            // 获取STS区域（优先使用配置的区域，否则从OSS endpoint提取）
            String region = ossConfig.getStsRegion();
            if (region == null || region.trim().isEmpty()) {
                region = extractRegionFromEndpoint(ossConfig.getEndpoint());
            }
            
            // 创建STS客户端
            DefaultProfile profile = DefaultProfile.getProfile(region, ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
            IAcsClient client = new DefaultAcsClient(profile);

            // 构建AssumeRole请求
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setRoleArn(ossConfig.getRoleArn());
            request.setRoleSessionName(ossConfig.getRoleSessionName() + "-" + UUID.randomUUID().toString().substring(0, 8));
            request.setDurationSeconds(ossConfig.getStsDurationSeconds());

            // 构建权限策略（限制只能上传到指定文件夹）
            String policy = buildPolicy(folder);
            log.info("STS Policy: {}", policy);
            request.setPolicy(policy);

            // 调用STS服务获取临时凭证
            AssumeRoleResponse response = client.getAcsResponse(request);
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 提取OSS区域（用于OSS SDK）
            String ossRegion = extractOssRegionFromEndpoint(ossConfig.getEndpoint());
            
            // 构建返回对象
            StsCredentialsVO stsCredentials = new StsCredentialsVO();
            stsCredentials.setAccessKeyId(credentials.getAccessKeyId());
            stsCredentials.setAccessKeySecret(credentials.getAccessKeySecret());
            stsCredentials.setSecurityToken(credentials.getSecurityToken());
            stsCredentials.setExpiration(credentials.getExpiration());
            stsCredentials.setOssHost(ossConfig.getOssHost());
            stsCredentials.setRegion(ossRegion);
            stsCredentials.setBucketName(ossConfig.getBucketName());
            stsCredentials.setPathPrefix(ossConfig.getPathPrefix());
            stsCredentials.setFolder(folder != null ? folder : "");

            log.info("STS临时凭证生成成功，文件夹: {}, 区域: {}", folder, ossRegion);
            return stsCredentials;
        } catch (ClientException e) {
            log.error("STS临时凭证生成失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "获取临时凭证失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("STS临时凭证生成异常: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "获取临时凭证失败: " + e.getMessage());
        }
    }

    @Override
    public StsCredentialsVO getStsCredentials() {
        return getStsCredentials(null);
    }

    /**
     * 从OSS endpoint提取STS区域
     * 例如：https://oss-cn-shanghai.aliyuncs.com -> cn-shanghai
     * 用于STS服务调用
     */
    private String extractRegionFromEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return "cn-shanghai"; // 默认区域（固定为上海）
        }
        
        // 移除协议前缀
        String endpointWithoutProtocol = endpoint;
        if (endpoint.startsWith("https://")) {
            endpointWithoutProtocol = endpoint.substring(8);
        } else if (endpoint.startsWith("http://")) {
            endpointWithoutProtocol = endpoint.substring(7);
        }
        
        // 提取区域
        // 格式：oss-cn-shanghai.aliyuncs.com -> cn-shanghai
        if (endpointWithoutProtocol.startsWith("oss-")) {
            String region = endpointWithoutProtocol.substring(4);
            int dotIndex = region.indexOf(".");
            if (dotIndex > 0) {
                return region.substring(0, dotIndex);
            }
        }
        
        return "cn-shanghai"; // 默认区域（固定为上海）
    }

    /**
     * 从OSS endpoint提取OSS区域
     * 例如：https://oss-cn-shanghai.aliyuncs.com -> oss-cn-shanghai
     * 用于OSS SDK
     */
    private String extractOssRegionFromEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return "oss-cn-shanghai"; // 默认区域（固定为上海）
        }
        
        // 移除协议前缀
        String endpointWithoutProtocol = endpoint;
        if (endpoint.startsWith("https://")) {
            endpointWithoutProtocol = endpoint.substring(8);
        } else if (endpoint.startsWith("http://")) {
            endpointWithoutProtocol = endpoint.substring(7);
        }
        
        // 提取区域
        // 格式：oss-cn-shanghai.aliyuncs.com -> oss-cn-shanghai
        if (endpointWithoutProtocol.startsWith("oss-")) {
            int dotIndex = endpointWithoutProtocol.indexOf(".");
            if (dotIndex > 0) {
                return endpointWithoutProtocol.substring(0, dotIndex);
            }
        }
        
        return "oss-cn-shanghai"; // 默认区域（固定为上海）
    }

    /**
     * 构建权限策略（精确匹配目录，参考阿里云官方文档）
     * 限制只能上传到指定文件夹，支持分片上传
     * 
     * 重要说明：
     * 1. Resource 必须精确匹配目录，例如：acs:oss:*:*:teng-oss/uploads/images/*
     * 2. 尾部必须写 /*，不能漏掉 uploads 或 images 中的任意一层
     * 3. Action 需要包含 oss:PutObject（上传）、oss:DeleteObject（覆盖删除）、oss:AbortMultipartUpload（分片上传取消）
     * 
     * @param folder 文件夹路径（例如：images/、avatars/），如果为null或空，则只允许uploads/目录
     * @return Policy JSON字符串
     */
    private String buildPolicy(String folder) {
        // 1. 获取路径前缀（默认：uploads/）
        String pathPrefix = ossConfig.getPathPrefix() != null ? ossConfig.getPathPrefix() : "uploads/";
        
        // 2. 确保 pathPrefix 以 / 结尾
        if (!pathPrefix.endsWith("/")) {
            pathPrefix += "/";
        }
        
        // 3. 构建完整的对象路径前缀
        String objectPrefix;
        if (folder != null && !folder.trim().isEmpty()) {
            // 处理 folder 参数（移除前后空格）
            String normalizedFolder = folder.trim();
            
            // 如果 folder 以 / 开头，移除它（避免双重斜杠）
            if (normalizedFolder.startsWith("/")) {
                normalizedFolder = normalizedFolder.substring(1);
            }
            
            // 确保 folder 以 / 结尾
            if (!normalizedFolder.endsWith("/")) {
                normalizedFolder += "/";
            }
            
            // 拼接完整路径：pathPrefix + folder
            // 例如：uploads/ + images/ = uploads/images/
            objectPrefix = pathPrefix + normalizedFolder;
        } else {
            // 如果 folder 为空，只使用 pathPrefix
            // 例如：uploads/
            objectPrefix = pathPrefix;
        }
        
        // 4. 构建Resource（格式：acs:oss:*:*:bucket-name/object-prefix*）
        // 例如：acs:oss:*:*:teng-oss/uploads/images/*
        // 重要：尾部必须有 /*，表示该目录下的所有文件
        // 不能漏掉 uploads 或 images 中的任意一层
        // 例如：acs:oss:*:*:teng-oss/uploads/images/*（正确）
        // 错误：acs:oss:*:*:teng-oss/images/*（缺少uploads）
        String resource = String.format("acs:oss:*:*:%s/%s*", ossConfig.getBucketName(), objectPrefix);
        
        // 5. 构建Policy JSON
        // Action 必须包含：
        // - oss:PutObject：上传文件（包括普通上传和分片上传）
        // - oss:DeleteObject：删除文件（用于覆盖上传时删除旧文件）
        // - oss:AbortMultipartUpload：取消分片上传（用于分片上传失败时清理）
        // 注意：这些权限是前端直传OSS所必需的，特别是分片上传功能
        String policy = String.format(
            "{\"Version\":\"1\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"oss:PutObject\",\"oss:DeleteObject\",\"oss:AbortMultipartUpload\"],\"Resource\":[\"%s\"]}]}",
            resource
        );
        
        // 6. 记录生成的Policy信息（用于调试和验证）
        log.info("STS Policy 对象前缀: {}", objectPrefix);
        log.info("STS Policy 资源路径: {}", resource);
        log.debug("STS Policy 完整内容: {}", policy);
        
        return policy;
    }
}

