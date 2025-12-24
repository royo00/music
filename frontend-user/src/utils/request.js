// src/utils/request.js
/**
 * Axios 请求封装
 * 统一处理请求拦截、响应拦截、错误处理
 */

import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { API_CONFIG, RESPONSE_CODE, RESPONSE_MESSAGE } from '@/constants'
import { tokenStorage } from '@/utils/storage'
import router from '@/router'

// 创建 Axios 实例
const service = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 是否正在刷新 Token 的标记（防止重复弹窗）
let isTokenExpired = false

/**
 * 请求拦截器
 */
service.interceptors.request.use(
  (config) => {
    // 获取 Token
    const token = tokenStorage.get()

    // 如果有 Token，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 打印请求信息（开发环境）
    if (import.meta.env.DEV) {
      console.log(`[Request] ${config.method?.toUpperCase()} ${config.url}`, config.data || config.params)
    }

    return config
  },
  (error) => {
    console.error('[Request Error]', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 打印响应信息（开发环境）
    if (import.meta.env.DEV) {
      console.log(`[Response] ${response.config.url}`, res)
    }

    // 根据响应码判断请求状态
    if (res.code === RESPONSE_CODE.SUCCESS) {
      return res
    }

    // 处理业务错误
    handleBusinessError(res)
    return Promise.reject(new Error(res.message || RESPONSE_MESSAGE.ERROR))
  },
  (error) => {
    console.error('[Response Error]', error)

    // 处理网络错误
    handleNetworkError(error)
    return Promise.reject(error)
  }
)

/**
 * 处理业务错误
 * @param {Object} res - 响应数据
 */
function handleBusinessError(res) {
  switch (res.code) {
    case RESPONSE_CODE.UNAUTHORIZED:
      handleUnauthorized()
      break
    case RESPONSE_CODE.FORBIDDEN:
      ElMessage.error(RESPONSE_MESSAGE.FORBIDDEN)
      break
    case RESPONSE_CODE.NOT_FOUND:
      ElMessage.error(RESPONSE_MESSAGE.NOT_FOUND)
      break
    case RESPONSE_CODE.BAD_REQUEST:
      ElMessage.error(res.message || RESPONSE_MESSAGE.BAD_REQUEST)
      break
    default:
      ElMessage.error(res.message || RESPONSE_MESSAGE.ERROR)
  }
}

/**
 * 处理网络错误
 * @param {Error} error - 错误对象
 */
function handleNetworkError(error) {
  let message = RESPONSE_MESSAGE.NETWORK_ERROR

  if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
    message = RESPONSE_MESSAGE.TIMEOUT_ERROR
  } else if (error.response) {
    switch (error.response.status) {
      case 401:
        handleUnauthorized()
        return
      case 403:
        message = RESPONSE_MESSAGE.FORBIDDEN
        break
      case 404:
        message = RESPONSE_MESSAGE.NOT_FOUND
        break
      case 500:
        message = RESPONSE_MESSAGE.ERROR
        break
      default:
        message = `请求失败 (${error.response.status})`
    }
  }

  ElMessage.error(message)
}

/**
 * 处理未授权（Token 过期或无效）
 */
function handleUnauthorized() {
  if (isTokenExpired) return

  isTokenExpired = true

  ElMessageBox.confirm(
    '登录状态已过期，请重新登录',
    '提示',
    {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 清除本地存储
    tokenStorage.remove()
    // 跳转到登录页
    router.push('/login')
  }).finally(() => {
    isTokenExpired = false
  })
}

/**
 * 封装 GET 请求
 * @param {string} url - 请求地址
 * @param {Object} params - 请求参数
 * @param {Object} config - 额外配置
 * @returns {Promise}
 */
export function get(url, params = {}, config = {}) {
  return service.get(url, { params, ...config })
}

/**
 * 封装 POST 请求
 * @param {string} url - 请求地址
 * @param {Object} data - 请求数据
 * @param {Object} config - 额外配置
 * @returns {Promise}
 */
export function post(url, data = {}, config = {}) {
  return service.post(url, data, config)
}

/**
 * 封装 PUT 请求
 * @param {string} url - 请求地址
 * @param {Object} data - 请求数据
 * @param {Object} config - 额外配置
 * @returns {Promise}
 */
export function put(url, data = {}, config = {}) {
  return service.put(url, data, config)
}

/**
 * 封装 DELETE 请求
 * @param {string} url - 请求地址
 * @param {Object} params - 请求参数
 * @param {Object} config - 额外配置
 * @returns {Promise}
 */
export function del(url, params = {}, config = {}) {
  return service.delete(url, { params, ...config })
}

/**
 * 封装文件上传请求
 * @param {string} url - 请求地址
 * @param {FormData} formData - 表单数据
 * @param {Function} onProgress - 进度回调
 * @returns {Promise}
 */
export function upload(url, formData, onProgress) {
  return service.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100)
        onProgress(percent)
      }
    }
  })
}

// 导出 axios 实例（用于特殊场景）
export default service
