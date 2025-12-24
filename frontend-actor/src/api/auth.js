import request from '@/utils/request'

export const loginAPI = (data) => {
  return request({
    url: '/auth/actor',
    method: 'POST',
    data
  })
}

export const registerAPI = (data) => {
  return request({
    url: '/auth/register',
    method: 'POST',
    data
  })
}
