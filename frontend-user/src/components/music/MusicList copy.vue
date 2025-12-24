<template>
  <div class="music-list">
    <div class="list-header" v-if="showHeader">
      <div class="col-index">#</div>
      <div class="col-title">标题</div>
      <div class="col-artist">艺术家</div>
      <div class="col-duration">时长</div>
      <div class="col-actions"></div>
    </div>
    <div class="list-body">
      <div
        v-for="(music, index) in list"
        :key="music.id"
        class="list-item"
        :class="{ active: isPlaying(music.id) }"
        @dblclick="handlePlay(music)"
      >
        <div class="col-index">
          <span class="index-num">{{ index + 1 }}</span>
          <el-icon class="play-icon" @click="handlePlay(music)"><VideoPlay /></el-icon>
        </div>
        <div class="col-title">
          <img :src="music.coverUrl || defaultCover" class="music-cover" />
          <div class="music-info">
            <span class="music-name">{{ music.musicName }}</span>
          </div>
        </div>
        <div class="col-artist">{{ music.artist }}</div>
        <div class="col-duration">{{ formatDuration(music.duration) }}</div>
        <div class="col-actions">
          <el-button text circle @click.stop="handleFavorite(music)" :title="music.isFavorite ? '取消收藏' : '收藏'">
            <el-icon>
              <StarFilled v-if="music.isFavorite" class="favorited" />
              <Star v-else />
            </el-icon>
          </el-button>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, music)">
            <el-button text circle>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="play">播放</el-dropdown-item>
                <el-dropdown-item command="addToPlaylist">添加到播放列表</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    <el-empty v-if="!list.length && !loading" :description="emptyText" />
  </div>
</template>

<script setup>
import { inject } from 'vue'
import { usePlayerStore, useMusicStore, useUserStore } from '@/stores'
import { formatDuration } from '@/utils/format'
import { VideoPlay, Star, StarFilled, More } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  list: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  showHeader: { type: Boolean, default: true },
  emptyText: { type: String, default: '暂无数据' }
})

const playerStore = usePlayerStore()
const musicStore = useMusicStore()
const userStore = useUserStore()
const openAuthDialog = inject('openAuthDialog', () => {})

const defaultCover = '/default-cover.png'

function isPlaying(musicId) {
  return playerStore.currentMusic?.id === musicId
}

function handlePlay(music) {
  playerStore.play(music)
}

async function handleFavorite(music) {
  if (!userStore.isLoggedIn) {
    openAuthDialog('login')
    return
  }
  try {
    const newStatus = await musicStore.toggleFavorite(music.id, music.isFavorite)
    music.isFavorite = newStatus
    ElMessage.success(newStatus ? '已添加到收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleCommand(command, music) {
  switch (command) {
    case 'play':
      handlePlay(music)
      break
    case 'addToPlaylist':
      playerStore.addToPlaylist(music)
      ElMessage.success('已添加到播放列表')
      break
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.music-list {
  width: 100%;
}

.list-header {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid $border-color;
  color: $text-secondary;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: $border-radius-sm;
  transition: background 0.15s;

  &:hover {
    background: $bg-tertiary;
    .index-num { display: none; }
    .play-icon { display: flex; }
  }

  &.active {
    .music-name { color: $primary-color; }
  }
}

.col-index {
  width: 40px;
  text-align: center;
  color: $text-secondary;

  .play-icon {
    display: none;
    cursor: pointer;
    justify-content: center;
    &:hover { color: $text-primary; }
  }
}

.col-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;

  .music-cover {
    width: 40px;
    height: 40px;
    border-radius: $border-radius-sm;
    object-fit: cover;
  }

  .music-info {
    flex: 1;
    min-width: 0;
  }

  .music-name {
    font-size: 14px;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.col-artist {
  width: 200px;
  color: $text-secondary;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-duration {
  width: 80px;
  text-align: right;
  color: $text-secondary;
  font-size: 14px;
}

.col-actions {
  width: 80px;
  display: flex;
  justify-content: flex-end;
  gap: 4px;

  .favorited { color: $primary-color; }
}
</style>
