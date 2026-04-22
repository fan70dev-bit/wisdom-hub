package com.wisdomhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性
 */
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties {
    
    /**
     * OSS 访问域名端点（例如：oss-cn-hangzhou.aliyuncs.com）
     */
    private String endpoint;
    
    /**
     * 访问密钥 ID
     */
    private String accessKeyId;
    
    /**
     * 访问密钥密码
     */
    private String accessKeySecret;
    
    /**
     * 存储桶名称
     */
    private String bucketName;
    
    /**
     * 自定义域名（可选，如已绑定自定义域名）
     */
    private String customDomain;
    
    /**
     * 文件存储根目录（可选，默认 uploads）
     */
    private String basePath = "uploads";
    
    public AliyunOssProperties() {
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getAccessKeyId() {
        return accessKeyId;
    }
    
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    
    public String getAccessKeySecret() {
        return accessKeySecret;
    }
    
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public String getCustomDomain() {
        return customDomain;
    }
    
    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }
    
    public String getBasePath() {
        return basePath;
    }
    
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    @Override
    public String toString() {
        return "AliyunOssProperties{" +
                "endpoint='" + endpoint + '\'' +
                ", accessKeyId='" + accessKeyId + '\'' +
                ", accessKeySecret='[PROTECTED]'" +
                ", bucketName='" + bucketName + '\'' +
                ", customDomain='" + customDomain + '\'' +
                ", basePath='" + basePath + '\'' +
                '}';
    }
}