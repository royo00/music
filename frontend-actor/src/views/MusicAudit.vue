<template>
  <div class="music-audit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>音乐审计</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="未通过" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索音乐名称或艺术家"
            clearable
            style="width: 250px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="filteredTableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="musicName" label="音乐名称" min-width="150">
          <template #default="{ row }">
            {{ row.musicName.replace(/^"|"$/g, '') }}
          </template>
        </el-table-column>
        <el-table-column prop="artist" label="艺术家" width="120">
          <template #default="{ row }">
            {{ row.artist.replace(/^"|"$/g, '') }}
          </template>
        </el-table-column>
        <el-table-column prop="album" label="专辑" width="120" />
        <el-table-column prop="duration" label="时长(秒)" width="100" />
        <el-table-column prop="playCount" label="播放量" width="100" />
        <el-table-column prop="avgScore" label="评分" width="100">
          <template #default="{ row }">
            <div v-if="row.avgScore > 0">
              <el-icon color="#E6A23C"><StarFilled /></el-icon>
              {{ row.avgScore.toFixed(1) }}
            </div>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="120" />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="dialogVisible" title="音乐详情" width="600px">
      <el-descriptions :column="1" border v-if="currentMusic">
        <el-descriptions-item label="音乐名称">
          {{ currentMusic.musicName.replace(/^"|"$/g, '') }}
        </el-descriptions-item>
        <el-descriptions-item label="艺术家">
          {{ currentMusic.artist.replace(/^"|"$/g, '') }}
        </el-descriptions-item>
        <el-descriptions-item label="专辑">
          {{ currentMusic.album || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="时长">
          {{ currentMusic.duration }} 秒
        </el-descriptions-item>
        <el-descriptions-item label="播放量">
          {{ currentMusic.playCount }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentMusic.status)">
            {{ getStatusText(currentMusic.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">
          {{ currentMusic.remark || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="描述">
          {{ currentMusic.description || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="上传时间">
          {{ currentMusic.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ currentMusic.updateTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMusicListAPI, deleteMusicAPI } from '@/api/music'
import { getArtistMusicStatsAPI } from '@/api/rate'

const loading = ref(false)
const dialogVisible = ref(false)
const currentMusic = ref(null)

const searchForm = ref({
  status: null,
  keyword: ''
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
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

// 客户端过滤
const filteredTableData = computed(() => {
  let data = tableData.value

  // 关键词搜索
  if (searchForm.value.keyword) {
    const keyword = searchForm.value.keyword.toLowerCase()
    data = data.filter(item => {
      const musicName = item.musicName.replace(/^"|"$/g, '').toLowerCase()
      const artist = item.artist.replace(/^"|"$/g, '').toLowerCase()
      return musicName.includes(keyword) || artist.includes(keyword)
    })
  }

  return data
})

// const fetchMusicList = async () => {
//   try {
//     loading.value = true
//     const res = await getMusicListAPI({
//       page: pagination.value.page,
//       size: pagination.value.size,
//       status: searchForm.value.status
//     })

//     tableData.value = res.data.list || []
//     pagination.value.total = res.data.total || 0
//   } catch (error) {
//     ElMessage.error('获取音乐列表失败')
//   } finally {
//     loading.value = false
//   }
// }

const fetchMusicList = async () => {
  try {
    loading.value = true

    // 2. 并行请求：同时获取音乐列表 和 评分统计数据
    const [listRes, statsRes] = await Promise.all([
      getMusicListAPI({
        page: pagination.value.page,
        size: pagination.value.size,
        status: searchForm.value.status
      }),
      getArtistMusicStatsAPI() // 获取统计数据
    ])

    const musicList = listRes.data.list || []
    const statsList = statsRes.data || []

    // 3. 数据合并：将统计数据映射到音乐列表中
    // 创建一个 Map 以便快速查找，key 为 musicId
    const statsMap = new Map(statsList.map(item => [item.musicId, item]))

    // 遍历音乐列表，注入 avgScore (也可以注入 playCount 以保持数据最新)
    tableData.value = musicList.map(music => {
      const stat = statsMap.get(music.id) // 假设音乐列表的 id 对应统计数据的 musicId
      return {
        ...music,
        // 如果找到了统计数据，使用 stats 中的 avgScore，否则为 0
        avgScore: stat ? stat.avgScore : 0,
        // 如果你想用统计接口的实时播放量覆盖列表的播放量，可以解开下面这行注释
        // playCount: stat ? stat.playCount : music.playCount
      }
    })

    pagination.value.total = listRes.data.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchMusicList()
}

const handleReset = () => {
  searchForm.value = {
    status: null,
    keyword: ''
  }
  pagination.value.page = 1
  fetchMusicList()
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  fetchMusicList()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchMusicList()
}

const handleView = (row) => {
  currentMusic.value = row
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该音乐吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteMusicAPI(row.id)
    ElMessage.success('删除成功')
    fetchMusicList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchMusicList()
})
</script>

<style scoped>
.music-audit {
  width: 100%;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.search-form {
  margin-bottom: 20px;
}
</style>
