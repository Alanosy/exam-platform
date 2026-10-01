<template>
  <div class="practice-page">
    <div v-if="pageLoading" class="practice-center">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="questions.length === 0" class="practice-center">
      <el-result icon="info" title="没有可重刷的错题" :sub-title="emptyTip">
        <template #extra>
          <el-button type="primary" plain @click="goBack">返回错题本</el-button>
        </template>
      </el-result>
    </div>

    <div v-else class="practice-body">
      <div class="practice-main">
        <div class="question-card">
          <!-- 顶部进度：刷到第几题、还有多少、来自哪场考试 -->
          <div class="progress-box">
            <div class="progress-left">
              <span class="progress-index">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
              <el-progress
                class="progress-bar"
                :percentage="Math.round(((currentIndex + (result ? 1 : 0)) / questions.length) * 100)"
                :stroke-width="8"
                :show-text="false"
              />
              <span class="progress-stat"> 已掌握 {{ masteredCount }} · 未掌握 {{ questions.length - masteredCount }} </span>
            </div>
            <el-button link icon="Close" @click="goBack">退出重刷</el-button>
          </div>

          <!-- 题目区 -->
          <div v-if="current" class="question-box">
            <div class="question-head">
              <span class="question-index">第 {{ currentIndex + 1 }} 题</span>
              <span class="question-type">{{ questionTypeLabel(current.questionType) }}</span>
              <el-tag size="small" effect="plain" :type="questionDifficultyTagType(current.difficulty)">
                {{ questionDifficultyLabel(current.difficulty || 'medium') }}
              </el-tag>
              <el-tag size="small" effect="light" :type="wrongMasterStatusTagType(current.masterStatus)">
                {{ wrongMasterStatusLabel(current.masterStatus) }}
              </el-tag>
              <span class="question-score">错 {{ current.wrongCount ?? 0 }} 次 / 答对 {{ current.rightCount ?? 0 }} 次</span>
            </div>

            <!-- 每道题都标清楚它来自哪场考试，混着刷也不会串 -->
            <div class="source-box">
              <dict-tag :options="wrongSourceTypeOptions" :value="current.sourceType" />
              <span class="source-name">{{ sourceLabel(current) }}</span>
              <span class="source-time">最近答错 {{ formatTime(current.lastWrongTime) }}</span>
            </div>

            <div class="ql-editor ql-content question-title" v-html="current.title"></div>

            <!-- 单选 / 判断 -->
            <el-radio-group v-if="isSingleChoice" v-model="choice" class="option-group" :disabled="!!result">
              <el-radio
                v-for="opt in current.options ?? []"
                :key="opt.optionKey"
                :value="opt.optionKey"
                class="option-item"
                :class="optionClass(opt.optionKey)"
              >
                <span class="option-inner">
                  <span class="option-key">{{ opt.optionKey }}</span>
                  <span class="option-content ql-editor" v-html="opt.optionContent"></span>
                  <span v-if="optionMark(opt.optionKey)" class="option-mark">{{ optionMark(opt.optionKey) }}</span>
                </span>
              </el-radio>
            </el-radio-group>

            <!-- 多选 -->
            <el-checkbox-group v-else-if="current.questionType === 'MULTIPLE'" v-model="choices" class="option-group" :disabled="!!result">
              <el-checkbox
                v-for="opt in current.options ?? []"
                :key="opt.optionKey"
                :value="opt.optionKey"
                class="option-item option-item-multiple"
                :class="optionClass(opt.optionKey)"
              >
                <span class="option-inner">
                  <span class="option-key">{{ opt.optionKey }}</span>
                  <span class="option-content ql-editor" v-html="opt.optionContent"></span>
                  <span v-if="optionMark(opt.optionKey)" class="option-mark">{{ optionMark(opt.optionKey) }}</span>
                </span>
              </el-checkbox>
            </el-checkbox-group>

            <!-- 填空 -->
            <div v-else-if="current.questionType === 'BLANK'" class="blank-group">
              <div v-for="(blank, i) in blanks" :key="i" class="blank-item">
                <span class="blank-label">第 {{ i + 1 }} 空</span>
                <el-input v-model="blanks[i]" :disabled="!!result" placeholder="请输入答案" />
              </div>
            </div>

            <!-- 主观题：富文本，可以贴图贴公式，跟正式考试作答体验一致 -->
            <div v-else class="text-group">
              <editor
                v-model="textAnswer"
                :simple="true"
                :height="240"
                :min-height="180"
                placeholder="请写下你的作答（主观题不自动判分，提交后可对照参考答案与解析自行检查）"
              />
              <div class="text-tip">主观题提交后可对照参考答案与解析自行检查</div>
            </div>

            <!-- 即时判分 -->
            <div v-if="result" class="judge-box" :class="judgeClass">
              <div class="judge-title">
                <el-tag :type="result.correct === true ? 'success' : result.correct === false ? 'danger' : 'info'" size="small" effect="dark">
                  {{ result.correct === true ? '回答正确' : result.correct === false ? '回答错误' : '已记录作答' }}
                </el-tag>
                <span v-if="result.autoMastered" class="judge-tip">连续答对已达标，这道题已自动移出错题本</span>
              </div>
              <div class="judge-line">
                <span class="judge-label">你的作答</span>
                <span class="judge-value ql-editor" v-html="result.myAnswerText || '未作答'"></span>
              </div>
              <div class="judge-line">
                <span class="judge-label">正确答案</span>
                <span class="judge-value judge-right ql-editor" v-html="result.standardAnswerText || '-'"></span>
              </div>
              <div v-if="result.analysis" class="judge-analysis">
                <div class="judge-label">解析</div>
                <div class="judge-value ql-editor" v-html="result.analysis"></div>
              </div>
              <div class="judge-message">{{ result.message }}</div>
            </div>

            <div class="question-actions">
              <el-button :disabled="currentIndex === 0" @click="prev">上一题</el-button>
              <el-button v-if="!result" type="primary" :loading="submitting" :disabled="!canSubmit" @click="handleSubmit"> 提交作答 </el-button>
              <el-button v-else type="primary" @click="next">
                {{ currentIndex >= questions.length - 1 ? '完成重刷' : '下一题' }}
              </el-button>
              <el-button v-if="result && current.masterStatus !== 'MASTERED'" type="success" plain @click="handleMaster"> 标记为已掌握 </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：本轮范围与答题进度 -->
      <div class="practice-side">
        <el-card shadow="never" class="side-card">
          <div class="side-title">本轮范围</div>
          <div class="side-range">
            <dict-tag :options="wrongSourceTypeOptions" :value="sourceType" />
            <span class="side-range-name" :title="sourceName">{{ sourceName || '全部错题' }}</span>
          </div>
          <div class="side-meta">共 {{ questions.length }} 题 · 已刷 {{ finishedCount }} 题</div>
          <div class="card-grid">
            <div
              v-for="(item, idx) in questions"
              :key="item.id"
              class="card-cell"
              :class="{ 'is-current': idx === currentIndex, 'is-done': finished.has(item.id) }"
              @click="jump(idx)"
            >
              {{ idx + 1 }}
            </div>
          </div>
          <div class="side-legend">
            <span><i class="dot dot-done"></i>已刷</span>
            <span><i class="dot dot-current"></i>当前</span>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="ExamWrongPractice">
