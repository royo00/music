// src/constants/index.js
/**
 * 应用常量定义
 */

// API 基础配置
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8080',
  TIMEOUT: 15000
}

// 响应状态码
export const RESPONSE_CODE = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  ERROR: 500
}

// 响应消息
export const RESPONSE_MESSAGE = {
  SUCCESS: '操作成功',
  ERROR: '系统错误，请尝试重新登录',
  UNAUTHORIZED: '未授权或登录已过期',
  FORBIDDEN: '无权限访问',
  NOT_FOUND: '资源不存在',
  BAD_REQUEST: '请求参数错误',
  NETWORK_ERROR: '网络连接失败',
  TIMEOUT_ERROR: '请求超时'
}

// 用户角色
export const USER_ROLE = {
  USER: 'user',
  ACTOR: 'actor',
  ADMIN: 'admin'
}

// 用户状态
export const USER_STATUS = {
  ENABLED: 1,
  DISABLED: 0
}

// 音乐状态
export const MUSIC_STATUS = {
  PENDING: 0,    // 待审核
  APPROVED: 1,   // 已通过
  REJECTED: 2    // 已拒绝
}

// 本地存储 Key
export const STORAGE_KEYS = {
  TOKEN: 'music_player_token',
  USER_INFO: 'music_player_user',
  PLAY_HISTORY: 'music_player_history',
  PLAY_MODE: 'music_player_mode',
  VOLUME: 'music_player_volume'
}

// 播放模式
export const PLAY_MODE = {
  SEQUENCE: 'sequence',   // 顺序播放
  LOOP: 'loop',           // 单曲循环
  RANDOM: 'random'        // 随机播放
}

// 分页默认配置
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_SIZE: 10,
  PAGE_SIZES: [10, 20, 30, 50]
}
