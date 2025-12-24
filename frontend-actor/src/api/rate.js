import request from '@/utils/request'

/**
 * 评分与统计相关接口
 */

// 1. 艺人获取自身所有单曲的统计数据 (包含单曲平均评分、播放量、收藏量)
// 对应后端接口：GET /rate/artist/{artistId}/list
export const getArtistMusicStatsAPI = () => {
  return request({
    url: `/rate/artist/list`,
    method: 'GET'
  })
}

// 2. 艺人获取综合平均评分 (所有歌曲的平均分)
// 对应后端接口：GET /rate/artist/{artistId}/average
export const getArtistAverageScoreAPI = (artistId) => {
  return request({
    url: `/rate/artist/${artistId}/average`,
    method: 'GET'
  })
}

// 3. 获取单条音乐的具体统计数据 (播放、收藏、平均分)
// 对应后端接口：GET /rate/music/{musicId}
export const getMusicDetailStatsAPI = (musicId) => {
  return request({
    url: `/rate/music/${musicId}`,
    method: 'GET'
  })
}

