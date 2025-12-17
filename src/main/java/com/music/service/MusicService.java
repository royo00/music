package com.music.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.music.common.PageResult;
import com.music.dto.MusicSearchDTO;
import com.music.dto.MusicUpdateDTO;
import com.music.dto.MusicUploadDTO;
import com.music.entity.Favorite;
import com.music.entity.Music;
import com.music.entity.PlayHistory;
import com.music.entity.User;
import com.music.exception.BusinessException;
import com.music.mapper.FavoriteMapper;
import com.music.mapper.MusicMapper;
import com.music.mapper.PlayHistoryMapper;
import com.music.mapper.UserMapper;
import com.music.vo.MusicDetailVO;
import com.music.vo.MusicVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 音乐服务类
 */
@Slf4j
@Service
public class MusicService {

    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private PlayHistoryMapper playHistoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String MUSIC_CACHE_PREFIX = "music:info:";
    private static final String MUSIC_PLAY_COUNT_PREFIX = "music:play:count:";
    private static final long MUSIC_CACHE_EXPIRE = 60; // 60分钟

    /**
     * 上传音乐
     *
     * @param uploadDTO 上传信息
     * @param userId    上传用户ID
     * @return 音乐ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long uploadMusic(MusicUploadDTO uploadDTO, Long userId) {
        // 1. 验证用户权限（只有actor和admin可以上传）
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!"actor".equals(user.getRole()) && !"admin".equals(user.getRole())) {
            throw new BusinessException("只有艺人和管理员可以上传音乐");
        }

        // 2. 上传音乐文件
        FileService.FileInfo musicFileInfo = fileService.uploadMusicFile(uploadDTO.getFile());

        // 3. 上传封面图片（如果有）
        String coverUrl = null;
        if (uploadDTO.getCover() != null && !uploadDTO.getCover().isEmpty()) {
            FileService.FileInfo coverFileInfo = fileService.uploadCoverImage(uploadDTO.getCover());
            coverUrl = coverFileInfo.getFileUrl();
        }

        // 4. 获取音乐时长（这里简化处理，实际应该解析音频文件获取）
        Integer duration = calculateDuration(uploadDTO.getFile());

        // 5. 创建音乐对象
        Music music = new Music();
        music.setMusicName(uploadDTO.getMusicName());
        music.setArtist(uploadDTO.getArtist());
        music.setAlbum(uploadDTO.getAlbum());
        music.setDuration(duration);
        music.setFileUuid(musicFileInfo.getFileUuid());
        music.setFilePath(musicFileInfo.getFilePath());
        music.setFileSize(musicFileInfo.getFileSize());
        music.setCoverUrl(coverUrl);
        music.setDescription(uploadDTO.getDescription());
        music.setStatus(0); // 待审核
        music.setPlayCount(0L);
        music.setUploadUserId(userId);
        music.setCreateTime(LocalDateTime.now());
        music.setUpdateTime(LocalDateTime.now());

        // 6. 保存到数据库
        int rows = musicMapper.insert(music);
        if (rows == 0) {
            // 上传失败，删除已上传的文件
            fileService.deleteFile(musicFileInfo.getFilePath());
            if (coverUrl != null) {
                fileService.deleteFile(coverUrl);
            }
            throw new BusinessException("音乐上传失败");
        }

        log.info("音乐上传成功: musicId={}, musicName={}, userId={}",
                music.getId(), music.getMusicName(), userId);
        return music.getId();
    }

    /**
     * 音乐列表查询（分页）
     *
     * @param page   页码
     * @param size   每页数量
     * @param status 状态筛选
     * @param userId 当前用户ID（用于判断是否收藏）
     * @return 分页结果
     */
    public PageResult<MusicVO> getMusicList(Integer page, Integer size, Integer status, Long userId) {
        PageHelper.startPage(page, size);

        List<Music> musicList = musicMapper.findByStatus(status);
        PageInfo<Music> pageInfo = new PageInfo<>(musicList);

        // 转换为VO
        List<MusicVO> voList = musicList.stream()
                .map(music -> convertToVO(music, userId))
                .collect(Collectors.toList());

        return PageResult.of(pageInfo.getTotal(), page, size, voList);
    }

