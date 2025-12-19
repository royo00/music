package com.music.controller;


import com.music.common.Constants;
import com.music.common.PageResult;
import com.music.common.Result;
import com.music.entity.User;
import com.music.mapper.UserMapper;
import com.music.service.MusicService;
import com.music.vo.MusicVO;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private final long userId = 0L;
    private final UserMapper userMapper;
    private final MusicService musicService;


    public AdminController(UserMapper userMapper, MusicService musicService) {
        this.userMapper = userMapper;
        this.musicService = musicService;
    }


    @GetMapping("/users")
    public PageResult<User> users(@RequestParam(required = false) String role,
                                  @RequestParam(required = false) Integer status) {
        return PageResult.of(userMapper.countUsers(role, status), 1, Constants.Page.DEFAULT_SIZE, userMapper.findAll(role, status));
    }


    @PutMapping("/user/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        userMapper.updateStatus(id, status);
        return Result.success();
    }
    /**
     * 获取音乐
     */
    @GetMapping("/music")
    public Result<PageResult<MusicVO>> adminGetMusicList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String authorization) {

        return Result.success(musicService.adminGetMusicList(page, size, status));
    }
    /**
     * 审核音乐
     */
    @PutMapping("/music/status")
    public Result<Void> updateMusicStatus(@RequestParam Long musicId, @RequestParam Integer status, String remark) {
        musicService.updateMusicStatus(musicId, status, remark);
        return Result.success();
    }
    /**
     * 删除音乐
     */
    @DeleteMapping("/{musicId}")
    public Result<Void> delete(@PathVariable Long musicId,
                               @RequestHeader("Authorization") String authorization) {
        musicService.deleteMusic(musicId, userId);
        return Result.success();
    }
}