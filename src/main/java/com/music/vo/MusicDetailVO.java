package com.music.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 音乐详情视图对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MusicDetailVO extends MusicVO {
    private String fileUuid;
    private Long fileSize;
}
