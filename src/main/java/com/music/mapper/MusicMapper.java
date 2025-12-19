package com.music.mapper;

import com.music.entity.Music;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 音乐Mapper接口
 */
@Mapper
public interface MusicMapper {

    /**
     * 插入音乐
     *
     * @param music 音乐对象
     * @return 影响行数
     */
    int insert(Music music);

    /**
     * 更新音乐
     *
     * @param music 音乐对象
     * @return 影响行数
     */
    int update(Music music);

    /**
     * 根据ID删除音乐
     *
     * @param id 音乐ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询音乐
     *
     * @param id 音乐ID
     * @return 音乐对象
     */
    Music findById(@Param("id") Long id);

    /**
     * 根据状态查询音乐列表
     *
     * @param status 状态（null表示查询所有）
     * @return 音乐列表
     */
    List<Music> findByStatus(@Param("status") Integer status);

    /**
     * 搜索音乐
     *
     * @param keyword   关键词
     * @param musicName 音乐名
     * @param artist    艺术家
     * @param album     专辑
     * @param status    状态
     * @return 音乐列表
     */
    List<Music> searchMusic(@Param("keyword") String keyword,
                            @Param("musicName") String musicName,
                            @Param("artist") String artist,
                            @Param("album") String album,
                            @Param("status") Integer status);

    /**
     * 根据上传用户ID查询音乐列表
     *
     * @param uploadUserId 上传用户ID
     * @param status       状态筛选
     * @return 音乐列表
     */
    List<Music> findByUploadUserId(@Param("uploadUserId") Long uploadUserId,
                                   @Param("status") Integer status);

    /**
     * 管理员查询音乐列表
     *
     * @param status       状态筛选
     * @return 音乐列表
     */
    List<Music> findByAdmin(@Param("status") Integer status);
    /**
     * 更新播放次数
     *
     * @param id        音乐ID
     * @param playCount 播放次数
     * @return 影响行数
     */
    int updatePlayCount(@Param("id") Long id, @Param("playCount") Long playCount);

    /**
     * 统计音乐数量
     *
     * @param status 状态筛选
     * @return 音乐数量
     */
    Long countMusic(@Param("status") Integer status);
}
