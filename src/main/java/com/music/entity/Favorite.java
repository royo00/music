package com.music.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 收藏实体类
 * 对应数据库表: t_favorite
 */
@Data
public class Favorite {
    /**
     * 收藏ID
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
     * 收藏时间
     */
    private LocalDateTime createTime;
}
