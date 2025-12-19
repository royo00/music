package com.music.controller;

import com.music.common.PageResult;
import com.music.common.Result;
import com.music.dto.MusicSearchDTO;
import com.music.service.MusicService;
import com.music.vo.MusicDetailVO;
import com.music.vo.MusicVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 音乐控制器 - 处理音乐查询、播放、收藏等请求
 */
@Tag(name = "音乐管理", description = "音乐查询、播放、收藏接口")
@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    /**
     * 音乐列表查询（分页）
     */
    @Operation(summary = "获取音乐列表")
    @GetMapping("/list")
    public Result<PageResult<MusicVO>> getMusicList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        PageResult<MusicVO> pageResult = musicService.getMusicList(page, size, status, userId);
        return Result.success(pageResult);
    }

    /**
     * 音乐搜索
     */
    @Operation(summary = "搜索音乐")
    @GetMapping("/search")
    public Result<PageResult<MusicVO>> searchMusic(
            MusicSearchDTO searchDTO,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        PageResult<MusicVO> pageResult = musicService.searchMusic(searchDTO, userId);
        return Result.success(pageResult);
    }

    /**
     * 获取音乐详情
     */
    @Operation(summary = "获取音乐详情")
    @GetMapping("/{id}")
    public Result<MusicDetailVO> getMusicDetail(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        MusicDetailVO musicDetail = musicService.getMusicDetail(id, userId);
        return Result.success(musicDetail);
    }

    /**
     * 获取音乐播放URL
     */
    @Operation(summary = "获取音乐播放地址")
    @GetMapping("/play/{musicId}")
    public Result<Map<String, Object>> getPlayUrl(
            @PathVariable Long musicId,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        Map<String, Object> playInfo = musicService.getMusicPlayUrl(musicId, userId);
        return Result.success(playInfo);
    }

    /**
     * 收藏音乐
     */
    @Operation(summary = "收藏音乐")
    @PostMapping("/favorite/{musicId}")
    public Result<String> favoriteMusic(
            @PathVariable Long musicId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        musicService.favoriteMusic(userId, musicId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorite/{musicId}")
    public Result<String> unfavoriteMusic(
            @PathVariable Long musicId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        musicService.unfavoriteMusic(userId, musicId);
        return Result.success("取消收藏成功");
    }

    /**
     * 我的收藏列表
     */
    @Operation(summary = "获取我的收藏")
    @GetMapping("/favorite/list")
    public Result<PageResult<MusicVO>> getFavoriteList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResult<MusicVO> pageResult = musicService.getFavoriteList(userId, page, size);
        return Result.success(pageResult);
    }

    /**
     * 播放历史列表
     */
    @Operation(summary = "获取播放历史")
    @GetMapping("/history")
    public Result<PageResult<MusicVO>> getPlayHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResult<MusicVO> pageResult = musicService.getPlayHistory(userId, page, size);
        return Result.success(pageResult);
    }

    /**
     * 获取热门音乐
     */
    @Operation(summary = "获取热门音乐")
    @GetMapping("/hot")
    public Result<PageResult<MusicVO>> getHotMusic(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        // 热门音乐按播放量排序，状态为已发布
        PageResult<MusicVO> pageResult = musicService.getMusicList(page, size, 1, userId);
        return Result.success(pageResult);
    }

    /**
     * 获取最新音乐
     */
    @Operation(summary = "获取最新音乐")
    @GetMapping("/latest")
    public Result<PageResult<MusicVO>> getLatestMusic(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        // 最新音乐按创建时间排序，状态为已发布
        PageResult<MusicVO> pageResult = musicService.getMusicList(page, size, 1, userId);
        return Result.success(pageResult);
    }
}
