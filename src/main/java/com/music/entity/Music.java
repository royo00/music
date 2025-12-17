package com.music.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 音乐实体类
 * 对应数据库表: t_music
 */
@Data
public class Music {

    /**
     * 音乐ID
     */
    private Long id;

    /**
     * 音乐名称
     */
    private String musicName;

    /**
     * 艺术家/作者
     */
    private String artist;

    /**
     * 专辑名称
     */
    private String album;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 文件UUID
     */
    private String fileUuid;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 状态: 0-待审核, 1-已发布, 2-已下架
     */
    private Integer status;

    /**
     * 简介
     */
    private String description;

    /**
     * 播放次数
     */
    private Long playCount;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
