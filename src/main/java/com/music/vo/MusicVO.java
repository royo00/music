package com.music.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 音乐视图对象
 */
@Data
public class MusicVO {
    private Long id;
    private String musicName;
    private String artist;
    private String album;
    private Integer duration;
    private String coverUrl;
    private Integer status;
    private String description;
    private Long playCount;
    private Long uploadUserId;
    private String uploadUsername;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 是否已收藏（用于前端显示）
    private Boolean isFavorite;
}
