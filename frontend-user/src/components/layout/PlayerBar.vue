<template>
  <div class="player-bar" :class="{ 'has-music': playerStore.currentMusic }">
    <!-- 左侧：当前音乐信息 -->
    <div class="player-left">
      <template v-if="playerStore.currentMusic">
        <div class="music-cover">
          <img :src="playerStore.currentMusic.coverUrl || defaultCover" alt="cover" />
        </div>
        <div class="music-info">
          <div class="music-name">{{ playerStore.currentMusic.musicName }}</div>
          <div class="music-artist">{{ playerStore.currentMusic.artist }}</div>
        </div>
        <el-button text circle @click="handleFavorite" class="favorite-btn">
          <el-icon :size="18">
            <StarFilled v-if="playerStore.currentMusic.isFavorite" class="favorited" />
            <Star v-else />
          </el-icon>
        </el-button>
      </template>
    </div>

    <!-- 中间：播放控制 -->
    <div class="player-center">
      <div class="control-buttons">
        <el-button text circle @click="playerStore.togglePlayMode" class="mode-btn" :title="playerStore.playModeText">
          <el-icon :size="18"><component :is="playModeIcon" /></el-icon>
        </el-button>
        <el-button text circle @click="playerStore.playPrev" :disabled="!playerStore.hasPrev">
          <el-icon :size="22"><DArrowLeft /></el-icon>
        </el-button>
        <el-button type="primary" circle class="play-btn" @click="playerStore.togglePlay" :loading="playerStore.loading">
          <el-icon :size="24">
            <VideoPause v-if="playerStore.isPlaying" />
            <VideoPlay v-else />
          </el-icon>
        </el-button>
        <el-button text circle @click="playerStore.playNext" :disabled="!playerStore.hasNext">
          <el-icon :size="22"><DArrowRight /></el-icon>
        </el-button>
        <el-button text circle @click="playerStore.togglePlaylist" class="playlist-btn">
          <el-icon :size="18"><Tickets /></el-icon>
        </el-button>
      </div>
      <div class="progress-bar">
        <span class="time">{{ formatDuration(playerStore.currentTime) }}</span>
        <el-slider v-model="progressValue" :show-tooltip="false" @change="handleProgressChange" class="progress-slider" />
        <span class="time">{{ formatDuration(playerStore.duration) }}</span>
      </div>
    </div>

    <!-- 右侧：音量控制 -->
    <div class="player-right">
      <el-button text circle @click="playerStore.toggleMute">
        <el-icon :size="18">
          <Mute v-if="playerStore.isMuted || playerStore.volume === 0" />
          <Microphone v-else />
        </el-icon>
      </el-button>
      <el-slider v-model="volumeValue" :show-tooltip="false" @change="handleVolumeChange" class="volume-slider" />
    </div>

    <!-- 隐藏的 Audio 元素 -->
    <audio ref="audioRef" :src="playerStore.playUrl" @timeupdate="onTimeUpdate" @ended="playerStore.onEnded" @loadedmetadata="onLoadedMetadata" @canplay="onCanPlay" />

    <!-- 播放列表抽屉 -->
    <el-drawer v-model="playerStore.showPlaylist" title="播放列表" direction="rtl" size="320px">
      <template #header>
        <div class="playlist-header">
          <span>播放列表 ({{ playerStore.playlist.length }})</span>
          <el-button text size="small" @click="playerStore.clearPlaylist">清空</el-button>
        </div>
      </template>
      <div class="playlist-content">
        <div v-for="(music, index) in playerStore.playlist" :key="music.id" class="playlist-item" :class="{ active: playerStore.currentIndex === index }" @click="playerStore.playByIndex(index)">
          <div class="item-info">
            <span class="item-name">{{ music.musicName }}</span>
            <span class="item-artist">{{ music.artist }}</span>
          </div>
          <el-button text circle size="small" @click.stop="playerStore.removeFromPlaylist(music.id)">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <el-empty v-if="!playerStore.playlist.length" description="播放列表为空" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, watch, shallowRef } from 'vue'
