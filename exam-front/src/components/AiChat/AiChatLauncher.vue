<template>
  <!-- 悬浮入口：不用 el-drawer，因为它 teleport 到 body 后会被全屏元素挤掉层级 -->
  <div
    v-show="!visible"
    class="ai-fab"
    :class="{ 'is-offline': !aiEnabled }"
    :title="aiEnabled ? 'AI 助手（Ctrl/⌘ + K）' : 'AI 助手：服务未连接'"
    @click="open"
  >
    <el-icon :size="20"><ChatDotRound /></el-icon>
    <span v-if="!aiEnabled" class="fab-dot"></span>
  </div>

  <transition name="ai-slide">
    <div v-show="visible" class="ai-panel">
      <header class="ai-head">
        <div class="head-left">
          <el-icon :size="16" class="head-icon"><ChatDotRound /></el-icon>
          <span class="head-title">AI 助手</span>
          <span class="head-sub">考试系统智能助理</span>
        </div>
        <div class="head-right">
          <el-button text size="small" title="新建会话" @click="newSession">
            <el-icon><RefreshLeft /></el-icon>
          </el-button>
          <el-button text size="small" title="关闭" @click="close">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </header>

      <div ref="bodyRef" class="ai-body">
        <div v-if="!messages.length" class="ai-empty">
          <div class="empty-title">我能帮你做这些</div>
          <div class="empty-desc">直接说人话就行，缺信息我会问你，写库前也会先让你确认。</div>
          <div class="chips">
            <div v-for="sample in SAMPLES" :key="sample" class="chip" @click="useSample(sample)">{{ sample }}</div>
          </div>
        </div>

        <ChatMessage v-for="msg in messages" :key="msg.id" :msg="msg" @submit="onAskSubmit" @cancel="onAskCancel" />

        <div v-if="!aiEnabled" class="ai-offline">AI 服务未连接，回复会是降级文案。请启动 ruoyi-exam-agent 后重试。</div>
      </div>

      <footer class="ai-foot">
        <el-input
          v-model="draft"
          type="textarea"
          resize="none"
          :autosize="{ minRows: 2, maxRows: 5 }"
          :placeholder="aiEnabled ? '描述你要做的事，Enter 发送 / Shift+Enter 换行' : 'AI 服务未连接，仍可发送'"
          @keydown.enter.exact.prevent="submitDraft"
        />
        <div class="foot-row">
          <span class="foot-hint">Ctrl/⌘ + K 开关 · 会按当前租户与身份取数</span>
          <el-button type="primary" size="small" :loading="sending" :disabled="!draft.trim()" @click="submitDraft"> 发送 </el-button>
        </div>
      </footer>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { ChatDotRound, Close, RefreshLeft } from '@element-plus/icons-vue';
import ChatMessage from './ChatMessage.vue';
import type { ChatMessageItem } from './types';
import type { AiChatVO } from '@/api/system/ai/types';
import { aiChat } from '@/api/system/ai/chat';
import { getAiEnabled } from '@/api/system/ai';

/**
 * 全局 AI 聊天窗
 *
 * 交互参照主流 Agent 对话（Trae / Claude 那套）：
 * - 一句话下指令，缺信息时**中断**并弹出表单，而不是回一段话让用户自己猜；
 * - 每一步工具 / 技能调用都留在运行流程里，可展开回看；
 * - 写库前一定会先给确认卡。
 *
 * 会话状态只在组件里维护：本组件挂在 Layout 上常驻，
 * 切页面不会丢会话，关掉浏览器再进来就是新会话，符合预期。
 */
const SAMPLES = ['帮我创建 10 道关于计算机基础知识的题到计算机题库', '期中考试考卷的答题情况怎么样', '找 5 道关于 TCP 的题'];

const visible = ref(false);
const aiEnabled = ref(false);
const sending = ref(false);
const draft = ref('');
const sessionId = ref('');
const messages = ref<ChatMessageItem[]>([]);
const bodyRef = ref<HTMLElement>();
let seq = 0;

const scrollToBottom = async () => {
  await nextTick();
  const el = bodyRef.value;
  if (el) {
    el.scrollTop = el.scrollHeight;
  }
};

const pushAssistant = (init: Partial<ChatMessageItem>): number => {
  const id = ++seq;
  messages.value.push({
    id,
    role: 'assistant',
    content: '',
    loading: false,
    ...init
  });
  return id;
};

/**
 * 请求一轮对话
 *
 * @param payload message 表示正常提问；answers 表示在回答中断卡
 * @param targetId 中断卡所在消息ID，用于把那张卡置为「已提交」
 */
