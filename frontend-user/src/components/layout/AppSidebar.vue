<template>
  <aside class="app-sidebar">
    <!-- Logo -->
    <div class="sidebar-logo" @click="router.push('/')">
      <el-icon :size="32"><Headset /></el-icon>
      <span class="logo-text">Music Player</span>
    </div>

    <!-- 主导航 -->
    <nav class="sidebar-nav">
      <div
        v-for="item in navItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        @click="handleNavClick(item)"
      >
        <el-icon :size="24"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
    </nav>

    <!-- 分割线 -->
    <div class="sidebar-divider"></div>

    <!-- 媒体库 -->
    <div class="sidebar-library">
      <div class="library-header">
        <el-icon :size="24"><Collection /></el-icon>
        <span>我的音乐库</span>
      </div>

      <div class="library-list">
        <div
          v-for="item in libraryItems"
          :key="item.path"
          class="library-item"
          :class="{ active: isActive(item.path) }"
          @click="handleNavClick(item)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import { HomeFilled, Search, Headset, Collection, Star, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const openAuthDialog = inject('openAuthDialog', () => {})

const navItems = [
  { path: '/', label: '首页', icon: HomeFilled },
  { path: '/search', label: '搜索', icon: Search }
]

const libraryItems = [
  { path: '/favorite', label: '我的收藏', icon: Star, requiresAuth: true },
  { path: '/history', label: '播放历史', icon: Clock, requiresAuth: true }
]

function isActive(path) {
  return route.path === path
}

function handleNavClick(item) {
  if (item.requiresAuth && !userStore.isLoggedIn) {
    openAuthDialog('login')
    return
  }
  router.push(item.path)
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.app-sidebar {
  display: flex;
  flex-direction: column;
  background: #000;
  padding: 16px 8px;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  margin-bottom: 16px;
  cursor: pointer;
  color: $text-primary;

  .logo-text {
    font-size: 18px;
    font-weight: 700;
  }
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  border-radius: $border-radius-md;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  font-weight: 500;

  &:hover {
    color: $text-primary;
  }

  &.active {
    color: $text-primary;
    background: $bg-tertiary;
  }
}

.sidebar-divider {
  height: 1px;
  background: $border-color;
  margin: 16px 16px;
}

.sidebar-library {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.library-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: $text-secondary;
  font-weight: 500;
}

.library-list {
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: transparent;
  }
  &:hover::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.3);
  }
}

.library-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: $border-radius-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  font-size: 14px;

  &:hover {
    background: $bg-tertiary;
    color: $text-primary;
  }

  &.active {
    color: $text-primary;
  }
}
</style>
