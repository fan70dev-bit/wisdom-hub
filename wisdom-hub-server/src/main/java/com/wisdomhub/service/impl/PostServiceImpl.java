package com.wisdomhub.service.impl;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.entity.Post;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.PostMapper;
import com.wisdomhub.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文章/碎碎念服务实现
 * 集成 B 站 BV 号自动解析功能
 */
@Service
public class PostServiceImpl implements PostService {
    
    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);
    
    /**
     * B 站 BV 号正则表达式（预编译，性能优化）
     * 格式：BV + 10位大小写字母或数字
     * 示例：BV1xx411c7mD, BV1Ab2Cd3Ef4
     */
    private static final Pattern BV_PATTERN = Pattern.compile("BV[a-zA-Z0-9]{10}");
    
    private final PostMapper postMapper;
    
    public PostServiceImpl(PostMapper postMapper) {
        this.postMapper = postMapper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Post post) {
        // ========== 第一步：身份鉴权 ==========
        Long authorId = UserContext.getUserId();
        String authorEmail = UserContext.getUserEmail();
        
        if (authorId == null || !StringUtils.hasText(authorEmail)) {
            throw new UnauthorizedException("请先登录");
        }
        
        // ========== 第二步：内容校验 ==========
        if (!StringUtils.hasText(post.getContent())) {
            throw new BusinessException("内容不能为空");
        }
        
        // ========== 第三步：B 站 BV 号自动解析与提取 ==========
        extractAndSetBvNumber(post);
        
        // ========== 第四步：设置作者信息（禁止从前端接收 userId） ==========
        post.setAuthorId(authorId);
        post.setAuthorEmail(authorEmail);
        
        // ========== 第五步：时间填充 ==========
        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        
        // ========== 第六步：类型默认值处理 ==========
        if (post.getType() == null) {
            post.setType(1); // 默认碎碎念
        }
        
        // ========== 第七步：可见性默认值处理 ==========
        if (post.getVisibility() == null) {
            post.setVisibility(0); // 默认公开
        }
        
        // ========== 第八步：插入数据库 ==========
        int rows = postMapper.insert(post);
        if (rows <= 0) {
            throw new BusinessException("发布失败，请稍后重试");
        }
        
        log.info("文章发布成功: id={}, author={}, type={}, bvNumber={}", 
                post.getId(), authorEmail, post.getType(), post.getVideoUrl());
        
        return post.getId();
    }
    
    /**
     * 提取并设置 B 站 BV 号
     * 
     * 优先级 1：处理 videoUrl 字段
     *   - 如果 videoUrl 不为空，提取其中的 BV 号并清洗（去除 URL 前缀等）
     * 
     * 优先级 2：处理 content 字段
     *   - 如果 videoUrl 为空，但 content 中包含 BV 号，自动提取第一个并赋值给 videoUrl
     * 
     * @param post 待处理的帖子对象
     */
    private void extractAndSetBvNumber(Post post) {
        String videoUrl = post.getVideoUrl();
        String content = post.getContent();
        
        // ========== 优先级 1：处理 videoUrl 字段 ==========
        if (StringUtils.hasText(videoUrl)) {
            String extractedBv = extractBvFromText(videoUrl);
            
            if (extractedBv != null) {
                // 找到 BV 号，清洗后重新赋值
                post.setVideoUrl(extractedBv);
                log.debug("从 videoUrl 提取到 BV 号: {} -> {}", videoUrl, extractedBv);
            } else {
                // videoUrl 不为空但未匹配到 BV 号，保持原值（可能是其他视频平台链接）
                log.debug("videoUrl 未匹配到 BV 号，保持原值: {}", videoUrl);
            }
            
            // 已处理 videoUrl，直接返回
            return;
        }
        
        // ========== 优先级 2：处理 content 字段 ==========
        if (StringUtils.hasText(content)) {
            String extractedBv = extractBvFromText(content);
            
            if (extractedBv != null) {
                // 从正文中提取到 BV 号，自动赋值给 videoUrl
                post.setVideoUrl(extractedBv);
                log.debug("从 content 提取到 BV 号并赋值给 videoUrl: {}", extractedBv);
            }
        }
    }
    
    /**
     * 从文本中提取 BV 号（取第一个匹配项）
     * 
     * @param text 待提取的文本（可能是 URL、正文等）
     * @return 提取到的 BV 号，未找到则返回 null
     */
    private String extractBvFromText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        
        Matcher matcher = BV_PATTERN.matcher(text);
        
        if (matcher.find()) {
            // 返回第一个匹配的 BV 号
            return matcher.group();
        }
        
        return null;
    }
    
    @Override
    public List<Post> getGarden() {
        String authorEmail = UserContext.getUserEmail();
        
        // "我的花园"：必须登录
        if (!StringUtils.hasText(authorEmail)) {
            throw new UnauthorizedException("请先登录查看我的花园");
        }
        
        // 本人访问：显示全部（包含私密）
        return postMapper.findGardenByAuthorEmail(authorEmail, true);
    }
    
    @Override
    public List<Post> getPlaza() {
        // 广场：仅展示 visibility=0（公开）的内容
        return postMapper.findPlaza();
    }
}