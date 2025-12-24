<template>
  <div id="app" class="dark">
    <router-view />
    <AuthDialog ref="authDialogRef" />
  </div>
</template>

<script setup>
import { ref, provide, onMounted } from 'vue'
import { useUserStore } from '@/stores'
import AuthDialog from '@/components/common/AuthDialog.vue'

const userStore = useUserStore()
const authDialogRef = ref(null)

// 提供打开登录弹窗的方法
function openAuthDialog(mode = 'login') {
  authDialogRef.value?.open(mode)
}
provide('openAuthDialog', openAuthDialog)

onMounted(async () => {
  await userStore.initUserState()
})
</script>

<style lang="scss">
html.dark {
  color-scheme: dark;
}
</style>
