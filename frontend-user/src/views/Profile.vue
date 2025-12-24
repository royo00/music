<template>
  <div class="profile-page page-container">
    <div class="profile-card">
      <h2 class="card-title">个人信息</h2>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        class="profile-form"
      >
        <!-- 头像 -->
        <el-form-item label="头像">
          <div class="avatar-uploader">
            <el-avatar :size="80" :src="avatarUrl">
              <el-icon :size="32"><User /></el-icon>
            </el-avatar>
            <el-upload
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
              accept="image/*"
            >
              <el-button size="small" class="upload-btn">更换头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 用户名（只读） -->
        <el-form-item label="用户名">
          <el-input
            :model-value="userStore.userInfo?.username"
            disabled
            :prefix-icon="User"
          />
        </el-form-item>

        <!-- 身份（只读） -->
        <el-form-item label="身份">
          <el-tag :type="isActor ? 'success' : 'info'" size="large">
            {{ isActor ? '创作者' : '普通用户' }}
          </el-tag>
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>

        <!-- 个性签名 -->
        <el-form-item label="个性签名" prop="signature">
          <el-input
            v-model="formData.signature"
            type="textarea"
            :rows="3"
            placeholder="介绍一下自己吧"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 保存按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSave"
            :loading="saving"
          >
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 修改密码 -->
    <div class="profile-card">
      <h2 class="card-title">修改密码</h2>

      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-position="top"
        class="profile-form"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            placeholder="6-20位新密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleChangePwd"
            :loading="changingPwd"
          >
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 账号操作 -->
    <div class="profile-card danger-zone">
      <h2 class="card-title">危险操作</h2>
      <div class="danger-actions">
        <div class="danger-item">
          <div class="danger-info">
            <h4>退出登录</h4>
            <p>退出当前账号，需要重新登录</p>
          </div>
          <el-button @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import { User, Message, Lock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formRules } from '@/utils/validator'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const pwdFormRef = ref(null)
const saving = ref(false)
const changingPwd = ref(false)

const isActor = computed(() => userStore.userInfo?.role === 'actor')
const avatarUrl = computed(() => userStore.userInfo?.avatar || '')

// 个人信息表单
const formData = reactive({
  email: '',
  signature: ''
})

const formRulesConfig = {
  email: formRules.email(false),
  signature: [
    { max: 200, message: '签名不能超过200个字符', trigger: 'blur' }
  ]
}

// 密码表单
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: formRules.password(),
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

onMounted(() => {
  // 初始化表单数据
  if (userStore.userInfo) {
    formData.email = userStore.userInfo.email || ''
    formData.signature = userStore.userInfo.signature || ''
  }
})

// 头像上传前验证
function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 上传头像
async function uploadAvatar({ file }) {
  try {
    await userStore.uploadAvatar(file)
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('头像上传失败')
  }
}

// 保存个人信息
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await userStore.updateProfile({
      email: formData.email,
      signature: formData.signature
    })
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 修改密码
async function handleChangePwd() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await userStore.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdFormRef.value?.resetFields()
    // 退出登录
    await userStore.logout()
    router.push('/')
  } catch {
    ElMessage.error('密码修改失败')
  } finally {
    changingPwd.value = false
  }
}

// 退出登录
async function handleLogout() {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出登录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.profile-page {
  max-width: 640px;
  padding-bottom: 40px;
}

.profile-card {
  background: $bg-secondary;
  border-radius: $border-radius-lg;
  padding: 24px;
  margin-bottom: 24px;

  .card-title {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid $border-color;
  }
}

.profile-form {
  max-width: 400px;
}

.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 16px;

  .upload-btn {
    margin-left: 8px;
  }
}

.danger-zone {
  border: 1px solid rgba($danger-color, 0.3);

  .card-title {
    color: $danger-color;
  }
}

.danger-actions {
  .danger-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;

    &:not(:last-child) {
      border-bottom: 1px solid $border-color;
    }
  }

  .danger-info {
    h4 {
      margin: 0 0 4px;
      font-size: 14px;
      font-weight: 600;
    }

    p {
      margin: 0;
      font-size: 12px;
      color: $text-secondary;
    }
  }
}
</style>
