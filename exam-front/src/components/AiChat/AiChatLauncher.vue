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

  <transition name="ai-pop">
    <div v-show="visible" class="ai-panel">
      <header class="ai-head">
        <div class="head-left">
          <!-- 二级视图才出现返回键：聊天时头部不放无关入口 -->
          <el-button v-if="view !== 'chat'" text size="small" title="返回对话" @click="backToChat">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-icon v-else :size="16" class="head-icon"><ChatDotRound /></el-icon>
          <span class="head-title">{{ view === 'chat' ? 'AI 助手' : viewTitle }}</span>
        </div>
        <div class="head-right">
          <template v-if="view === 'chat'">
            <el-button text size="small" title="历史会话" @click="openView('history')">
              <el-icon><Clock /></el-icon>
            </el-button>
            <el-button text size="small" title="设置" @click="openView('settings')">
              <el-icon><Setting /></el-icon>
            </el-button>
            <el-button text size="small" title="新建会话" @click="newSession">
              <el-icon><RefreshLeft /></el-icon>
            </el-button>
          </template>
          <el-button text size="small" title="关闭" @click="close">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </header>

      <!--
        二级视图整框接管，而不是挤在聊天区上方占一半：
        一个窗口只做一件事——要么聊天，要么翻历史，要么改设置。
        没有用 el-dialog / el-drawer：它们 teleport 到 body，全屏场景下会看不到。
      -->
      <div v-if="view !== 'chat'" class="ai-view">
        <div class="view-body">
          <SessionList
            v-if="view === 'history'"
            :sessions="sessions"
            :active-id="sessionId"
            :loading="loadingSessions"
            :title="''"
            empty-text="还没有历史会话，聊一句就有了"
            @pick="resumeSession"
            @refresh="loadSessions"
          />
          <SettingsPanel v-else v-model="settings" @reset="saveSettings" @applied="backToChat" />
        </div>
        <div class="view-foot">
          <span class="view-hint">{{ view === 'history' ? '点一条就能接着聊' : '改动即时生效并会被记住' }}</span>
          <el-button size="small" type="primary" plain @click="backToChat">返回对话</el-button>
        </div>
      </div>

      <template v-else>
        <div ref="bodyRef" class="ai-body">
          <div v-if="!messages.length" class="ai-empty">
            <div class="empty-title">我能帮你做这些</div>
            <div class="empty-desc">直接说人话就行：缺信息我会问你，写库前会先确认，做不到的会告诉你为什么。</div>
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
            :placeholder="inputPlaceholder"
            @keydown.enter.exact.prevent="submitDraft"
          />
          <div class="foot-row">
            <span class="foot-hint">Ctrl/⌘ + K 开关 · 以你的身份取数，越权的查不到</span>
            <el-button type="primary" size="small" :loading="sending" :disabled="!draft.trim()" @click="submitDraft"> 发送 </el-button>
          </div>
        </footer>
      </template>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { ArrowLeft, ChatDotRound, Clock, Close, RefreshLeft, Setting } from '@element-plus/icons-vue';
import ChatMessage from './ChatMessage.vue';
import SessionList from './SessionList.vue';
import SettingsPanel from './SettingsPanel.vue';
import type { ChatMessageItem } from './types';
import type { AiChatSessionVO, AiChatSettings, AiChatVO } from '@/api/system/ai/types';
import { aiChat, aiChatSession, aiChatSessions } from '@/api/system/ai/chat';
import { getAiEnabled } from '@/api/system/ai';

/**
 * 全局 AI 聊天窗
 *
 * 交互参照主流 Agent 对话（Trae / Claude 那套）：
 * - 一句话下指令，缺信息时**中断**并弹出表单，而不是回一段话让用户自己猜；
 * - 每一步工具 / 技能 / 接口调用都留在运行流程里，可展开回看；
 * - 写库前一定会先给确认卡；
 * - 历史会话可回看、可接着聊；设置里能改上下文轮数等偏好。
 *
 * 视图是互斥的（chat / history / settings）：点历史或设置会**整框切换**过去，
 * 而不是在聊天区上方压一层半屏浮层——一个窗口只做一件事，同时避免聊天记录被挤成窄条。
 *
 * 会话状态只在组件里维护：本组件挂在 Layout 上常驻，切页面不会丢会话。
 */
const SAMPLES = ['帮我创建 10 道关于计算机基础知识的题到计算机题库', '期中考试考卷的答题情况怎么样', '找 5 道关于 TCP 的题', '我这学期有哪些考试？'];

const SETTINGS_KEY = 'ai-chat-settings';
const DEFAULT_SETTINGS: AiChatSettings = { contextRounds: 6, confirmWrite: true, planner: true, modelCode: '' };

