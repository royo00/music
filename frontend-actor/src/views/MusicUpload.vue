<template>
  <div class="music-upload">
    <el-card>
      <template #header>
        <span>音乐上传</span>
      </template>

      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px"
      >
        <el-form-item label="音乐文件" prop="file" required>
          <el-upload
            ref="musicUploadRef"
            :auto-upload="false"
            :limit="1"
            accept="audio/*"
            :on-change="handleMusicChange"
            :on-remove="handleMusicRemove"
            :file-list="musicFileList"
          >
            <el-button type="primary">选择音乐文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 mp3, wav, flac 等音频格式</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="音乐名称" prop="musicName">
          <el-input v-model="uploadForm.musicName" placeholder="请输入音乐名称" />
        </el-form-item>

        <el-form-item label="艺术家" prop="artist">
          <el-input v-model="uploadForm.artist" placeholder="请输入艺术家名称" />
        </el-form-item>

        <el-form-item label="专辑">
          <el-input v-model="uploadForm.album" placeholder="请输入专辑名称(可选)" />
        </el-form-item>

        <el-form-item label="封面图片">
          <el-upload
            ref="coverUploadRef"
            :auto-upload="false"
            :limit="1"
            accept="image/*"
            :on-change="handleCoverChange"
            :on-remove="handleCoverRemove"
            :file-list="coverFileList"
            list-type="picture"
          >
            <el-button type="primary">选择封面图片</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 jpg, png 等图片格式</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入音乐描述(可选)"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="uploading">
            {{ uploading ? '上传中...' : '提交上传' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-progress
        v-if="uploading"
        :percentage="uploadProgress"
        :color="customColors"
        style="margin-top: 20px; max-width: 600px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadMusicAPI } from '@/api/music'

const uploadFormRef = ref(null)
const musicUploadRef = ref(null)
const coverUploadRef = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)

const uploadForm = ref({
  file: null,
  musicName: '',
  artist: '',
  album: '',
  description: '',
  cover: null
})

const musicFileList = ref([])
const coverFileList = ref([])

const rules = {
  musicName: [
    { required: true, message: '请输入音乐名称', trigger: 'blur' }
  ],
  artist: [
    { required: true, message: '请输入艺术家名称', trigger: 'blur' }
  ]
}

const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 }
]

const handleMusicChange = (file) => {
  uploadForm.value.file = file.raw
  musicFileList.value = [file]
}

const handleMusicRemove = () => {
  uploadForm.value.file = null
  musicFileList.value = []
}

const handleCoverChange = (file) => {
  uploadForm.value.cover = file.raw
  coverFileList.value = [file]
}

const handleCoverRemove = () => {
  uploadForm.value.cover = null
  coverFileList.value = []
}

const handleSubmit = async () => {
  try {
    // 验证表单
    await uploadFormRef.value.validate()

    // 检查是否选择了音乐文件
    if (!uploadForm.value.file) {
      ElMessage.warning('请选择音乐文件')
      return
    }

    uploading.value = true
    uploadProgress.value = 0

    // 模拟上传进度
    const progressTimer = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 200)

    // 构建 FormData
    const formData = new FormData()
    formData.append('file', uploadForm.value.file)
    formData.append('musicName', uploadForm.value.musicName)
    formData.append('artist', uploadForm.value.artist)

    if (uploadForm.value.album) {
      formData.append('album', uploadForm.value.album)
    }

    if (uploadForm.value.description) {
      formData.append('description', uploadForm.value.description)
    }

    if (uploadForm.value.cover) {
      formData.append('cover', uploadForm.value.cover)
    }

    // 发送请求
    await uploadMusicAPI(formData)

    clearInterval(progressTimer)
    uploadProgress.value = 100

    ElMessage.success('上传成功，等待审核')

    // 重置表单
    setTimeout(() => {
      handleReset()
      uploadProgress.value = 0
    }, 1000)

  } catch (error) {
    ElMessage.error('上传失败: ' + (error.message || '未知错误'))
    uploadProgress.value = 0
  } finally {
    uploading.value = false
  }
}

const handleReset = () => {
  uploadFormRef.value?.resetFields()
  uploadForm.value = {
    file: null,
    musicName: '',
    artist: '',
    album: '',
    description: '',
    cover: null
  }
  musicFileList.value = []
  coverFileList.value = []
  musicUploadRef.value?.clearFiles()
  coverUploadRef.value?.clearFiles()
}
</script>

<style scoped>
.music-upload {
  width: 100%;
}

.el-upload__tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
