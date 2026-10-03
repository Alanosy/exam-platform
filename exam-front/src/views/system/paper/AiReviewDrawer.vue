<template>
  <el-drawer v-model="visible" :title="`AI 试卷审查${paperName ? '：' + paperName : ''}`" size="52%" :close-on-click-modal="false" append-to-body>
    <div v-loading="loading" class="ai-review">
      <el-form label-width="96px" class="review-form">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="考试时长">
              <el-input-number v-model="duration" :min="10" :max="300" :step="10" controls-position="right" class="w-full" />
              <span class="tip-text">分钟，用于估算作答时间是否够</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审查侧重">
              <el-input v-model="focus" placeholder="例如：重点看知识点覆盖是否均匀" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" icon="MagicStick" :loading="loading" @click="handleReview">开始审查</el-button>
          <span class="tip-text">AI 只出结论，不会改动试卷</span>
        </el-form-item>
      </el-form>

      <template v-if="result">
        <el-alert :type="verdictType" :closable="false" class="review-verdict">
          <div class="text-[13px] leading-[20px]">
            <div><b>{{ result.verdict || '审查完成' }}</b></div>
            <div v-if="result.questionCount" class="text-[#606266]">
              共 {{ result.questionCount }} 题<span v-if="result.estimatedMinutes">，预计用时 {{ result.estimatedMinutes }} 分钟</span>
              <span v-if="duration && result.estimatedMinutes > duration" class="text-[#f56c6c]">（超出考试时长 {{ result.estimatedMinutes - duration }} 分钟）</span>
            </div>
          </div>
        </el-alert>

        <!-- 难度分布：直接用 el-tag 呈现，不引图表库 -->
        <div v-if="difficultyRows.length > 0" class="section">
          <div class="section-title">难度分布</div>
          <div class="dist-row">
            <el-tag v-for="row in difficultyRows" :key="row.label" effect="light" :type="row.type">
              {{ row.label }} {{ row.count }} 题
            </el-tag>
          </div>
        </div>

        <div v-if="result.knowledgeCoverage && result.knowledgeCoverage.length > 0" class="section">
          <div class="section-title">知识点覆盖</div>
          <el-table :data="result.knowledgeCoverage" size="small" border max-height="240">
            <el-table-column label="知识点" prop="point" min-width="160" show-overflow-tooltip />
            <el-table-column label="题数" prop="count" align="center" width="90" />
            <el-table-column label="占比" align="center" width="110">
              <template #default="{ row }">{{ ratioText(row.ratio) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="result.issues && result.issues.length > 0" class="section">
          <div class="section-title">发现的问题</div>
          <el-table :data="result.issues" size="small" border>
            <el-table-column label="级别" align="center" width="90">
              <template #default="{ row }">
                <el-tag size="small" effect="dark" :type="issueTagType(row.level)">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="说明" prop="detail" min-width="200" show-overflow-tooltip />
            <el-table-column label="建议" prop="suggestion" min-width="180" show-overflow-tooltip />
          </el-table>
        </div>

        <div v-if="result.suggestions && result.suggestions.length > 0" class="section">
          <div class="section-title">修改建议</div>
          <ol class="suggest-list">
            <li v-for="(item, index) in result.suggestions" :key="index">{{ item }}</li>
          </ol>
        </div>

        <div v-if="result.model" class="model-text">审查模型：{{ result.model }}</div>
      </template>

      <el-empty v-else-if="!loading" description="调整参数后点「开始审查」，AI 会给出覆盖度、难度分布与修改建议" />
    </div>
  </el-drawer>
</template>

<script setup lang="ts" name="PaperAiReviewDrawer">
import { aiReviewPaper } from '@/api/system/ai';
import type { AiPaperReviewVO, AiPaperQuestionItem } from '@/api/system/ai/types';
import { getPaper } from '@/api/system/paper';
import { listQuestionByIds } from '@/api/system/question';

/** 题干是富文本，送给模型的只留纯文本——HTML 标签会白白吃掉大量 token */
const plainText = (html?: string): string => {
  if (!html) return '';
  return html
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .trim();
};

