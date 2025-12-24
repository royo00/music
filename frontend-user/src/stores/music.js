// src/stores/music.js
/**
 * 音乐列表状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { musicApi } from '@/api'
import { PAGINATION } from '@/constants'

export const useMusicStore = defineStore('music', () => {
  // ==================== 状态 ====================

  /** 音乐列表 */
  const musicList = ref([])

  /** 分页信息 */
  const pagination = ref({
    page: PAGINATION.DEFAULT_PAGE,
    size: PAGINATION.DEFAULT_SIZE,
    total: 0,
    pages: 0
  })

  /** 搜索关键词 */
  const searchKeyword = ref('')

  /** 加载状态 */
  const loading = ref(false)

  /** 收藏列表 */
  const favoriteList = ref([])

  /** 播放历史 */
  const historyList = ref([])

  /** 当前音乐详情 */
  const currentMusicDetail = ref(null)

  // ==================== 计算属性 ====================

  /** 是否有更多数据 */
  const hasMore = computed(() => pagination.value.page < pagination.value.pages)

  /** 是否为空列表 */
  const isEmpty = computed(() => musicList.value.length === 0 && !loading.value)

  // ==================== 方法 ====================

  /**
   * 获取音乐列表
   * @param {Object} params - 查询参数
   * @param {boolean} append - 是否追加数据（加载更多）
   */
  async function fetchMusicList(params = {}, append = false) {
    loading.value = true
    try {
      const queryParams = {
        page: params.page || pagination.value.page,
        size: params.size || pagination.value.size,
        status: 1 // 只获取已审核通过的音乐
      }

      const res = await musicApi.getMusicList(queryParams)
      const data = res.data

      if (append) {
        musicList.value = [...musicList.value, ...data.list]
      } else {
        musicList.value = data.list
      }

      pagination.value = {
        page: data.page,
        size: data.size,
        total: data.total,
        pages: data.pages
      }

      return data
    } catch (error) {
      console.error('获取音乐列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 搜索音乐
   * @param {string} keyword - 搜索关键词
   * @param {Object} params - 分页参数
   */
  async function searchMusic(keyword, params = {}) {
    loading.value = true
    searchKeyword.value = keyword

    try {
      const searchParams = {
        keyword,
        page: params.page || PAGINATION.DEFAULT_PAGE,
        size: params.size || PAGINATION.DEFAULT_SIZE
      }

      const res = await musicApi.searchMusic(searchParams)
      const data = res.data

      musicList.value = data.list
      pagination.value = {
        page: data.page,
        size: data.size,
        total: data.total,
        pages: data.pages
      }

      return data
    } catch (error) {
      console.error('搜索音乐失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取音乐详情
   * @param {number} musicId - 音乐ID
   */
  async function fetchMusicDetail(musicId) {
    try {
      const res = await musicApi.getMusicDetail(musicId)
      currentMusicDetail.value = res.data
      return res.data
    } catch (error) {
      console.error('获取音乐详情失败:', error)
      throw error
    }
  }

  /**
   * 获取收藏列表
   * @param {Object} params - 分页参数
   */
  async function fetchFavoriteList(params = {}) {
    loading.value = true
    try {
      const queryParams = {
        page: params.page || PAGINATION.DEFAULT_PAGE,
        size: params.size || PAGINATION.DEFAULT_SIZE
      }

      const res = await musicApi.getFavoriteList(queryParams)
      favoriteList.value = res.data.list

      return res.data
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取播放历史
   * @param {Object} params - 分页参数
   */
  async function fetchPlayHistory(params = {}) {
    loading.value = true
    try {
      const queryParams = {
        page: params.page || PAGINATION.DEFAULT_PAGE,
        size: params.size || PAGINATION.DEFAULT_SIZE
      }

      const res = await musicApi.getPlayHistory(queryParams)
      historyList.value = res.data.list

      return res.data
    } catch (error) {
      console.error('获取播放历史失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 收藏/取消收藏音乐
   * @param {number} musicId - 音乐ID
   * @param {boolean} isFavorite - 当前是否已收藏
   */
  async function toggleFavorite(musicId, isFavorite) {
    try {
      if (isFavorite) {
        await musicApi.unfavoriteMusic(musicId)
      } else {
        await musicApi.favoriteMusic(musicId)
      }

      // 更新列表中的收藏状态
      updateMusicFavoriteStatus(musicId, !isFavorite)

      return !isFavorite
    } catch (error) {
      console.error('收藏操作失败:', error)
      throw error
    }
  }

  /**
   * 更新音乐收藏状态
   * @param {number} musicId - 音乐ID
   * @param {boolean} isFavorite - 是否收藏
   */
  function updateMusicFavoriteStatus(musicId, isFavorite) {
    // 更新音乐列表
    const musicIndex = musicList.value.findIndex(m => m.id === musicId)
    if (musicIndex !== -1) {
      musicList.value[musicIndex].isFavorite = isFavorite
    }

    // 更新当前详情
    if (currentMusicDetail.value?.id === musicId) {
      currentMusicDetail.value.isFavorite = isFavorite
    }

    // 更新收藏列表
    if (!isFavorite) {
      favoriteList.value = favoriteList.value.filter(m => m.id !== musicId)
    }
  }

  /**
   * 加载更多
   */
  async function loadMore() {
    if (!hasMore.value || loading.value) return

    const nextPage = pagination.value.page + 1

    if (searchKeyword.value) {
      await searchMusic(searchKeyword.value, { page: nextPage })
    } else {
      await fetchMusicList({ page: nextPage }, true)
    }
  }

  /**
   * 刷新列表
   */
  async function refreshList() {
    pagination.value.page = PAGINATION.DEFAULT_PAGE

    if (searchKeyword.value) {
      await searchMusic(searchKeyword.value)
    } else {
      await fetchMusicList()
    }
  }

  /**
   * 清空搜索
   */
  function clearSearch() {
    searchKeyword.value = ''
  }

  /**
   * 重置状态
   */
  function resetState() {
    musicList.value = []
    favoriteList.value = []
    historyList.value = []
    currentMusicDetail.value = null
    searchKeyword.value = ''
    pagination.value = {
      page: PAGINATION.DEFAULT_PAGE,
      size: PAGINATION.DEFAULT_SIZE,
      total: 0,
      pages: 0
    }
  }

  return {
    // 状态
    musicList,
    pagination,
    searchKeyword,
    loading,
    favoriteList,
    historyList,
    currentMusicDetail,

    // 计算属性
    hasMore,
    isEmpty,

    // 方法
    fetchMusicList,
    searchMusic,
    fetchMusicDetail,
    fetchFavoriteList,
    fetchPlayHistory,
    toggleFavorite,
    loadMore,
    refreshList,
    clearSearch,
    resetState
  }
})
