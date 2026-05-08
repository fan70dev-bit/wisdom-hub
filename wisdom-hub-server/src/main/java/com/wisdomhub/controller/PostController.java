package com.wisdomhub.controller;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.dto.*;
import com.wisdomhub.entity.Post;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.PostFavoriteService;
import com.wisdomhub.service.PostLikeService;
import com.wisdomhub.service.PostService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子控制器（扩展点赞、收藏功能）
 */
@RestController
@RequestMapping("/api/post")
public class PostController {
    
    private final PostService postService;
    private final PostLikeService postLikeService;
    private final PostFavoriteService postFavoriteService;
    private final UserMapper userMapper;
    
    public PostController(PostService postService,
                         PostLikeService postLikeService,
                         PostFavoriteService postFavoriteService,
                         UserMapper userMapper) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.postFavoriteService = postFavoriteService;
        this.userMapper = userMapper;
    }
    
    /**
     * 创建帖子
     */
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody CreatePostRequest request) {
        if (UserContext.getUserId() == null || !StringUtils.hasText(UserContext.getUserEmail())) {
            throw new UnauthorizedException("请先登录");
        }
        
        Long postId = postService.create(request);
        return Result.success("发布成功", postId);
    }
    
    /**
     * 查询帖子详情（包含点赞、收藏状态）
     */
    @GetMapping("/{id}")
    public Result<PostDetailVO> getById(@PathVariable Long id) {
        Post post = postService.getById(id);
        
        // 查询当前用户的交互状态
        String email = UserContext.getUserEmail();
        boolean isLiked = false;
        boolean isFavorited = false;
        
        if (StringUtils.hasText(email)) {
            User user = userMapper.findByEmail(email);
            if (user != null) {
                String userId = user.getAccountId();
                isLiked = postLikeService.isLiked(id, userId);
                isFavorited = postFavoriteService.isFavorited(id, userId);
            }
        }
        
        PostDetailVO vo = new PostDetailVO(post, isLiked, isFavorited);
        return Result.success(vo);
    }
    
    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        boolean isLiked = postLikeService.toggleLike(id);

        // 🔥 查询点赞后的最新总数
        Post post = postService.getById(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("isLiked", isLiked);
        data.put("likeCount", post.getLikeCount());  // 🔥 加上这一行
        data.put("message", isLiked ? "点赞成功" : "已取消点赞");
        
        return Result.success(data);
    }
    
    /**
     * 收藏/取消收藏
     */
    @PostMapping("/{id}/favorite")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Long id) {
        boolean isFavorited = postFavoriteService.toggleFavorite(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorited", isFavorited);
        data.put("message", isFavorited ? "收藏成功" : "已取消收藏");
        
        return Result.success(data);
    }
    
    /**
     * 我的收藏列表
     */
    @GetMapping("/favorites")
    public Result<List<Post>> getFavorites() {
        List<Post> posts = postFavoriteService.getFavoriteList();
        return Result.success(posts);
    }
    
    /**
     * 修改帖子
     */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody UpdatePostRequest request) {
        if (UserContext.getUserId() == null) {
            throw new UnauthorizedException("请先登录");
        }
        
        boolean success = postService.update(request);
        
        if (success) {
            return Result.success("修改成功", null);
        } else {
            return Result.fail(500, "修改失败");
        }
    }
    
    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (UserContext.getUserId() == null) {
            throw new UnauthorizedException("请先登录");
        }
        
        boolean success = postService.delete(id);
        
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.fail(500, "删除失败");
        }
    }
    
    /**
     * 我的花园
     */
    @GetMapping("/garden")
    public Result<List<Post>> garden() {
        List<Post> posts = postService.getGarden();
        return Result.success(posts);
    }
    
    /**
     * 广场
     */
    @GetMapping("/plaza")
    public Result<List<Post>> plaza() {
        List<Post> posts = postService.getPlaza();
        return Result.success(posts);
    }

    /**
     * 探索广场（分页查询公开帖子，按时间倒序）
     *
     * GET /api/post/explore?pageNum=1&pageSize=10
     */
    @GetMapping("/explore")
    public Result<PageResult<PostVO>> explore(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {

        PageResult<PostVO> pageResult = postService.explore(pageNum, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 关键字搜索（分页查询）
     *
     * GET /api/post/search?keyword=人工智能&pageNum=1&pageSize=10
     */
    @GetMapping("/search")
    public Result<PageResult<PostVO>> search(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {

        PageResult<PostVO> pageResult = postService.search(keyword, pageNum, pageSize);
        return Result.success(pageResult);
    }
}