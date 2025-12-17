package com.music.mapper;

import com.music.entity.Music;
import com.music.entity.PlayHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 播放历史Mapper接口
 */
@Mapper
public interface PlayHistoryMapper {

    /**
     * 插入播放历史
     *
     * @param playHistory 播放历史对象
     * @return 影响行数
     */
    int insert(PlayHistory playHistory);

    /**
     * 根据音乐ID删除播放历史
     *
     * @param musicId 音乐ID
     * @return 影响行数
     */
    int deleteByMusicId(@Param("musicId") Long musicId);

    /**
     * 根据用户ID删除播放历史
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询播放历史的音乐列表（按播放时间倒序）
     *
     * @param userId 用户ID
     * @return 音乐列表
     */
    List<Music> findMusicByUserId(@Param("userId") Long userId);

    /**
     * 统计用户播放次数
     *
     * @param userId 用户ID
     * @return 播放次数
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计音乐播放次数
     *
     * @param musicId 音乐ID
     * @return 播放次数
     */
    Long countByMusicId(@Param("musicId") Long musicId);

    /**
     * 清理指定天数之前的播放历史
     *
     * @param days 天数
     * @return 影响行数
     */
    int cleanOldHistory(@Param("days") Integer days);
}
