<template>
  <div class="music-list">
    <div class="list-header" v-if="showHeader">
      <div class="col-index">#</div>
      <div class="col-title">标题</div>
      <div class="col-artist">艺术家</div>
      <div class="col-duration">时长</div>
      <div class="col-actions"></div>
    </div>
    <div class="list-body">
      <div
        v-for="(music, index) in list"
        :key="music.id"
        class="list-item"
        :class="{ active: isPlaying(music.id) }"
        @dblclick="handlePlay(music)"
      >
        <div class="col-index">
          <span class="index-num">{{ index + 1 }}</span>
          <el-icon class="play-icon" @click="handlePlay(music)"><VideoPlay /></el-icon>
        </div>
        <div class="col-title">
          <img :src="music.coverUrl || defaultCover" class="music-cover" />
          <div class="music-info">
            <span class="music-name">{{ music.musicName }}</span>
          </div>
        </div>
        <div class="col-artist">{{ music.artist }}</div>
        <div class="col-duration">{{ formatDuration(music.duration) }}</div>
        <div class="col-actions">
          <!-- 显示平均评分 -->
          <div class="music-score" v-if="music.rating !== undefined">
            <span class="score-text">{{ formatScore(music.rating.avgScore) }}</span>
          </div>

          <!-- 评分按钮（在收藏按钮之前） -->
          <el-button text circle @click.stop="openRateDialog(music)" :title="`评分 (${music.rating ? formatScore(music.rating.avgScore) : '-'})`">
            <el-icon>
              <Trophy  />
            </el-icon>
          </el-button>

          <!-- 收藏按钮 -->
          <el-button text circle @click.stop="handleFavorite(music)" :title="music.isFavorite ? '取消收藏' : '收藏'">
            <el-icon>
              <StarFilled v-if="music.isFavorite" class="favorited" />
              <Star v-else />
            </el-icon>
          </el-button>

          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, music)">
            <el-button text circle>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="play">播放</el-dropdown-item>
                <el-dropdown-item command="addToPlaylist">添加到播放列表</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    <el-empty v-if="!list.length && !loading" :description="emptyText" />

    <!-- 评分对话框 -->
    <el-dialog
  v-model="ratingDialogVisible"
  :title="`为：${currentMusic?.musicName || ''} 评分`"
  width="420px"
  :close-on-click-modal="false"
>

      <div class="rating-body">
        <div style="margin-bottom: 12px">选择分数（1 - 5）</div>
        <el-rate v-model="selectedScore" :max="5" show-text />
      </div>
      <template #footer>
        <el-button text @click="ratingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRating" :loading="ratingLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { inject } from 'vue'
import { usePlayerStore, useMusicStore, useUserStore } from '@/stores'
import { formatDuration } from '@/utils/format'
import { VideoPlay, Star, StarFilled, More, Trophy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { rateMusic, getMusicRating } from '@/api/rate'

const props = defineProps({
  list: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  showHeader: { type: Boolean, default: true },
  emptyText: { type: String, default: '暂无数据' }
})

const playerStore = usePlayerStore()
const musicStore = useMusicStore()
const userStore = useUserStore()
const openAuthDialog = inject('openAuthDialog', () => {})

const defaultCover = '/default-cover.png'

function isPlaying(musicId) {
  return playerStore.currentMusic?.id === musicId
}

function handlePlay(music) {
  playerStore.play(music)
}

async function handleFavorite(music) {
  if (!userStore.isLoggedIn) {
    openAuthDialog('login')
    return
  }
  try {
    const newStatus = await musicStore.toggleFavorite(music.id, music.isFavorite)
    music.isFavorite = newStatus
    ElMessage.success(newStatus ? '已添加到收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleCommand(command, music) {
  switch (command) {
    case 'play':
      handlePlay(music)
      break
    case 'addToPlaylist':
      playerStore.addToPlaylist(music)
      ElMessage.success('已添加到播放列表')
      break
  }
}

// ========== 评分相关 ===========
const ratingDialogVisible = ref(false)
const selectedScore = ref(5)
const currentMusic = ref(null)
const ratingLoading = ref(false)

function formatScore(score) {
  if (score === undefined || score === null) return '-'
  return Number(score).toFixed(1)
}

async function openRateDialog(music) {
  // 如果用户未登录，引导登录
  if (!userStore.isLoggedIn) {
    openAuthDialog('login')
    return
  }

  currentMusic.value = music
  // 预设分数为 5 或者使用已有平均分向下取整（仅作参考）
  selectedScore.value = Math.min(5, Math.max(1, Math.round(music.rating?.avgScore || 5)))
  ratingDialogVisible.value = true
}

async function submitRating() {
  if (!currentMusic.value) return
  ratingLoading.value = true
  try {
    await rateMusic(currentMusic.value.id, selectedScore.value)
    ElMessage.success('评分成功')
    ratingDialogVisible.value = false
    // 刷新该歌曲的评分数据
    await refreshRatingFor(currentMusic.value)
  } catch (err) {
    console.error(err)
    ElMessage.error(err?.message || '评分失败')
  } finally {
    ratingLoading.value = false
  }
}

async function refreshRatingFor(music) {
  try {
    const res = await getMusicRating(music.id)
    // 后端返回的 data 是评分对象
    music.rating = res
  } catch (err) {
    console.error('获取评分失败', err)
  }
}

// 批量加载列表中歌曲的评分（简易实现）
async function loadRatingsForList() {
  if (!props.list || !props.list.length) return
  const promises = props.list.map(async (m) => {
    try {
      const r = await getMusicRating(m.id)
      m.rating = r
    } catch (e) {
      // 忽略单条失败
      m.rating = undefined
    }
  })
  await Promise.all(promises)
}

onMounted(() => {
  loadRatingsForList()
})

// 当 list 变化时重新加载评分
watch(() => props.list, () => {
  loadRatingsForList()
})
</script>




<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.music-list {
  width: 100%;
}

.list-header {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid $border-color;
  color: $text-secondary;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: $border-radius-sm;
  transition: background 0.15s;

  &:hover {
    background: $bg-tertiary;
    .index-num { display: none; }
    .play-icon { display: flex; }
  }

  &.active {
    .music-name { color: $primary-color; }
  }
}

.col-index {
  width: 40px;
  text-align: center;
  color: $text-secondary;

  .play-icon {
    display: none;
    cursor: pointer;
    justify-content: center;
    &:hover { color: $text-primary; }
  }
}

.col-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;

  .music-cover {
    width: 40px;
    height: 40px;
    border-radius: $border-radius-sm;
    object-fit: cover;
  }

  .music-info {
    flex: 1;
    min-width: 0;
  }

  .music-name {
    font-size: 14px;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.col-artist {
  width: 200px;
  color: $text-secondary;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-duration {
  width: 80px;
  flex-shrink: 0;     // ⭐ 关键
  text-align: right;
  color: $text-secondary;
  font-size: 14px;
}


.col-actions {
  width: 180px;       // ⭐ 给评分 + 收藏 + 更多 预留空间
  flex-shrink: 0;     // ⭐ 防止被压缩
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  .rate-icon {
    color: #f2b705;
  }

  .favorited { color: $primary-color; }
}

</style>
