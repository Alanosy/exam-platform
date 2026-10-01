<template>
  <div class="answer-page">
    <!-- 加载中 / 出错 -->
    <div v-if="loading" class="answer-center">
      <el-skeleton :rows="6" animated />
    </div>
    <div v-else-if="errorMsg" class="answer-center">
      <el-result icon="warning" title="无法进入答题" :sub-title="errorMsg">
        <template #extra>
          <el-button type="primary" plain @click="goCenter">返回考试中心</el-button>
        </template>
      </el-result>
    </div>

    <!-- 成绩视图 -->
    <div v-else-if="result" class="answer-center">
      <el-card class="result-card" shadow="never">
        <el-result :icon="result.passed ? 'success' : 'info'" :title="result.passed ? '恭喜，考试通过' : '已交卷'">
          <template #sub-title>
            <div class="result-score">
              <span class="result-score-num">{{ result.totalScore }}</span>
              <span class="result-score-unit">/ {{ result.paperTotalScore }} 分</span>
            </div>
            <div class="result-tip">
              客观题 {{ result.objectiveScore }} 分 · 用时 {{ Math.round(result.usedSeconds / 60) }} 分钟
              <el-tag v-if="result.autoSubmit" type="warning" size="small" effect="plain" class="ml-1">超时自动交卷</el-tag>
            </div>
          </template>
          <template #extra>
            <el-button v-if="result.showAnswer" plain @click="showDetail = !showDetail">
              {{ showDetail ? '收起解析' : '查看答案与解析' }}
            </el-button>
            <el-button plain @click="goRecordDetail">答题记录</el-button>
            <el-button type="primary" @click="goCenter">返回考试中心</el-button>
          </template>
        </el-result>

        <div v-if="showDetail && result.showAnswer" class="result-detail">
          <div v-for="q in result.questions" :key="q.questionId" class="detail-item">
            <div class="detail-title">
              <el-tag :type="q.correct === 1 ? 'success' : q.correct === 2 ? 'danger' : 'info'" size="small" effect="plain">
                {{ q.correct === 1 ? '正确' : q.correct === 2 ? '错误' : '待阅' }}
              </el-tag>
              <span class="ml-2">第 {{ q.sort }} 题（{{ q.score }} 分，得 {{ q.gainedScore }} 分）</span>
            </div>
            <div class="detail-question ql-editor" v-html="q.title"></div>
            <div class="detail-answer">你的作答：<span class="answer-html ql-editor" v-html="readableAnswer(q.myAnswer)"></span></div>
            <div v-if="q.standardAnswer" class="detail-answer">
              参考答案：<span class="answer-html ql-editor" v-html="readableAnswer(q.standardAnswer)"></span>
            </div>
            <div v-if="q.analysis" class="detail-analysis ql-editor" v-html="q.analysis"></div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 试卷为空 / 下标越界，兜底防渲染崩溃 -->
    <div v-else-if="!current" class="answer-center">
      <el-result icon="info" title="暂无可作答的题目" sub-title="该试卷当前没有题目，请联系管理员检查组卷">
        <template #extra>
          <el-button type="primary" plain @click="goCenter">返回考试中心</el-button>
        </template>
      </el-result>
    </div>

    <!-- 答题视图 -->
    <div v-else class="answer-body">
      <div class="answer-main">
        <div class="question-card">
          <div class="question-head">
            <span class="question-index">第 {{ currentIndex + 1 }} 题</span>
            <span class="question-type">{{ typeLabel(current.questionType) }}</span>
            <span class="question-score">{{ current.score }} 分</span>
          </div>
          <div class="ql-editor ql-content question-title" v-html="current.title"></div>

          <!-- 单选 / 判断 -->
          <el-radio-group
            v-if="current.questionType === 'SINGLE' || current.questionType === 'JUDGE'"
            v-model="choiceSingle"
            class="option-group"
            @change="(val: string) => saveChoice([val])"
          >
            <el-radio v-for="op in current.options ?? []" :key="op.optionKey" :value="op.optionKey" class="option-item">
              <span class="option-inner">
                <span class="option-key">{{ op.optionKey }}</span>
                <span class="option-content ql-editor" v-html="op.optionContent"></span>
              </span>
            </el-radio>
          </el-radio-group>

          <!-- 多选 -->
          <el-checkbox-group
            v-else-if="current.questionType === 'MULTIPLE'"
            v-model="choiceMultiple"
            class="option-group"
            @change="(val: string[]) => saveChoice(val)"
          >
            <el-checkbox v-for="op in current.options ?? []" :key="op.optionKey" :value="op.optionKey" class="option-item option-item-multiple">
              <span class="option-inner">
                <span class="option-key">{{ op.optionKey }}</span>
                <span class="option-content ql-editor" v-html="op.optionContent"></span>
              </span>
            </el-checkbox>
          </el-checkbox-group>

          <!-- 填空 -->
          <div v-else-if="current.questionType === 'BLANK'" class="blank-group">
            <div v-for="(blank, idx) in blankAnswers" :key="idx" class="blank-item">
              <span class="blank-label">第 {{ idx + 1 }} 空</span>
              <el-input v-model="blankAnswers[idx]" placeholder="请输入答案" @blur="saveBlank" />
            </div>
          </div>

          <!-- 主观题用富文本：可以贴图、加粗、列公式，跟出卷时看到的排版一致 -->
          <!-- quill 内部元素没有可冒泡的 blur，用外层 div 的 focusout 才能兜住「点别处」时的保存 -->
          <div v-else-if="!isCode" class="text-group" @focusout="saveText">
            <editor v-model="textAnswer" :simple="true" :height="240" :min-height="180" placeholder="请输入你的作答，支持富文本与图片" />
            <div class="text-tip">主观题作答会在离开本题或交卷时保存</div>
          </div>
          <!-- 代码题保持纯文本，富文本会破坏缩进与语法字符 -->
          <div v-else class="text-group">
            <el-input v-model="textAnswer" type="textarea" :rows="8" class="plain-answer" placeholder="请输入代码" @blur="saveText" />
            <div class="text-tip">代码题请用纯文本作答，保留缩进</div>
          </div>

          <div class="question-actions">
            <el-button :disabled="currentIndex === 0" @click="prev">上一题</el-button>
            <el-button :disabled="currentIndex >= questions.length - 1" @click="next">下一题</el-button>
            <el-button type="danger" plain @click="handleSubmit">交卷</el-button>
          </div>
        </div>
      </div>

      <!-- 右侧：倒计时 + 题卡 -->
      <div class="answer-side">
        <el-card shadow="never" class="side-card">
          <div class="clock">
            <div class="clock-label">剩余时间</div>
            <div class="clock-value" :class="{ 'is-danger': remaining >= 0 && remaining <= 60 }">
              {{ remaining < 0 ? '不限时' : clockText }}
            </div>
          </div>
          <div class="side-meta">共 {{ questions.length }} 题 · 已答 {{ answeredCount }} 题</div>
          <div class="card-grid">
            <div
              v-for="(q, idx) in questions"
              :key="q.questionId"
              class="card-cell"
              :class="{ 'is-current': idx === currentIndex, 'is-done': !!answers[q.questionId], 'is-flagged': flagged.has(q.questionId) }"
              @click="jump(idx)"
            >
              {{ idx + 1 }}
            </div>
          </div>
          <div class="side-legend">
            <span><i class="dot dot-done"></i>已答</span>
            <span><i class="dot dot-current"></i>当前</span>
          </div>
          <el-button type="danger" class="side-submit" @click="handleSubmit">交卷</el-button>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="submitVisible" title="确认交卷" width="380px" append-to-body>
      <div>已答 {{ answeredCount }} / {{ questions.length }} 题，交卷后不可修改，确认交卷？</div>
      <template #footer>
        <el-button @click="submitVisible = false">再检查一下</el-button>
        <el-button type="primary" :loading="submitting" @click="doSubmit">确认交卷</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="ExamAnswer">
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import { getExamPaper, saveAnswer, submitExam, getExamResult } from '@/api/exam/answer';
import type { ExamPaperVO, ExamQuestionVO, ExamResultVO } from '@/api/exam/answer/types';
import { useExamDicts } from '@/hooks/useExamDicts';

