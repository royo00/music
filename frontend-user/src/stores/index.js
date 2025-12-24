// src/stores/index.js
/**
 * Pinia Store 统一导出
 */

import { createPinia } from 'pinia'

// 创建 Pinia 实例
const pinia = createPinia()

// 导出 Store
export { useUserStore } from './user'
export { useMusicStore } from './music'
export { usePlayerStore } from './player'

export default pinia