// 题干 / 选项 / 解析都是 quill 富文本，得带上它的阅读样式，否则排版跟考试页不一致
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import { getWrongList, reviewWrong, masterWrong } from '@/api/exam/wrong';
import type { WrongQuestionVO, WrongReviewResultVO } from '@/api/exam/wrong/types';
import { useExamDicts } from '@/hooks/useExamDicts';

const route = useRoute();
const router = useRouter();

const {
  wrongSourceTypeOptions,
  wrongMasterStatusLabel,
  wrongMasterStatusTagType,
  questionTypeLabel,
  questionDifficultyLabel,
  questionDifficultyTagType
} = useExamDicts();

const pageLoading = ref(true);
const submitting = ref(false);

const questions = ref<WrongQuestionVO[]>([]);
const currentIndex = ref(0);
/** 已刷过的错题ID */
const finished = reactive<Set<string>>(new Set());

// 本轮范围：首页点进来带 sourceId；明细页点「重刷」还会带 wrongId，只刷那一道
const sourceType = ref('');
const sourceId = ref('');
const sourceName = ref('');
const wrongId = ref('');

// 当前题的输入绑定
const choice = ref('');
const choices = ref<string[]>([]);
const blanks = ref<string[]>([]);
const textAnswer = ref('');

const result = ref<WrongReviewResultVO | null>(null);