import { usePlayerStore, useMusicStore, useUserStore } from '@/stores'
import { formatDuration } from '@/utils/format'
import { PLAY_MODE } from '@/constants'
import { Star, StarFilled, VideoPlay, VideoPause, DArrowLeft, DArrowRight, Mute, Microphone, Tickets, Close, RefreshRight, Sort, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const playerStore = usePlayerStore()
const musicStore = useMusicStore()
const userStore = useUserStore()

const audioRef = ref(null)
const defaultCover = '/default-cover.png'

// 进度和音量使用本地值，避免频繁更新
const progressValue = ref(0)
const volumeValue = ref(playerStore.volume * 100)

// 播放模式图标
const playModeIcon = computed(() => {
  switch (playerStore.playMode) {
    case PLAY_MODE.LOOP: return RefreshRight
    case PLAY_MODE.RANDOM: return Sort
    default: return Refresh
  }
})

// 监听进度变化
watch(() => playerStore.progress, (val) => {
  progressValue.value = val
})

// 监听音量变化
watch(() => playerStore.volume, (val) => {
  volumeValue.value = val * 100
  if (audioRef.value) audioRef.value.volume = val
})

// 监听播放状态
watch(() => playerStore.isPlaying, (playing) => {
  if (audioRef.value) {
    playing ? audioRef.value.play() : audioRef.value.pause()
  }
})

// 监听静音
watch(() => playerStore.isMuted, (muted) => {
  if (audioRef.value) audioRef.value.muted = muted
})

// 监听播放地址
watch(() => playerStore.playUrl, () => {
  if (audioRef.value && playerStore.playUrl) {
    audioRef.value.load()
  }
})

function onTimeUpdate() {
  if (audioRef.value) {
    playerStore.updateCurrentTime(audioRef.value.currentTime)
  }
}

function onLoadedMetadata() {
  if (audioRef.value) {
    playerStore.duration = audioRef.value.duration
  }
}

function onCanPlay() {
  if (audioRef.value && playerStore.isPlaying) {
    audioRef.value.play()
  }
}

function handleProgressChange(val) {
  playerStore.seekByPercent(val)
  if (audioRef.value) {
    audioRef.value.currentTime = (val / 100) * playerStore.duration
  }
}

function handleVolumeChange(val) {
  playerStore.setVolume(val / 100)
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  const music = playerStore.currentMusic
  if (!music) return
  try {
    await musicStore.toggleFavorite(music.id, music.isFavorite)
    music.isFavorite = !music.isFavorite
    ElMessage.success(music.isFavorite ? '已添加到收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.player-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: $bg-secondary;
  border-top: 1px solid $border-color;
}

.player-left {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 250px;
  min-width: 180px;

  .music-cover {
    width: 56px;
    height: 56px;
    border-radius: $border-radius-sm;
    overflow: hidden;
    flex-shrink: 0;
    img { width: 100%; height: 100%; object-fit: cover; }
  }
  .music-info {
    flex: 1;
    min-width: 0;
    .music-name {
      font-size: 14px;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .music-artist {
      font-size: 12px;
      color: $text-secondary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  .favorite-btn .favorited { color: $primary-color; }
}

.player-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 600px;
  padding: 0 20px;

  .control-buttons {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 8px;
    .play-btn { width: 40px; height: 40px; }
  }
  .progress-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;
    .time { font-size: 12px; color: $text-secondary; width: 40px; text-align: center; }
    .progress-slider { flex: 1; }
  }
}

.player-right {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 180px;
  justify-content: flex-end;
  .volume-slider { width: 100px; }
}

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.playlist-content {
  .playlist-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    border-radius: $border-radius-sm;
    cursor: pointer;
    transition: background 0.2s;
    &:hover { background: $bg-tertiary; }
    &.active {
      background: $bg-tertiary;
      .item-name { color: $primary-color; }
    }
    .item-info {
      flex: 1;
      min-width: 0;
      .item-name {
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        display: block;
      }
      .item-artist {
        font-size: 12px;
        color: $text-secondary;
      }
    }
  }
}
</style>
