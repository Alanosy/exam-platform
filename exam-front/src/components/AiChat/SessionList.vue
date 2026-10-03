<template>
  <div class="session-list">
    <!-- 标题交给外层（整框视图的头部已经有了），这里只留刷新 -->
    <div class="list-head" :class="{ 'is-end': !title }">
      <span v-if="title" class="list-title">{{ title }}</span>
      <el-button text size="small" title="刷新" :loading="loading" @click="emit('refresh')">
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>

    <div v-if="!sessions.length && !loading" class="list-empty">
      {{ emptyText }}
    </div>

    <div v-for="item in sessions" :key="item.id" class="session-item" :class="{ 'is-active': item.id === activeId }" @click="emit('pick', item.id)">
      <div class="item-top">
        <span class="item-title" :title="item.title">{{ item.title }}</span>
        <span class="item-time">{{ formatTime(item.updatedAt) }}</span>
      </div>
      <div class="item-sub">
        <span>{{ item.messageCount ?? 0 }} 条</span>
        <span v-if="item.intent" class="item-intent">{{ intentLabel(item.intent) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue';
import type { AiChatSessionVO } from '@/api/system/ai/types';

/**
 * 历史会话列表
 *
 * 为什么列表里不直接带消息：一次拉几十个会话的完整聊天记录太重，
 * 点进去才按需取（/ai/chat/session/{id}）。
 */
withDefaults(
  defineProps<{
    sessions: AiChatSessionVO[];
    activeId?: string;
    loading?: boolean;
    emptyText?: string;
    /** 置空则不渲染标题行文字（外层头部已有标题时使用） */
    title?: string;
  }>(),
  { activeId: '', loading: false, emptyText: '还没有历史会话', title: '历史会话' }
);

const emit = defineEmits<{ pick: [id: string]; refresh: [] }>();

const INTENT_LABEL: Record<string, string> = {
  question_create: '出题',
  exam_analysis: '答题分析',
  question_search: '检索试题',
  chat: '问答',
  general: '问答'
};

const intentLabel = (intent: string) => INTENT_LABEL[intent] || '';

const formatTime = (ts?: number) => {
  if (!ts) return '';
  const date = new Date(ts * 1000);
  const now = new Date();
  const sameDay = date.toDateString() === now.toDateString();
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  return sameDay ? `${hh}:${mm}` : `${date.getMonth() + 1}/${date.getDate()}`;
};
</script>

<style scoped lang="scss">
.session-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;

  // 没有标题时刷新按钮靠右，避免孤零零一个图标顶在左边
  &.is-end {
    justify-content: flex-end;
  }
}

.list-title {
  font-size: 13px;
  font-weight: 600;
}

.list-empty {
  padding: 12px 2px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.session-item {
  padding: 8px 10px;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;

  &:hover {
    border-color: var(--el-color-primary-light-5);
  }

  &.is-active {
    background: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary-light-5);
  }
}

.item-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.item-title {
  overflow: hidden;
  font-size: 12px;
  color: var(--el-text-color-regular);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.item-sub {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.item-intent {
  padding: 0 6px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border-radius: 8px;
}
</style>
