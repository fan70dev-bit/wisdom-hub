package com.wisdomhub.controller;

import com.wisdomhub.dto.Result;
import com.wisdomhub.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "*")
public class FileController {
    
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    
    private final StorageService storageService;
    
    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }
    
    /**
     * 上传图片
     * 
     * POST /api/file/upload
     * Content-Type: multipart/form-data
     * 
     * @param file 上传的文件
     * @return 文件的公网访问 URL
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        log.info("接收文件上传请求: filename={}, size={} bytes", 
                file.getOriginalFilename(), file.getSize());
        
        String fileUrl = storageService.upload(file);
        
        Map<String, String> data = new HashMap<>();
        data.put("url", fileUrl);
        data.put("filename", file.getOriginalFilename());
        
        return Result.success("上传成功", data);
    }
    
    /**
     * 删除文件（可选）
     * 
     * DELETE /api/file/delete
     * 
     * @param fileUrl 文件的完整 URL
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam("fileUrl") String fileUrl) {
        log.info("接收文件删除请求: url={}", fileUrl);
        
        boolean success = storageService.delete(fileUrl);
        
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.fail(500, "删除失败");
        }
    }
}