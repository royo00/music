import request from './request'

export const adminLogin = (data) => {
  return request({
    url: '/auth/admin',
    method: 'post',
    data
  })
}

// 如果后端有获取当前用户信息的接口，添加这个
export const getCurrentUser = () => {
  return request({
    url: '/auth/current',
    method: 'get'
  })
}
