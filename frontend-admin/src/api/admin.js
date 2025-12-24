import request from './request'

// 用户管理
export const getUserList = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export const updateUserStatus = (id, status) => {
  return request({
    url: '/admin/user/status',
    method: 'put',
    params: {
      id,
      status
    }
  })
}

// 音乐管理
export const getMusicList = (params) => {
  return request({
    url: '/admin/music',
    method: 'get',
    params
  })
}

export const updateMusicStatus = (musicId, status, remark = '') => {
  return request({
    url: '/admin/music/status',
    method: 'put',
    params: {
      musicId,
      status,
      remark
    }
  })
}

export const deleteMusic = (musicId) => {
  return request({
    url: `/admin/${musicId}`,
    method: 'delete'
  })
}
