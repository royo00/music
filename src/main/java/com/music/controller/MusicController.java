package com.music.controller;

import com.music.common.PageResult;
import com.music.common.Result;
import com.music.dto.MusicSearchDTO;
import com.music.service.MusicService;
import com.music.util.JwtUtil;
import com.music.vo.MusicDetailVO;
import com.music.vo.MusicVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/music")
public class MusicController {

    private final MusicService musicService;
    private final JwtUtil jwtUtil;

    public MusicController(MusicService musicService, JwtUtil jwtUtil) {
        this.musicService = musicService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 音乐列表（游客 / 登录用户）
     */
    @GetMapping("/list")
    public Result<PageResult<MusicVO>> list(@RequestParam Integer page,
                                            @RequestParam Integer size,
                                            @RequestParam(required = false) Integer status,
                                            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = extractUserId(authorization);
        return Result.success(musicService.getMusicList(page, size, status, userId));
    }

    /**
     * 音乐搜索
     */
    @PostMapping("/search")
    public Result<PageResult<MusicVO>> search(@RequestBody MusicSearchDTO dto,
                                              @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = extractUserId(authorization);
        return Result.success(musicService.searchMusic(dto, userId));
    }

    /**
     * 音乐详情
     */
    @GetMapping("/detail/{musicId}")
    public Result<MusicDetailVO> detail(@PathVariable Long musicId,
                                        @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = extractUserId(authorization);
        return Result.success(musicService.getMusicDetail(musicId, userId));
    }

    /**
     * 获取播放 URL
     */
    @GetMapping("/play/{musicId}")
    public Result<Map<String, Object>> play(@PathVariable Long musicId,
                                            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = extractUserId(authorization);
        return Result.success(musicService.getMusicPlayUrl(musicId, userId));
    }

    /**
     * 收藏音乐（登录用户）
     */
    @PostMapping("/favorite/{musicId}")
    public Result<Void> favorite(@PathVariable Long musicId,
                                 @RequestHeader("Authorization") String authorization) {

        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        musicService.favoriteMusic(userId, musicId);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/favorite/{musicId}")
    public Result<Void> unfavorite(@PathVariable Long musicId,
                                   @RequestHeader("Authorization") String authorization) {

        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        musicService.unfavoriteMusic(userId, musicId);
        return Result.success();
    }

    /**
     * 收藏列表
     */
    @GetMapping("/favorite/list")
    public Result<PageResult<MusicVO>> favoriteList(@RequestParam Integer page,
                                                    @RequestParam Integer size,
                                                    @RequestHeader("Authorization") String authorization) {

        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        return Result.success(musicService.getFavoriteList(userId, page, size));
    }

    /**
     * 播放历史
     */
    @GetMapping("/history")
    public Result<PageResult<MusicVO>> history(@RequestParam Integer page,
                                               @RequestParam Integer size,
                                               @RequestHeader("Authorization") String authorization) {

        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        return Result.success(musicService.getPlayHistory(userId, page, size));
    }

    /**
     * 工具方法：游客返回 null
     */
    private Long extractUserId(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
    }
}
