// src/stores/player.js
/**
 * 播放器状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { musicApi } from '@/api'
import { playerStorage } from '@/utils/storage'
import { PLAY_MODE } from '@/constants'

export const usePlayerStore = defineStore('player', () => {
  // ==================== 状态 ====================

  /** 当前播放的音乐信息 */
  const currentMusic = ref(null)

  /** 播放列表 */
  const playlist = ref([])

  /** 当前播放索引 */
  const currentIndex = ref(-1)

  /** 播放状态 */
  const isPlaying = ref(false)

  /** 当前播放时间（秒） */
  const currentTime = ref(0)

  /** 音乐总时长（秒） */
  const duration = ref(0)

  /** 音量 (0-1) */
  const volume = ref(playerStorage.getVolume())

  /** 是否静音 */
  const isMuted = ref(false)

  /** 播放模式 */
  const playMode = ref(playerStorage.getPlayMode())

  /** 播放地址 */
  const playUrl = ref('')

  /** 加载状态 */
  const loading = ref(false)

  /** 是否显示播放列表 */
  const showPlaylist = ref(false)

  // ==================== 计算属性 ====================

  /** 播放进度 (0-100) */
  const progress = computed(() => {
    if (duration.value === 0) return 0
    return (currentTime.value / duration.value) * 100
  })

  /** 是否有上一曲 */
  const hasPrev = computed(() => {
    if (playlist.value.length === 0) return false
    if (playMode.value === PLAY_MODE.RANDOM) return true
    return currentIndex.value > 0
  })

  /** 是否有下一曲 */
  const hasNext = computed(() => {
    if (playlist.value.length === 0) return false
    if (playMode.value === PLAY_MODE.RANDOM) return true
    if (playMode.value === PLAY_MODE.LOOP) return true
    return currentIndex.value < playlist.value.length - 1
  })

  /** 播放列表是否为空 */
  const isPlaylistEmpty = computed(() => playlist.value.length === 0)

  /** 当前播放模式图标 */
  const playModeIcon = computed(() => {
    const icons = {
      [PLAY_MODE.SEQUENCE]: 'sequence',
      [PLAY_MODE.LOOP]: 'loop',
      [PLAY_MODE.RANDOM]: 'random'
    }
    return icons[playMode.value] || 'sequence'
  })

  /** 当前播放模式文字 */
  const playModeText = computed(() => {
    const texts = {
      [PLAY_MODE.SEQUENCE]: '顺序播放',
      [PLAY_MODE.LOOP]: '单曲循环',
      [PLAY_MODE.RANDOM]: '随机播放'
    }
    return texts[playMode.value] || '顺序播放'
  })

  // ==================== 监听器 ====================

  // 监听音量变化，保存到本地
  watch(volume, (newVolume) => {
    playerStorage.setVolume(newVolume)
  })

  // 监听播放模式变化，保存到本地
  watch(playMode, (newMode) => {
    playerStorage.setPlayMode(newMode)
  })

  // ==================== 方法 ====================

  /**
   * 播放指定音乐
   * @param {Object} music - 音乐信息
   */
  async function play(music) {
    if (!music || !music.id) return

    loading.value = true

    try {
      // 获取播放地址
      const res = await musicApi.getMusicPlayUrl(music.id)
      const playInfo = res.data

      // 设置当前音乐
      currentMusic.value = {
        ...music,
        ...playInfo
      }
      playUrl.value = playInfo.playUrl
      duration.value = playInfo.duration || music.duration || 0

      // 添加到播放列表（如果不在列表中）
      addToPlaylist(music)

      // 更新当前索引
      currentIndex.value = playlist.value.findIndex(m => m.id === music.id)

      // 开始播放
      isPlaying.value = true
      currentTime.value = 0

    } catch (error) {
      console.error('获取播放地址失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 暂停播放
   */
  function pause() {
    isPlaying.value = false
  }

  /**
   * 继续播放
   */
  function resume() {
    if (currentMusic.value) {
      isPlaying.value = true
    }
  }

  /**
   * 切换播放/暂停
   */
  function togglePlay() {
    if (isPlaying.value) {
      pause()
    } else {
      resume()
    }
  }

  /**
   * 播放上一曲
   */
  async function playPrev() {
    if (!hasPrev.value) return

    let prevIndex

    if (playMode.value === PLAY_MODE.RANDOM) {
      prevIndex = getRandomIndex()
    } else {
      prevIndex = currentIndex.value - 1
      if (prevIndex < 0) {
        prevIndex = playlist.value.length - 1
      }
    }

    const prevMusic = playlist.value[prevIndex]
    if (prevMusic) {
      await play(prevMusic)
    }
  }

  /**
   * 播放下一曲
   */
  async function playNext() {
    if (!hasNext.value && playMode.value === PLAY_MODE.SEQUENCE) return

    let nextIndex

    if (playMode.value === PLAY_MODE.RANDOM) {
      nextIndex = getRandomIndex()
    } else if (playMode.value === PLAY_MODE.LOOP) {
      nextIndex = currentIndex.value // 单曲循环
    } else {
      nextIndex = currentIndex.value + 1
      if (nextIndex >= playlist.value.length) {
        nextIndex = 0
      }
    }

    const nextMusic = playlist.value[nextIndex]
    if (nextMusic) {
      await play(nextMusic)
    }
  }

  /**
   * 播放结束处理
   */
  async function onEnded() {
    if (playMode.value === PLAY_MODE.LOOP) {
      // 单曲循环：重新播放当前歌曲
      currentTime.value = 0
      isPlaying.value = true
    } else {
      // 播放下一曲
      await playNext()
    }
  }

  /**
   * 获取随机索引
   */
  function getRandomIndex() {
    if (playlist.value.length <= 1) return 0

    let randomIndex
    do {
      randomIndex = Math.floor(Math.random() * playlist.value.length)
    } while (randomIndex === currentIndex.value)

    return randomIndex
  }

  /**
   * 设置播放进度
   * @param {number} time - 时间（秒）
   */
  function seek(time) {
    currentTime.value = Math.max(0, Math.min(time, duration.value))
  }

  /**
   * 设置播放进度（百分比）
   * @param {number} percent - 百分比 (0-100)
   */
  function seekByPercent(percent) {
    const time = (percent / 100) * duration.value
    seek(time)
  }

  /**
   * 更新播放时间
   * @param {number} time - 当前时间
   */
  function updateCurrentTime(time) {
    currentTime.value = time
  }

  /**
   * 设置音量
   * @param {number} value - 音量值 (0-1)
   */
  function setVolume(value) {
    volume.value = Math.max(0, Math.min(1, value))
    if (volume.value > 0) {
      isMuted.value = false
    }
  }

  /**
   * 切换静音
   */
  function toggleMute() {
    isMuted.value = !isMuted.value
  }

  /**
   * 切换播放模式
   */
  function togglePlayMode() {
    const modes = [PLAY_MODE.SEQUENCE, PLAY_MODE.LOOP, PLAY_MODE.RANDOM]
    const currentModeIndex = modes.indexOf(playMode.value)
    const nextModeIndex = (currentModeIndex + 1) % modes.length
    playMode.value = modes[nextModeIndex]
  }

  /**
   * 设置播放模式
   * @param {string} mode - 播放模式
   */
  function setPlayMode(mode) {
    if (Object.values(PLAY_MODE).includes(mode)) {
      playMode.value = mode
    }
  }

  /**
   * 添加到播放列表
   * @param {Object} music - 音乐信息
   */
  function addToPlaylist(music) {
    const exists = playlist.value.some(m => m.id === music.id)
    if (!exists) {
      playlist.value.push(music)
    }
  }

  /**
   * 批量添加到播放列表
   * @param {Array} musicList - 音乐列表
   * @param {boolean} replace - 是否替换现有列表
   */
  function addAllToPlaylist(musicList, replace = false) {
    if (replace) {
      playlist.value = [...musicList]
    } else {
      musicList.forEach(music => addToPlaylist(music))
    }
  }

  /**
   * 从播放列表移除
   * @param {number} musicId - 音乐ID
   */
  function removeFromPlaylist(musicId) {
    const index = playlist.value.findIndex(m => m.id === musicId)
    if (index === -1) return

    playlist.value.splice(index, 1)

    // 如果移除的是当前播放的音乐
    if (currentMusic.value?.id === musicId) {
      if (playlist.value.length > 0) {
        // 播放下一曲
        const nextIndex = Math.min(index, playlist.value.length - 1)
        play(playlist.value[nextIndex])
      } else {
        // 播放列表为空，停止播放
        stop()
      }
    } else if (index < currentIndex.value) {
      // 移除的是当前播放之前的音乐，更新索引
      currentIndex.value--
    }
  }

  /**
   * 清空播放列表
   */
  function clearPlaylist() {
    playlist.value = []
    currentIndex.value = -1
    stop()
  }

  /**
   * 停止播放
   */
  function stop() {
    isPlaying.value = false
    currentMusic.value = null
    playUrl.value = ''
    currentTime.value = 0
    duration.value = 0
    currentIndex.value = -1
  }

  /**
   * 切换播放列表显示
   */
  function togglePlaylist() {
    showPlaylist.value = !showPlaylist.value
  }

  /**
   * 播放指定索引的音乐
   * @param {number} index - 索引
   */
  async function playByIndex(index) {
    if (index >= 0 && index < playlist.value.length) {
      await play(playlist.value[index])
    }
  }

  return {
    // 状态
    currentMusic,
    playlist,
    currentIndex,
    isPlaying,
    currentTime,
    duration,
    volume,
    isMuted,
    playMode,
    playUrl,
    loading,
    showPlaylist,

    // 计算属性
    progress,
    hasPrev,
    hasNext,
    isPlaylistEmpty,
    playModeIcon,
    playModeText,

    // 方法
    play,
    pause,
    resume,
    togglePlay,
    playPrev,
    playNext,
    onEnded,
    seek,
    seekByPercent,
    updateCurrentTime,
    setVolume,
    toggleMute,
    togglePlayMode,
    setPlayMode,
    addToPlaylist,
    addAllToPlaylist,
    removeFromPlaylist,
    clearPlaylist,
    stop,
    togglePlaylist,
    playByIndex
  }
})
