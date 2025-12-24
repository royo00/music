import request from '@/utils/request'

// 获取音乐列表
export const getMusicListAPI = (params) => {
  return request({
    url: '/actor/music',
    method: 'GET',
    params
  })
}

// 上传音乐
export const uploadMusicAPI = (formData) => {
  return request({
    url: '/actor/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新音乐
export const updateMusicAPI = (musicId, data) => {
  return request({
    url: `/actor/${musicId}`,
    method: 'PUT',
    data
  })
}

// 删除音乐
export const deleteMusicAPI = (musicId) => {
  return request({
    url: `/actor/${musicId}`,
    method: 'DELETE'
  })
}
