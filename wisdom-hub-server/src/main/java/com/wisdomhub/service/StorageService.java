package com.wisdomhub.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口（解耦设计，支持多种存储实现）
 */
public interface StorageService {
    
    /**
     * 上传文件
     * 
     * @param file 待上传的文件
     * @return 文件的公网访问 URL
     * @throws com.wisdomhub.exception.BusinessException 上传失败时抛出
     */
    String upload(MultipartFile file);
    
    /**
     * 删除文件（可选实现）
     * 
     * @param fileUrl 文件的完整 URL 或对象键（objectKey）
     * @return 是否删除成功
     */
    boolean delete(String fileUrl);
}