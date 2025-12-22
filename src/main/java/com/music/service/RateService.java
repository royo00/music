package com.music.service;

import com.music.dto.RateDTO;
import com.music.mapper.RateMapper;
import com.music.vo.MusicStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RateService {

    @Autowired
    private RateMapper rateMapper;

    /**
     * 用户打分
     */
    @Transactional
    public void rateMusic(Long userId, RateDTO rateDTO) {
        // 这里可以加逻辑判断，比如必须播放过才能评分，或者必须登录等
        rateMapper.insertOrUpdateRating(userId, rateDTO.getMusicId(), rateDTO.getScore());
    }

    /**
     * 获取单曲综合数据（播放、收藏、评分）
     */
    public MusicStatsVO getMusicStats(Long musicId) {
        return rateMapper.selectMusicStats(musicId);
    }

    /**
     * 获取艺人综合评分
     */
    public BigDecimal getArtistAverageScore(Long artistId) {
        return rateMapper.selectArtistAverageScore(artistId);
    }

    /**
     * 获取艺人所有歌曲的评分详情
     */
    public List<MusicStatsVO> getArtistMusicStats(Long artistId) {
        return rateMapper.selectMusicStatsByArtist(artistId);
    }
}
