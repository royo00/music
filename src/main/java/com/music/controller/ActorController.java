package com.music.controller;

import com.music.common.PageResult;
import com.music.common.Result;
import com.music.dto.MusicUpdateDTO;
import com.music.dto.MusicUploadDTO;
import com.music.service.MusicService;
import com.music.util.JwtUtil;
import com.music.vo.MusicVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actor")
public class ActorController {

    private final MusicService musicService;
    private final JwtUtil jwtUtil;

    public ActorController(MusicService musicService, JwtUtil jwtUtil) {
        this.musicService = musicService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 上传音乐（艺人 / 管理员）
     */
    @PostMapping("/upload")
    public Result<Long> upload(@ModelAttribute MusicUploadDTO dto,
                               @RequestHeader("Authorization") String authorization) {

        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        return Result.success(musicService.uploadMusic(dto, userId));
    }

    /**
     * 获取自己上传的音乐
     */
    @GetMapping("/music")
    public Result<PageResult<MusicVO>> myMusic(@RequestParam Integer page,
                                               @RequestParam Integer size,
                                               @RequestParam(required = false) Integer status,
                                               @RequestHeader("Authorization") String authorization) {

        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        return Result.success(musicService.getUserMusicList(userId, page, size, status));
    }

    /**
     * 更新音乐信息
     */
    @PutMapping("/{musicId}")
    public Result<Void> update(@PathVariable Long musicId,
                               @RequestBody MusicUpdateDTO dto,
                               @RequestHeader("Authorization") String authorization) {

        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        musicService.updateMusic(musicId, dto, userId);
        return Result.success();
    }

    /**
     * 删除音乐
     */
    @DeleteMapping("/{musicId}")
    public Result<Void> delete(@PathVariable Long musicId,
                               @RequestHeader("Authorization") String authorization) {

        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        musicService.deleteMusic(musicId, userId);
        return Result.success();
    }
}