const call = async (payload: { message?: string; answers?: Record<string, any> }, targetId?: number) => {
  if (sending.value) {
    return;
  }
  sending.value = true;
  const placeholderId = pushAssistant({ loading: true });
  if (targetId !== undefined) {
    const target = messages.value.find((m) => m.id === targetId);
    if (target) {
      target.answered = true;
      target.loading = true;
    }
  }
  await scrollToBottom();

  try {
    const res: any = await aiChat({
      sessionId: sessionId.value || undefined,
      ...payload
    });
    // 拦截器已解一层壳，这里两种形态都兜一下
    const vo: AiChatVO = (res?.data ?? res) as AiChatVO;
    if (vo?.sessionId) {
      sessionId.value = vo.sessionId;
    }
    const target = messages.value.find((m) => m.id === placeholderId);
    if (target) {
      target.loading = false;
      target.content = vo?.reply || '';
      target.trace = vo?.trace || [];
      target.ask = vo?.ask || null;
    }
  } catch (e: any) {
    const target = messages.value.find((m) => m.id === placeholderId);
    if (target) {
      target.loading = false;
      target.error = e?.msg || e?.message || '请求失败，请稍后重试';
    }
  } finally {
    const pending = messages.value.find((m) => m.id === targetId);
    if (pending) {
      pending.loading = false;
    }
    sending.value = false;
    await scrollToBottom();
  }
};

const submitDraft = () => {
  const text = draft.value.trim();
  if (!text || sending.value) {
    return;
  }
  draft.value = '';
  messages.value.push({ id: ++seq, role: 'user', content: text });
  void call({ message: text });
};

const useSample = (sample: string) => {
  draft.value = sample;
  submitDraft();
};

const onAskSubmit = (id: number, answers: Record<string, any>) => {
  void call({ answers }, id);
};

const onAskCancel = (id: number) => {
  const target = messages.value.find((m) => m.id === id);
  if (target) {
    target.answered = true;
    target.loading = false;
  }
  pushAssistant({ content: '已取消。需要的时候换个说法再让我试一次就行。' });
  void scrollToBottom();
};

const newSession = () => {
  messages.value = [];
  sessionId.value = '';
  draft.value = '';
};

const open = () => {
  visible.value = true;
  void scrollToBottom();
};

const close = () => {
  visible.value = false;
};

const toggle = () => {
  visible.value ? close() : open();
};

/** Ctrl / ⌘ + K 唤起，Esc 关闭 */
const onKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault();
    toggle();
    return;
  }
  if (e.key === 'Escape' && visible.value) {
    close();
  }
};

onMounted(async () => {
  window.addEventListener('keydown', onKeydown);
  try {
    const res: any = await getAiEnabled();
    const data = res?.data ?? res;
    aiEnabled.value = !!data?.enabled;
  } catch {
    aiEnabled.value = false;
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown);
});
</script>

<style scoped lang="scss">
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 32px;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  color: #fff;
  background: var(--el-color-primary);
  border-radius: 50%;
  box-shadow: 0 6px 16px rgb(0 0 0 / 20%);
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  &.is-offline {
    background: var(--el-color-info);
  }
}

.fab-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: var(--el-color-danger);
  border: 2px solid #fff;
  border-radius: 50%;
}

.ai-panel {
  position: fixed;
  right: 20px;
  bottom: 20px;
  top: 74px;
  z-index: 3000;
  display: flex;
  flex-direction: column;
  width: 440px;
  max-width: calc(100vw - 40px);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  box-shadow: 0 12px 32px rgb(0 0 0 / 16%);
  overflow: hidden;
}

.ai-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.head-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.head-icon {
  color: var(--el-color-primary);
}

.head-title {
  font-size: 14px;
  font-weight: 600;
}

.head-sub {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.head-right {
  display: flex;
  gap: 2px;
}

.ai-body {
  flex: 1;
  padding: 14px 12px;
  overflow-y: auto;
  background: var(--el-bg-color-page);
}

.ai-empty {
  padding: 8px 2px;
}

.empty-title {
  font-size: 14px;
  font-weight: 600;
}

.empty-desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.chips {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.chip {
  padding: 8px 10px;
  font-size: 12px;
  color: var(--el-text-color-regular);
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  cursor: pointer;

  &:hover {
    color: var(--el-color-primary);
    border-color: var(--el-color-primary-light-5);
  }
}

.ai-offline {
  margin-top: 12px;
  padding: 8px 10px;
  font-size: 12px;
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-radius: 6px;
}

.ai-foot {
  padding: 10px 12px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}

.foot-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.foot-hint {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.ai-slide-enter-active,
.ai-slide-leave-active {
  transition: all 0.22s ease;
}

.ai-slide-enter-from,
.ai-slide-leave-to {
  opacity: 0;
  transform: translateX(16px);
}
</style>