const route = useRoute();
const router = useRouter();

/** 题型文案走字典，改字典即可，不用改代码 */
const { questionTypeLabel } = useExamDicts();

// 首屏必须先落在骨架屏上：初始 render 发生在 onMounted 之前，
// 若这里给 false，会直接渲染答题区并因 questions 为空而报 Cannot read properties of undefined
const loading = ref(true);
const submitting = ref(false);
const submitVisible = ref(false);
const showDetail = ref(false);
const errorMsg = ref('');

const recordId = computed(() => (route.params.recordId as string) || '');
const questions = ref<ExamQuestionVO[]>([]);
const currentIndex = ref(0);
const remaining = ref(-1);
const paperName = ref('');
const result = ref<ExamResultVO | null>(null);

/** 题目ID → 作答内容（JSON 字符串） */
const answers = reactive<Record<string, string>>({});
/** 已标记待复查的题 */
const flagged = reactive<Set<string>>(new Set());

// 当前题的输入绑定
const choiceSingle = ref('');
const choiceMultiple = ref<string[]>([]);
const blankAnswers = ref<string[]>([]);
const textAnswer = ref('');

const current = computed<ExamQuestionVO | null>(() => questions.value[currentIndex.value] ?? null);
const answeredCount = computed(() => Object.keys(answers).filter((key) => answers[key]).length);

