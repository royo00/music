import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 情况1: 登录接口返回 { code: 200, data: "token", success: true }
    if (res.code !== undefined) {
      if (res.code === 200 || res.success === true) {
        return res
      } else {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    }

    // 情况2: 分页接口直接返回 { total, page, list, ... }
    // 或其他直接返回数据的接口
    if (response.status === 200) {
      return res
    }

    ElMessage.error('请求失败')
    return Promise.reject(new Error('请求失败'))
  },
  error => {
    console.error('响应错误:', error)

    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else if (error.response?.status === 403) {
      ElMessage.error('没有访问权限，请退出尝试重新登录以获取权限！')
    } else {
      const message = error.response?.data?.message || error.message || '网络错误'
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default request
