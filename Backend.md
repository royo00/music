# 在线音乐网站项目文档

## 一、项目概述

### 1.1 项目名称
在线音乐平台系统（Online Music Platform）

### 1.2 项目简介
本项目是一个基于前后端分离架构的在线音乐平台，支持音乐播放、搜索、上传和管理等功能。系统面向三类用户：普通用户、艺人和管理员，提供差异化的功能服务。

### 1.3 技术栈
- **后端**: Spring Boot 3.x + MyBatis + MySQL 8.0
- **前端**: Vue 3 + Axios + Element Plus / Ant Design Vue
- **文件存储**: 本地存储 / 阿里云OSS / MinIO
- **其他**: JWT认证、Redis缓存、Nginx反向代理

---

## 二、系统架构设计

### 2.1 系统架构图
```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  用户前端    │      │  艺人前端    │      │  管理员前端  │
│  (Vue3)     │      │  (Vue3)     │      │  (Vue3)     │
└──────┬──────┘      └──────┬──────┘      └──────┬──────┘
       │                    │                    │
       └────────────────────┼────────────────────┘
                            │ (Axios/HTTP)
                     ┌──────▼──────┐
                     │   Nginx     │
                     └──────┬──────┘
                            │
                     ┌──────▼──────────┐
                     │  Spring Boot 3  │
                     │   (REST API)    │
                     └──────┬──────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
       ┌──────▼──────┐ ┌───▼────┐ ┌─────▼─────┐
       │   MySQL 8   │ │ Redis  │ │文件存储系统│
       └─────────────┘ └────────┘ └───────────┘
```

### 2.2 模块划分
- **用户模块**: 注册、登录、个人信息管理
- **音乐模块**: 音乐播放、搜索、收藏
- **艺人模块**: 作品上传、作品管理
- **管理模块**: 用户管理、音乐审核、系统配置

---

## 三、数据库设计

### 3.1 用户表 (t_user)
```sql
CREATE TABLE `t_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `role` ENUM('admin', 'user', 'actor') NOT NULL DEFAULT 'user' COMMENT '角色',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (`username`),
  INDEX idx_role (`role`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 3.2 音乐表 (t_music)
```sql
CREATE TABLE `t_music` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '音乐ID',
  `music_name` VARCHAR(200) NOT NULL COMMENT '音乐名称',
  `artist` VARCHAR(100) NOT NULL COMMENT '艺术家/作者',
  `album` VARCHAR(200) COMMENT '专辑名称',
  `duration` INT COMMENT '时长(秒)',
  `file_uuid` VARCHAR(64) NOT NULL UNIQUE COMMENT '文件UUID',
  `file_path` VARCHAR(500) COMMENT '文件存储路径',
  `file_size` BIGINT COMMENT '文件大小(字节)',
  `cover_url` VARCHAR(255) COMMENT '封面图片URL',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已发布, 2-已下架',
  `description` TEXT COMMENT '简介',
  `play_count` BIGINT DEFAULT 0 COMMENT '播放次数',
  `upload_user_id` BIGINT COMMENT '上传用户ID',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_music_name (`music_name`),
  INDEX idx_artist (`artist`),
  INDEX idx_album (`album`),
  INDEX idx_status (`status`),
  INDEX idx_upload_user (`upload_user_id`),
  FULLTEXT INDEX ft_search (`music_name`, `artist`, `album`) WITH PARSER ngram,
  FOREIGN KEY (`upload_user_id`) REFERENCES `t_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐表';
```

### 3.3 收藏表 (t_favorite)
```sql
CREATE TABLE `t_favorite` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `music_id` BIGINT NOT NULL COMMENT '音乐ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  UNIQUE KEY uk_user_music (`user_id`, `music_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_music_id (`music_id`),
  FOREIGN KEY (`user_id`) REFERENCES `t_user`(`id`),
  FOREIGN KEY (`music_id`) REFERENCES `t_music`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
```

### 3.4 播放历史表 (t_play_history)
```sql
CREATE TABLE `t_play_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `music_id` BIGINT NOT NULL COMMENT '音乐ID',
  `play_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '播放时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_music_id (`music_id`),
  INDEX idx_play_time (`play_time`),
  FOREIGN KEY (`user_id`) REFERENCES `t_user`(`id`),
  FOREIGN KEY (`music_id`) REFERENCES `t_music`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播放历史表';
```

---

## 四、后端API设计

### 4.1 用户模块API

#### 4.1.1 用户注册
- **接口**: `POST /api/auth/register`
- **请求体**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "phone": "string",
  "role": "user|actor"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "string"
  }
}
```

#### 4.1.2 用户登录
- **接口**: `POST /api/auth/login`
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "jwt_token_string",
    "userInfo": {
      "id": 1,
      "username": "string",
      "nickname": "string",
      "role": "user",
      "avatar": "url"
    }
  }
}
```

#### 4.1.3 获取用户信息
- **接口**: `GET /api/user/info`
- **请求头**: `Authorization: Bearer {token}`
- **响应**: 返回当前用户详细信息

#### 4.1.4 更新用户信息
- **接口**: `PUT /api/user/info`
- **请求头**: `Authorization: Bearer {token}`
- **请求体**: 用户可修改字段

### 4.2 音乐模块API

#### 4.2.1 音乐列表查询
- **接口**: `GET /api/music/list`
- **参数**:
  - `page`: 页码 (默认1)
  - `size`: 每页数量 (默认20)
  - `status`: 状态筛选 (可选)
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "musicName": "歌曲名",
        "artist": "艺术家",
        "album": "专辑",
        "duration": 240,
        "coverUrl": "url",
        "playCount": 1000
      }
    ]
  }
}
```

