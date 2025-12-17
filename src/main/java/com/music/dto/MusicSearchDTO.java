package com.music.dto;

import lombok.Data;

/**
 * 音乐搜索DTO
 */
@Data
public class MusicSearchDTO {
    private String keyword;
    private String musicName;
    private String artist;
    private String album;
    private Integer status;
    private Integer page = 1;
    private Integer size = 20;
}
