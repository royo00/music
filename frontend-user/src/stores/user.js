// src/stores/user.js
/**
 * 用户状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '@/api'
import { tokenStorage, userStorage } from '@/utils/storage'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // ==================== 状态 ====================
  const token = ref(tokenStorage.get() || '')
  const userInfo = ref(userStorage.get() || null)
  const loading = ref(false)

  // ==================== 计算属性 ====================

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 用户名 */
  const username = computed(() => userInfo.value?.username || '')

  /** 昵称（优先显示昵称，没有则显示用户名） */
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  /** 头像 */
  const avatar = computed(() => userInfo.value?.avatar || '')

  /** 用户角色 */
  const role = computed(() => userInfo.value?.role || '')

  /** 用户ID */
  const userId = computed(() => userInfo.value?.id || null)

  /** 是否是普通用户 */
  const isUser = computed(() => role.value === 'user')

  /** 是否是创作者 */
  const isActor = computed(() => role.value === 'actor')

  // ==================== 方法 ====================

  /**
   * 用户登录
   * @param {Object} loginData - 登录信息
   * @param {string} loginData.username - 用户名
   * @param {string} loginData.password - 密码
   * @returns {Promise<boolean>} 登录是否成功
   */
  async function login(loginData) {
    loading.value = true
    try {
      const res = await authApi.login(loginData)

      // 保存 Token
      token.value = res.data
      tokenStorage.set(res.data)

      // 获取用户信息
      await fetchUserInfo()

      return true
    } catch (error) {
      console.error('登录失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户注册
   * @param {Object} registerData - 注册信息
   * @returns {Promise<boolean>} 注册是否成功
   */
  async function register(registerData) {
    loading.value = true
    try {
      await authApi.register(registerData)
      return true
    } catch (error) {
      console.error('注册失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户信息
   * @returns {Promise<Object|null>} 用户信息
   */
  async function fetchUserInfo() {
    if (!token.value) return null

    try {
      const res = await userApi.getUserInfo()
      userInfo.value = res.data
      userStorage.set(res.data)
      return res.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // Token 无效时清除登录状态
      if (error.response?.status === 401) {
        logout()
      }
      return null
    }
  }

  /**
   * 更新用户信息
   * @param {Object} updateData - 更新数据
   * @returns {Promise<boolean>} 更新是否成功
   */
  async function updateUserInfo(updateData) {
    loading.value = true
    try {
      await userApi.updateUserInfo(updateData)
      // 重新获取用户信息
      await fetchUserInfo()
      return true
    } catch (error) {
      console.error('更新用户信息失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 退出登录
   * @param {boolean} redirect - 是否跳转到登录页
   */
  function logout(redirect = true) {
    // 清除状态
    token.value = ''
    userInfo.value = null

    // 清除本地存储
    tokenStorage.remove()
    userStorage.remove()

    // 跳转到登录页
    if (redirect) {
      router.push('/login')
    }
  }

  /**
   * 初始化用户状态（应用启动时调用）
   */
  async function initUserState() {
    if (token.value && !userInfo.value) {
      await fetchUserInfo()
    }
  }

  /**
   * 检查登录状态
   * @returns {boolean} 是否已登录
   */
  function checkLogin() {
    if (!isLoggedIn.value) {
      router.push('/login')
      return false
    }
    return true
  }

  return {
    // 状态
    token,
    userInfo,
    loading,

    // 计算属性
    isLoggedIn,
    username,
    nickname,
    avatar,
    role,
    userId,
    isUser,
    isActor,

    // 方法
    login,
    register,
    fetchUserInfo,
    updateUserInfo,
    logout,
    initUserState,
    checkLogin
  }
})
