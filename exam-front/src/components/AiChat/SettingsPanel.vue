<template>
  <div class="settings">
    <div class="who">
      <div class="who-head">
        <span class="who-name">{{ identity.nickName || '当前用户' }}</span>
        <el-tag :type="scopeTagType" size="small" effect="light">{{ scopeLabel }}</el-tag>
      </div>
      <div class="who-line">
        能让 AI 用到 <b>{{ identity.visibleApiCount ?? '—' }}</b> / {{ identity.totalApiCount ?? '—' }} 条系统能力
      </div>
      <div v-if="identity.examPermissions && identity.examPermissions.length" class="who-perms">
        {{ identity.examPermissions.join('、') }}
      </div>
      <div v-else class="who-tip">考生视角：只能看我自己的考试、成绩、错题与证书，改数据的活儿做不了。</div>
    </div>

    <div class="set-row">
      <div class="set-label">
        <span>上下文轮数</span>
        <span class="set-tip">每轮带多少历史进模型</span>
      </div>
      <el-input-number v-model="draft.contextRounds" :min="1" :max="20" size="small" controls-position="right" />
    </div>

    <div class="set-row">
      <div class="set-label">
        <span>写操作先确认</span>
        <span class="set-tip">改数据前弹确认卡</span>
      </div>
      <el-switch v-model="draft.confirmWrite" size="small" />
    </div>

    <div class="set-row">
      <div class="set-label">
        <span>先规划再执行</span>
        <span class="set-tip">关掉就直接让模型回答</span>
      </div>
      <el-switch v-model="draft.planner" size="small" />
    </div>

    <div class="set-row">
      <div class="set-label">
        <span>模型编码</span>
        <span class="set-tip">留空走默认主备链</span>
      </div>
      <el-input v-model="draft.modelCode" size="small" placeholder="如：qwen-max" class="set-input" />
    </div>

    <div class="set-foot">
      <el-button size="small" @click="reset">恢复默认</el-button>
      <el-button type="primary" size="small" @click="apply">保存</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { aiChatWhoami } from '@/api/system/ai/chat';
import type { AiChatIdentityVO, AiChatSettings } from '@/api/system/ai/types';

const SCOPE_LABEL: Record<string, string> = {
  admin: '超级管理员',
  teacher: '教师 / 管理员',
  student: '考生',
  unknown: '未识别角色'
};
// el-tag 的 type 只接受字面量联合，不能用 string；用本项目已有的 ElTagType
const SCOPE_TAG: Record<string, ElTagType> = {
  admin: 'danger',
  teacher: 'primary',
  student: 'success',
  unknown: 'info'
};

/**
 * 聊天窗设置
 *
 * 改动当场写回父组件并落到 localStorage；下一次请求就会带上新的 options，
 * 服务端按会话记住（同一会话不用每轮都传）。
 */
const props = defineProps<{ modelValue: AiChatSettings }>();
const emit = defineEmits<{ 'update:modelValue': [value: AiChatSettings]; reset: [] }>();

const DEFAULTS: AiChatSettings = { contextRounds: 6, confirmWrite: true, planner: true, modelCode: '' };

const draft = reactive<AiChatSettings>({ ...props.modelValue });

/**
 * 身份：摆出来就是最好的权限说明
 *
 * 用户看到「我能用 12/52 条能力」，就不会再问「为什么你查不到全班成绩」——
 * 答案是权限，不是 AI 笨。
 */
const identity = ref<AiChatIdentityVO>({});
const scopeLabel = computed(() => SCOPE_LABEL[identity.value.roleScope || 'unknown'] || '未识别角色');
const scopeTagType = computed<ElTagType>(() => SCOPE_TAG[identity.value.roleScope || 'unknown'] || 'info');

onMounted(async () => {
  try {
    const res: any = await aiChatWhoami();
    identity.value = (res?.data ?? res ?? {}) as AiChatIdentityVO;
  } catch {
    // 拿不到身份不影响设置本身，留空即可
    identity.value = {};
  }
});

watch(
  () => props.modelValue,
  (val) => Object.assign(draft, val),
  { deep: true }
);

const apply = () => {
  emit('update:modelValue', { ...draft });
};

const reset = () => {
  Object.assign(draft, DEFAULTS);
  emit('reset');
};
</script>

<style scoped lang="scss">
.settings {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.who {
  padding: 8px 10px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.who-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.who-name {
  font-size: 13px;
  font-weight: 600;
}

.who-line {
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.who-perms {
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  word-break: break-all;
}

.who-tip {
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-color-warning);
}

.set-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.set-label {
  display: flex;
  flex-direction: column;
}

.set-label > span:first-child {
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.set-tip {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.set-input {
  width: 120px;
}

.set-foot {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 4px;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
