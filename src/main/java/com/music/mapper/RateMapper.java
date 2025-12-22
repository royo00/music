package com.music.mapper;

import com.music.vo.MusicStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RateMapper {

    // 1. 插入或更新评分 (利用 MySQL ON DUPLICATE KEY UPDATE)
    int insertOrUpdateRating(@Param("userId") Long userId,
                             @Param("musicId") Long musicId,
                             @Param("score") Integer score);

    // 2, 3, 4. 聚合查询：播放量、收藏量、评分数据
    MusicStatsVO selectMusicStats(@Param("musicId") Long musicId);

    // 5. 查询艺人综合评分 (所有歌曲评分平均值)
    // 假设 t_music.upload_user_id 是艺人ID
    BigDecimal selectArtistAverageScore(@Param("artistId") Long artistId);

    // 6. 查询艺人名下所有歌曲的评分情况
    List<MusicStatsVO> selectMusicStatsByArtist(@Param("artistId") Long artistId);
}
