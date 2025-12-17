package com.music.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 播放历史实体类
 * 对应数据库表: t_play_history
 */
@Data
public class PlayHistory {
    /**
     * 历史ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 音乐ID
     */
    private Long musicId;

    /**
     * 播放时间
     */
    private LocalDateTime playTime;
}
