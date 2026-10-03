<template>
  <div class="ask-card">
    <div class="ask-title">{{ ask.title }}</div>
    <div v-if="ask.desc" class="ask-desc">
      <MarkdownView :content="ask.desc" />
    </div>

    <div v-for="field in ask.fields || []" :key="field.key" class="ask-field">
      <div class="field-label">
        {{ field.label }}
        <span v-if="field.required !== false" class="required">*</span>
        <span v-if="field.tip" class="field-tip">{{ field.tip }}</span>
      </div>

      <el-select
        v-if="field.type === 'select'"
        v-model="form[field.key]"
        size="small"
        class="field-control"
        :placeholder="field.placeholder || '请选择'"
      >
        <el-option v-for="opt in field.options || []" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>

      <el-select
        v-else-if="field.type === 'multi'"
        v-model="form[field.key]"
        size="small"
        multiple
        class="field-control"
        :placeholder="field.placeholder || '可多选'"
      >
        <el-option v-for="opt in field.options || []" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>

      <el-input-number
        v-else-if="field.type === 'number'"
        v-model="form[field.key]"
        size="small"
        class="field-control"
        :min="1"
        :max="100"
        controls-position="right"
      />

      <el-switch v-else-if="field.type === 'switch'" v-model="form[field.key]" size="small" />

      <el-input v-else v-model="form[field.key]" size="small" class="field-control" :placeholder="field.placeholder || '请输入'" />
    </div>

    <div class="ask-actions">
      <el-button size="small" type="primary" :loading="loading" @click="onSubmit">
        {{ ask.submitText || '继续' }}
      </el-button>
      <el-button size="small" plain @click="emit('cancel')">
        {{ ask.cancelText || '取消' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import MarkdownView from './MarkdownView.vue';
import type { AiChatAskVO } from '@/api/system/ai/types';

/**
 * 中断卡：Agent 缺信息 / 需要确认时铺出来让人填
 *
 * confirm 类型的卡没有字段，提交时固定带 confirmed=true，
 * Agent 侧靠这个标记放行写操作。
 */
const props = defineProps<{ ask: AiChatAskVO; loading?: boolean; answered?: boolean }>();
const emit = defineEmits<{ submit: [answers: Record<string, any>]; cancel: [] }>();

const form = ref<Record<string, any>>({});

const buildForm = () => {
  const next: Record<string, any> = {};
  for (const field of props.ask.fields || []) {
    if (field.type === 'number') {
      // 数字框绑 null 会显示不出来，给个 0 也不对，用 undefined 让它保持空
      next[field.key] = typeof field.value === 'number' ? field.value : undefined;
    } else {
      next[field.key] = field.value ?? undefined;
    }
  }
  form.value = next;
};

watch(() => props.ask, buildForm, { immediate: true, deep: true });

const onSubmit = () => {
  const answers: Record<string, any> = { ...form.value };
  // 空值不回传：Agent 侧 merge_slots 会跳过空值，避免把已有槽位冲掉
  for (const key of Object.keys(answers)) {
    if (answers[key] === undefined || answers[key] === null || answers[key] === '') {
      delete answers[key];
    }
  }
  if (props.ask.kind === 'confirm') {
    answers.confirmed = true;
  }
  emit('submit', answers);
};
</script>

<style scoped lang="scss">
.ask-card {
  margin-top: 10px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 8px;
}

.ask-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.ask-desc {
  margin-top: 8px;
}

.ask-field {
  margin-top: 10px;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.required {
  color: var(--el-color-danger);
}

.field-tip {
  margin-left: auto;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.field-control {
  width: 100%;
}

.ask-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
</style>