const current = computed<WrongQuestionVO | null>(() => questions.value[currentIndex.value] ?? null);
const isSingleChoice = computed(() => current.value?.questionType === 'SINGLE' || current.value?.questionType === 'JUDGE');

/** 富文本去标签后的纯文本，用于与正确答案做比对 */
const plainOf = (html: string): string => {
  if (!html) return '';
  const div = document.createElement('div');
  div.innerHTML = html;
  return (div.textContent ?? '').replace(/[\s\u200b\ufeff]/g, '');
};

/**
 * 富文本是否真的写了内容。
 * quill 的空值是 `<p><br></p>`，只贴图不写字时 textContent 也是空的，
 * 两种情况都不能当成「未作答」。
 */
const richHasContent = (html: string): boolean => {
  if (!html) return false;
  const div = document.createElement('div');
  div.innerHTML = html;
  if (div.querySelector('img, video, audio, iframe')) return true;
  return plainOf(html).length > 0;
};

const canSubmit = computed(() => {
  const type = current.value?.questionType;
  if (type === 'SINGLE' || type === 'JUDGE') return !!choice.value;
  if (type === 'MULTIPLE') return choices.value.length > 0;
  if (type === 'BLANK') return blanks.value.some((item) => !!item && item.trim() !== '');
  return richHasContent(textAnswer.value);
});

const masteredCount = computed(() => questions.value.filter((item) => item.masterStatus === 'MASTERED').length);
const finishedCount = computed(() => finished.size);

const emptyTip = computed(() => (wrongId.value ? '这道错题可能已被移出或删除' : '这个来源下暂无错题，换一场考试试试'));

const pad = (n: number) => String(n).padStart(2, '0');

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

/** 每题都标来源：从首页进来是本场考试，从明细进来可能是单题 */
const sourceLabel = (item: WrongQuestionVO): string => item.sourceName || sourceName.value || '未知来源';

/**
 * 来源ID 只有合法正整数才作为过滤条件，且必须原样返回字符串。
 * 19 位雪花 ID 超过 Number.MAX_SAFE_INTEGER，用 Number() 转换会丢精度。
 */
const parseSourceId = (value?: string): string | undefined => {
  if (!value) return undefined;
  const text = String(value).trim();
  return /^\d+$/.test(text) ? text : undefined;
};

/** 把当前输入序列化成后端约定的作答 JSON */
const buildAnswerContent = (): string => {
  const type = current.value?.questionType;
  if (type === 'SINGLE' || type === 'JUDGE') return JSON.stringify({ choices: [choice.value] });
  if (type === 'MULTIPLE') return JSON.stringify({ choices: choices.value });
  if (type === 'BLANK') return JSON.stringify({ blanks: blanks.value.map((text) => ({ text: text ?? '' })) });
  return JSON.stringify({ text: textAnswer.value });
};

/** 切题时把输入清空，避免上一题的答案带到下一题 */
const fillInput = () => {
  const type = current.value?.questionType;
  choice.value = '';
  choices.value = [];
  // 填空题默认给一个空位，题干里有多处空白时由用户自行补填
  blanks.value = type === 'BLANK' ? Array.from({ length: blankCount.value }, () => '') : [];
  textAnswer.value = '';
  result.value = null;
};

/** 填空空位数量：用选项数兜底，题干没有选项时至少给 1 个 */
const blankCount = computed(() => {
  const opts = current.value?.options?.length ?? 0;
  return opts > 0 ? opts : 1;
});

/* ---------------------------------- 判分高亮 ---------------------------------- */

/** 判分后选项标记：正确答案绿框，用户选错的红框 */
const optionClass = (key: string): Record<string, boolean> => {
  if (!result.value) return {};
  const myKeys = myAnswerKeys.value;
  const rightKeys = rightAnswerKeys.value;
  return {
    'is-answer': rightKeys.includes(key),
    'is-mywrong': myKeys.includes(key) && !rightKeys.includes(key)
  };
};

const optionMark = (key: string): string => {
  if (!result.value) return '';
  const myKeys = myAnswerKeys.value;
  const rightKeys = rightAnswerKeys.value;
  if (rightKeys.includes(key)) return '正确答案';
  if (myKeys.includes(key)) return '你的作答';
  return '';
};

