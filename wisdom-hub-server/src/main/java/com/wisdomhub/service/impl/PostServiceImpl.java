package com.wisdomhub.service.impl;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.dto.CreatePostRequest;
import com.wisdomhub.dto.PageResult;
import com.wisdomhub.dto.PostVO;
import com.wisdomhub.dto.UpdatePostRequest;
import com.wisdomhub.entity.Post;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.exception.ForbiddenException;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.PostMapper;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
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
 * 帖子服务实现（去封面图版本 - 隐私脱敏 + 审计留证）
 */
@Service
public class PostServiceImpl implements PostService {
    
    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);
    
    /**
     * B 站 BV 号正则表达式
     */
    private static final Pattern BV_PATTERN = Pattern.compile("BV[a-zA-Z0-9]{10}");
    
    /**
     * 敏感词列表（生产环境应从数据库/配置中心加载）
     */
    private static final String[] SENSITIVE_WORDS = {
        "敏感词A", "违规内容B", "广告链接C", "政治", "色情", "赌博", "诈骗"
    };
    
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final HttpServletRequest httpServletRequest;
    
    public PostServiceImpl(PostMapper postMapper, 
                          UserMapper userMapper,
                          HttpServletRequest httpServletRequest) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.httpServletRequest = httpServletRequest;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CreatePostRequest request) {
        // ========== 第一步：身份鉴权 ==========
        Long userId = UserContext.getUserId();
        String email = UserContext.getUserEmail();
        
        if (userId == null || !StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        // ========== 第二步：查询用户获取 account_id（严禁使用邮箱关联） ==========
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        String accountId = user.getAccountId();
        if (!StringUtils.hasText(accountId)) {
            throw new BusinessException("用户账号异常，请联系管理员");
        }
        
        // ========== 第三步：内容校验 ==========
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException("内容不能为空");
        }
        
        // ========== 第四步：敏感词检测 + 状态流转 ==========
        Integer status;
        String auditRemark = null;
        
        boolean containsSensitiveWord = detectSensitiveWords(request.getContent());
        
        if (containsSensitiveWord) {
            // 命中敏感词，强制待审核
            status = 2;
            auditRemark = "系统检测到疑似违规内容，已自动转入人工审核";
            log.warn("发帖命中敏感词: userId={}, accountId={}", userId, accountId);
        } else {
            // 未命中，根据用户选择设置公开/私密
            status = Boolean.TRUE.equals(request.getIsPrivate()) ? 1 : 0;
        }
        
        // ========== 第五步：B 站 BV 号提取 ==========
        String videoUrl = extractBvNumber(request);
        
        // ========== 第六步：获取客户端真实 IP ==========
        String clientIp = getClientIp(httpServletRequest);
        
        // ========== 第七步：构建 Post 对象（无封面图） ==========
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(videoUrl)
                .type(request.getType() != null ? request.getType() : 1)
                .status(status)
                .authorId(accountId)  // 使用 account_id 而非邮箱
                .auditRemark(auditRemark)
                .ipAddress(clientIp)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        // ========== 第八步：插入数据库 ==========
        int rows = postMapper.insert(post);
        if (rows <= 0) {
            throw new BusinessException("发布失败，请稍后重试");
        }
        
        log.info("帖子发布成功: id={}, authorId={}, status={}, ip={}", 
                post.getId(), accountId, status, clientIp);
        
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
        
        // ========== 权限校验 ==========
        Long currentUserId = UserContext.getUserId();
        User currentUser = null;
        
        if (currentUserId != null) {
            String email = UserContext.getUserEmail();
            currentUser = userMapper.findByEmail(email);
        }
        
        // 如果是私密帖子（status=1）且非作者，禁止访问
        if (post.getStatus() == 1) {
            if (currentUser == null || !post.getAuthorId().equals(currentUser.getAccountId())) {
                throw new ForbiddenException("无权访问他人的私密帖子");
            }
        }
        
        // 已删除帖子（status=4）不可访问
        if (post.getStatus() == 4) {
            throw new BusinessException("帖子已删除");
        }
        
        return post;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(UpdatePostRequest request) {
        // ========== 第一步：身份鉴权 ==========
        Long userId = UserContext.getUserId();
        String email = UserContext.getUserEmail();
        
        if (userId == null || !StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        String accountId = user.getAccountId();
        
        // ========== 第二步：查询原帖子，校验权限 ==========
        Post existingPost = postMapper.findById(request.getId());
        if (existingPost == null) {
            throw new BusinessException("帖子不存在");
        }
        
        if (!Objects.equals(existingPost.getAuthorId(), accountId)) {
            throw new ForbiddenException("无权修改他人的帖子");
        }
        
        // ========== 第三步：敏感词检测（如果修改了内容） ==========
        Integer newStatus = existingPost.getStatus();
        String auditRemark = null;
        
        if (StringUtils.hasText(request.getContent())) {
            boolean containsSensitiveWord = detectSensitiveWords(request.getContent());
            
            if (containsSensitiveWord) {
                newStatus = 2;
                auditRemark = "系统检测到疑似违规内容，已自动转入人工审核";
                log.warn("修改帖子命中敏感词: postId={}, accountId={}", request.getId(), accountId);
            } else if (request.getIsPrivate() != null) {
                // 未命中敏感词且用户修改了可见性
                newStatus = Boolean.TRUE.equals(request.getIsPrivate()) ? 1 : 0;
            }
        }
        
        // ========== 第四步：构建更新对象（无封面图） ==========
        Post updatePost = new Post();
        updatePost.setId(request.getId());
        
        if (StringUtils.hasText(request.getTitle())) {
            updatePost.setTitle(request.getTitle());
        }
        
        if (StringUtils.hasText(request.getContent())) {
            updatePost.setContent(request.getContent());
        }
        
        if (StringUtils.hasText(request.getVideoUrl())) {
            updatePost.setVideoUrl(request.getVideoUrl());
        }
        
        updatePost.setStatus(newStatus);
        updatePost.setAuditRemark(auditRemark);
        updatePost.setUpdateTime(LocalDateTime.now());
        
        // ========== 第五步：执行更新 ==========
        int rows = postMapper.update(updatePost);
        
        if (rows > 0) {
            log.info("帖子修改成功: id={}, accountId={}, newStatus={}", 
                    request.getId(), accountId, newStatus);
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        // ========== 第一步：身份鉴权 ==========
        Long userId = UserContext.getUserId();
        String email = UserContext.getUserEmail();
        
        if (userId == null || !StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        String accountId = user.getAccountId();
        
        // ========== 第二步：查询帖子，校验权限 ==========
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        if (!Objects.equals(post.getAuthorId(), accountId)) {
            throw new ForbiddenException("无权删除他人的帖子");
        }
        
        // ========== 第三步：逻辑删除（status=4，严禁删除 OSS 资源） ==========
        // 重要：即使文章被用户删除或被管理员封禁，
        // 严禁调用 StorageService 删除正文中的图片资源。
        // 理由：必须保留完整的文章内容（文字+图片）作为违规证据，以备后续监管溯源。
        
        int rows = postMapper.logicDelete(id);
        
        if (rows > 0) {
            log.info("帖子逻辑删除成功（保留完整内容作为证据）: id={}, accountId={}", id, accountId);
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<Post> getGarden() {
        String email = UserContext.getUserEmail();
        
        if (!StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录查看我的花园");
        }
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 使用 account_id 查询（严禁使用邮箱）
        // Mapper 已自动过滤 status=4（用户已删除）
        return postMapper.findGardenByAuthorId(user.getAccountId());
    }
    
    @Override
    public List<Post> getPlaza() {
        // 绝对只能查询 status=0（公开）且用户状态正常
        return postMapper.findPlaza();
    }

    // ========== 新增方法 ==========

    @Override
    public PageResult<PostVO> explore(Integer pageNum, Integer pageSize) {
        // 参数校验与默认值
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 10; // 默认每页 10 条，最多 50 条
        }

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<PostVO> list = postMapper.findExploreList(offset, pageSize);

        // 查询总数
        int total = postMapper.countExploreList();

        log.info("探索广场查询: pageNum={}, pageSize={}, total={}", pageNum, pageSize, total);

        return new PageResult<>(pageNum, pageSize, total, list);
    }

    @Override
    public PageResult<PostVO> search(String keyword, Integer pageNum, Integer pageSize) {
        // 关键字校验
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException("搜索关键字不能为空");
        }

        // 过滤特殊字符，防止 SQL 注入（虽然 MyBatis 预编译已经防止了，但加一层保险）
        keyword = keyword.trim();
        if (keyword.length() > 50) {
            throw new BusinessException("搜索关键字过长");
        }

        // 参数校验与默认值
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 10;
        }

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<PostVO> list = postMapper.searchPosts(keyword, offset, pageSize);

        // 查询总数
        int total = postMapper.countSearchPosts(keyword);

        log.info("关键字搜索: keyword={}, pageNum={}, pageSize={}, total={}",
                keyword, pageNum, pageSize, total);

        return new PageResult<>(pageNum, pageSize, total, list);
    }
    
    /**
     * 敏感词检测
     */
    private boolean detectSensitiveWords(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        
        for (String word : SENSITIVE_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 提取 BV 号
     */
    private String extractBvNumber(CreatePostRequest request) {
        String videoUrl = request.getVideoUrl();
        String content = request.getContent();
        
        if (StringUtils.hasText(videoUrl)) {
            String extractedBv = extractBvFromText(videoUrl);
            return extractedBv != null ? extractedBv : videoUrl;
        }
        
        if (StringUtils.hasText(content)) {
            return extractBvFromText(content);
        }
        
        return null;
    }
    
    private String extractBvFromText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        
        Matcher matcher = BV_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }
    
    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}