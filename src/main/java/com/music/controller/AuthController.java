package com.music.controller;

import com.music.common.Constants;
import com.music.common.Result;
import com.music.dto.LoginDTO;
import com.music.dto.RegisterDTO;
import com.music.entity.User;
import com.music.mapper.UserMapper;
import com.music.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final String username = "admin";
    private  final String password = "admin123";

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
        user.setRole(dto.getRole());
        user.setStatus(Constants.UserStatus.ENABLED);

        LocalDateTime now = LocalDateTime.now(); // 获取当前系统时间
        user.setCreateTime(now);
        user.setUpdateTime(now);

        userMapper.insert(user);
        return Result.success();
    }
    //管理员登录
    @PostMapping("/admin")
    public Result<String> admin(@RequestBody LoginDTO loginDTO) {
        // 验证是否是管理员账号
        if (!username.equals(loginDTO.getUsername()) ||
                !password.equals(loginDTO.getPassword())) {
            return Result.error("管理员账号或密码错误!");
        }

        // 查询用户信息
        User user = new User();
        user.setId(0L);
        user.setUsername("admin");
        user.setRole("admin");

        // 生成 token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        return Result.success(token);
    }
    /**
     * 艺人登录
     */
    @PostMapping("/actor")
    public Result<String> actor(@RequestBody LoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername());

        // 用户不存在或被禁用
        if (user == null || !Constants.UserStatus.ENABLED.equals(user.getStatus())) {
            return Result.error(Constants.ResponseMessage.UNAUTHORIZED);
        }

        // 密码错误
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }

        // 非艺人账号
        if(!user.getRole().equals(Constants.UserRole.ACTOR)){
            return Result.error("请使用艺人账号");
        }
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        return Result.success(token);
    }
}
