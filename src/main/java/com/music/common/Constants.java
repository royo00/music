package com.music.common;

/**
 * 系统常量类
 * 定义系统中使用的各类常量
 */
public class Constants {

    /**
     * 响应状态码
     */
    public static class ResponseCode {
        /** 成功 */
        public static final Integer SUCCESS = 200;
        /** 失败 */
        public static final Integer ERROR = 500;
        /** 参数错误 */
        public static final Integer BAD_REQUEST = 400;
        /** 未授权 */
        public static final Integer UNAUTHORIZED = 401;
        /** 无权限 */
        public static final Integer FORBIDDEN = 403;
        /** 资源不存在 */
        public static final Integer NOT_FOUND = 404;
    }

    /**
     * 响应消息
     */
    public static class ResponseMessage {
        public static final String SUCCESS = "操作成功";
        public static final String ERROR = "操作失败";
        public static final String UNAUTHORIZED = "未登录或登录已过期";
        public static final String FORBIDDEN = "无权限访问";
        public static final String NOT_FOUND = "资源不存在";
        public static final String BAD_REQUEST = "请求参数错误";
    }

    /**
     * 用户角色
     */
    public static class UserRole {
        /** 管理员 */
        public static final String ADMIN = "admin";
        /** 普通用户 */
        public static final String USER = "user";
        /** 艺人 */
        public static final String ACTOR = "actor";
    }

    /**
     * 用户状态
     */
    public static class UserStatus {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 启用 */
        public static final Integer ENABLED = 1;
    }

    /**
     * 音乐状态
     */
    public static class MusicStatus {
        /** 待审核 */
        public static final Integer PENDING = 0;
        /** 已发布 */
        public static final Integer PUBLISHED = 1;
        /** 已下架 */
        public static final Integer OFFLINE = 2;
    }

    /**
     * 文件类型
     */
    public static class FileType {
        /** 音乐文件 */
        public static final String MUSIC = "music";
        /** 图片文件 */
        public static final String IMAGE = "images";
    }

    /**
     * 缓存键前缀
     */
    public static class CacheKey {
        /** 用户信息缓存 */
        public static final String USER_INFO = "user:info:";
        /** 音乐信息缓存 */
        public static final String MUSIC_INFO = "music:info:";
        /** 音乐列表缓存 */
        public static final String MUSIC_LIST = "music:list:";
        /** 验证码缓存 */
        public static final String CAPTCHA = "captcha:";
    }

    /**
     * 缓存过期时间（秒）
     */
    public static class CacheExpire {
        /** 1小时 */
        public static final Long ONE_HOUR = 3600L;
        /** 1天 */
        public static final Long ONE_DAY = 86400L;
        /** 7天 */
        public static final Long ONE_WEEK = 604800L;
        /** 30天 */
        public static final Long ONE_MONTH = 2592000L;
    }

    /**
     * 分页默认值
     */
    public static class Page {
        /** 默认页码 */
        public static final Integer DEFAULT_PAGE = 1;
        /** 默认每页数量 */
        public static final Integer DEFAULT_SIZE = 20;
        /** 最大每页数量 */
        public static final Integer MAX_SIZE = 100;
    }

    /**
     * JWT相关常量
     */
    public static class Jwt {
        /** Token请求头名称 */
        public static final String HEADER = "Authorization";
        /** Token前缀 */
        public static final String PREFIX = "Bearer ";
        /** Token过期时间（毫秒） - 7天 */
        public static final Long EXPIRATION = 604800000L;
    }

    /**
     * 文件大小限制（字节）
     */
    public static class FileSize {
        /** 音乐文件最大大小 - 50MB */
        public static final Long MAX_MUSIC_SIZE = 52428800L;
        /** 图片文件最大大小 - 5MB */
        public static final Long MAX_IMAGE_SIZE = 5242880L;
    }

    /**
     * 正则表达式
     */
    public static class Regex {
        /** 用户名：4-20位字母、数字、下划线 */
        public static final String USERNAME = "^[a-zA-Z0-9_]{4,20}$";
        /** 密码：6-20位 */
        public static final String PASSWORD = "^.{6,20}$";
        /** 邮箱 */
        public static final String EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        /** 手机号 */
        public static final String PHONE = "^1[3-9]\\d{9}$";
    }

    /**
     * 系统配置
     */
    public static class System {
        /** 系统名称 */
        public static final String NAME = "在线音乐平台";
        /** 系统版本 */
        public static final String VERSION = "1.0.0";
        /** 默认头像 */
        public static final String DEFAULT_AVATAR = "/static/images/default-avatar.png";
        /** 默认封面 */
        public static final String DEFAULT_COVER = "/static/images/default-cover.png";
    }

    /**
     * 操作类型
     */
    public static class OperationType {
        /** 新增 */
        public static final String INSERT = "INSERT";
        /** 更新 */
        public static final String UPDATE = "UPDATE";
        /** 删除 */
        public static final String DELETE = "DELETE";
        /** 查询 */
        public static final String SELECT = "SELECT";
    }
}
