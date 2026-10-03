<template>
  <div class="chat-msg" :class="`is-${msg.role}`">
    <div class="avatar">
      <span v-if="msg.role === 'user'">我</span>
      <span v-else class="ai-avatar">AI</span>
    </div>

    <div class="msg-main">
      <div class="bubble">
        <template v-if="msg.role === 'user'">
          <span class="plain-text">{{ msg.content }}</span>
        </template>

        <template v-else>
          <div v-if="msg.loading && !msg.content" class="typing">
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
            <span class="typing-text">正在处理…</span>
          </div>
          <MarkdownView v-else-if="msg.content" :content="msg.content" />
          <div v-if="msg.error" class="msg-error">{{ msg.error }}</div>
        </template>
      </div>

      <RunTrace v-if="msg.trace && msg.trace.length" :steps="msg.trace" />

      <AskCard
        v-if="msg.ask"
        :ask="msg.ask"
        :loading="msg.loading"
        :answered="msg.answered"
        @submit="(answers) => emit('submit', msg.id, answers)"
        @cancel="emit('cancel', msg.id)"
      />
      <div v-if="msg.ask && msg.answered" class="answered-tip">已提交，等待结果…</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import MarkdownView from './MarkdownView.vue';
import RunTrace from './RunTrace.vue';
import AskCard from './AskCard.vue';
import type { ChatMessageItem } from './types';

defineProps<{ msg: ChatMessageItem }>();
const emit = defineEmits<{ submit: [id: number, answers: Record<string, any>]; cancel: [id: number] }>();
</script>

<style scoped lang="scss">
.chat-msg {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;

  &.is-user {
    flex-direction: row-reverse;

    .msg-main {
      align-items: flex-end;
    }

    .bubble {
      background: var(--el-color-primary);
      color: #fff;
      border: none;
    }

    .avatar {
      background: var(--el-color-primary-light-3);
      color: #fff;
    }
  }
}

.avatar {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  font-size: 11px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-8);
  border-radius: 50%;
}

.ai-avatar {
  font-weight: 600;
}

.msg-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  max-width: calc(100% - 40px);
  flex: 1;
}

.bubble {
  padding: 8px 10px;
  font-size: 13px;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  word-break: break-word;
}

.plain-text {
  white-space: pre-wrap;
}

.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.typing-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--el-color-primary-light-3);
  animation: blink 1.2s infinite ease-in-out;

  &:nth-child(2) {
    animation-delay: 0.2s;
  }

  &:nth-child(3) {
    animation-delay: 0.4s;
  }
}

.typing-text {
  margin-left: 4px;
}

@keyframes blink {
  0%,
  80%,
  100% {
    opacity: 0.3;
  }

  40% {
    opacity: 1;
  }
}

.msg-error {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-color-danger);
}

.answered-tip {
  margin-top: 6px;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}
</style>
