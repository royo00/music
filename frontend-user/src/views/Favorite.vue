<template>
  <div class="favorite-page page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-cover">
        <el-icon :size="48"><Star /></el-icon>
      </div>
      <div class="header-info">
        <span class="header-tag">歌单</span>
        <h1 class="header-title">我喜欢的音乐</h1>
        <p class="header-meta">{{ userStore.userInfo?.username }} · {{ total }} 首歌曲</p>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button
        type="primary"
        size="large"
        circle
        @click="playAll"
        :disabled="!favoriteList.length"
      >
        <el-icon :size="24"><VideoPlay /></el-icon>
      </el-button>
    </div>

    <!-- 收藏列表 -->
    <div class="favorite-content" v-loading="loading">
      <MusicList
        :list="favoriteList"
        :loading="loading"
        empty-text="还没有收藏的音乐"
      />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore, useMusicStore, usePlayerStore } from '@/stores'
import { Star, VideoPlay } from '@element-plus/icons-vue'
import MusicList from '@/components/music/MusicList.vue'

const userStore = useUserStore()
const musicStore = useMusicStore()
const playerStore = usePlayerStore()

const loading = ref(false)
const favoriteList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

onMounted(() => {
  fetchFavorites()
})

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await musicStore.fetchFavoriteList({
      page: currentPage.value,
      size: pageSize
    })
    favoriteList.value = res.list || []
    total.value = res.total || 0
    // 标记为已收藏
    favoriteList.value.forEach(item => {
      item.isFavorite = true
    })
  } catch (e) {
    favoriteList.value = []
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchFavorites()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function playAll() {
  if (favoriteList.value.length > 0) {
    playerStore.playList(favoriteList.value)
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.favorite-page {
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  align-items: flex-end;
  gap: 24px;
  padding: 40px 0;
  background: linear-gradient(transparent 0, rgba($primary-color, 0.1) 100%);
  margin: -24px -24px 0;
  padding: 40px 24px;

  .header-cover {
    width: 192px;
    height: 192px;
    background: linear-gradient(135deg, $primary-color 0%, darken($primary-color, 15%) 100%);
    border-radius: $border-radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: $shadow-lg;
    flex-shrink: 0;
  }

  .header-info {
    flex: 1;
    min-width: 0;

    .header-tag {
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 1px;
    }

    .header-title {
      font-size: 48px;
      font-weight: 700;
      margin: 8px 0 16px;
      line-height: 1.2;
    }

    .header-meta {
      color: $text-secondary;
      font-size: 14px;
      margin: 0;
    }
  }
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 0;

  .el-button.is-circle {
    width: 56px;
    height: 56px;
  }
}

.favorite-content {
  min-height: 200px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
