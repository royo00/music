// src/utils/storage.js
/**
 * 本地存储工具函数
 * 封装 localStorage 操作，支持数据序列化
 */

import { STORAGE_KEYS } from '@/constants'

/**
 * 获取存储项
 * @param {string} key - 存储键名
 * @param {*} defaultValue - 默认值
 * @returns {*} 存储值或默认值
 */
export function getItem(key, defaultValue = null) {
  try {
    const value = localStorage.getItem(key)
    if (value === null) {
      return defaultValue
    }
    return JSON.parse(value)
  } catch (error) {
    console.error(`获取存储项 ${key} 失败:`, error)
    return defaultValue
  }
}

/**
 * 设置存储项
 * @param {string} key - 存储键名
 * @param {*} value - 存储值
 */
export function setItem(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (error) {
    console.error(`设置存储项 ${key} 失败:`, error)
  }
}

/**
 * 移除存储项
 * @param {string} key - 存储键名
 */
export function removeItem(key) {
  try {
    localStorage.removeItem(key)
  } catch (error) {
    console.error(`移除存储项 ${key} 失败:`, error)
  }
}

/**
 * 清空所有存储
 */
export function clear() {
  try {
    localStorage.clear()
  } catch (error) {
    console.error('清空存储失败:', error)
  }
}

// Token 操作
export const tokenStorage = {
  get: () => getItem(STORAGE_KEYS.TOKEN, ''),
  set: (token) => setItem(STORAGE_KEYS.TOKEN, token),
  remove: () => removeItem(STORAGE_KEYS.TOKEN),
  exists: () => !!getItem(STORAGE_KEYS.TOKEN)
}

// 用户信息操作
export const userStorage = {
  get: () => getItem(STORAGE_KEYS.USER_INFO, null),
  set: (user) => setItem(STORAGE_KEYS.USER_INFO, user),
  remove: () => removeItem(STORAGE_KEYS.USER_INFO)
}

// 播放设置操作
export const playerStorage = {
  getVolume: () => getItem(STORAGE_KEYS.VOLUME, 0.7),
  setVolume: (volume) => setItem(STORAGE_KEYS.VOLUME, volume),
  getPlayMode: () => getItem(STORAGE_KEYS.PLAY_MODE, 'sequence'),
  setPlayMode: (mode) => setItem(STORAGE_KEYS.PLAY_MODE, mode)
}

export default {
  getItem,
  setItem,
  removeItem,
  clear,
  tokenStorage,
  userStorage,
  playerStorage
}
