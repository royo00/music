<template>
  <div class="home-page page-container">
    <!-- 欢迎区域 -->
    <section class="welcome-section">
      <h1 class="welcome-title">{{ greeting }}</h1>
    </section>

    <!-- 推荐音乐卡片 -->
    <section class="music-section">
      <div class="section-header">
        <h2 class="section-title">推荐音乐</h2>
        <el-button text @click="refreshList">
          <el-icon><Refresh /></el-icon>
          换一批
        </el-button>
      </div>
      <div class="music-grid" v-loading="musicStore.loading">
        <MusicCard v-for="music in displayList" :key="music.id" :music="music" />
      </div>
      <el-empty v-if="!musicStore.loading && musicStore.isEmpty" description="暂无音乐" />
    </section>

    <!-- 全部音乐列表 -->
    <section class="music-section">
      <h2 class="section-title">全部音乐</h2>
      <MusicList :list="musicStore.musicList" :loading="musicStore.loading" />
      <!-- 加载更多 -->
      <div class="load-more" v-if="musicStore.hasMore">
        <el-button @click="loadMore" :loading="loadingMore">加载更多</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMusicStore } from '@/stores'
import { Refresh } from '@element-plus/icons-vue'
import MusicCard from '@/components/music/MusicCard.vue'
import MusicList from '@/components/music/MusicList.vue'

const musicStore = useMusicStore()
const loadingMore = ref(false)

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，听点音乐吧'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 展示卡片的音乐（前6首）
const displayList = computed(() => {
  return musicStore.musicList.slice(0, 6)
})

onMounted(() => {
  if (musicStore.musicList.length === 0) {
    musicStore.fetchMusicList({ page: 1, size: 20 })
  }
})

function refreshList() {
  musicStore.refreshList()
}

async function loadMore() {
  loadingMore.value = true
  try {
    await musicStore.loadMore()
  } finally {
    loadingMore.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.home-page {
  padding-bottom: 40px;
}

.welcome-section {
  margin-bottom: 32px;

  .welcome-title {
    font-size: 32px;
    font-weight: 700;
  }
}

.music-section {
  margin-bottom: 40px;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 24px;
    font-weight: 700;
    margin: 0;
  }
}

.music-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 24px;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
