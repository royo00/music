// src/utils/validator.js
/**
 * 验证工具函数
 */

/**
 * 验证用户名
 * 规则：4-20位，只能包含字母、数字、下划线
 * @param {string} username - 用户名
 * @returns {boolean}
 */
export function isValidUsername(username) {
  const regex = /^[a-zA-Z0-9_]{4,20}$/
  return regex.test(username)
}

/**
 * 验证密码
 * 规则：6-20位
 * @param {string} password - 密码
 * @returns {boolean}
 */
export function isValidPassword(password) {
  return password && password.length >= 6 && password.length <= 20
}

/**
 * 验证邮箱
 * @param {string} email - 邮箱
 * @returns {boolean}
 */
export function isValidEmail(email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

/**
 * 验证手机号
 * 规则：1开头，第二位3-9，共11位
 * @param {string} phone - 手机号
 * @returns {boolean}
 */
export function isValidPhone(phone) {
  const regex = /^1[3-9]\d{9}$/
  return regex.test(phone)
}

/**
 * 验证用户角色
 * @param {string} role - 角色
 * @returns {boolean}
 */
export function isValidRole(role) {
  return ['user', 'actor'].includes(role)
}

/**
 * 检查是否为空
 * @param {*} value - 值
 * @returns {boolean}
 */
export function isEmpty(value) {
  if (value === null || value === undefined) return true
  if (typeof value === 'string') return value.trim() === ''
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}

/**
 * 检查是否不为空
 * @param {*} value - 值
 * @returns {boolean}
 */
export function isNotEmpty(value) {
  return !isEmpty(value)
}

/**
 * 表单验证规则生成器 - 用于 Element Plus
 */
export const formRules = {
  // 必填
  required: (message = '此项为必填项') => ({
    required: true,
    message,
    trigger: 'blur'
  }),

  // 用户名
  username: () => [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]{4,20}$/,
      message: '用户名只能包含字母、数字、下划线，长度4-20位',
      trigger: 'blur'
    }
  ],

  // 密码
  password: () => [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20位之间', trigger: 'blur' }
  ],

  // 邮箱
  email: (required = false) => {
    const rules = [
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ]
    if (required) {
      rules.unshift({ required: true, message: '请输入邮箱', trigger: 'blur' })
    }
    return rules
  },

  // 手机号
  phone: (required = false) => {
    const rules = [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ]
    if (required) {
      rules.unshift({ required: true, message: '请输入手机号', trigger: 'blur' })
    }
    return rules
  }
}

export default {
  isValidUsername,
  isValidPassword,
  isValidEmail,
  isValidPhone,
  isValidRole,
  isEmpty,
  isNotEmpty,
  formRules
}
