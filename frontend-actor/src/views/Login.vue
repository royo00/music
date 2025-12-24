<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>艺人登录</span>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item class="button-item">
        <el-button plain @click="handleRegister" :loading="loading">
          注册账号
        </el-button>
        <el-button type="primary" @click="handleLogin" :loading="loading">
          立即登录
        </el-button>
      </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginAPI, registerAPI } from '@/api/auth'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    const res = await loginAPI(loginForm.value)

    // 保存 token
    localStorage.setItem('token', res.data)
    localStorage.setItem('username', loginForm.value.username)

    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
const handleRegister = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    const res = await registerAPI({
      ...loginForm.value,
      role: 'actor'
    }
    )

    // // 保存 token
    // localStorage.setItem('token', res.data)
    // localStorage.setItem('username', loginForm.value.username)

    ElMessage.success('注册成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}
.button-item {
  :deep(.el-form-item__content) {
    display: flex;
    gap: 12px;
  }

  .el-button {
    flex: 1;
    height: 40px;
    font-size: 14px;
  }
}
</style>
