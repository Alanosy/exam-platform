<template>
  <div v-if="steps.length" class="run-trace">
    <div class="trace-head" @click="open = !open">
      <span class="head-title">
        <span class="status-dot" :class="hasError ? 'is-error' : 'is-ok'"></span>
        运行流程 · {{ steps.length }} 步
      </span>
      <span class="head-extra">
        <span v-if="totalMs" class="cost">{{ totalMs }} ms</span>
        <span class="arrow" :class="{ 'is-open': open }">⌄</span>
      </span>
    </div>

    <div v-show="open" class="trace-body">
      <div v-for="step in steps" :key="step.id || step.title" class="trace-item">
        <span class="node" :class="[`type-${step.type}`, `status-${step.status || 'ok'}`]"></span>
        <div class="node-body">
          <div class="node-title">
            {{ step.title }}
            <span class="node-type">{{ typeLabel(step.type) }}</span>
            <span v-if="step.latencyMs" class="node-cost">{{ step.latencyMs }} ms</span>
          </div>
          <div v-if="step.ref" class="node-ref">{{ step.ref }}</div>
          <div v-if="step.detail" class="node-detail">{{ step.detail }}</div>
          <ul v-if="step.preview && step.preview.length" class="node-preview">
            <li v-for="(line, idx) in step.preview" :key="idx">{{ line }}</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { AiChatTraceVO } from '@/api/system/ai/types';

/**
 * 运行流程时间线
 *
 * 默认折叠：大部分时候用户只关心结论，展开才看它到底调了什么。
 * 有失败步骤时自动展开——出错时「卡在哪一步」是第一诉求。
 */
const props = defineProps<{ steps: AiChatTraceVO[] }>();

const TYPE_LABEL: Record<string, string> = {
  think: '思考',
  tool: '工具',
  skill: '技能',
  ask: '等待输入',
  write: '写入',
  done: '完成',
  error: '出错'
};

const hasError = computed(() => props.steps.some((s) => s.status === 'error'));
const totalMs = computed(() => props.steps.reduce((sum, s) => sum + (s.latencyMs || 0), 0));
// 有错就默认展开，别让用户自己去猜
const open = ref(hasError.value);

const typeLabel = (type: string) => TYPE_LABEL[type] || type;
</script>

<style scoped lang="scss">
.run-trace {
  margin-top: 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
  overflow: hidden;
}

.trace-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
  user-select: none;

  &:hover {
    background: var(--el-fill-color-light);
  }
}

.head-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;

  &.is-ok {
    background: var(--el-color-success);
  }

  &.is-error {
    background: var(--el-color-danger);
  }
}

.head-extra {
  display: flex;
  align-items: center;
  gap: 8px;
}

.arrow {
  transition: transform 0.2s;

  &.is-open {
    transform: rotate(180deg);
  }
}

.trace-body {
  padding: 8px 10px 10px 10px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.trace-item {
  position: relative;
  display: flex;
  gap: 8px;
  padding-bottom: 10px;

  &::before {
    content: '';
    position: absolute;
    left: 3px;
    top: 12px;
    bottom: 0;
    width: 1px;
    background: var(--el-border-color-lighter);
  }

  &:last-child {
    padding-bottom: 0;

    &::before {
      display: none;
    }
  }
}

.node {
  position: relative;
  z-index: 1;
  flex: none;
  width: 7px;
  height: 7px;
  margin-top: 5px;
  border-radius: 50%;
  background: var(--el-color-info);

  &.status-error {
    background: var(--el-color-danger);
  }

  &.status-waiting {
    background: var(--el-color-warning);
  }

  &.type-tool {
    background: var(--el-color-primary);
  }

  &.type-skill {
    background: #8b5cf6;
  }

  &.type-done {
    background: var(--el-color-success);
  }

  &.status-error {
    background: var(--el-color-danger);
  }
}

.node-body {
  min-width: 0;
  flex: 1;
}

.node-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-primary);
}

.node-type {
  padding: 0 5px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  border-radius: 3px;
}

.node-cost {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.node-ref {
  margin-top: 2px;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  word-break: break-all;
}

.node-detail {
  margin-top: 2px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  word-break: break-all;
}

.node-preview {
  margin: 4px 0 0;
  padding-left: 14px;
  font-size: 11px;
  color: var(--el-text-color-secondary);

  li {
    margin: 1px 0;
  }
}
</style>
