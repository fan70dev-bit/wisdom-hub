package com.wisdomhub.service.impl;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.dto.UpdatePostRequest;
import com.wisdomhub.entity.Post;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.PostMapper;
import com.wisdomhub.service.PostService;
import com.wisdomhub.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文章/碎碎念服务实现
 * 集成 B 站 BV 号自动解析功能 + 完整 CRUD 逻辑
 */
@Service
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    /**
     * B 站 BV 号正则表达式（预编译，性能优化）
     */
    private static final Pattern BV_PATTERN = Pattern.compile("BV[a-zA-Z0-9]{10}");

    private final PostMapper postMapper;
    private final StorageService storageService;

    public PostServiceImpl(PostMapper postMapper, StorageService storageService) {
        this.postMapper = postMapper;
        this.storageService = storageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Post post) {
        // 身份鉴权
        Long authorId = UserContext.getUserId();
        String authorEmail = UserContext.getUserEmail();

        if (authorId == null || !StringUtils.hasText(authorEmail)) {
            throw new UnauthorizedException("请先登录");
        }

        // 内容校验
        if (!StringUtils.hasText(post.getContent())) {
            throw new BusinessException("内容不能为空");
        }

        // B 站 BV 号自动解析与提取
        extractAndSetBvNumber(post);

        // 设置作者信息
        post.setAuthorId(authorId);
        post.setAuthorEmail(authorEmail);

        // 时间填充
        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);

        // 类型默认值
        if (post.getType() == null) {
            post.setType(1); // 默认碎碎念
        }

        // 可见性默认值
        if (post.getVisibility() == null) {
            post.setVisibility(0); // 默认公开
        }

        // 插入数据库
        int rows = postMapper.insert(post);
        if (rows <= 0) {
            throw new BusinessException("发布失败，请稍后重试");
        }

        log.info("文章发布成功: id={}, author={}, type={}, bvNumber={}",
                post.getId(), authorEmail, post.getType(), post.getVideoUrl());

        return post.getId();
    }

    @Override
    public Post getById(Long id) {
        if (id == null) {
            throw new BusinessException("帖子 ID 不能为空");
        }

        Post post = postMapper.findById(id);

        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(UpdatePostRequest request) {
        // ========== 第一步：身份鉴权 ==========
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("请先登录");
        }

        // ========== 第二步：查询原帖子，校验权限 ==========
        Post existingPost = postMapper.findById(request.getId());
        if (existingPost == null) {
            throw new BusinessException("帖子不存在");
        }

        if (!Objects.equals(existingPost.getAuthorId(), currentUserId)) {
            throw new BusinessException("无权修改他人的帖子");
        }

        // ========== 第三步：构建更新对象（仅更新非空字段） ==========
        Post updatePost = new Post();
        updatePost.setId(request.getId());

        if (StringUtils.hasText(request.getTitle())) {
            updatePost.setTitle(request.getTitle());
        }

        if (StringUtils.hasText(request.getContent())) {
            updatePost.setContent(request.getContent());
        }

        if (StringUtils.hasText(request.getCoverImage())) {
            updatePost.setCoverImage(request.getCoverImage());

            // ========== 第四步：清理旧封面图（OSS 资源） ==========
            String oldCoverImage = existingPost.getCoverImage();
            if (StringUtils.hasText(oldCoverImage) &&
                    !oldCoverImage.equals(request.getCoverImage())) {

                try {
                    boolean deleted = storageService.delete(oldCoverImage);
                    if (deleted) {
                        log.info("旧封面图已删除: {}", oldCoverImage);
                    }
                } catch (Exception e) {
                    log.warn("旧封面图删除失败（非致命错误）: {}", oldCoverImage, e);
                }
            }
        }

        if (StringUtils.hasText(request.getVideoUrl())) {
            updatePost.setVideoUrl(request.getVideoUrl());
        }

        if (request.getVisibility() != null) {
            updatePost.setVisibility(request.getVisibility());
        }

        // 更新时间
        updatePost.setUpdateTime(LocalDateTime.now());

        // ========== 第五步：执行更新 ==========
        int rows = postMapper.update(updatePost);

        if (rows > 0) {
            log.info("帖子修改成功: id={}, userId={}", request.getId(), currentUserId);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        // ========== 第一步：身份鉴权 ==========
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("请先登录");
        }

        // ========== 第二步：查询帖子，校验权限 ==========
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        if (!Objects.equals(post.getAuthorId(), currentUserId)) {
            throw new BusinessException("无权删除他人的帖子");
        }

        // ========== 第三步：删除 OSS 资源（封面图） ==========
        if (StringUtils.hasText(post.getCoverImage())) {
            try {
                boolean deleted = storageService.delete(post.getCoverImage());
                if (deleted) {
                    log.info("封面图已删除: {}", post.getCoverImage());
                } else {
                    log.warn("封面图删除失败（可能已不存在）: {}", post.getCoverImage());
                }
            } catch (Exception e) {
                log.error("OSS 资源清理异常（继续删除数据库记录）: {}", post.getCoverImage(), e);
            }
        }

        // ========== 第四步：删除数据库记录 ==========
        int rows = postMapper.deleteById(id);

        if (rows > 0) {
            log.info("帖子删除成功: id={}, userId={}", id, currentUserId);
            return true;
        }

        return false;
    }

    @Override
    public List<Post> getGarden() {
        String authorEmail = UserContext.getUserEmail();

        if (!StringUtils.hasText(authorEmail)) {
            throw new UnauthorizedException("请先登录查看我的花园");
        }

        return postMapper.findGardenByAuthorEmail(authorEmail, true);
    }

    @Override
    public List<Post> getPlaza() {
        return postMapper.findPlaza();
    }

    /**
     * 提取并设置 B 站 BV 号
     */
    private void extractAndSetBvNumber(Post post) {
        String videoUrl = post.getVideoUrl();
        String content = post.getContent();

        if (StringUtils.hasText(videoUrl)) {
            String extractedBv = extractBvFromText(videoUrl);

            if (extractedBv != null) {
                post.setVideoUrl(extractedBv);
                log.debug("从 videoUrl 提取到 BV 号: {} -> {}", videoUrl, extractedBv);
            }

            return;
        }

        if (StringUtils.hasText(content)) {
            String extractedBv = extractBvFromText(content);

            if (extractedBv != null) {
                post.setVideoUrl(extractedBv);
                log.debug("从 content 提取到 BV 号并赋值给 videoUrl: {}", extractedBv);
            }
        }
    }

    /**
     * 从文本中提取 BV 号
     */
    private String extractBvFromText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        Matcher matcher = BV_PATTERN.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }
}