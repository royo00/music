<template>
  <div class="history-page page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">播放历史</h1>
      <!-- <el-button
        v-if="historyList.length"
        type="danger"
        text
        @click="handleClear"
      >
        <el-icon><Delete /></el-icon>
        清空历史
      </el-button> -->
    </div>

    <!-- 时间筛选 -->
    <div class="filter-bar" v-if="historyList.length">
      <el-radio-group v-model="timeRange" @change="fetchHistory">
        <el-radio-button value="today">今天</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
        <el-radio-button value="all">全部</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 历史列表 -->
    <div class="history-content" v-loading="loading">
      <div
        v-for="(group, date) in groupedHistory"
        :key="date"
        class="history-group"
      >
        <div class="group-date">{{ formatGroupDate(date) }}</div>
        <MusicList :list="group" :show-header="false" />
      </div>

      <el-empty
        v-if="!loading && !historyList.length"
        description="暂无播放记录"
      >
        <template #image>
          <el-icon :size="64" class="empty-icon"><Clock /></el-icon>
        </template>
      </el-empty>
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
import { ref, computed, onMounted } from 'vue'
import { useMusicStore } from '@/stores'
import { Delete, Clock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MusicList from '@/components/music/MusicList.vue'

const musicStore = useMusicStore()

const loading = ref(false)
const historyList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 50
const timeRange = ref('all')

// 按日期分组
const groupedHistory = computed(() => {
  const groups = {}
  historyList.value.forEach(item => {
    const date = formatDate(item.playTime || item.createTime)
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(item)
  })
  return groups
})

onMounted(() => {
  fetchHistory()
})

async function fetchHistory() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize
    }

    // 根据时间范围设置参数
    if (timeRange.value !== 'all') {
      const now = new Date()
      let startDate = new Date()

      switch (timeRange.value) {
        case 'today':
          startDate.setHours(0, 0, 0, 0)
          break
        case 'week':
          startDate.setDate(now.getDate() - 7)
          break
        case 'month':
          startDate.setMonth(now.getMonth() - 1)
          break
      }
      params.startTime = startDate.toISOString()
    }

    const res = await musicStore.fetchPlayHistory(params)
    historyList.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    historyList.value = []
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchHistory()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function handleClear() {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有播放历史吗？此操作不可恢复。',
      '清空历史',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await musicStore.clearPlayHistory()
    historyList.value = []
    total.value = 0
    ElMessage.success('已清空播放历史')
  } catch {
    // 取消操作
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '未知日期'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function formatGroupDate(dateStr) {
  const today = formatDate(new Date().toISOString())
  const yesterday = formatDate(new Date(Date.now() - 86400000).toISOString())

  if (dateStr === today) return '今天'
  if (dateStr === yesterday) return '昨天'
  return dateStr
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.history-page {
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    margin: 0;
  }
}

.filter-bar {
  margin-bottom: 24px;
}

.history-content {
  min-height: 200px;
}

.history-group {
  margin-bottom: 32px;

  .group-date {
    font-size: 14px;
    font-weight: 600;
    color: $text-secondary;
    margin-bottom: 12px;
    padding-left: 16px;
  }
}

.empty-icon {
  color: $text-tertiary;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
