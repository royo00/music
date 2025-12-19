package com.music.controller;


import com.music.common.Result;
import com.music.dto.UserUpdateDTO;
import com.music.entity.User;
import com.music.mapper.UserMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {


    private final UserMapper userMapper;


    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    @GetMapping("/me")
    public Result<User> me() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(userMapper.findByUsername(username));
    }


    @PutMapping("/update")
    public Result<Void> update(@RequestBody UserUpdateDTO dto) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.findByUsername(username);
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        userMapper.update(user);
        return Result.success();
    }
}