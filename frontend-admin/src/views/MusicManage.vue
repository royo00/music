<template>
  <div class="music-manage">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="音乐名称/艺术家"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="musicName" label="音乐名称" width="200">
          <template #default="{ row }">
            {{ row.musicName.replace(/"/g, '') }}
          </template>
        </el-table-column>
        <el-table-column prop="artist" label="艺术家" width="150">
          <template #default="{ row }">
            {{ row.artist.replace(/"/g, '') }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadUsername" label="上传者" width="120" />
        <el-table-column prop="duration" label="时长(秒)" width="100" />
        <el-table-column prop="playCount" label="播放次数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="审核意见" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleAudit(row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="warning"
              size="small"
              @click="handleAudit(row, 2)"
            >
              拒绝
            </el-button>
            <el-button
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
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditForm.status === 1 ? '通过审核' : '拒绝审核'"
      width="500px"
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMusicList, updateMusicStatus, deleteMusic } from '../api/admin'

const loading = ref(false)
const tableData = ref([])
const auditDialogVisible = ref(false)

const searchForm = reactive({
  status: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const auditForm = reactive({
  musicId: null,
  status: null,
  remark: ''
})

const loadMusicList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    if (searchForm.status !== '') params.status = searchForm.status
    if (searchForm.keyword) params.keyword = searchForm.keyword

    const res = await getMusicList(params)
    const data = res.data || res
    tableData.value = data.list || []
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载音乐列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadMusicList()
}

const handleReset = () => {
  searchForm.status = ''
  searchForm.keyword = ''
  pagination.page = 1
  loadMusicList()
}

const handleAudit = (row, status) => {
  auditForm.musicId = row.id
  auditForm.status = status
  auditForm.remark = ''
  auditDialogVisible.value = true
}

const confirmAudit = async () => {
  try {
    await updateMusicStatus(
      auditForm.musicId,
      auditForm.status,
      auditForm.remark
    )
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    loadMusicList()
  } catch (error) {
    console.error('审核失败:', error)
  }
}


const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该音乐吗？删除后无法恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMusic(row.id)
      ElMessage.success('删除成功')
      loadMusicList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleSizeChange = () => {
  loadMusicList()
}

const handleCurrentChange = () => {
  loadMusicList()
}

onMounted(() => {
  loadMusicList()
})
</script>

<style scoped>
.music-manage {
  padding: 20px;
}
</style>
