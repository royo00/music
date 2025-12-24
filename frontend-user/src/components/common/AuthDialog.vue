<template>
  <el-dialog
    v-model="visible"
    :title="isLogin ? '登录' : '注册'"
    width="400px"
    :close-on-click-modal="false"
    @closed="handleClosed"
    class="auth-dialog"
  >
    <!-- 登录表单 -->
    <el-form
      v-if="isLogin"
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      label-position="top"
      @submit.prevent="handleLogin"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="请输入用户名"
          :prefix-icon="User"
          size="large"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          :prefix-icon="Lock"
          size="large"
          show-password
          @keyup.enter="handleLogin"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="handleLogin"
          class="submit-btn"
        >
          登录
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 注册表单 -->
    <el-form
      v-else
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      label-position="top"
      @submit.prevent="handleRegister"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="registerForm.username"
          placeholder="4-20位字母、数字或下划线"
          :prefix-icon="User"
          size="large"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="6-20位密码"
          :prefix-icon="Lock"
          size="large"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          :prefix-icon="Lock"
          size="large"
          show-password
        />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="registerForm.email"
          placeholder="请输入邮箱（选填）"
          :prefix-icon="Message"
          size="large"
        />
      </el-form-item>
      <!-- <el-form-item label="注册身份" prop="role">
        <el-radio-group v-model="registerForm.role" size="large">
          <el-radio-button value="user">普通用户</el-radio-button>
          <el-radio-button value="actor">创作者</el-radio-button>
        </el-radio-group>
      </el-form-item> -->
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="handleRegister"
          class="submit-btn"
        >
          注册
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 切换登录/注册 -->
    <div class="auth-switch">
      <span v-if="isLogin">
        还没有账号？
        <el-button type="primary" link @click="switchMode('register')">立即注册</el-button>
      </span>
      <span v-else>
        已有账号？
        <el-button type="primary" link @click="switchMode('login')">立即登录</el-button>
      </span>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, provide } from 'vue'
import { useUserStore } from '@/stores'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { formRules } from '@/utils/validator'
import user from '@/api/user'

const userStore = useUserStore()

const visible = ref(false)
const mode = ref('login') // 'login' | 'register'
const loading = ref(false)

const isLogin = computed(() => mode.value === 'login')

// 登录表单
const loginFormRef = ref(null)
const loginForm = reactive({
  username: '',
  password: ''
})
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册表单
const registerFormRef = ref(null)
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  role: 'user'
})

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: formRules.username(),
  password: formRules.password(),
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: formRules.email(false),
  role: [{ required: true, message: '请选择注册身份', trigger: 'change' }]
}

// 打开弹窗
function open(authMode = 'login') {
  mode.value = authMode
  visible.value = true
}

// 关闭后重置
function handleClosed() {
  loginFormRef.value?.resetFields()
  registerFormRef.value?.resetFields()
  loginForm.username = ''
  loginForm.password = ''
  registerForm.username = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.email = ''
  registerForm.role = 'user'
}

// 切换模式
function switchMode(newMode) {
  mode.value = newMode
}

// 登录处理
async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const success = await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    if (success) {
      ElMessage.success('登录成功')
      visible.value = false
    }
  } finally {
    loading.value = false
  }
}

// 注册处理
async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const success = await userStore.register({
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email || undefined,
      role: "user"
    })
    if (success) {
      ElMessage.success('注册成功，请登录')
      switchMode('login')
      // 自动填充用户名
      loginForm.username = registerForm.username
    }
  } finally {
    loading.value = false
  }
}

// 暴露方法给父组件
defineExpose({ open })
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.auth-dialog {
  :deep(.el-dialog) {
    border-radius: $border-radius-lg;
    background: $bg-secondary;
  }

  :deep(.el-dialog__header) {
    padding: 20px 24px 16px;
    margin-right: 0;
    border-bottom: 1px solid $border-color;

    .el-dialog__title {
      font-size: 20px;
      font-weight: 600;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-top: 8px;
}

.auth-switch {
  text-align: center;
  margin-top: 16px;
  color: $text-secondary;
  font-size: 14px;
}

:deep(.el-radio-group) {
  width: 100%;
  display: flex;

  .el-radio-button {
    flex: 1;

    .el-radio-button__inner {
      width: 100%;
    }
  }
}
</style>