/** 代码题保持纯文本输入，富文本会破坏缩进 */
const isCode = computed(() => current.value?.questionType === 'CODE');

const typeLabel = (type: string) => questionTypeLabel(type);

/**
 * 富文本是否真的写了内容。
 * quill 的空值是 `<p><br></p>`，textContent 看起来是空的；
 * 只贴图不写字时 textContent 也是空的，所以还得单独看媒体标签。
 */
const richHasContent = (html: string): boolean => {
  if (!html) return false;
  const div = document.createElement('div');
  div.innerHTML = html;
  if (div.querySelector('img, video, audio, iframe')) return true;
  return (div.textContent ?? '').replace(/[\s\u200b\ufeff]/g, '').length > 0;
};

const pad = (n: number) => String(n).padStart(2, '0');
const clockText = computed(() => {
  const total = Math.max(0, remaining.value);
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  const s = total % 60;
  return h > 0 ? `${pad(h)}:${pad(m)}:${pad(s)}` : `${pad(m)}:${pad(s)}`;
});

/** 把存下来的 JSON 作答转成可读文本，用于结果展示 */
const readableAnswer = (json?: string): string => {
  if (!json) return '未作答';
  try {
    const obj = JSON.parse(json);
    if (obj.choices?.length) return obj.choices.join('、');
    if (obj.blanks?.length) return obj.blanks.map((b: any) => b.text ?? '').join(' | ');
    if (obj.text) return obj.text;
    return json;
  } catch {
    return json;
  }
};

/** 用当前题的已存答案回填输入控件 */
const fillInput = (question: ExamQuestionVO) => {
  const raw = answers[question.questionId];
  let parsed: any = {};
  if (raw) {
    try {
      parsed = JSON.parse(raw);
    } catch {
      parsed = {};
    }
  }
  if (question.questionType === 'SINGLE' || question.questionType === 'JUDGE') {
    choiceSingle.value = parsed.choices?.[0] ?? '';
  } else if (question.questionType === 'MULTIPLE') {
    choiceMultiple.value = parsed.choices ?? [];
  } else if (question.questionType === 'BLANK') {
    const count = (parsed.blanks?.length ?? 0) > 0 ? parsed.blanks.length : 1;
    blankAnswers.value = Array.from({ length: count }, (_, i) => parsed.blanks?.[i]?.text ?? '');
  } else {
    textAnswer.value = parsed.text ?? '';
  }
};

/** 保存当前题作答（切题、失焦、选项变更都走这里） */
const persistCurrent = async (silent = true) => {
  const question = current.value;
  if (!question) return;
  let content = '';
  if (question.questionType === 'SINGLE' || question.questionType === 'JUDGE') {
    if (!choiceSingle.value) return;
    content = JSON.stringify({ choices: [choiceSingle.value] });
  } else if (question.questionType === 'MULTIPLE') {
    if (choiceMultiple.value.length === 0) return;
    content = JSON.stringify({ choices: choiceMultiple.value });
  } else if (question.questionType === 'BLANK') {
    if (!blankAnswers.value.some((item) => item && item.trim())) return;
    content = JSON.stringify({ blanks: blankAnswers.value.map((text) => ({ text: text ?? '' })) });
  } else {
    // 富文本空内容会残留 <p><br></p>，要去标签后再判空
    if (!richHasContent(textAnswer.value)) return;
    content = JSON.stringify({ text: textAnswer.value });
  }
  if (answers[question.questionId] === content) return;
  answers[question.questionId] = content;
  try {
    await saveAnswer(recordId.value, { questionId: question.questionId, answerContent: content });
  } catch (e: any) {
    if (!silent) throw e;
  }
};

const saveChoice = (val: string[]) => persistCurrent();
const saveBlank = () => persistCurrent();
const saveText = () => persistCurrent();

const goto = async (index: number) => {
  await persistCurrent();
  const target = questions.value[index];
  if (!target) return;
  currentIndex.value = index;
  fillInput(target);
};

