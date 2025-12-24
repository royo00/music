<template>
  <div class="music-card" @click="handlePlay">
    <div class="card-cover">
      <img :src="music.coverUrl || defaultCover" :alt="music.musicName" />
      <div class="cover-overlay">
        <el-button type="primary" circle class="play-btn">
          <el-icon :size="24"><VideoPlay /></el-icon>
        </el-button>
      </div>
    </div>
    <div class="card-info">
      <div class="music-name" :title="music.musicName">{{ music.musicName }}</div>
      <div class="music-artist" :title="music.artist">{{ music.artist }}</div>
    </div>
  </div>
</template>

<script setup>
import { VideoPlay } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores'

const props = defineProps({
  music: {
    type: Object,
    required: true
  }
})

const playerStore = usePlayerStore()
const defaultCover = '/default-cover.png'

function handlePlay() {
  playerStore.play(props.music)
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.music-card {
  padding: 12px;
  border-radius: $border-radius-md;
  background: $bg-secondary;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: $bg-tertiary;

    .cover-overlay {
      opacity: 1;
    }

    .play-btn {
      transform: translateY(0);
      opacity: 1;
    }
  }
}

.card-cover {
  position: relative;
  aspect-ratio: 1;
  border-radius: $border-radius-md;
  overflow: hidden;
  margin-bottom: 12px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cover-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;
  }

  .play-btn {
    width: 48px;
    height: 48px;
    transform: translateY(8px);
    opacity: 0;
    transition: all 0.2s;
    box-shadow: $shadow-md;
  }
}

.card-info {
  .music-name {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 4px;
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
</style>
