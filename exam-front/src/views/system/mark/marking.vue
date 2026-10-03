<template>
  <div class="p-[16px]">
    <el-card v-loading="loading" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2 min-w-0">
            <el-button link icon="ArrowLeft" @click="goBack">返回</el-button>
            <span class="font-medium">阅卷打分</span>
            <span class="meta-text" :title="examName">{{ examName || '-' }}</span>
            <el-divider direction="vertical" />
            <span class="meta-text">考生：{{ userName || '-' }}</span>
          </div>
          <div class="flex items-center gap-2">
            <el-button plain icon="View" @click="logVisible = true">阅卷日志</el-button>
            <el-tooltip :disabled="aiEnabled" content="AI 服务不可用，请检查 ruoyi-exam-agent 是否已启动" placement="top">
              <el-button plain icon="MagicStick" :loading="aiLoading" :disabled="!aiEnabled" @click="handleAiPreview">AI 预评</el-button>
            </el-tooltip>
            <el-button type="primary" :loading="finishing" :disabled="questions.length === 0" @click="handleFinish"> 完成阅卷 </el-button>
          </div>
        </div>
      </template>

      <div v-if="questions.length === 0 && !loading" class="empty-box">
        <el-empty description="这份答卷没有需要人工阅卷的主观题" />
      </div>

      <div v-else class="mark-body">
        <div class="summary-box">
          <div class="summary-item">
            <span class="summary-label">题目数</span>
            <span class="summary-value">{{ questions.length }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">已阅</span>
            <span class="summary-value is-success">{{ markedCount }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">待阅</span>
            <span class="summary-value is-warning">{{ questions.length - markedCount }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">已给分</span>
            <span class="summary-value is-primary">{{ totalScore }} / {{ fullScore }}</span>
          </div>
        </div>

        <!-- 逐题阅卷：考生作答 / 参考答案 / 解析摆在一起，教师不用来回切页 -->
        <div v-for="(item, index) in questions" :key="item.itemId" class="question-item" :class="{ 'is-marked': item.status === 'marked' }">
          <div class="question-head">
            <span class="question-index">第 {{ item.sort ?? index + 1 }} 题</span>
            <el-tag size="small" effect="plain" type="primary">{{ item.questionTypeName || questionTypeLabel(item.questionType) }}</el-tag>
            <el-tag size="small" effect="plain" :type="questionDifficultyTagType(item.difficulty)">
              {{ questionDifficultyLabel(item.difficulty || 'medium') }}
            </el-tag>
            <span class="question-score">满分 {{ item.fullScore ?? 0 }} 分</span>
            <el-tag v-if="item.status === 'marked'" size="small" effect="dark" type="success">已阅</el-tag>
            <el-tag v-else size="small" effect="plain" type="warning">待阅</el-tag>
          </div>

          <div class="ql-editor ql-content question-title" v-html="item.title"></div>

          <div class="answer-box">
            <div class="answer-label">考生作答</div>
            <!-- 答案存的是 JSON，统一按题型翻译成人话再展示（后端给的 *Text 只作兜底） -->
            <div class="answer-content ql-editor" v-html="myAnswerHtml(item)"></div>
          </div>

          <div class="answer-box">
            <div class="answer-label">参考答案</div>
            <div class="answer-content answer-standard ql-editor" v-html="standardAnswerHtml(item)"></div>
          </div>

          <div v-if="item.analysis" class="answer-box">
            <div class="answer-label">解析</div>
            <div class="answer-content answer-analysis ql-editor" v-html="item.analysis"></div>
          </div>

          <!-- AI 建议：只作参考，教师点了「采用」才写进得分 -->
          <div v-if="item.aiScore !== undefined && item.aiScore !== null" class="ai-box">
            <el-tag size="small" effect="plain" type="info">AI 建议 {{ item.aiScore }} 分</el-tag>
            <span class="ai-reason">{{ item.aiReason || '暂无理由' }}</span>
            <el-button link type="primary" @click="applyAiScore(item)">采用该分数</el-button>
          </div>

          <div class="score-box">
            <span class="score-label">得分</span>
            <el-input-number
              v-model="scoreForm[item.itemId].score"
              :min="0"
              :max="item.fullScore ?? 0"
              :step="0.5"
              :precision="1"
              size="small"
              controls-position="right"
            />
            <span class="score-full">/ {{ item.fullScore ?? 0 }}</span>
            <span class="score-label">判定</span>
            <el-radio-group v-model="scoreForm[item.itemId].correct" size="small">
              <el-radio :value="1">正确</el-radio>
              <el-radio :value="2">错误</el-radio>
              <el-radio :value="0">不判</el-radio>
            </el-radio-group>
            <el-input
              v-model="scoreForm[item.itemId].markComment"
              class="comment-input"
              size="small"
              placeholder="评语（可选）"
              maxlength="200"
              show-word-limit
            />
            <el-button type="primary" size="small" :loading="saving[item.itemId]" @click="handleScore(item)">保存</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-drawer v-model="logVisible" title="阅卷日志" size="520px">
      <el-table v-loading="logLoading" :data="logs" size="small" border>
        <el-table-column label="时间" align="center" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="动作" align="center" width="100">
          <template #default="{ row }">
            <dict-tag :options="markLogActionOptions" :value="row.action" />
          </template>
        </el-table-column>
        <el-table-column label="分数变化" align="center" width="110">
          <template #default="{ row }">
            <span v-if="row.oldScore === undefined && row.newScore === undefined">-</span>
            <span v-else>{{ row.oldScore ?? '-' }} → {{ row.newScore ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="operatorName" align="center" width="100" />
        <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!logLoading && logs.length === 0" description="暂无阅卷日志" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="SystemMarking">
// 题干 / 作答 / 解析都是 quill 富文本，得带上它的阅读样式
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import { getMarkQuestions, getMarkLogs, saveMarkScore, finishMarkTask, aiPreviewMarkTask } from '@/api/system/mark';
import type { MarkQuestionVO, MarkLogVO, MarkScoreForm } from '@/api/system/mark/types';
import { getAiEnabled } from '@/api/system/ai';
import { useExamDicts } from '@/hooks/useExamDicts';
import { formatAnswer } from '@/utils/answer';

const route = useRoute();
const router = useRouter();

const { markLogActionOptions, questionTypeLabel, questionDifficultyLabel, questionDifficultyTagType } = useExamDicts();

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const loading = ref(true);
const finishing = ref(false);
const aiLoading = ref(false);
/** AI 服务可用性：不可用时直接置灰按钮，别让教师点了才知道不通 */
const aiEnabled = ref(false);
const logVisible = ref(false);
const logLoading = ref(false);

const taskId = ref('');
const examName = ref('');
const userName = ref('');

const questions = ref<MarkQuestionVO[]>([]);
const logs = ref<MarkLogVO[]>([]);

/** itemId → 打分表单，用对象而不是数组，避免题目顺序变化导致错位 */
const scoreForm = reactive<Record<string, MarkScoreForm>>({});
/** itemId → 保存中状态 */
const saving = reactive<Record<string, boolean>>({});

const markedCount = computed(
  () => questions.value.filter((item) => item.status === 'marked' || (item.score !== undefined && item.score !== null)).length
);
const totalScore = computed(() => Number(questions.value.reduce((sum, item) => sum + Number(scoreForm[item.itemId]?.score ?? 0), 0).toFixed(1)));
const fullScore = computed(() => Number(questions.value.reduce((sum, item) => sum + Number(item.fullScore ?? 0), 0).toFixed(1)));

/**
 * 考生作答（人话）
 *
 * <p>优先用原始 JSON 自己翻译：后端 answerText 只认 `{"text":..}` 这一种，
 * 遇到别的题型会把整串 JSON 原样返回，页面上就出现 `{"answer":"..."}`。
 * 原始字段缺失时再退回后端文本。
 */
const myAnswerHtml = (item: MarkQuestionVO): string =>
  formatAnswer(item.answerContent || item.answerText, item.questionType, {
    standard: false,
    options: item.options,
    empty: '未作答'
  });

/** 参考答案（人话），同上 */
const standardAnswerHtml = (item: MarkQuestionVO): string =>
  formatAnswer(item.standardAnswer || item.standardAnswerText, item.questionType, {
    standard: true,
    options: item.options,
    empty: '-'
  });

const pad = (n: number) => String(n).padStart(2, '0');

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

/** 用已阅结果初始化每道题的打分表单 */
const initScoreForm = () => {
  questions.value.forEach((item) => {
    scoreForm[item.itemId] = {
      itemId: item.itemId,
      score: item.score ?? 0,
      correct: item.correct ?? 0,
      markComment: item.markComment ?? ''
    };
  });
};

const loadQuestions = async () => {
  if (!taskId.value) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const res = await getMarkQuestions(taskId.value);
    questions.value = res.data ?? [];
    initScoreForm();
  } catch {
    questions.value = [];
  } finally {
    loading.value = false;
  }
};

const loadLogs = async () => {
  if (!taskId.value) return;
  logLoading.value = true;
  try {
    const res = await getMarkLogs(taskId.value);
    logs.value = res.data ?? [];
  } catch {
    logs.value = [];
  } finally {
    logLoading.value = false;
  }
};

const handleScore = async (item: MarkQuestionVO) => {
  const form = scoreForm[item.itemId];
  if (!form) return;
  saving[item.itemId] = true;
  try {
    await saveMarkScore({
      itemId: form.itemId,
      score: form.score ?? 0,
      correct: form.correct ?? 0,
      markComment: form.markComment ?? ''
    });
    // 保存成功后就地更新，题卡立刻变「已阅」，不用整页刷新
    item.score = form.score ?? 0;
    item.correct = form.correct ?? 0;
    item.markComment = form.markComment ?? '';
    item.status = 'marked';
  } catch {
    // 失败提示由全局拦截器统一弹出
  } finally {
    saving[item.itemId] = false;
  }
};

/** 采用 AI 建议分：填进输入框，仍需教师点保存，避免误改 */
const applyAiScore = (item: MarkQuestionVO) => {
  const form = scoreForm[item.itemId];
  if (!form || item.aiScore === undefined || item.aiScore === null) return;
  form.score = item.aiScore;
};

const handleAiPreview = async () => {
  if (!taskId.value) return;
  aiLoading.value = true;
  try {
    await aiPreviewMarkTask(taskId.value);
    proxy?.$modal.msgSuccess('AI 预评已完成，建议分已填入，请逐题确认后再保存');
    await loadQuestions();
  } catch {
    // 同上
  } finally {
    aiLoading.value = false;
  }
};

const handleFinish = async () => {
  if (!taskId.value) return;
  const unmarked = questions.value.length - markedCount.value;
  if (unmarked > 0) {
    try {
      await proxy?.$modal.confirm(`还有 ${unmarked} 道题未打分，确认完成阅卷？未打分的题将按 0 分计`);
    } catch {
      return;
    }
  }
  finishing.value = true;
  try {
    await finishMarkTask(taskId.value);
    proxy?.$modal.msgSuccess('已完成阅卷');
    await loadQuestions();
  } catch {
    // 同上
  } finally {
    finishing.value = false;
  }
};

const goBack = () => router.back();

watch(logVisible, (visible) => {
  if (visible) loadLogs();
});

const loadAiEnabled = async () => {
  try {
    const res = await getAiEnabled();
    aiEnabled.value = res.data?.enabled === true;
  } catch {
    aiEnabled.value = false;
  }
};

onMounted(() => {
  const q = route.query;
  taskId.value = String(q.taskId ?? '');
  examName.value = String(q.examName ?? '');
  userName.value = String(q.userName ?? '');
  loadQuestions();
  loadAiEnabled();
});
</script>

<style scoped lang="scss">
.mark-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.summary-box {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px 14px;
  background: #f8f9fb;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.summary-label {
  font-size: 12px;
  color: #909399;
}

.summary-value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.summary-value.is-success {
  color: #67c23a;
}

.summary-value.is-warning {
  color: #e6a23c;
}

.summary-value.is-primary {
  color: #409eff;
}

.question-item {
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;

  &.is-marked {
    background: #fafcff;
    border-color: #d9ecff;
  }
}

.question-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding-bottom: 10px;
  margin-bottom: 10px;
  border-bottom: 1px solid #f0f2f5;
}

.question-index {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.question-score {
  font-size: 12px;
  color: #909399;
}

.question-title {
  margin-bottom: 12px;
  font-size: 15px;
  line-height: 1.8;
  color: #303133;

  /* quill 的 .ql-editor 自带 height:100% + 内边距，展示场景要还原掉 */
  &.ql-editor {
    height: auto;
    padding: 0;
    overflow: visible;
  }
}

.answer-box {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.answer-label {
  width: 72px;
  flex-shrink: 0;
  font-size: 13px;
  color: #909399;
}

.answer-content {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #303133;

  /* 同上：还原 quill 阅读区样式 */
  &.ql-editor {
    height: auto;
    padding: 0;
    overflow: visible;
  }

  /* 代码题参考答案由 v-html 插入，拿不到 scoped 属性，只能 :deep 兜 */
  :deep(.answer-code-lang) {
    display: inline-block;
    margin-bottom: 4px;
    padding: 0 6px;
    font-size: 12px;
    line-height: 20px;
    color: #409eff;
    background: #ecf5ff;
    border-radius: 4px;
  }

  :deep(.answer-code) {
    margin: 0;
    padding: 10px 12px;
    font-family: Menlo, Consolas, 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-all;
    color: #303133;
    background: #f5f7fa;
    border-radius: 6px;
  }

  :deep(.answer-remark) {
    margin-top: 6px;
    font-size: 13px;
    color: #606266;
  }
}

.answer-standard {
  font-weight: 600;
  color: #67c23a;
}

.answer-analysis {
  padding: 8px 12px;
  font-size: 13px;
  background: #fafcff;
  border-radius: 6px;
}

.ai-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin: 8px 0;
  background: #f4f4f5;
  border-radius: 6px;
}

.ai-reason {
  flex: 1;
  overflow: hidden;
  font-size: 12px;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.score-box {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.score-label {
  font-size: 13px;
  color: #606266;
}

.score-full {
  margin-left: -6px;
  font-size: 13px;
  color: #909399;
}

.comment-input {
  flex: 1;
  min-width: 180px;
}

.meta-text {
  overflow: hidden;
  font-size: 13px;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-box {
  padding: 30px 0;
}
</style>
