package com.music.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 音乐上传DTO
 */
@Data
public class MusicUploadDTO {
    @NotNull(message = "音乐文件不能为空")
    private MultipartFile file;

    @NotBlank(message = "音乐名称不能为空")
    private String musicName;

    @NotBlank(message = "艺术家不能为空")
    private String artist;

    private String album;
    private String description;
    private MultipartFile cover;
}
