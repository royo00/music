package com.music.controller;

import com.music.common.PageResult;
import com.music.common.Result;
import com.music.service.MusicService;
import com.music.service.UserService;
import com.music.vo.MusicDetailVO;
import com.music.vo.MusicVO;
import com.music.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器 - 处理用户管理、音乐审核等管理功能
 */
@Tag(name = "管理员管理", description = "用户管理、音乐审核接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final MusicService musicService;

    // ==================== 用户管理 ====================

    /**
     * 用户列表查询
     * 注意：UserService 中需要添加 getUserList 方法
     */
    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        // TODO: 需要在 UserService 中实现此方法
        // PageResult<UserVO> pageResult = userService.getUserList(page, size, role, status, keyword);
        return Result.error("功能开发中");
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/user/{userId}")
    public Result<UserVO> getUserDetail(@PathVariable Long userId) {
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 启用/禁用用户
     * 注意：UserService 中需要添加 updateUserStatus 方法
     */
    @Operation(summary = "修改用户状态")
    @PutMapping("/user/{userId}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        // TODO: 需要在 UserService 中实现此方法
        // userService.updateUserStatus(userId, status, remark);
        String message = status == 1 ? "用户已启用" : "用户已禁用";
        return Result.error("功能开发中");
    }

    // ==================== 音乐管理 ====================

    /**
     * 音乐列表查询（管理端）
     */
    @Operation(summary = "获取音乐列表")
    @GetMapping("/music")
    public Result<PageResult<MusicVO>> getMusicList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        // 管理员可以查看所有状态的音乐
        PageResult<MusicVO> pageResult = musicService.getMusicList(page, size, status, null);
        return Result.success(pageResult);
    }

    /**
     * 获取音乐详情
     */
    @Operation(summary = "获取音乐详情")
    @GetMapping("/music/{musicId}")
    public Result<MusicDetailVO> getMusicDetail(@PathVariable Long musicId) {
        MusicDetailVO musicDetail = musicService.getMusicDetail(musicId, null);
        return Result.success(musicDetail);
    }

    /**
     * 审核音乐
     */
    @Operation(summary = "审核音乐")
    @PutMapping("/music/{musicId}/status")
    public Result<String> reviewMusic(
            @PathVariable Long musicId,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        musicService.updateMusicStatus(musicId, status, remark);
        String message = status == 1 ? "审核通过" : "审核不通过";
        return Result.success(message);
    }

    /**
     * 删除音乐
     */
    @Operation(summary = "删除音乐")
    @DeleteMapping("/music/{musicId}")
    public Result<String> deleteMusic(@PathVariable Long musicId) {
        // 管理员删除，传入 null 作为 userId（在 Service 中会检查权限）
        // 需要修改 Service 方法以支持管理员操作
        musicService.updateMusicStatus(musicId, 2, "管理员删除");
        return Result.success("音乐已删除");
    }

    /**
     * 下架音乐
     */
    @Operation(summary = "下架音乐")
    @PutMapping("/music/{musicId}/offline")
    public Result<String> offlineMusic(
            @PathVariable Long musicId,
            @RequestParam(required = false) String reason) {
        musicService.updateMusicStatus(musicId, 2, reason);
        return Result.success("音乐已下架");
    }
}