const next = () => {
  if (currentIndex.value < questions.value.length - 1) goto(currentIndex.value + 1);
};

const prev = () => {
  if (currentIndex.value > 0) goto(currentIndex.value - 1);
};

const jump = (index: number) => goto(index);

/* ---------------------------------- 倒计时 ---------------------------------- */

let timer: ReturnType<typeof setInterval> | undefined;

const startTimer = () => {
  stopTimer();
  if (remaining.value < 0) return;
  timer = setInterval(() => {
    if (remaining.value > 0) {
      remaining.value -= 1;
      return;
    }
    stopTimer();
    // 时间到：主动交卷（后端对已交卷的答卷幂等），交卷失败再退回查成绩
    autoFinish();
  }, 1000);
};

const stopTimer = () => {
  if (timer) {
    clearInterval(timer);
    timer = undefined;
  }
};

/* ---------------------------------- 加载与交卷 ---------------------------------- */

const loadPaper = async () => {
  if (!recordId.value) {
    errorMsg.value = '缺少答卷ID';
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const res = await getExamPaper(recordId.value);
    const data = res.data;
    questions.value = data.questions ?? [];
    remaining.value = data.remainingSeconds ?? -1;
    paperName.value = data.paperName;
    // 已保存的作答回填，支持中途退出后继续
    questions.value.forEach((q) => {
      if (q.myAnswer) answers[q.questionId] = q.myAnswer;
    });
    if (questions.value.length === 0) {
      errorMsg.value = '该试卷没有题目';
      return;
    }
    fillInput(questions.value[0]);
    startTimer();
  } catch (e: any) {
    // 超时自动交卷 / 已交卷都会走到这里，直接看成绩
    const msg = e instanceof Error ? e.message : '';
    if (msg.includes('自动交卷') || msg.includes('已提交')) {
      await fetchResult();
      return;
    }
    errorMsg.value = msg || '加载试卷失败';
  } finally {
    loading.value = false;
  }
};

const fetchResult = async () => {
  stopTimer();
  loading.value = true;
  try {
    const res = await getExamResult(recordId.value);
    result.value = res.data;
  } catch (e: any) {
    errorMsg.value = (e instanceof Error && e.message) || '加载成绩失败';
  } finally {
    loading.value = false;
  }
};

/** 倒计时归零：先落最后一次作答再交卷，保证「到点即交」而不是等定时任务兜底 */
const autoFinish = async () => {
  try {
    await persistCurrent();
  } catch {
    // 最后一题没存上也要把卷交掉，不能卡在答题页
  }
  try {
    const res = await submitExam(recordId.value);
    result.value = res.data;
  } catch {
    await fetchResult();
  }
};

const handleSubmit = () => {
  submitVisible.value = true;
};

const doSubmit = async () => {
  submitting.value = true;
  try {
    await persistCurrent(false);
    const res = await submitExam(recordId.value);
    result.value = res.data;
    submitVisible.value = false;
    stopTimer();
  } catch (e: any) {
    errorMsg.value = (e instanceof Error && e.message) || '交卷失败，请重试';
    submitVisible.value = false;
  } finally {
    submitting.value = false;
  }
};

const goCenter = () => router.push('/exam/center');

/** 成绩视图里跳完整的逐题答题记录 */
const goRecordDetail = () => router.push(`/exam/record/${recordId.value}`);

// 同一个组件服务两个路由：/exam/answer/{id} 答题，/exam/result/{id} 直接看成绩
onMounted(() => {
  if (route.path.startsWith('/exam/result')) {
    fetchResult();
  } else {
    loadPaper();
  }
});
onBeforeUnmount(stopTimer);
</script>

<style scoped lang="scss">
.answer-page {
  min-height: 100vh;
  padding: 20px;
  background: #f5f7fa;
  box-sizing: border-box;
}

.answer-center {
  display: flex;
  justify-content: center;
  padding: 40px 16px;
}

.answer-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  max-width: 1180px;
  margin: 0 auto;
}

.answer-main {
  flex: 1;
  min-width: 0;
}

.question-card {
  padding: 20px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
}