const myAnswerKeys = computed<string[]>(() => {
  const type = current.value?.questionType;
  if (type === 'SINGLE' || type === 'JUDGE') return choice.value ? [choice.value] : [];
  if (type === 'MULTIPLE') return choices.value;
  return [];
});

/** 用判分结果回传的选项顺序推断正确答案：后端 standardAnswerText 是文本，这里用选项内容匹配兜底 */
const rightAnswerKeys = computed<string[]>(() => {
  if (!result.value) return [];
  const standard = (result.value.standardAnswerText ?? '').trim();
  if (!standard) return [];
  const opts = result.value.options ?? current.value?.options ?? [];
  const matched = opts.filter((item) => plainOf(item.optionContent) === standard).map((item) => item.optionKey);
  if (matched.length > 0) return matched;
  // 直接就是选项字母（A / A、B）的情况
  return standard
    .split(/[、,，\s]+/)
    .map((item) => item.trim().toUpperCase())
    .filter((item) => /^[A-Z]$/.test(item));
});

const judgeClass = computed(() => {
  if (result.value?.correct === true) return 'is-right';
  if (result.value?.correct === false) return 'is-wrong';
  return 'is-neutral';
});

/* ---------------------------------- 交互 ---------------------------------- */

const goto = (index: number) => {
  const target = questions.value[index];
  if (!target) return;
  currentIndex.value = index;
  fillInput();
};

const next = () => {
  if (currentIndex.value < questions.value.length - 1) {
    goto(currentIndex.value + 1);
    return;
  }
  // 最后一题刷完：回明细页看结果
  goBack();
};

const prev = () => {
  if (currentIndex.value > 0) goto(currentIndex.value - 1);
};

const jump = (index: number) => goto(index);

const handleSubmit = async () => {
  const question = current.value;
  if (!question) return;
  submitting.value = true;
  try {
    const res = await reviewWrong(question.id, { answerContent: buildAnswerContent() });
    result.value = res.data;
    // 判分结果里带回最新的掌握状态，同步到本地列表，右侧题卡立刻变绿
    question.masterStatus = res.data.masterStatus;
    question.wrongCount = res.data.wrongCount;
    question.rightCount = res.data.rightCount;
    finished.add(question.id);
  } catch {
    // 失败提示由全局拦截器统一弹出
  } finally {
    submitting.value = false;
  }
};

const handleMaster = async () => {
  const question = current.value;
  if (!question) return;
  try {
    await masterWrong(question.id);
    question.masterStatus = 'MASTERED';
    finished.add(question.id);
  } catch {
    // 同上
  }
};

const goBack = () => {
  // 单题重刷回明细页，整场重刷回错题本首页
  if (wrongId.value && sourceId.value) {
    router.push({
      path: '/exam/wrong/detail',
      query: { sourceType: sourceType.value, sourceId: sourceId.value, sourceName: sourceName.value }
    });
    return;
  }
  router.push('/exam/wrong');
};

const loadQuestions = async () => {
  pageLoading.value = true;
  try {
    const res = await getWrongList({
      sourceType: sourceType.value || undefined,
      sourceId: parseSourceId(sourceId.value),
      pageNum: 1,
      pageSize: 200
    });
    let list = res.rows ?? [];
    // 明细页点「重刷」只刷这一道
    if (wrongId.value) {
      list = list.filter((item) => String(item.id) === wrongId.value);
    }
    questions.value = list;
    if (list.length > 0) fillInput();
  } catch {
    questions.value = [];
  } finally {
    pageLoading.value = false;
  }
};

onMounted(() => {
  const q = route.query;
  sourceType.value = String(q.sourceType ?? '');
  sourceId.value = String(q.sourceId ?? '');
  sourceName.value = String(q.sourceName ?? '');
  wrongId.value = String(q.wrongId ?? '');
  loadQuestions();
});
</script>

<style scoped lang="scss">
.practice-page {
  min-height: 100vh;
  padding: 20px;
  background: #f5f7fa;
  box-sizing: border-box;
}

.practice-center {
  display: flex;
  justify-content: center;
  padding: 40px 16px;
}