    /**
     * 音乐搜索
     *
     * @param searchDTO 搜索条件
     * @param userId    当前用户ID
     * @return 分页结果
     */
    public PageResult<MusicVO> searchMusic(MusicSearchDTO searchDTO, Long userId) {
        PageHelper.startPage(searchDTO.getPage(), searchDTO.getSize());

        List<Music> musicList = musicMapper.searchMusic(
                searchDTO.getKeyword(),
                searchDTO.getMusicName(),
                searchDTO.getArtist(),
                searchDTO.getAlbum(),
                searchDTO.getStatus()
        );

        PageInfo<Music> pageInfo = new PageInfo<>(musicList);

        // 转换为VO
        List<MusicVO> voList = musicList.stream()
                .map(music -> convertToVO(music, userId))
                .collect(Collectors.toList());

        return PageResult.of(pageInfo.getTotal(), searchDTO.getPage(), searchDTO.getSize(), voList);
    }

    /**
     * 获取音乐详情
     *
     * @param musicId 音乐ID
     * @param userId  当前用户ID
     * @return 音乐详情
     */
    public MusicDetailVO getMusicDetail(Long musicId, Long userId) {
        // 1. 先从缓存获取
        String cacheKey = MUSIC_CACHE_PREFIX + musicId;
        MusicDetailVO cachedMusic = (MusicDetailVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedMusic != null) {
            // 设置是否收藏
            if (userId != null) {
                cachedMusic.setIsFavorite(isFavorite(userId, musicId));
            }
            return cachedMusic;
        }

        // 2. 从数据库查询
        Music music = musicMapper.findById(musicId);
        if (music == null) {
            throw new BusinessException("音乐不存在");
        }

        // 3. 转换为VO并缓存
        MusicDetailVO detailVO = convertToDetailVO(music, userId);
        redisTemplate.opsForValue().set(cacheKey, detailVO, MUSIC_CACHE_EXPIRE, TimeUnit.MINUTES);

        return detailVO;
    }

