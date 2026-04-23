package com.wisdomhub.controller;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.dto.Result;
import com.wisdomhub.dto.UpdatePostRequest;
import com.wisdomhub.entity.Post;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.service.PostService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章/碎碎念控制器
 */
@RestController
@RequestMapping("/api/post")
@CrossOrigin(origins = "*")
public class PostController {
    
    private final PostService postService;
    
    public PostController(PostService postService) {
        this.postService = postService;
    }
    
    /**
     * 创建文章/碎碎念
     * POST /api/post/create
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody Post post) {
        if (UserContext.getUserId() == null || !StringUtils.hasText(UserContext.getUserEmail())) {
            throw new UnauthorizedException("请先登录");
        }
        
        Long postId = postService.create(post);
        return Result.success("发布成功", postId);
    }
    
    /**
     * 根据 ID 查询帖子详情
     * GET /api/post/{id}
     */
    @GetMapping("/{id}")
    public Result<Post> getById(@PathVariable Long id) {
        Post post = postService.getById(id);
        return Result.success(post);
    }
    
    /**
     * 修改帖子
     * PUT /api/post/update
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
     * DELETE /api/post/{id}
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
     * GET /api/post/garden
     */
    @GetMapping("/garden")
    public Result<List<Post>> garden() {
        String email = UserContext.getUserEmail();
        
        if (!StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录查看我的花园");
        }
        
        List<Post> posts = postService.getGarden();
        return Result.success(posts);
    }
    
    /**
     * 广场
     * GET /api/post/plaza
     */
    @GetMapping("/plaza")
    public Result<List<Post>> plaza() {
        List<Post> posts = postService.getPlaza();
        return Result.success(posts);
    }
}