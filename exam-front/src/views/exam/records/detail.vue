<template>
  <div class="p-[16px]">
    <el-skeleton v-if="loading" :rows="8" animated />

    <el-result v-else-if="errorMsg" icon="warning" title="无法加载答题记录" :sub-title="errorMsg">
      <template #extra>
        <el-button type="primary" plain @click="goRecords">返回考试记录</el-button>
      </template>
    </el-result>

    <template v-else-if="result">
      <!-- 成绩概览 -->
      <el-card shadow="never" class="summary-card">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="summary-title">{{ result.examName }}</span>
            <div class="flex items-center gap-2">
              <el-tag :type="result.passed ? 'success' : 'danger'" size="small" effect="plain">
                {{ result.passed ? '及格' : '未及格' }}
              </el-tag>
              <el-button v-if="cert" type="warning" plain size="small" icon="Medal" @click="certVisible = true">查看证书</el-button>
              <el-button plain size="small" @click="goRecords">返回考试记录</el-button>
            </div>
          </div>
        </template>

        <div class="score-row">
          <div class="score-main">
            <span class="score-value">{{ result.totalScore ?? 0 }}</span>
            <span class="score-unit">/ {{ result.paperTotalScore ?? 0 }} 分</span>
          </div>
          <div class="score-side">
            <div>客观题 {{ result.objectiveScore ?? 0 }} 分 · 主观题 {{ result.subjectiveScore ?? 0 }} 分</div>
            <div>及格分 {{ result.passScore ?? 0 }} 分 · 用时 {{ usedText(result.usedSeconds) }}</div>
            <div>
              共 {{ result.questionCount ?? 0 }} 题 · 已答 {{ result.answeredCount ?? 0 }} 题 · 对
              <span class="text-right">{{ result.correctCount ?? 0 }}</span> 题 · 错 <span class="text-wrong">{{ result.wrongCount ?? 0 }}</span> 题
            </div>
          </div>
        </div>

        <el-descriptions :column="3" border size="small" class="mt-[12px]">
          <el-descriptions-item label="试卷">{{ result.paperName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="考试次数">第 {{ result.attemptNo ?? 1 }} 次</el-descriptions-item>
          <el-descriptions-item label="状态">
            <dict-tag :options="examRecordStatusOptions" :value="result.status" />
          </el-descriptions-item>
          <el-descriptions-item label="开考时间">{{ formatTime(result.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="交卷时间">{{ formatTime(result.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="限时">
            {{ result.durationMinutes ? `${result.durationMinutes} 分钟` : '不限时' }}
            <el-tag v-if="result.autoSubmit" type="warning" size="small" effect="plain" class="ml-1">超时自动交卷</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="!result.showAnswer"
          class="mt-[12px]"
          type="info"
          :closable="false"
          show-icon
          title="该考试不公布答案与解析，以下仅展示你的作答与得分"
        />
      </el-card>

      <!-- 逐题明细 -->
      <el-card shadow="never" class="detail-card">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-medium">答题明细</span>
            <el-checkbox v-model="onlyWrong">只看错题</el-checkbox>
          </div>
        </template>

        <div v-for="q in shownQuestions" :key="q.questionId" class="question-item">
          <div class="question-head">
            <span class="question-index">第 {{ q.sort }} 题</span>
            <dict-tag :options="questionTypeOptions" :value="q.questionType" />
            <span class="question-score">{{ q.score }} 分</span>
            <span class="question-gained">得 {{ q.gainedScore ?? 0 }} 分</span>
            <el-tag :type="q.correct === 1 ? 'success' : q.correct === 2 ? 'danger' : 'info'" size="small" effect="light">
              {{ q.correct === 1 ? '正确' : q.correct === 2 ? '错误' : '未判' }}
            </el-tag>
          </div>

          <div class="ql-editor ql-content question-title" v-html="q.title"></div>

          <!-- 客观题选项：标出我选的与正确答案 -->
          <div v-if="q.options && q.options.length" class="option-list">
            <div
              v-for="op in q.options"
              :key="op.optionKey"
              class="option-row"
              :class="{
                'is-mine': myKeys(q).includes(op.optionKey),
                'is-right': result.showAnswer && rightKeys(q).includes(op.optionKey)
              }"
            >
              <span class="option-key">{{ op.optionKey }}</span>
              <span class="ql-editor ql-content option-content" v-html="op.optionContent"></span>
              <span v-if="myKeys(q).includes(op.optionKey)" class="option-flag flag-mine">我的选择</span>
              <span v-else-if="result.showAnswer && rightKeys(q).includes(op.optionKey)" class="option-flag flag-right">正确答案</span>
            </div>
          </div>

          <div class="answer-row">
            <span class="answer-label">我的作答</span>
            <!-- 主观题作答是富文本，必须按 HTML 渲染，否则会看到一堆标签 -->
            <span class="answer-value" :class="{ 'is-empty': !q.myAnswerText }" v-html="q.myAnswerText || '未作答'"></span>
          </div>
          <div v-if="result.showAnswer" class="answer-row">
            <span class="answer-label">正确答案</span>
            <span class="answer-value is-standard" v-html="q.standardAnswerText || '-'"></span>
          </div>
          <div v-if="result.showAnswer && q.analysis" class="analysis-box">
            <div class="analysis-label">解析</div>
            <div class="ql-editor ql-content" v-html="q.analysis"></div>
          </div>
        </div>

        <el-empty v-if="shownQuestions.length === 0" description="没有符合条件的题目" />
      </el-card>
    </template>

    <!-- 证书：及格且这场考试配了证书模板才会有 -->
    <el-dialog v-model="certVisible" title="我的证书" width="900px" append-to-body>
      <div v-if="cert">
        <certificate-paper
          :title="cert.title"
          :subtitle="cert.subtitle"
          :content="cert.content"
          :issuer="cert.issuer"
          :bg-color="cert.bgColor"
          :bg-url="cert.bgUrl"
          :seal-url="cert.sealUrl"
          :orientation="cert.orientation"
          :holder="cert.nickName || cert.account"
          :cert-no="cert.certNo"
          :issue-date="formatDate(cert.issueTime)"
          :expire-date="cert.expireTime ? formatDate(cert.expireTime) : '长期有效'"
        />
        <el-descriptions :column="3" border size="small" class="mt-[12px]">
          <el-descriptions-item label="证书编号">{{ cert.certNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="颁发时间">{{ formatTime(cert.issueTime) }}</el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ cert.expireTime ? formatDate(cert.expireTime) : '永久有效' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="certVisible = false">关闭</el-button>
        <el-button type="primary" icon="Printer" @click="printCert">打印证书</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="ExamRecordDetail">
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import CertificatePaper from '@/components/CertificatePaper.vue';
import { getExamResult } from '@/api/exam/answer';
import type { ExamResultVO, ExamResultQuestionVO } from '@/api/exam/answer/types';
import { getMyCertificateByRecord } from '@/api/exam/cert';
import type { CertificateRecordVO } from '@/api/exam/cert/types';
import { useExamDicts } from '@/hooks/useExamDicts';

const route = useRoute();
const router = useRouter();

/** 题型与答卷状态都走字典 */
const { questionTypeOptions, examRecordStatusOptions } = useExamDicts();

// 首屏先显示骨架屏：首次 render 早于 onMounted，loading 给 false 会直接渲染空 result
const loading = ref(true);
const errorMsg = ref('');
const result = ref<ExamResultVO | null>(null);
const onlyWrong = ref(false);

const recordId = computed(() => (route.params.recordId as string) || '');

const shownQuestions = computed<ExamResultQuestionVO[]>(() => {
  const list = result.value?.questions ?? [];
  return onlyWrong.value ? list.filter((q) => q.correct === 2) : list;
});

const pad = (n: number) => String(n).padStart(2, '0');

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const formatDate = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

const usedText = (seconds?: number): string => {
  if (seconds === undefined || seconds === null) return '-';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return m > 0 ? `${m}分${s}秒` : `${s}秒`;
};

/** 服务端给的是「A、C」这种人话文本，切回数组方便比对 */
const splitKeys = (text?: string): string[] =>
  text
    ? text
        .split('、')
        .map((item) => item.trim())
        .filter(Boolean)
    : [];

const myKeys = (q: ExamResultQuestionVO): string[] => splitKeys(q.myAnswerText);
const rightKeys = (q: ExamResultQuestionVO): string[] => splitKeys(q.standardAnswerText);

const goRecords = () => router.push('/exam/records');

/* ---------------------------------- 证书 ---------------------------------- */

const cert = ref<CertificateRecordVO | undefined>(undefined);
const certVisible = ref(false);

/**
 * 及格不一定有证书：这场考试得配了证书模板才会发。
 * 没发到就当没有（按钮不显示），证书服务挂了也不能影响看成绩。
 */
const loadCert = async () => {
  if (!recordId.value) return;
  try {
    const res = await getMyCertificateByRecord(recordId.value);
    cert.value = res.data ?? undefined;
  } catch {
    cert.value = undefined;
  }
};

const printCert = () => window.print();

const loadResult = async () => {
  if (!recordId.value) {
    errorMsg.value = '缺少答卷ID';
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const res = await getExamResult(recordId.value);
    result.value = res.data ?? null;
    if (!result.value) {
      errorMsg.value = '没有找到该答卷';
    }
  } catch (e: any) {
    errorMsg.value = (e instanceof Error && e.message) || '加载答题记录失败';
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  await loadResult();
  await loadCert();
});
</script>

<style scoped lang="scss">
.summary-card,
.detail-card {
  margin-bottom: 16px;
}

.summary-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.score-row {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 8px 0;
}

.score-main {
  display: flex;
  align-items: baseline;
}

.score-value {
  font-size: 40px;
  font-weight: 600;
  color: #409eff;
}

.score-unit {
  margin-left: 6px;
  color: #909399;
}

.score-side {
  font-size: 13px;
  line-height: 1.9;
  color: #606266;
}

.text-right {
  color: #67c23a;
}

.text-wrong {
  color: #f56c6c;
}

.question-item {
  padding: 16px 0;
  border-top: 1px solid #f0f2f5;

  &:first-child {
    border-top: none;
  }
}

.question-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.question-index {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.question-score,
.question-gained {
  font-size: 12px;
  color: #909399;
}

.question-title {
  margin-bottom: 10px;
  font-size: 14px;
  color: #303133;
}

.option-list {
  margin-bottom: 10px;
}

.option-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 6px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.option-row.is-mine {
  background: #fdf3f3;
  border-color: #fab6b6;
}

.option-row.is-right {
  background: #f0f9eb;
  border-color: #b3e19d;
}

.option-key {
  width: 22px;
  font-weight: 600;
  color: #606266;
}

.option-content {
  flex: 1;
  font-size: 14px;
  line-height: 1.7;
}

.option-flag {
  padding: 1px 8px;
  font-size: 12px;
  border-radius: 8px;
}

.flag-mine {
  color: #f56c6c;
  background: #fef0f0;
}

.flag-right {
  color: #67c23a;
  background: #f0f9eb;
}

.answer-row {
  display: flex;
  gap: 10px;
  margin-bottom: 6px;
  font-size: 13px;
}

.answer-label {
  width: 68px;
  color: #909399;
}

.answer-value {
  flex: 1;
  color: #303133;

  /* 富文本作答：quill 会给段落带默认边距，这里还原成与纯文本一致的排版 */
  :deep(p) {
    margin: 0;
  }

  :deep(img) {
    max-width: 100%;
  }
}

.answer-value.is-empty {
  color: #c0c4cc;
}

.answer-value.is-standard {
  color: #67c23a;
}

.analysis-box {
  padding: 8px 12px;
  margin-top: 8px;
  background: #fafcff;
  border-radius: 6px;
}

.analysis-label {
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
}
</style>
