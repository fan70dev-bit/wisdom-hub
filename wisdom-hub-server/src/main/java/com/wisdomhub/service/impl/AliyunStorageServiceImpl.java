package com.wisdomhub.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.wisdomhub.config.AliyunOssProperties;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.service.StorageService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云 OSS 存储服务实现
 */
@Service
public class AliyunStorageServiceImpl implements StorageService {
    
    private static final Logger log = LoggerFactory.getLogger(AliyunStorageServiceImpl.class);
    
    /**
     * 支持的图片格式
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    );
    
    /**
     * 文件大小限制（10MB）
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    private final AliyunOssProperties ossProperties;
    
    public AliyunStorageServiceImpl(AliyunOssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }
    
    @Override
    public String upload(MultipartFile file) {
        // ========== 第一步：文件校验 ==========
        validateFile(file);
        
        // ========== 第二步：生成唯一文件名 ==========
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        
        // ========== 第三步：构建存储路径（按日期分组） ==========
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectKey = ossProperties.getBasePath() + "/" + datePath + "/" + newFileName;
        
        // ========== 第四步：上传到阿里云 OSS ==========
        OSS ossClient = null;
        try {
            // 创建 OSS 客户端
            ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
            );
            
            // 上传文件流
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossProperties.getBucketName(),
                objectKey,
                inputStream
            );
            
            ossClient.putObject(putObjectRequest);
            
            log.info("文件上传成功: objectKey={}, size={} bytes", objectKey, file.getSize());
            
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new BusinessException("文件读取失败");
        } catch (Exception e) {
            log.error("OSS 上传失败", e);
            throw new BusinessException("文件上传失败，请稍后重试");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        
        // ========== 第五步：返回公网访问 URL ==========
        return buildFileUrl(objectKey);
    }
    
    @Override
    public boolean delete(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }
        
        OSS ossClient = null;
        try {
            // 从 URL 中提取 objectKey
            String objectKey = extractObjectKey(fileUrl);
            
            ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
            );
            
            ossClient.deleteObject(ossProperties.getBucketName(), objectKey);
            
            log.info("文件删除成功: objectKey={}", objectKey);
            return true;
            
        } catch (Exception e) {
            log.error("OSS 删除失败: url={}", fileUrl, e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 文件校验
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 10MB");
        }
        
        // 校验文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("文件名不合法");
        }
        
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件格式，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }
    
    /**
     * 构建文件的公网访问 URL
     */
    private String buildFileUrl(String objectKey) {
        // 如果配置了自定义域名，优先使用自定义域名
        if (StringUtils.hasText(ossProperties.getCustomDomain())) {
            return ossProperties.getCustomDomain() + "/" + objectKey;
        }
        
        // 否则使用默认的 OSS 域名
        // 格式：https://bucketName.endpoint/objectKey
        return "https://" + ossProperties.getBucketName() + "." + 
               ossProperties.getEndpoint() + "/" + objectKey;
    }
    
    /**
     * 从 URL 中提取 objectKey
     */
    private String extractObjectKey(String fileUrl) {
        // 示例 URL：https://wisdom-hub.oss-cn-hangzhou.aliyuncs.com/uploads/2026/04/21/abc123.jpg
        // 提取：uploads/2026/04/21/abc123.jpg
        
        if (StringUtils.hasText(ossProperties.getCustomDomain()) && 
            fileUrl.startsWith(ossProperties.getCustomDomain())) {
            // 自定义域名场景
            return fileUrl.substring(ossProperties.getCustomDomain().length() + 1);
        }
        
        // 默认 OSS 域名场景
        String prefix = "https://" + ossProperties.getBucketName() + "." + 
                       ossProperties.getEndpoint() + "/";
        
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        
        // 如果已经是纯 objectKey，直接返回
        return fileUrl;
    }
}