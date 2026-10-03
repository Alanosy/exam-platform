<template>
  <el-drawer v-model="visible" title="AI 错题诊断" size="46%" :close-on-click-modal="false" append-to-body>
    <div v-loading="loading" class="ai-diagnose">
      <el-empty v-if="!loading && !result" description="点「开始诊断」，AI 会分析错题定位薄弱知识点" />

      <template v-if="result">
        <el-alert type="info" :closable="false">
          <div class="text-[13px] leading-[20px]">{{ result.advice || '暂无建议' }}</div>
        </el-alert>

        <div v-if="result.weakPoints && result.weakPoints.length > 0" class="section">
          <div class="section-title">薄弱知识点</div>
          <el-table :data="result.weakPoints" size="small" border>
            <el-table-column label="知识点" prop="knowledgePoint" min-width="140" show-overflow-tooltip />
            <el-table-column label="掌握度" align="center" width="130">
              <template #default="{ row }">
                <el-progress
                  :percentage="Math.round(Number(row.mastery ?? 0) * 100)"
                  :stroke-width="10"
                  :color="masteryColor(row.mastery)"
                  text-inside
                />
              </template>
            </el-table-column>
            <el-table-column label="错次" prop="wrongCount" align="center" width="80" />
            <el-table-column label="错误类型" align="center" width="110">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ errorTypeLabel(row.errorType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="依据" prop="evidence" min-width="180" show-overflow-tooltip />
          </el-table>
        </div>

        <div v-if="result.priority && result.priority.length > 0" class="section">
          <div class="section-title">建议优先补这些</div>
          <div class="priority-row">
            <el-tag v-for="(point, index) in result.priority" :key="index" effect="dark" :type="index === 0 ? 'danger' : 'warning'">
              {{ index + 1 }}. {{ point }}
            </el-tag>
          </div>
        </div>

        <div v-if="result.model" class="model-text">诊断模型：{{ result.model }}</div>
      </template>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="loading" @click="handleDiagnose">开始诊断</el-button>
        <el-button @click="visible = false">关 闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts" name="WrongAiDiagnoseDrawer">
import { aiDiagnose } from '@/api/system/ai';
import type { AiDiagnoseVO, AiWrongItem } from '@/api/system/ai/types';

const props = defineProps<{
  modelValue: boolean;
  /** 错题数据，由明细页传入 */
  wrongItems: AiWrongItem[];
  /** 已掌握的知识点，用于对比 */
  mastered?: string[];
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v)
});

const loading = ref(false);
const result = ref<AiDiagnoseVO>();

/** 错误类型 code → 中文，字典里没有就原样显示 */
const ERROR_TYPE_LABEL: Record<string, string> = {
  concept: '概念不清',
  calculation: '计算失误',
  misread: '审题偏差',
  incomplete: '要点不全',
  skill: '方法不熟',
  careless: '粗心'
};

const errorTypeLabel = (type?: string) => (type && ERROR_TYPE_LABEL[type] ? ERROR_TYPE_LABEL[type] : type || '-');

const masteryColor = (mastery?: number) => {
  const v = Number(mastery ?? 0);
  if (v < 0.3) return '#f56c6c';
  if (v < 0.6) return '#e6a23c';
  return '#67c23a';
};

const handleDiagnose = async () => {
  if (props.wrongItems.length === 0) {
    proxy?.$modal.msgWarning('当前没有可诊断的错题');
    return;
  }
  loading.value = true;
  result.value = undefined;
  try {
    // 最多送 30 条：再多也诊断不出更多结论，只会白烧 token
    const res = await aiDiagnose({
      wrongItems: props.wrongItems.slice(0, 30),
      mastered: props.mastered ?? []
    });
    result.value = res.data;
    if (!res.data?.success) {
      proxy?.$modal.msgError(res.data?.message || '诊断失败');
    }
  } catch {
    // 失败提示由全局拦截器统一弹出
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
.ai-diagnose {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.priority-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.model-text {
  font-size: 12px;
  color: #909399;
}
</style>