.practice-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  max-width: 1180px;
  margin: 0 auto;
}

.practice-main {
  flex: 1;
  min-width: 0;
}

.question-card {
  padding: 20px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
}

/* 顶部进度条 */
.progress-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.progress-left {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.progress-index {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.progress-bar {
  flex: 1;
  max-width: 260px;
}

.progress-stat {
  flex-shrink: 0;
  font-size: 12px;
  color: #909399;
}

.question-box {
  min-height: 180px;
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

.source-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 14px;
  background: #f5f9ff;
  border-radius: 8px;
}

.source-name {
  flex: 1;
  overflow: hidden;
  font-size: 13px;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-time {
  flex-shrink: 0;
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

/* 选项做成卡片：Element Plus 的 el-radio/el-checkbox 默认固定 32px 高且不换行，
   富文本选项会被压扁溢出，这里放开高度、允许换行并把输入控件顶到第一行 */
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

  &:not(.is-disabled):hover {
    background: #f7faff;
    border-color: #a0cfff;
  }

  &.is-checked {
    background: #ecf5ff;
    border-color: #409eff;
  }

  /* 提交后整组禁用，但判分结果恰恰是最要看清选项的时候，不能刷成灰字 */
  &.is-disabled {
    background: #fff;

    :deep(.el-radio__label),
    :deep(.el-checkbox__label) {
      color: #303133 !important;
    }

    &.is-answer {
      background: #f0f9eb;
    }

    &.is-mywrong {
      background: #fef0f0;
    }
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
  /* 抵消 quill 给 .ql-editor 加的固定高度 + 内边距，否则内容会被压成一小团 */
  height: auto;
  padding: 0;
  overflow: visible;
  font-size: 15px;
  line-height: 1.7;
  color: #303133;
  white-space: normal;
}

.option-mark {
  flex-shrink: 0;
  align-self: center;
  padding: 1px 8px;
  font-size: 12px;
  color: #fff;
  background: #909399;
  border-radius: 10px;
}

/* 判分后：正确答案绿框，用户选错的红框 */
.option-item.is-answer {
  background: #f0f9eb;
  border-color: #67c23a;
}

.option-item.is-mywrong {
  background: #fef0f0;
  border-color: #f56c6c;
}

.is-answer .option-mark {
  background: #67c23a;
}

.is-mywrong .option-mark {
  background: #f56c6c;
}

.blank-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.blank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.blank-label {
  width: 64px;
  flex-shrink: 0;
  font-size: 13px;
  color: #909399;
}

.text-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 判分结果：左侧色条 + 你的作答 / 正确答案 / 解析，与考试页「即时判题」一致 */
.judge-box {
  padding: 14px 16px;
  margin-top: 18px;
  background: #fafafa;
  border-left: 4px solid #909399;
  border-radius: 6px;

  &.is-right {
    background: #f0f9eb;
    border-left-color: #67c23a;
  }

  &.is-wrong {
    background: #fef0f0;
    border-left-color: #f56c6c;
  }
}

.judge-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.judge-tip {
  font-size: 12px;
  color: #67c23a;
}

.judge-line {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #303133;
}

.judge-label {
  width: 70px;
  flex-shrink: 0;
  color: #909399;
}

.judge-value {
  flex: 1;
  min-width: 0;

  /* 作答与答案也可能是富文本 */
  &.ql-editor {
    height: auto;
    padding: 0;
    overflow: visible;
    line-height: 1.7;
  }
}

.judge-right {
  font-weight: 600;
  color: #67c23a;
}

.judge-analysis {
  margin-top: 10px;
}

.judge-message {
  margin-top: 10px;
  font-size: 13px;
  color: #409eff;
}

.question-actions {
  display: flex;
  gap: 10px;
  padding-top: 18px;
  margin-top: 18px;
  border-top: 1px solid #f0f2f5;
}

.practice-side {
  width: 260px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
}

.side-card {
  border-radius: 10px;
}

.side-title {
  padding-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #f0f2f5;
}

.side-range {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 0;
}

.side-range-name {
  overflow: hidden;
  font-size: 13px;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.side-meta {
  padding-bottom: 10px;
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

@media (width <= 900px) {
  .practice-body {
    flex-direction: column;
  }

  .practice-side {
    width: 100%;
    position: static;
  }
}
</style>