const visible = ref(false);
const aiEnabled = ref(false);
const sending = ref(false);
const draft = ref('');
const sessionId = ref('');
const messages = ref<ChatMessageItem[]>([]);
const bodyRef = ref<HTMLElement>();
// 互斥视图：聊天 / 历史 / 设置，同一时刻只显示一个，整框接管
const view = ref<'chat' | 'history' | 'settings'>('chat');
const viewTitle = computed(() => (view.value === 'history' ? '历史会话' : '设置'));
const sessions = ref<AiChatSessionVO[]>([]);
const loadingSessions = ref(false);
const settings = ref<AiChatSettings>({ ...DEFAULT_SETTINGS });
let seq = 0;

const loadSettings = () => {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) {
      settings.value = { ...DEFAULT_SETTINGS, ...JSON.parse(raw) };
    }
  } catch {
    settings.value = { ...DEFAULT_SETTINGS };
  }
};

const saveSettings = () => {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings.value));
};

// 设置变了就落盘，下一轮请求自动带上
watch(settings, saveSettings, { deep: true });

/**
 * 输入框提示语
 *
 * 上一轮在等补充信息时，用户完全可以手打答案（比如直接敲「1」），
 * 但界面上不说的话没人知道——所以这里明确提示一句。
 */
const inputPlaceholder = computed(() => {
  if (!aiEnabled.value) return 'AI 服务未连接，仍可发送';
  const pending = [...messages.value].reverse().find((m) => m.ask && !m.answered);
  if (pending?.ask?.kind === 'confirm') return '回「确认」执行，或「算了」取消';
  if (pending) return `补充信息：可直接输入答案，如 ${pendingHint(pending.ask)}`;
  return '描述你要做的事，Enter 发送 / Shift+Enter 换行';
});

const pendingHint = (ask?: AiChatVO['ask'] | null): string => {
  const first = ask?.fields?.[0];
  if (!first) return '1';
  if (first.type === 'number') return '1';
  if (first.type === 'select') return `${first.options?.[0]?.label ?? '选项名'}`;
  return first.label ?? '答案';
};

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
      options: { ...settings.value },
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
  view.value = 'chat';
};

/** 打开二级视图：整框切过去，聊天区直接让位 */
const openView = (name: 'history' | 'settings') => {
  view.value = name;
  if (name === 'history') {
    void loadSessions();
  }
};

const backToChat = async () => {
  view.value = 'chat';
  await scrollToBottom();
};

const loadSessions = async () => {
  loadingSessions.value = true;
  try {
    const res: any = await aiChatSessions();
    sessions.value = (res?.data ?? res ?? []) as AiChatSessionVO[];
  } catch {
    sessions.value = [];
  } finally {
    loadingSessions.value = false;
  }
};

/** 回到某个历史会话：把消息回放出来，之后接着聊就是继续这个会话 */
const resumeSession = async (id: string) => {
  try {
    const res: any = await aiChatSession(id);
    const vo: AiChatSessionVO = (res?.data ?? res) as AiChatSessionVO;
    messages.value = (vo?.messages || []).map((m) => ({
      id: ++seq,
      role: (m.role === 'user' ? 'user' : 'assistant') as 'user' | 'assistant',
      content: m.content || ''
    }));
    sessionId.value = id;
    view.value = 'chat';
    await scrollToBottom();
  } catch (e: any) {
    pushAssistant({ content: `打开会话失败：${e?.msg || e?.message || '会话可能已过期'}。新建一次会话再试。` });
  }
};

const open = () => {
  visible.value = true;
  // 每次唤起都从聊天视图开始：上一次停在设置里不该被带出来
  view.value = 'chat';
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
    // 先退回聊天，再按一次才关窗：避免在看历史时一把 Esc 把整个窗关掉
    if (view.value !== 'chat') {
      view.value = 'chat';
      return;
    }
    close();
  }
};

onMounted(async () => {
  window.addEventListener('keydown', onKeydown);
  loadSettings();
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
  overflow: hidden;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  box-shadow: 0 12px 32px rgb(0 0 0 / 16%);
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

.head-right {
  display: flex;
  gap: 2px;
}

/* 二级视图：整框接管，聊天区与输入区整体让位 */
.ai-view {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.view-body {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  background: var(--el-bg-color-page);
}

.view-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 12px;
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
}

.view-hint {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
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
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
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

.ai-pop-enter-active,
.ai-pop-leave-active {
  transition: all 0.22s ease;
}

.ai-pop-enter-from,
.ai-pop-leave-to {
  opacity: 0;
  transform: translateX(16px);
}
</style>
