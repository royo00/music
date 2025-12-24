<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Headset /></el-icon>
            <div>
              <div class="stat-value">{{ stats.playCount }}</div>
              <div class="stat-label">总播放量</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><Star /></el-icon>
            <div>
              <div class="stat-value">{{ stats.favoriteCount }}</div>
              <div class="stat-label">总收藏量</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><Trophy /></el-icon>
            <div>
              <div class="stat-value">{{ stats.rating }}</div>
              <div class="stat-label">综合评分</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C"><Promotion /></el-icon>
            <div>
              <div class="stat-value">{{ stats.approvedCount }}</div>
              <div class="stat-label">通过审核数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>音乐状态分布</span>
          </template>
          <div class="status-list">
            <div class="status-item">
              <span>总音乐数：</span>
              <el-tag type="info" size="large">{{ musicStats.total }}</el-tag>
            </div>
            <div class="status-item">
              <span>审核通过：</span>
              <el-tag type="success" size="large">{{ musicStats.approved }}</el-tag>
            </div>
            <div class="status-item">
              <span>审核未通过：</span>
              <el-tag type="danger" size="large">{{ musicStats.rejected }}</el-tag>
            </div>
            <div class="status-item">
              <span>待审核：</span>
              <el-tag type="warning" size="large">{{ musicStats.pending }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近上传</span>
          </template>
          <el-table :data="recentMusic" style="width: 100%">
            <el-table-column prop="musicName" label="音乐名称" min-width="150">
              <template #default="{ row }">
                {{ cleanQuotes(row.musicName) }}
              </template>
            </el-table-column>
            <el-table-column prop="artist" label="艺术家" width="120">
              <template #default="{ row }">
                {{ cleanQuotes(row.artist) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Headset, Star, Trophy, Promotion } from '@element-plus/icons-vue'
import { getMusicListAPI } from '@/api/music'
import { ElMessage, ElMessageBox } from 'element-plus'
// 模拟数据
const stats = ref({
  playCount: '12.5K',
  favoriteCount: '3.2K',
  rating: '4.5分',
  approvedCount: 0
})

const musicStats = ref({
  total: 0,
  approved: 0,
  rejected: 0,
  pending: 0
})

const recentMusic = ref([])

const cleanQuotes = (str) => {
  if (!str) return ''
  return str.replace(/^"|"$/g, '')
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',  // 待审核
    1: 'success',  // 通过
    2: 'danger'    // 未通过
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '未通过'
  }
  return statusMap[status] || '未知'
}

const fetchMusicStats = async () => {
  try {
    // 获取所有音乐
    const res = await getMusicListAPI({ page: 1, size: 1000 })
    const list = res.data.list || []

    musicStats.value.total = list.length
    musicStats.value.approved = list.filter(m => m.status === 1).length
    musicStats.value.rejected = list.filter(m => m.status === 2).length
    musicStats.value.pending = list.filter(m => m.status === 0).length

    stats.value.approvedCount = musicStats.value.approved

    // 获取最近5条
    recentMusic.value = list.slice(0, 5)
  } catch (error) {
    console.error('获取统计数据失败:', error)
    if (error.response?.status === 403 || error.code === 'TOKEN_EXPIRED') {
      ElMessage.warning('登录状态过期，请退出重新登录')
    }
  }
}

onMounted(() => {
  fetchMusicStats()
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 48px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
}
</style>


<style scoped>
.dashboard {
  width: 100%;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 48px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
}
</style>
