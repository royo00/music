import { post, get } from '@/utils/request'

/**
 * 提交评分
 * @param {number} musicId - 歌曲 id
 * @param {number} score - 评分（例如 1-5）
 * @returns {Promise<string>} 返回后端 data（示例："评分成功"）
 */
export async function rateMusic(musicId, score) {
  const payload = { musicId, score }
  const res = await post('/rate', payload)
  // request 封装会在成功时返回整个 res 对象，后端的真实数据在 res.data
  return res.data
}

/**
 * 根据歌曲 id 获取评分信息
 * @param {number|string} musicId - 歌曲 id
 * @returns {Promise<Object>} 返回评分对象，示例：{ musicId, playCount, favoriteCount, avgScore, totalRatings }
 */
export async function getMusicRating(musicId) {
  const res = await get(`/rate/music/${musicId}`)
  return res.data
}

export default {
  rateMusic,
  getMusicRating
}
