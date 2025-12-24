<template>
  <div class="main-layout">
    <AppSidebar class="layout-sidebar" />
    <div class="layout-main">
      <AppHeader class="layout-header" />
      <main class="layout-content" ref="contentRef">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    <PlayerBar class="layout-player" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import PlayerBar from './PlayerBar.vue'

const contentRef = ref(null)
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.main-layout {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  padding-bottom: $player-height;
}

.layout-sidebar {
  width: $sidebar-width;
  flex-shrink: 0;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: linear-gradient(180deg, #1a1a1a 0%, $bg-color 300px);
}

.layout-header {
  height: $header-height;
  flex-shrink: 0;
}

.layout-content {
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 8px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.3);
    border-radius: 4px;
  }
}

.layout-player {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: $player-height;
  z-index: 100;
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
