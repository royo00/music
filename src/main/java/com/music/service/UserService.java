package com.music.service;

import com.music.dto.LoginDTO;
import com.music.dto.RegisterDTO;
import com.music.dto.UserUpdateDTO;
import com.music.entity.User;
import com.music.exception.BusinessException;
import com.music.mapper.UserMapper;
import com.music.util.JwtUtil;
import com.music.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务类
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private static final String USER_CACHE_PREFIX = "user:info:";
    private static final long USER_CACHE_EXPIRE = 30; // 30分钟

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        User existUser = userMapper.findByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查邮箱是否已存在
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
            existUser = userMapper.findByEmail(registerDTO.getEmail());
            if (existUser != null) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 3. 检查手机号是否已存在
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            existUser = userMapper.findByPhone(registerDTO.getPhone());
            if (existUser != null) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 4. 创建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(registerDTO.getRole());
        user.setNickname(registerDTO.getUsername()); // 默认昵称为用户名
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 5. 保存用户
        int rows = userMapper.insert(user);
        if (rows == 0) {
            throw new BusinessException("注册失败");
        }

        log.info("用户注册成功: username={}, userId={}", user.getUsername(), user.getId());
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 包含token和用户信息的Map
     */
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 1. 查询用户
        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 5. 缓存用户信息到Redis
        cacheUserInfo(user);

        // 6. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", convertToVO(user));

        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());
        return result;
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserVO getUserInfo(Long userId) {
        // 1. 先从缓存获取
        String cacheKey = USER_CACHE_PREFIX + userId;
        UserVO cachedUser = (UserVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 2. 缓存未命中，从数据库查询
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 转换为VO并缓存
        UserVO userVO = convertToVO(user);
        redisTemplate.opsForValue().set(cacheKey, userVO, USER_CACHE_EXPIRE, TimeUnit.MINUTES);

        return userVO;
    }

    /**
     * 更新用户信息
     *
     * @param userId        用户ID
     * @param updateDTO     更新信息
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        // 1. 查询用户
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查邮箱是否被其他用户使用
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().isEmpty()) {
            User existUser = userMapper.findByEmail(updateDTO.getEmail());
            if (existUser != null && !existUser.getId().equals(userId)) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            user.setEmail(updateDTO.getEmail());
        }

        // 3. 检查手机号是否被其他用户使用
        if (updateDTO.getPhone() != null && !updateDTO.getPhone().isEmpty()) {
            User existUser = userMapper.findByPhone(updateDTO.getPhone());
            if (existUser != null && !existUser.getId().equals(userId)) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            user.setPhone(updateDTO.getPhone());
        }

        // 4. 更新其他信息
        if (updateDTO.getNickname() != null && !updateDTO.getNickname().isEmpty()) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatar() != null && !updateDTO.getAvatar().isEmpty()) {
            user.setAvatar(updateDTO.getAvatar());
        }

        user.setUpdateTime(LocalDateTime.now());

        // 5. 保存更新
        int rows = userMapper.update(user);
        if (rows == 0) {
            throw new BusinessException("更新失败");
        }

        // 6. 清除缓存
        clearUserCache(userId);

        log.info("用户信息更新成功: userId={}", userId);
        return convertToVO(user);
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.update(user);
        if (rows == 0) {
            throw new BusinessException("密码修改失败");
        }

        log.info("用户密码修改成功: userId={}", userId);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public User findById(Long userId) {
        return userMapper.findById(userId);
    }

    /**
     * 缓存用户信息
     *
     * @param user 用户对象
     */
    private void cacheUserInfo(User user) {
        String cacheKey = USER_CACHE_PREFIX + user.getId();
        UserVO userVO = convertToVO(user);
        redisTemplate.opsForValue().set(cacheKey, userVO, USER_CACHE_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 清除用户缓存
     *
     * @param userId 用户ID
     */
    private void clearUserCache(Long userId) {
        String cacheKey = USER_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 转换为VO对象
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 不返回密码
        return userVO;
    }
}
