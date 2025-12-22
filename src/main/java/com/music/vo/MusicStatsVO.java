package com.music.vo;

import lombok.Data;

@Data
public class MusicStatsVO {
    private Long musicId;
    private Long playCount;      // 播放量 (来自 t_music)
    private Long favoriteCount;  // 收藏量 (来自 t_favorite)
    private Double avgScore;     // 平均评分
    private Integer totalRatings;// 评分人数
}
