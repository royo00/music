package com.music.controller;

import com.music.common.Result;
import com.music.dto.UserUpdateDTO;
import com.music.service.UserService;
import com.music.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 - 处理用户信息相关请求
 */
@Tag(name = "用户管理", description = "用户信息查询、修改接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public Result<UserVO> updateUser(
            @Valid @RequestBody UserUpdateDTO updateDTO,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        UserVO userVO = userService.updateUserInfo(userId, updateDTO);
        return Result.success("更新成功", userVO);
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<String> changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 根据ID获取用户信息（公开信息）
     */
    @Operation(summary = "获取用户公开信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }
}
