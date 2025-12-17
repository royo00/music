package com.music.controller;

import com.music.common.Constants;
import com.music.common.Result;
import com.music.dto.LoginDTO;
import com.music.dto.RegisterDTO;
import com.music.entity.User;
import com.music.mapper.UserMapper;
import com.music.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserMapper userMapper,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername());

        // 用户不存在或被禁用
        if (user == null || !Constants.UserStatus.ENABLED.equals(user.getStatus())) {
            return Result.error(Constants.ResponseMessage.UNAUTHORIZED);
        }

        // 密码错误
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error(Constants.ResponseMessage.BAD_REQUEST);
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        return Result.success(token);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {

        if (userMapper.findByUsername(dto.getUsername()) != null) {
            return Result.error(Constants.ResponseMessage.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(Constants.UserRole.USER);
        user.setStatus(Constants.UserStatus.ENABLED);

        userMapper.insert(user);
        return Result.success();
    }
}
