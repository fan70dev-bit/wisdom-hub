package com.wisdomhub.controller;

import com.wisdomhub.dto.Result;
import com.wisdomhub.dto.UpdateProfileRequest;
import com.wisdomhub.entity.User;
import com.wisdomhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 更新用户资料（用户名、头像）
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        boolean success = userService.updateProfile(request);
        
        if (success) {
            return Result.success("资料更新成功", null);
        } else {
            return Result.fail(500, "资料更新失败");
        }
    }
    
    /**
     * 账号注销
     * POST /api/user/deactivate
     */
    @PostMapping("/deactivate")
    public Result<Void> deactivate() {
        boolean success = userService.deactivate();
        
        if (success) {
            return Result.success("账号已注销", null);
        } else {
            return Result.fail(500, "注销失败");
        }
    }
    
    /**
     * 根据账号 ID 查询用户（公开接口，用于查看作者信息）
     * GET /api/user/{accountId}
     */
    @GetMapping("/{accountId}")
    public Result<User> getByAccountId(@PathVariable String accountId) {
        User user = userService.getByAccountId(accountId);
        return Result.success(user);
    }
}