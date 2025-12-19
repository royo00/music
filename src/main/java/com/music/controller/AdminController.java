package com.music.controller;


import com.music.common.Constants;
import com.music.common.PageResult;
import com.music.common.Result;
import com.music.entity.User;
import com.music.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
public class AdminController {


    private final UserMapper userMapper;


    public AdminController(UserMapper userMapper) {
        this.userMapper = userMapper;
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
}