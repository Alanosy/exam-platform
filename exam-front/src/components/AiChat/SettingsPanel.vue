<template>
  <div class="settings">
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
import { reactive, watch } from 'vue';
import type { AiChatSettings } from '@/api/system/ai/types';

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
