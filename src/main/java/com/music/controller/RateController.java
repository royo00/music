package com.music.controller;

import com.music.common.Result;
import com.music.dto.RateDTO;
import com.music.service.RateService;
import com.music.util.JwtUtil;
import com.music.vo.MusicStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/rate")
public class RateController {

    private final JwtUtil jwtUtil;
    @Autowired
    private RateService rateService;

    public RateController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 1. 用户评分
    @PostMapping
    public Result<String> addRating(@RequestBody RateDTO rateDTO,
                                    @RequestHeader("Authorization") String authorization) {
        Long userId = jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
        rateService.rateMusic(userId, rateDTO);
        return Result.success("评分成功");
    }

    // 2,3,4. 获取单曲综合数据 (播放量、收藏量、评分)
    @GetMapping("/music/{musicId}")
    public Result<MusicStatsVO> getMusicStats(@PathVariable Long musicId) {
        return Result.success(rateService.getMusicStats(musicId));
    }

    // 5. 获取艺人综合评分
    @GetMapping("/artist/{artistId}/average")
    public Result<BigDecimal> getArtistAverage(@PathVariable Long artistId) {
        return Result.success(rateService.getArtistAverageScore(artistId));
    }

    // 6. 获取艺人所有歌曲评分列表
    @GetMapping("/artist/{artistId}/list")
    public Result<List<MusicStatsVO>> getArtistMusicStats(@PathVariable Long artistId) {
        return Result.success(rateService.getArtistMusicStats(artistId));
    }
}