#### 4.2.2 音乐搜索
- **接口**: `GET /api/music/search`
- **参数**:
  - `keyword`: 搜索关键词 (支持音乐名、作者、专辑)
  - `musicName`: 音乐名 (可选)
  - `artist`: 艺术家 (可选)
  - `album`: 专辑 (可选)
  - `page`: 页码
  - `size`: 每页数量
- **响应**: 同音乐列表格式

#### 4.2.3 音乐详情
- **接口**: `GET /api/music/{id}`
- **响应**: 返回音乐完整信息

#### 4.2.4 音乐播放URL
- **接口**: `GET /api/music/{id}/play`
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
```json
{
  "code": 200,
  "data": {
    "playUrl": "音乐文件访问URL",
    "duration": 240
  }
}
```

#### 4.2.5 收藏音乐
- **接口**: `POST /api/music/{id}/favorite`
- **请求头**: `Authorization: Bearer {token}`

#### 4.2.6 取消收藏
- **接口**: `DELETE /api/music/{id}/favorite`
- **请求头**: `Authorization: Bearer {token}`

#### 4.2.7 我的收藏列表
- **接口**: `GET /api/music/favorites`
- **请求头**: `Authorization: Bearer {token}`

### 4.3 艺人模块API

#### 4.3.1 上传音乐
- **接口**: `POST /api/actor/music/upload`
- **请求头**: `Authorization: Bearer {token}`
- **Content-Type**: `multipart/form-data`
- **请求体**:
  - `file`: 音乐文件
  - `musicName`: 音乐名称
  - `artist`: 艺术家
  - `album`: 专辑
  - `description`: 简介
  - `cover`: 封面图片 (可选)

#### 4.3.2 我的作品列表
- **接口**: `GET /api/actor/music/list`
- **请求头**: `Authorization: Bearer {token}`
- **参数**: page, size, status

#### 4.3.3 编辑作品
- **接口**: `PUT /api/actor/music/{id}`
- **请求头**: `Authorization: Bearer {token}`