.question-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.question-index {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.question-type {
  padding: 1px 8px;
  font-size: 12px;
  color: #337ecc;
  background: #ecf5ff;
  border-radius: 8px;
}

.question-score {
  font-size: 12px;
  color: #909399;
}

.question-title {
  margin-bottom: 18px;
  font-size: 15px;
  color: #303133;

  /* quill 的 .ql-editor 自带 height:100% + 12px/15px 内边距，展示场景要还原掉 */
  &.ql-editor {
    height: auto;
    padding: 0;
    overflow: visible;
    line-height: 1.8;
  }
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.option-item {
  display: flex !important;
  align-items: flex-start !important;
  width: 100%;
  height: auto !important;
  min-height: 48px;
  padding: 12px 14px !important;
  margin: 0 !important;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition:
    border-color 0.2s,
    background-color 0.2s;

  &:hover {
    background: #f7faff;
    border-color: #a0cfff;
  }

  &.is-checked {
    background: #ecf5ff;
    border-color: #409eff;
  }

  /* 控件本体（圆圈 / 方框）放在最左，跟首行文字对齐 */
  :deep(.el-radio__input),
  :deep(.el-checkbox__input) {
    flex-shrink: 0;
    margin-top: 3px;
  }

  /* 标签区域撑满整行，否则内容会缩在左边一小块 */
  :deep(.el-radio__label),
  :deep(.el-checkbox__label) {
    display: block;
    flex: 1;
    min-width: 0;
    padding-left: 10px;
    font-size: 15px;
    line-height: 1.7;
    color: #303133;
    white-space: normal;
    word-break: break-word;
  }
}

.option-inner {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
}

.option-key {
  flex-shrink: 0;
  min-width: 20px;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.7;
  color: #606266;
}

.option-content {
  flex: 1;
  min-width: 0;
  /* 抵消 quill 给 .ql-editor 加的固定高度 + 12px/15px 内边距，否则内容会被压成一小团 */
  height: auto;
  padding: 0;
  overflow: visible;
  font-size: 15px;
  line-height: 1.7;
  color: #303133;
  white-space: normal;
}

.blank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.blank-label {
  width: 64px;
  font-size: 13px;
  color: #909399;
}

.text-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 代码题用等宽字体，缩进才对得上 */
.plain-answer {
  :deep(textarea) {
    font-family: Menlo, Monaco, Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
  }
}

.question-actions {
  display: flex;
  gap: 10px;
  padding-top: 18px;
  margin-top: 18px;
  border-top: 1px solid #f0f2f5;
}

.answer-side {
  width: 260px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
}

.side-card {
  border-radius: 10px;
}

.clock {
  padding-bottom: 12px;
  text-align: center;
  border-bottom: 1px solid #f0f2f5;
}

.clock-label {
  font-size: 12px;
  color: #909399;
}

.clock-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  font-variant-numeric: tabular-nums;
}

.clock-value.is-danger {
  color: #f56c6c;
}

.side-meta {
  padding: 10px 0;
  font-size: 13px;
  color: #606266;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.card-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.card-cell.is-done {
  color: #fff;
  background: #67c23a;
  border-color: #67c23a;
}

.card-cell.is-current {
  color: #fff;
  background: #409eff;
  border-color: #409eff;
}

.card-cell.is-flagged {
  border-color: #e6a23c;
}

.side-legend {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  font-size: 12px;
  color: #909399;
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 4px;
  border-radius: 50%;
}

.dot-done {
  background: #67c23a;
}

.dot-current {
  background: #409eff;
}

.side-submit {
  width: 100%;
}

.result-card {
  width: 100%;
  max-width: 760px;
}

.result-score {
  font-size: 15px;
  color: #606266;
}

.result-score-num {
  font-size: 32px;
  font-weight: 600;
  color: #409eff;
}

.result-score-unit {
  margin-left: 4px;
}

.result-tip {
  margin-top: 6px;
  font-size: 13px;
}

.result-detail {
  padding: 0 20px 20px;
}

.detail-item {
  padding: 14px 0;
  border-top: 1px solid #f0f2f5;
}

.detail-title {
  margin-bottom: 8px;
  font-size: 13px;
  color: #303133;
}

.detail-answer {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
}

/* 作答与参考答案可能是富文本：还原 quill 阅读区样式，避免被压成一小团 */
.answer-html {
  &.ql-editor {
    display: inline-block;
    height: auto;
    padding: 0;
    overflow: visible;
    vertical-align: top;
    line-height: 1.7;
  }
}

.detail-question {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;

  /* 同上：还原 quill 阅读区样式 */
  &.ql-editor {
    height: auto;
    padding: 0;
    overflow: visible;
  }
}

.detail-analysis {
  padding: 8px 12px;
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.8;
  background: #fafcff;
  border-radius: 6px;

  &.ql-editor {
    height: auto;
    overflow: visible;
  }
}

@media (width <= 900px) {
  .answer-body {
    flex-direction: column;
  }

  .answer-side {
    width: 100%;
    position: static;
  }
}
</style>
