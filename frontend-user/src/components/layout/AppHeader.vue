<template>
  <header class="app-header">
    <div class="header-left">
      <!-- 导航按钮 -->
      <div class="nav-buttons">
        <el-button circle @click="router.back()" :disabled="!canGoBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <el-button circle @click="router.forward()" :disabled="!canGoForward">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索音乐、歌手"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
      </div>
    </div>

    <div class="header-right">
      <!-- 未登录状态 -->
      <template v-if="!userStore.isLoggedIn">
        <!-- 创作者中心 -->
        <el-button text @click="openCreatorCenter">
          <el-icon><Promotion /></el-icon>
          创作者中心
        </el-button>
        <el-divider direction="vertical" />
        <el-button text @click="openAuth('register')">注册</el-button>
        <el-button type="primary" round @click="openAuth('login')">登录</el-button>
      </template>

      <!-- 已登录状态 -->
      <template v-else>
        <!-- 创作者中心 -->
        <el-button text @click="openCreatorCenter">
          <el-icon><Promotion /></el-icon>
          创作者中心
        </el-button>
        <el-divider direction="vertical" />
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" :src="userStore.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="username">{{ userStore.nickname }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="favorite">
                <el-icon><Star /></el-icon>
                我的收藏
              </el-dropdown-item>
              <el-dropdown-item command="history">
                <el-icon><Clock /></el-icon>
                播放历史
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import {
  Search,
  User,
  ArrowLeft,
  ArrowRight,
  ArrowDown,
  Star,
  Clock,
  SwitchButton,
  Promotion  // 添加图标
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref('')
const canGoBack = ref(true)
const canGoForward = ref(false)

// 创作者中心地址
const CREATOR_CENTER_URL = 'http://localhost:5174'

// 注入打开登录弹窗的方法
const openAuthDialog = inject('openAuthDialog', () => {})

function openAuth(mode) {
  openAuthDialog(mode)
}

// 打开创作者中心（新窗口）
function openCreatorCenter() {
  window.open(CREATOR_CENTER_URL, '_blank')
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({
      name: 'Search',
      query: { keyword: searchKeyword.value.trim() }
    })
    searchKeyword.value = ''
  }
}

function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push({ name: 'Profile' })
      break
    case 'favorite':
      router.push({ name: 'Favorite' })
      break
    case 'history':
      router.push({ name: 'History' })
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout(false)
        router.push({ name: 'Home' })
      }).catch(() => {})
      break
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(0, 0, 0, 0.5);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-buttons {
  display: flex;
  gap: 8px;

  .el-button {
    background: rgba(0, 0, 0, 0.7);
    border: none;
    color: $text-primary;

    &:hover:not(:disabled) {
      background: rgba(0, 0, 0, 0.8);
    }

    &:disabled {
      opacity: 0.5;
    }
  }
}

.search-box {
  width: 300px;

  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 20px;

    &:hover, &.is-focus {
      background: rgba(255, 255, 255, 0.15);
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;

  // 分隔线样式
  .el-divider--vertical {
    height: 16px;
    margin: 0 4px;
    border-color: rgba(255, 255, 255, 0.3);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px 4px 4px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(0, 0, 0, 0.8);
  }

  .username {
    font-size: 14px;
    font-weight: 500;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
