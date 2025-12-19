package com.music.controller;

import com.music.common.PageResult;
import com.music.common.Result;
import com.music.dto.MusicUpdateDTO;
import com.music.dto.MusicUploadDTO;
import com.music.service.MusicService;
import com.music.vo.MusicDetailVO;
import com.music.vo.MusicVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 艺人控制器 - 处理艺人作品上传、管理等请求
 */
@Tag(name = "艺人管理", description = "艺人作品上传、管理接口")
@RestController
@RequestMapping("/actor")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ACTOR', 'ADMIN')")
public class ActorController {

    private final MusicService musicService;

    /**
     * 上传音乐
     */
    @Operation(summary = "上传音乐")
    @PostMapping("/upload")
    public Result<Long> uploadMusic(
            @Valid @ModelAttribute MusicUploadDTO uploadDTO,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Long musicId = musicService.uploadMusic(uploadDTO, userId);
        return Result.success("上传成功，等待审核", musicId);
    }

    /**
     * 我的作品列表
     */
    @Operation(summary = "获取我的作品列表")
    @GetMapping("/music")
    public Result<PageResult<MusicVO>> getMyMusicList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PageResult<MusicVO> pageResult = musicService.getUserMusicList(userId, page, size, status);
        return Result.success(pageResult);
    }

    /**
     * 获取作品详情
     */
    @Operation(summary = "获取作品详情")
    @GetMapping("/music/{musicId}")
    public Result<MusicDetailVO> getMusicDetail(
            @PathVariable Long musicId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        MusicDetailVO musicDetail = musicService.getMusicDetail(musicId, userId);
        return Result.success(musicDetail);
    }

    /**
     * 编辑作品
     */
    @Operation(summary = "编辑作品信息")
    @PutMapping("/music/{musicId}")
    public Result<String> updateMusic(
            @PathVariable Long musicId,
            @Valid @RequestBody MusicUpdateDTO updateDTO,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        musicService.updateMusic(musicId, updateDTO, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除作品
     */
    @Operation(summary = "删除作品")
    @DeleteMapping("/music/{musicId}")
    public Result<String> deleteMusic(
            @PathVariable Long musicId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        musicService.deleteMusic(musicId, userId);
        return Result.success("删除成功");
    }
}
