package com.music.dto;

import lombok.Data;

@Data
public class RateDTO {
    private Long musicId;
    private Integer score; // 1-5
}
