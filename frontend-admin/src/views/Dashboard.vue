<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon color="#409eff" :size="24"><User /></el-icon>
              <span>注册用户数量</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="stat-number">{{ userCount }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon color="#67c23a" :size="24"><Headset /></el-icon>
              <span>音乐数量</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="stat-number">{{ musicCount }}</div>
            <div class="stat-label">总音乐数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon color="#e6a23c" :size="24"><Clock /></el-icon>
              <span>待审核音乐</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="stat-number">{{ pendingMusicCount }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon color="#f56c6c" :size="24"><Warning /></el-icon>
              <span>禁用用户</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="stat-number">{{ disabledUserCount }}</div>
            <div class="stat-label">已禁用</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, getMusicList } from '../api/admin'

const userCount = ref(0)
const musicCount = ref(0)
const pendingMusicCount = ref(0)
const disabledUserCount = ref(0)

const loadStatistics = async () => {
  try {
    // 获取用户统计
    const userRes = await getUserList({ page: 1, size: 1 })
    userCount.value = userRes.data?.total || userRes.total || 0

    // 获取禁用用户数
    const disabledUserRes = await getUserList({ page: 1, size: 1, status: 0 })
    disabledUserCount.value = disabledUserRes.data?.total || disabledUserRes.total || 0

    // 获取音乐统计
    const musicRes = await getMusicList({ page: 1, size: 1 })
    musicCount.value = musicRes.data?.total || musicRes.total || 0

    // 获取待审核音乐数
    const pendingMusicRes = await getMusicList({ page: 1, size: 1, status: 0 })
    pendingMusicCount.value = pendingMusicRes.data?.total || pendingMusicRes.total || 0
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
}

.stat-content {
  text-align: center;
  padding: 30px 0;
}

.stat-number {
  font-size: 48px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  color: #909399;
}
</style>
