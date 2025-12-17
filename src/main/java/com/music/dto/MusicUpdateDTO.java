package com.music.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 音乐更新DTO
 */
@Data
public class MusicUpdateDTO {
    private String musicName;
    private String artist;
    private String album;
    private String description;
    private MultipartFile cover;
}
