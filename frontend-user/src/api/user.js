// src/api/user.js
/**
 * 用户相关 API
 */

import { get, put } from '@/utils/request'

const USER_BASE_URL = '/user'

/**
 * 获取当前用户信息
 * @returns {Promise<{code: number, message: string, data: UserInfo}>}
 */
export function getUserInfo() {
  return get(`${USER_BASE_URL}/me`)
}

/**
 * 更新用户信息
 * @param {Object} data - 更新信息
 * @param {string} data.nickname - 昵称
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @param {string} data.avatar - 头像 URL
 * @returns {Promise<{code: number, message: string, data: null}>}
 */
export function updateUserInfo(data) {
  return put(`${USER_BASE_URL}/update`, data)
}

export default {
  getUserInfo,
  updateUserInfo
}
