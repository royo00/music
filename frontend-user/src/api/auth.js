// src/api/auth.js
/**
 * 认证相关 API
 */

import { post } from '@/utils/request'

const AUTH_BASE_URL = '/auth'

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise<{code: number, message: string, data: string}>}
 */
export function login(data) {
  return post(`${AUTH_BASE_URL}/login`, data)
}

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.email - 邮箱（可选）
 * @param {string} data.phone - 手机号（可选）
 * @param {string} data.role - 角色（user/actor）
 * @returns {Promise<{code: number, message: string, data: null}>}
 */
export function register(data) {
  return post(`${AUTH_BASE_URL}/register`, data)
}

export default {
  login,
  register
}
