package com.music.controller;

import com.music.common.Result;
import com.music.dto.LoginDTO;
import com.music.dto.RegisterDTO;
import com.music.service.UserService;
import com.music.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 - 处理注册、登录等认证相关请求
 */
@Tag(name = "认证管理", description = "用户注册、登录接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        Long userId = userService.register(registerDTO);

        Map<String, Object> data = Map.of(
                "userId", userId,
                "username", registerDTO.getUsername()
        );

        return Result.success("注册成功", data);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> loginResult = userService.login(loginDTO);
        return Result.success("登录成功", loginResult);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String token) {
        // 可以将 token 加入黑名单（使用 Redis）
        // 这里简化处理，实际项目中应该实现 token 黑名单机制
        return Result.success("登出成功");
    }

    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        // 提取 token
        String oldToken = jwtUtil.extractToken(authHeader);

        // 验证 token 是否有效
        if (!jwtUtil.validateToken(oldToken)) {
            return Result.unauthorized();
        }

        // 刷新 token
        String newToken = jwtUtil.refreshToken(oldToken);

        Map<String, String> data = Map.of("token", newToken);
        return Result.success("Token刷新成功", data);
    }
}
