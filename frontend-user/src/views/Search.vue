<template>
  <div class="search-page page-container">
    <!-- 搜索头部 -->
    <div class="search-header">
      <h1 class="search-title" v-if="keyword">
        搜索结果：<span class="keyword">{{ keyword }}</span>
      </h1>
      <h1 class="search-title" v-else>搜索音乐</h1>
      <p class="search-count" v-if="keyword && !loading">
        共找到 {{ total }} 首音乐
      </p>
    </div>


    <!-- 搜索结果 -->
    <div class="search-content" v-loading="loading">
      <MusicList
        :list="searchResults"
        :loading="loading"
        :empty-text="emptyText"
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

    <!-- 无关键词提示 -->
    <div class="search-tip" v-if="!keyword && !loading">
      <el-icon :size="64" class="tip-icon"><Search /></el-icon>
      <p>在顶部搜索框输入关键词</p>
      <p class="sub-tip">支持搜索歌曲名称、艺术家</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useMusicStore } from '@/stores'
import { Search } from '@element-plus/icons-vue'
import MusicList from '@/components/music/MusicList.vue'

const route = useRoute()
const musicStore = useMusicStore()

const loading = ref(false)
const searchResults = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

const keyword = computed(() => route.query.keyword || '')

const emptyText = computed(() => {
  if (!keyword.value) return ''
  return `未找到与"${keyword.value}"相关的音乐`
})

// 监听关键词变化
watch(keyword, (newVal) => {
  if (newVal) {
    currentPage.value = 1
    doSearch()
  } else {
    searchResults.value = []
    total.value = 0
  }
}, { immediate: true })

// async function doSearch() {
//   if (!keyword.value) return

//   loading.value = true
//   try {
//     const res = await musicStore.searchMusic({
//       keyword: keyword.value,
//       page: currentPage.value,
//       size: pageSize
//     })
//     searchResults.value = res.list || []
//     total.value = res.total || 0
//   } catch (e) {
//     searchResults.value = []
//     total.value = 0
//   } finally {
//     loading.value = false
//   }
// }

async function doSearch() {
  if (!keyword.value) return
  loading.value = true
  try {
    // 第一个参数是 keyword 字符串，第二个参数是分页对象
    const res = await musicStore.searchMusic(keyword.value, {
      page: currentPage.value,
      size: pageSize
    })
    searchResults.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    searchResults.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  doSearch()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.search-page {
  min-height: 60vh;
}

.search-header {
  margin-bottom: 24px;

  .search-title {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;

    .keyword {
      color: $primary-color;
    }
  }

  .search-count {
    color: $text-secondary;
    font-size: 14px;
    margin: 0;
  }
}

.search-content {
  min-height: 200px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 24px;
}

.search-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: $text-secondary;

  .tip-icon {
    margin-bottom: 24px;
    opacity: 0.5;
  }

  p {
    margin: 0 0 8px;
    font-size: 16px;
  }

  .sub-tip {
    font-size: 14px;
    opacity: 0.7;
  }
}
</style>
