package com.music.mapper;

import com.music.entity.Favorite;
import com.music.entity.Music;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏Mapper接口
 */
@Mapper
public interface FavoriteMapper {

    /**
     * 插入收藏
     *
     * @param favorite 收藏对象
     * @return 影响行数
     */
    int insert(Favorite favorite);

    /**
     * 根据用户ID和音乐ID删除收藏
     *
     * @param userId  用户ID
     * @param musicId 音乐ID
     * @return 影响行数
     */
    int deleteByUserIdAndMusicId(@Param("userId") Long userId, @Param("musicId") Long musicId);

    /**
     * 根据音乐ID删除所有收藏
     *
     * @param musicId 音乐ID
     * @return 影响行数
     */
    int deleteByMusicId(@Param("musicId") Long musicId);

    /**
     * 根据用户ID和音乐ID查询收藏
     *
     * @param userId  用户ID
     * @param musicId 音乐ID
     * @return 收藏对象
     */
    Favorite findByUserIdAndMusicId(@Param("userId") Long userId, @Param("musicId") Long musicId);

    /**
     * 根据用户ID查询收藏的音乐列表
     *
     * @param userId 用户ID
     * @return 音乐列表
     */
    List<Music> findMusicByUserId(@Param("userId") Long userId);

    /**
     * 统计用户收藏数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计音乐被收藏数量
     *
     * @param musicId 音乐ID
     * @return 收藏数量
     */
    Long countByMusicId(@Param("musicId") Long musicId);
}