const props = defineProps<{
  modelValue: boolean;
  paperId?: string | number;
  paperName?: string;
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
const result = ref<AiPaperReviewVO>();
const duration = ref(60);
const focus = ref('全面审查');

/** 难度分布：后端给的是 JSON 字符串，这里拆成可直接渲染的行 */
const difficultyRows = computed(() => {
  const raw = result.value?.difficultyDistribution;
  if (!raw) return [] as { label: string; count: number; type: 'success' | 'warning' | 'danger' | 'info' }[];
  let map: Record<string, number> = {};
  try {
    map = JSON.parse(raw);
  } catch {
    return [] as { label: string; count: number; type: 'success' | 'warning' | 'danger' | 'info' }[];
  }
  const meta: Record<string, { label: string; type: 'success' | 'warning' | 'danger' | 'info' }> = {
    easy: { label: '简单', type: 'success' },
    medium: { label: '中等', type: 'warning' },
    hard: { label: '困难', type: 'danger' }
  };
  return Object.entries(map)
    .filter(([key]) => key in meta)
    .map(([key, count]) => ({ label: meta[key].label, count: Number(count) || 0, type: meta[key].type }));
});

const verdictType = computed(() => {
  const verdict = result.value?.verdict ?? '';
  if (verdict.includes('直接使用')) return 'success';
  if (verdict.includes('需修改')) return 'error';
  return 'warning';
});

/** el-tag 的 type 只接受字面量联合，兜底一律走 info */
const issueTagType = (level: string): 'danger' | 'warning' | 'info' => {
  if (level === 'fatal') return 'danger';
  if (level === 'major') return 'warning';
  return 'info';
};

const ratioText = (ratio?: number) => (ratio === undefined || ratio === null ? '-' : `${(Number(ratio) * 100).toFixed(1)}%`);

const handleReview = async () => {
  if (!props.paperId) {
    proxy?.$modal.msgWarning('请先选择试卷');
    return;
  }
  loading.value = true;
  result.value = undefined;
  try {
    // 试卷详情只给得出题目ID与卷内分值，题干要再去题库取一次
    const paperRes = await getPaper(props.paperId);
    const paper: any = paperRes.data ?? {};
    const items: any[] = paper.questions ?? [];
    const ids = items.map((item) => item.questionId).filter(Boolean);
    if (ids.length === 0) {
      proxy?.$modal.msgWarning('这份试卷还没有选题，无从审查');
      return;
    }
    const qRes = await listQuestionByIds(ids);
    const detailMap = new Map<string, any>();
    (qRes.data ?? []).forEach((q: any) => detailMap.set(String(q.id), q));

    const questions: AiPaperQuestionItem[] = items.map((item) => {
      const detail = detailMap.get(String(item.questionId));
      return {
        questionId: String(item.questionId),
        questionType: detail?.questionType ?? '',
        stem: plainText(detail?.title),
        difficulty: detail?.difficulty ?? 'medium',
        knowledgePoints: detail?.knowledgePoints ?? [],
        score: item.paperScore ?? paper.defaultScore ?? detail?.score
      };
    });

    const res = await aiReviewPaper({
      paperId: String(props.paperId),
      title: paper.paperName ?? props.paperName,
      duration: duration.value,
      totalScore: paper.totalScore,
      passScore: paper.passScore,
      questions,
      focus: focus.value
    });
    result.value = res.data;
    if (!res.data?.success) {
      proxy?.$modal.msgError(res.data?.message || '审查失败');
    }
  } catch {
    // 失败提示由全局拦截器统一弹出
  } finally {
    loading.value = false;
  }
};

watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      result.value = undefined;
    }
  }
);
</script>

<style scoped lang="scss">
.ai-review {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-form {
  padding: 12px;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.tip-text {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.review-verdict {
  margin-bottom: 4px;
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

.dist-row {
  display: flex;
  gap: 8px;
}

.suggest-list {
  padding-left: 20px;
  font-size: 13px;
  line-height: 1.9;
  color: #606266;
}

.model-text {
  font-size: 12px;
  color: #909399;
}
</style>