#### 4.3.4 删除作品
- **接口**: `DELETE /api/actor/music/{id}`
- **请求头**: `Authorization: Bearer {token}`

### 4.4 管理员模块API

#### 4.4.1 用户管理列表
- **接口**: `GET /api/admin/users`
- **请求头**: `Authorization: Bearer {token}` (需admin角色)
- **参数**: page, size, role, status

#### 4.4.2 启用/禁用用户
- **接口**: `PUT /api/admin/users/{id}/status`
- **请求体**:
```json
{
  "status": 0 | 1
}
```

#### 4.4.3 音乐管理列表
- **接口**: `GET /api/admin/music`
- **参数**: page, size, status

#### 4.4.4 审核音乐
- **接口**: `PUT /api/admin/music/{id}/status`
- **请求体**:
```json
{
  "status": 1 | 2,
  "remark": "审核意见"
}
```

#### 4.4.5 删除音乐
- **接口**: `DELETE /api/admin/music/{id}`

---

## 六、后端实现细节

### 6.1 项目结构
```
src/main/java/com/music
├── config              # 配置类
│   ├── SecurityConfig.java
│   ├── MyBatisConfig.java
│   └── CorsConfig.java
├── controller          # 控制器
│   ├── AuthController.java
│   ├── UserController.java
│   ├── MusicController.java
│   ├── ActorController.java
│   └── AdminController.java
├── service             # 服务层
│   ├── UserService.java
│   ├── MusicService.java
│   └── FileService.java
├── mapper              # MyBatis映射
│   ├── UserMapper.java
│   └── MusicMapper.java
├── entity              # 实体类
│   ├── User.java
│   └── Music.java
├── dto                 # 数据传输对象
│   ├── LoginDTO.java
│   └── MusicSearchDTO.java
├── vo                  # 视图对象
│   ├── UserVO.java
│   └── MusicVO.java
├── common              # 公共类
│   ├── Result.java
│   ├── PageResult.java
│   └── Constants.java
├── exception           # 异常处理
│   ├── GlobalExceptionHandler.java
│   └── BusinessException.java
└── util                # 工具类
    ├── JwtUtil.java
    └── FileUtil.java
```

### 6.2 核心代码示例

#### 6.2.1 统一响应结果
```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
```

#### 6.2.2 音乐搜索Service
```java
@Service
public class MusicService {
    
    @Autowired
    private MusicMapper musicMapper;
    
    public PageResult<MusicVO> searchMusic(MusicSearchDTO searchDTO) {
        // 构建查询条件
        PageHelper.startPage(searchDTO.getPage(), searchDTO.getSize());
        
        List<Music> musicList = musicMapper.searchMusic(
            searchDTO.getKeyword(),
            searchDTO.getMusicName(),
            searchDTO.getArtist(),
            searchDTO.getAlbum()
        );
        
        PageInfo<Music> pageInfo = new PageInfo<>(musicList);
        
        // 转换为VO
        List<MusicVO> voList = musicList.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(pageInfo.getTotal(), voList);
    }
}
```

#### 6.2.3 MyBatis Mapper
```xml
<!-- MusicMapper.xml -->
<select id="searchMusic" resultType="Music">
    SELECT * FROM t_music
    WHERE status = 1
    <if test="keyword != null and keyword != ''">
        AND (
            music_name LIKE CONCAT('%', #{keyword}, '%')
            OR artist LIKE CONCAT('%', #{keyword}, '%')
            OR album LIKE CONCAT('%', #{keyword}, '%')
        )
    </if>
    <if test="musicName != null and musicName != ''">
        AND music_name LIKE CONCAT('%', #{musicName}, '%')
    </if>
    <if test="artist != null and artist != ''">
        AND artist LIKE CONCAT('%', #{artist}, '%')
    </if>
    <if test="album != null and album != ''">
        AND album LIKE CONCAT('%', #{album}, '%')
    </if>
    ORDER BY create_time DESC
</select>
```

---