    /**
     * 获取音乐播放URL
     *
     * @param musicId 音乐ID
     * @param userId  当前用户ID
     * @return 播放URL和相关信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getMusicPlayUrl(Long musicId, Long userId) {
        // 1. 查询音乐
        Music music = musicMapper.findById(musicId);
        if (music == null) {
            throw new BusinessException("音乐不存在");
        }

        // 2. 检查音乐状态
        if (music.getStatus() != 1) {
            throw new BusinessException("音乐未发布，无法播放");
        }

        // 3. 记录播放历史
        if (userId != null) {
            recordPlayHistory(userId, musicId);
        }

        // 4. 增加播放次数（使用Redis计数，定时同步到数据库）
        incrementPlayCount(musicId);

        // 5. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("playUrl", fileService.getFileUrl(music.getFilePath()));
        result.put("duration", music.getDuration());
        result.put("musicName", music.getMusicName());
        result.put("artist", music.getArtist());
        result.put("coverUrl", music.getCoverUrl());

        return result;
    }

    /**
     * 收藏音乐
     *
     * @param userId  用户ID
     * @param musicId 音乐ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void favoriteMusic(Long userId, Long musicId) {
        // 1. 检查音乐是否存在
        Music music = musicMapper.findById(musicId);
        if (music == null) {
            throw new BusinessException("音乐不存在");
        }

        // 2. 检查是否已收藏
        Favorite existFavorite = favoriteMapper.findByUserIdAndMusicId(userId, musicId);
        if (existFavorite != null) {
            throw new BusinessException("已经收藏过该音乐");
        }

        // 3. 添加收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setMusicId(musicId);
        favorite.setCreateTime(LocalDateTime.now());

        int rows = favoriteMapper.insert(favorite);
        if (rows == 0) {
            throw new BusinessException("收藏失败");
        }

        log.info("用户收藏音乐: userId={}, musicId={}", userId, musicId);
    }

    /**
     * 取消收藏
     *
     * @param userId  用户ID
     * @param musicId 音乐ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteMusic(Long userId, Long musicId) {
        int rows = favoriteMapper.deleteByUserIdAndMusicId(userId, musicId);
        if (rows == 0) {
            throw new BusinessException("取消收藏失败，可能未收藏该音乐");
        }

        log.info("用户取消收藏音乐: userId={}, musicId={}", userId, musicId);
    }

    /**
     * 获取用户收藏列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 分页结果
     */
    public PageResult<MusicVO> getFavoriteList(Long userId, Integer page, Integer size) {
        PageHelper.startPage(page, size);

        List<Music> musicList = favoriteMapper.findMusicByUserId(userId);
        PageInfo<Music> pageInfo = new PageInfo<>(musicList);

        // 转换为VO（收藏列表中都是已收藏的）
        List<MusicVO> voList = musicList.stream()
                .map(music -> {
                    MusicVO vo = convertToVO(music, userId);
                    vo.setIsFavorite(true);
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.of(pageInfo.getTotal(), page, size, voList);
    }

    /**
     * 获取用户上传的音乐列表（艺人作品）
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @param status 状态筛选
     * @return 分页结果
     */
    public PageResult<MusicVO> getUserMusicList(Long userId, Integer page, Integer size, Integer status) {
        PageHelper.startPage(page, size);

        List<Music> musicList = musicMapper.findByUploadUserId(userId, status);
        PageInfo<Music> pageInfo = new PageInfo<>(musicList);

        // 转换为VO
        List<MusicVO> voList = musicList.stream()
                .map(music -> convertToVO(music, userId))
                .collect(Collectors.toList());

        return PageResult.of(pageInfo.getTotal(), page, size, voList);
    }

    /**
     * 更新音乐信息
     *
     * @param musicId   音乐ID
     * @param updateDTO 更新信息
     * @param userId    操作用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMusic(Long musicId, MusicUpdateDTO updateDTO, Long userId) {
        // 1. 查询音乐
        Music music = musicMapper.findById(musicId);
        if (music == null) {
            throw new BusinessException("音乐不存在");
        }

        // 2. 权限检查（只能修改自己上传的音乐，或管理员可以修改所有）
        User user = userService.findById(userId);
        if (!music.getUploadUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权修改该音乐");
        }

        // 3. 更新信息
        if (updateDTO.getMusicName() != null && !updateDTO.getMusicName().isEmpty()) {
            music.setMusicName(updateDTO.getMusicName());
        }
        if (updateDTO.getArtist() != null && !updateDTO.getArtist().isEmpty()) {
            music.setArtist(updateDTO.getArtist());
        }
        if (updateDTO.getAlbum() != null) {
            music.setAlbum(updateDTO.getAlbum());
        }
        if (updateDTO.getDescription() != null) {
            music.setDescription(updateDTO.getDescription());
        }

        // 4. 更新封面（如果有）
        if (updateDTO.getCover() != null && !updateDTO.getCover().isEmpty()) {
            // 删除旧封面
            if (music.getCoverUrl() != null) {
                fileService.deleteFile(music.getCoverUrl());
            }
            // 上传新封面
            FileService.FileInfo coverFileInfo = fileService.uploadCoverImage(updateDTO.getCover());
            music.setCoverUrl(coverFileInfo.getFileUrl());
        }

        music.setUpdateTime(LocalDateTime.now());

        // 5. 保存更新
        int rows = musicMapper.update(music);
        if (rows == 0) {
            throw new BusinessException("更新失败");
        }

        // 6. 清除缓存
        clearMusicCache(musicId);

        log.info("音乐信息更新成功: musicId={}, userId={}", musicId, userId);
    }

    /**
     * 删除音乐
     *
     * @param musicId 音乐ID
     * @param userId  操作用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMusic(Long musicId, Long userId) {
        // 1. 查询音乐
        Music music = musicMapper.findById(musicId);
        if (music == null) {
            throw new BusinessException("音乐不存在");
        }

        // 2. 权限检查
        User user = userService.findById(userId);
        if (!music.getUploadUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权删除该音乐");
        }

        // 3. 删除文件
        fileService.deleteFile(music.getFilePath());
        if (music.getCoverUrl() != null) {
            fileService.deleteFile(music.getCoverUrl());
        }

        // 4. 删除数据库记录
        int rows = musicMapper.deleteById(musicId);
        if (rows == 0) {
            throw new BusinessException("删除失败");
        }

        // 5. 删除相关的收藏和播放历史
        favoriteMapper.deleteByMusicId(musicId);
        playHistoryMapper.deleteByMusicId(musicId);

        // 6. 清除缓存
        clearMusicCache(musicId);

        log.info("音乐删除成功: musicId={}, userId={}", musicId, userId);
    }

    /**
     * 更新音乐状态（管理员审核）
     *
     * @param musicId 音乐ID
     * @param status  状态
     * @param remark  备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMusicStatus(Long musicId, Integer status, String remark) {
        Music music = musicMapper.findById(musicId);
        if (music == null) {
            throw new BusinessException("音乐不存在");
        }

        music.setStatus(status);
        music.setRemark(remark);
        music.setUpdateTime(LocalDateTime.now());

        int rows = musicMapper.update(music);
        if (rows == 0) {
            throw new BusinessException("状态更新失败");
        }

        // 清除缓存
        clearMusicCache(musicId);

        log.info("音乐状态更新: musicId={}, status={}", musicId, status);
    }

    /**
     * 获取播放历史
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 分页结果
     */
    public PageResult<MusicVO> getPlayHistory(Long userId, Integer page, Integer size) {
        PageHelper.startPage(page, size);

        List<Music> musicList = playHistoryMapper.findMusicByUserId(userId);
        PageInfo<Music> pageInfo = new PageInfo<>(musicList);

        // 转换为VO
        List<MusicVO> voList = musicList.stream()
                .map(music -> convertToVO(music, userId))
                .collect(Collectors.toList());

        return PageResult.of(pageInfo.getTotal(), page, size, voList);
    }

    /**
     * 记录播放历史
     *
     * @param userId  用户ID
     * @param musicId 音乐ID
     */
    private void recordPlayHistory(Long userId, Long musicId) {
        PlayHistory playHistory = new PlayHistory();
        playHistory.setUserId(userId);
        playHistory.setMusicId(musicId);
        playHistory.setPlayTime(LocalDateTime.now());

        playHistoryMapper.insert(playHistory);
    }

    /**
     * 增加播放次数（Redis计数）
     *
     * @param musicId 音乐ID
     */
    private void incrementPlayCount(Long musicId) {
        String key = MUSIC_PLAY_COUNT_PREFIX + musicId;
        redisTemplate.opsForValue().increment(key, 1);

        // 设置过期时间（1天后同步到数据库）
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    /**
     * 判断是否已收藏
     *
     * @param userId  用户ID
     * @param musicId 音乐ID
     * @return 是否已收藏
     */
    private boolean isFavorite(Long userId, Long musicId) {
        if (userId == null) {
            return false;
        }
        Favorite favorite = favoriteMapper.findByUserIdAndMusicId(userId, musicId);
        return favorite != null;
    }

    /**
     * 计算音乐时长（简化实现，实际应解析音频文件）
     *
     * @param file 音乐文件
     * @return 时长（秒）
     */
    private Integer calculateDuration(org.springframework.web.multipart.MultipartFile file) {
        // TODO: 实际项目中应使用音频解析库（如 Jaudiotagger）获取真实时长
        // 这里返回默认值
        return 180; // 默认3分钟
    }

    /**
     * 转换为VO对象
     *
     * @param music  音乐实体
     * @param userId 当前用户ID
     * @return 音乐VO
     */
    private MusicVO convertToVO(Music music, Long userId) {
        MusicVO vo = new MusicVO();
        BeanUtils.copyProperties(music, vo);

        // 设置上传用户名
        if (music.getUploadUserId() != null) {
            User user = userMapper.findById(music.getUploadUserId());
            if (user != null) {
                vo.setUploadUsername(user.getUsername());
            }
        }

        // 设置是否收藏
        if (userId != null) {
            vo.setIsFavorite(isFavorite(userId, music.getId()));
        }

        return vo;
    }

    /**
     * 转换为详情VO对象
     *
     * @param music  音乐实体
     * @param userId 当前用户ID
     * @return 音乐详情VO
     */
    private MusicDetailVO convertToDetailVO(Music music, Long userId) {
        MusicDetailVO vo = new MusicDetailVO();
        BeanUtils.copyProperties(music, vo);

        // 设置上传用户名
        if (music.getUploadUserId() != null) {
            User user = userMapper.findById(music.getUploadUserId());
            if (user != null) {
                vo.setUploadUsername(user.getUsername());
            }
        }

        // 设置是否收藏
        if (userId != null) {
            vo.setIsFavorite(isFavorite(userId, music.getId()));
        }

        return vo;
    }

    /**
     * 清除音乐缓存
     *
     * @param musicId 音乐ID
     */
    private void clearMusicCache(Long musicId) {
        String cacheKey = MUSIC_CACHE_PREFIX + musicId;
        redisTemplate.delete(cacheKey);
    }
}
