// src/api/music.js
/**
 * 音乐相关 API
 */

import { get, post, del } from '@/utils/request'
import { MUSIC_STATUS } from '@/constants'

const MUSIC_BASE_URL = '/music'

/**
 * 获取音乐列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {number} [params.status] - 音乐状态（1=已通过）
 * @returns {Promise<{code: number, message: string, data: PageResult<MusicVO>}>}
 */
export function getMusicList(params) {
  return get(`${MUSIC_BASE_URL}/list`, {
    ...params,
    status: params.status ?? MUSIC_STATUS.APPROVED // 默认只获取已通过的音乐
  })
}

/**
 * 搜索音乐
 * @param {Object} data - 搜索参数
 * @param {string} data.keyword - 关键词
 * @param {number} data.page - 页码
 * @param {number} data.size - 每页数量
 * @returns {Promise<{code: number, message: string, data: PageResult<MusicVO>}>}
 */
export function searchMusic(data) {
  return post(`${MUSIC_BASE_URL}/search`, data)
}

/**
 * 获取音乐详情
 * @param {number} musicId - 音乐 ID
 * @returns {Promise<{code: number, message: string, data: MusicDetailVO}>}
 */
export function getMusicDetail(musicId) {
  return get(`${MUSIC_BASE_URL}/detail/${musicId}`)
}

/**
 * 获取音乐播放地址
 * @param {number} musicId - 音乐 ID
 * @returns {Promise<{code: number, message: string, data: PlayInfo}>}
 */
export function getMusicPlayUrl(musicId) {
  return get(`${MUSIC_BASE_URL}/play/${musicId}`)
}

/**
 * 收藏音乐
 * @param {number} musicId - 音乐 ID
 * @returns {Promise<{code: number, message: string, data: null}>}
 */
export function favoriteMusic(musicId) {
  return post(`${MUSIC_BASE_URL}/favorite/${musicId}`)
}

/**
 * 取消收藏
 * @param {number} musicId - 音乐 ID
 * @returns {Promise<{code: number, message: string, data: null}>}
 */
export function unfavoriteMusic(musicId) {
  return del(`${MUSIC_BASE_URL}/favorite/${musicId}`)
}

/**
 * 获取收藏列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @returns {Promise<{code: number, message: string, data: PageResult<MusicVO>}>}
 */
export function getFavoriteList(params) {
  return get(`${MUSIC_BASE_URL}/favorite/list`, params)
}

/**
 * 获取播放历史
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @returns {Promise<{code: number, message: string, data: PageResult<MusicVO>}>}
 */
export function getPlayHistory(params) {
  return get(`${MUSIC_BASE_URL}/history`, params)
}

export default {
  getMusicList,
  searchMusic,
  getMusicDetail,
  getMusicPlayUrl,
  favoriteMusic,
  unfavoriteMusic,
  getFavoriteList,
  getPlayHistory
}
