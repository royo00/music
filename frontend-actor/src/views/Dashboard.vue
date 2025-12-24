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
import { getArtistMusicStatsAPI } from '@/api/rate' // 1. 引入新接口
import { ElMessage } from 'element-plus'

// 2. 初始化 stats，不再使用模拟字符串，改为数字初始值
const stats = ref({
  playCount: 0,
  favoriteCount: 0,
  rating: '0.0', // 评分通常保留一位小数，用字符串表示
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

// 3. 新增：获取并聚合 播放/收藏/评分 数据
const fetchRateStats = async () => {
  try {
    const res = await getArtistMusicStatsAPI()
    const list = res.data || []

    let totalPlay = 0
    let totalFavorite = 0
    let weightedScoreSum = 0 // 加权分数总和
    let totalRatingsSum = 0  // 总评分人数

    list.forEach(item => {
      // 累加播放和收藏
      totalPlay += (item.playCount || 0)
      totalFavorite += (item.favoriteCount || 0)

      // 计算加权平均分所需数据
      if (item.totalRatings > 0) {
        weightedScoreSum += (item.avgScore * item.totalRatings)
        totalRatingsSum += item.totalRatings
      }
    })

    // 更新状态
    stats.value.playCount = totalPlay
    stats.value.favoriteCount = totalFavorite

    // 计算平均分：如果没人评分过，则为 0.0
    stats.value.rating = totalRatingsSum > 0
      ? (weightedScoreSum / totalRatingsSum).toFixed(1)
      : '0.0'

  } catch (error) {
    console.error('获取互动统计数据失败:', error)
  }
}

// 4. 原有的获取列表逻辑（保持不变，用于获取 approvedCount 和 recentMusic）
const fetchMusicStats = async () => {
  try {
    // 获取所有音乐
    const res = await getMusicListAPI({ page: 1, size: 1000 })
    const list = res.data.list || []

    musicStats.value.total = list.length
    musicStats.value.approved = list.filter(m => m.status === 1).length
    musicStats.value.rejected = list.filter(m => m.status === 2).length
    musicStats.value.pending = list.filter(m => m.status === 0).length

    // 这里只更新 approvedCount，其他的由 fetchRateStats 更新
    stats.value.approvedCount = musicStats.value.approved

    // 获取最近5条
    recentMusic.value = list.slice(0, 5)
  } catch (error) {
    console.error('获取音乐列表统计失败:', error)
    if (error.response?.status === 403 || error.code === 'TOKEN_EXPIRED') {
      ElMessage.warning('登录状态过期，请退出重新登录')
    }
  }
}

onMounted(() => {
  // 并行请求两个接口
  fetchMusicStats()
  fetchRateStats()
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
