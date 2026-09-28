<template>
  <div class="p-[16px] pb-[70px]">
    <div class="flex items-center justify-between mb-[12px]">
      <div class="flex items-center gap-2">
        <el-button plain icon="Back" @click="goBack">返回</el-button>
        <span class="text-[16px] font-500">{{ isEdit ? '编辑试题' : '新增试题' }}</span>
        <el-tag v-if="meta.label" size="small" effect="plain">{{ meta.label }}</el-tag>
      </div>
      <div class="flex items-center gap-2">
        <el-button plain @click="submitForm(0)">存为草稿</el-button>
        <el-button type="primary" :loading="buttonLoading" @click="submitForm(1)">保存</el-button>
      </div>
    </div>

    <el-card shadow="never" class="mb-[12px]">
      <template #header>
        <span>基础信息</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="题型" prop="questionType">
              <el-select v-model="form.questionType" placeholder="请选择题型" class="w-full" @change="handleTypeChange">
                <el-option v-for="item in QUESTION_TYPES" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="所属题库" prop="bankId">
              <el-select v-model="form.bankId" filterable placeholder="请选择题库" class="w-full">
                <el-option v-for="item in bankList" :key="item.id" :label="item.bankName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="请选择难度" class="w-full">
                <el-option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="默认分值" prop="score">
              <el-input-number v-model="form.score" :min="0" :max="1000" :precision="1" controls-position="right" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mb-[12px]">
      <template #header>
        <span class="mr-2">题干</span>
        <el-text type="danger" size="small">*</el-text>
      </template>
      <editor v-model="form.title" :height="240" :min-height="200" placeholder="请输入题干内容，支持富文本与图片" />
    </el-card>

    <el-card shadow="never" class="mb-[12px]">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ answerCardTitle }}</span>
          <el-text type="info" size="small">{{ meta.tip }}</el-text>
        </div>
      </template>

      <!-- 单选 / 多选 / 判断：选项列表 -->
      <template v-if="meta.answerMode === 'option'">
        <div v-for="(row, index) in optionRows" :key="index" class="flex items-start gap-3 mb-[12px]">
          <div class="w-[100px] flex items-center gap-1 shrink-0 pt-[8px]">
            <el-checkbox v-model="row.isRight" @change="(val) => handleRightChange(index, val as boolean)" />
            <el-tag :type="row.isRight ? 'success' : 'info'" size="small" effect="plain">{{ optionKeyOf(index) }}</el-tag>
          </div>
          <div class="flex-1 min-w-0">
            <editor
              v-model="row.content"
              :simple="true"
              :height="120"
              :min-height="80"
              :read-only="!!meta.fixedOptions"
              placeholder="请输入选项内容"
            />
          </div>
          <div class="flex items-center gap-1 shrink-0 pt-[8px]">
            <el-button circle plain icon="Top" :disabled="!!meta.fixedOptions || index === 0" @click="moveOption(index, -1)" />
            <el-button circle plain icon="Bottom" :disabled="!!meta.fixedOptions || index === optionRows.length - 1" @click="moveOption(index, 1)" />
            <el-button
              circle
              plain
              type="danger"
              icon="Delete"
              :disabled="!!meta.fixedOptions || optionRows.length <= 2"
              @click="removeOption(index)"
            />
          </div>
        </div>
        <el-button v-if="!meta.fixedOptions" plain icon="Plus" @click="addOption">添加选项</el-button>
      </template>

      <!-- 填空：每个空可以有多个可接受答案 -->
      <template v-else-if="meta.answerMode === 'blank'">
        <div v-for="(blank, index) in blankRows" :key="index" class="flex items-center gap-3 mb-[10px]">
          <div class="w-[100px] shrink-0 text-right">第 {{ index + 1 }} 空</div>
          <el-select
            v-model="blank.answers"
            multiple
            filterable
            allow-create
            default-first-option
            :reserve-keyword="false"
            placeholder="输入答案后回车，可添加多个可接受写法"
            class="flex-1"
          />
          <el-button circle plain type="danger" icon="Delete" :disabled="blankRows.length <= 1" @click="removeBlank(index)" />
        </div>
        <el-button plain icon="Plus" @click="addBlank">添加填空</el-button>
      </template>

      <!-- 简答 / 论述 / 文件上传：参考答案富文本 -->
      <template v-else-if="meta.answerMode === 'text'">
        <editor v-model="answerText" :height="240" :min-height="180" placeholder="请输入参考答案" />
      </template>

      <!-- 代码题：语言 + 参考实现 -->
      <template v-else-if="meta.answerMode === 'code'">
        <el-form label-width="96px" class="mb-[12px]">
          <el-form-item label="编程语言">
            <el-select v-model="codeForm.language" filterable allow-create placeholder="选择或输入语言" class="w-[240px]">
              <el-option v-for="item in CODE_LANGUAGES" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="参考实现">
            <el-input v-model="codeForm.answer" type="textarea" :rows="16" class="code-textarea" placeholder="请输入参考实现代码" />
          </el-form-item>
          <el-form-item label="评分说明">
            <el-input v-model="codeForm.remark" type="textarea" :rows="4" placeholder="可填写判分要点、输出要求等" />
          </el-form-item>
        </el-form>
      </template>

      <!-- 匹配题：左右两列 -->
      <template v-else-if="meta.answerMode === 'pairs'">
        <div v-for="(pair, index) in pairRows" :key="index" class="flex items-center gap-3 mb-[10px]">
          <div class="w-[40px] shrink-0 text-center">{{ index + 1 }}</div>
          <el-input v-model="pair.left" placeholder="左项（题干侧）" />
          <span class="text-[16px] shrink-0">→</span>
          <el-input v-model="pair.right" placeholder="右项（待匹配侧）" />
          <el-button circle plain type="danger" icon="Delete" :disabled="pairRows.length <= 2" @click="removePair(index)" />
        </div>
        <el-button plain icon="Plus" @click="addPair">添加匹配项</el-button>
      </template>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <span>试题解析</span>
      </template>
      <editor v-model="form.analysis" :height="220" :min-height="180" placeholder="请输入整题解析，支持富文本" />
    </el-card>
  </div>
</template>

<script setup name="QuestionEdit" lang="ts">
import { useRoute, useRouter } from 'vue-router';
import type { FormRules } from 'element-plus';
import Editor from '@/components/Editor/index.vue';
import { createQuestion, getQuestion, updateQuestion } from '@/api/system/question';
import { QuestionForm, QuestionOption, QuestionVO } from '@/api/system/question/types';
import { listOption } from '@/api/system/option';
import { OptionVO } from '@/api/system/option/types';
import { listBank } from '@/api/system/bank';
import { BankVO } from '@/api/system/bank/types';
import {
  CODE_LANGUAGES,
  DIFFICULTY_OPTIONS,
  QUESTION_TYPES,
  STATUS_OPTIONS,
  getQuestionTypeMeta,
  optionKeyOf,
  parseAnswer,
  QuestionAnswerPayload,
  QuestionTypeMeta
} from './questionMeta';

/**
 * 列表页路由地址，需要与后台「试题管理」菜单的路由地址保持一致
 */
const QUESTION_LIST_PATH = '/system/question';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();
const router = useRouter();

/** 路由上的试题ID，为空表示新增 */
const questionId = computed<string | undefined>(() => (route.params.questionId as string) || undefined);
const isEdit = computed(() => !!questionId.value);
const buttonLoading = ref(false);

const formRef = ref<ElFormInstance>();
const bankList = ref<BankVO[]>([]);

const form = reactive<QuestionForm>({
  id: undefined,
  bankId: undefined,
  title: '',
  questionType: 'SINGLE',
  difficulty: 'easy',
  score: 5,
  analysis: '',
  answer: undefined,
  status: 1,
  options: []
});

/** 当前题型的元数据 */
const meta = computed<QuestionTypeMeta>(() => getQuestionTypeMeta(form.questionType));

const rules: FormRules = {
  bankId: [{ required: true, message: '请选择所属题库', trigger: 'change' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  score: [{ required: true, message: '请输入默认分值', trigger: 'blur' }]
};

/* ---------------------------------- 录入数据结构 --------------------------------- */

interface OptionRow {
  id?: string | number;
  content: string;
  isRight: boolean;
}

const optionRows = ref<OptionRow[]>([]);
const blankRows = ref<Array<{ answers: string[] }>>([]);
const pairRows = ref<Array<{ left: string; right: string }>>([]);
const answerText = ref('');
const codeForm = reactive({ language: '', answer: '', remark: '' });

const answerCardTitleMap: Record<string, string> = {
  option: '选项与正确答案',
  blank: '填空参考答案',
  text: '参考答案',
  code: '代码题参考答案',
  pairs: '匹配项'
};
const answerCardTitle = computed(() => answerCardTitleMap[meta.value.answerMode] ?? '参考答案');

/** 去掉富文本标签，用于判空与摘要 */
const stripHtml = (html?: string): string => {
  if (!html) return '';
  return html
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim();
};

/* ---------------------------------- 答案区增删改 --------------------------------- */

const judgeRows = (): OptionRow[] => [
  { content: '<p>正确</p>', isRight: true },
  { content: '<p>错误</p>', isRight: false }
];

const emptyOptionRows = (): OptionRow[] => Array.from({ length: Math.max(meta.value.defaultCount, 2) }, () => ({ content: '', isRight: false }));

const defaultBlankRows = () => Array.from({ length: Math.max(meta.value.defaultCount, 1) }, () => ({ answers: [] as string[] }));
const defaultPairRows = () => Array.from({ length: Math.max(meta.value.defaultCount, 2) }, () => ({ left: '', right: '' }));

/** 按当前题型初始化默认的答案录入区 */
const initAnswerArea = () => {
  const mode = meta.value.answerMode;
  if (mode === 'option') {
    optionRows.value = meta.value.fixedOptions ? judgeRows() : emptyOptionRows();
  } else if (mode === 'blank') {
    blankRows.value = defaultBlankRows();
  } else if (mode === 'pairs') {
    pairRows.value = defaultPairRows();
  } else if (mode === 'code') {
    if (!codeForm.language) codeForm.language = 'java';
  }
};

const clearAnswerArea = () => {
  optionRows.value = [];
  blankRows.value = [];
  pairRows.value = [];
  answerText.value = '';
  codeForm.language = '';
  codeForm.answer = '';
  codeForm.remark = '';
};

const addOption = () => {
  optionRows.value.push({ content: '', isRight: false });
};

const removeOption = (index: number) => {
  optionRows.value.splice(index, 1);
};

const moveOption = (index: number, offset: number) => {
  const target = index + offset;
  if (target < 0 || target >= optionRows.value.length) return;
  const rows = optionRows.value;
  [rows[index], rows[target]] = [rows[target], rows[index]];
};

/** 单选/判断题互斥，只保留最新勾选的那个 */
const handleRightChange = (index: number, checked: boolean) => {
  if (!checked || meta.value.multiAnswer) return;
  optionRows.value.forEach((row, i) => {
    row.isRight = i === index;
  });
};

const enforceSingleAnswer = (m: QuestionTypeMeta) => {
  if (m.multiAnswer) return;
  const first = optionRows.value.findIndex((row) => row.isRight);
  optionRows.value.forEach((row, i) => {
    row.isRight = i === first;
  });
};

const addBlank = () => blankRows.value.push({ answers: [] });
const removeBlank = (index: number) => blankRows.value.splice(index, 1);

const addPair = () => pairRows.value.push({ left: '', right: '' });
const removePair = (index: number) => pairRows.value.splice(index, 1);

/* ----------------------------------- 切换题型 ----------------------------------- */

const hasAnswerContent = (m: QuestionTypeMeta): boolean => {
  switch (m.answerMode) {
    case 'option':
      return optionRows.value.some((row) => stripHtml(row.content));
    case 'blank':
      return blankRows.value.some((blank) => blank.answers.length > 0);
    case 'text':
      return !!stripHtml(answerText.value);
    case 'code':
      return !!codeForm.answer.trim();
    case 'pairs':
      return pairRows.value.some((pair) => pair.left.trim() || pair.right.trim());
    default:
      return false;
  }
};

const applyTypeChange = (prev: QuestionTypeMeta, next: QuestionTypeMeta) => {
  if (prev.answerMode !== next.answerMode) {
    clearAnswerArea();
    initAnswerArea();
    return;
  }
  if (next.answerMode === 'option') {
    if (next.fixedOptions) {
      optionRows.value = judgeRows();
    } else if (prev.fixedOptions) {
      optionRows.value = emptyOptionRows();
    } else {
      enforceSingleAnswer(next);
    }
  }
};

const handleTypeChange = (value: string) => {
  const prevValue = form.questionType;
  const prev = getQuestionTypeMeta(prevValue);
  const next = getQuestionTypeMeta(value);
  if (prev.answerMode === next.answerMode) {
    form.questionType = value;
    applyTypeChange(prev, next);
    return;
  }
  if (hasAnswerContent(prev)) {
    proxy?.$modal
      .confirm(`切换为「${next.label}」会清空当前已录入的答案内容，是否继续？`)
      .then(() => {
        form.questionType = value;
        applyTypeChange(prev, next);
      })
      .catch(() => {
        // 取消时把题型还原回去
        form.questionType = prevValue;
      });
    return;
  }
  form.questionType = value;
  applyTypeChange(prev, next);
};

/* ----------------------------------- 数据回显 ----------------------------------- */

const restoreAnswer = (answerStr?: string, options: QuestionOption[] = []) => {
  const answer = parseAnswer(answerStr);
  switch (meta.value.answerMode) {
    case 'option': {
      const rightKeys: string[] = Array.isArray(answer?.rightKeys) ? answer.rightKeys : [];
      optionRows.value = options.length
        ? options.map((item) => ({
            id: item.id,
            content: item.optionContent ?? '',
            isRight: rightKeys.includes(item.optionKey ?? '')
          }))
        : meta.value.fixedOptions
          ? judgeRows()
          : emptyOptionRows();
      if (meta.value.fixedOptions) {
        // 判断题固定两项，库里没存内容时才补默认文案，避免覆盖已保存的选项内容
        const judge = judgeRows();
        optionRows.value.forEach((row, index) => {
          if (!stripHtml(row.content)) {
            row.content = judge[index]?.content ?? '';
          }
        });
      }
      enforceSingleAnswer(meta.value);
      break;
    }
    case 'blank': {
      const blanks = Array.isArray(answer?.blanks) ? answer.blanks : [];
      blankRows.value = blanks.length ? blanks.map((item: any) => ({ answers: [...(item?.answers ?? [])] })) : defaultBlankRows();
      break;
    }
    case 'text':
      answerText.value = answer?.answer ?? '';
      break;
    case 'code':
      codeForm.language = answer?.language ?? 'java';
      codeForm.answer = answer?.answer ?? '';
      codeForm.remark = answer?.remark ?? '';
      break;
    case 'pairs': {
      const pairs = Array.isArray(answer?.pairs) ? answer.pairs : [];
      pairRows.value = pairs.length ? pairs.map((item: any) => ({ left: item?.left ?? '', right: item?.right ?? '' })) : defaultPairRows();
      break;
    }
  }
};

const loadBankList = async () => {
  try {
    const res = await listBank({ pageNum: 1, pageSize: 500 });
    bankList.value = res.rows ?? [];
  } catch {
    bankList.value = [];
  }
};

const loadDetail = async (id: string | number) => {
  const res = await getQuestion(id);
  const data = res.data as QuestionVO;
  Object.assign(form, {
    id: data.id,
    bankId: data.bankId,
    title: data.title ?? '',
    questionType: data.questionType,
    difficulty: data.difficulty ?? 'easy',
    score: data.score ?? 0,
    analysis: data.analysis ?? '',
    status: data.status ?? 0
  });
  let options: QuestionOption[] = data.options ?? [];
  // 详情接口没有嵌套返回选项时，兜底单独查一次选项列表
  if (!options.length) {
    try {
      const optionRes = await listOption({ pageNum: 1, questionId: id, pageSize: 200 });
      options = ((optionRes.rows ?? []) as OptionVO[]).map((item) => ({
        id: item.id,
        questionId: item.questionId,
        optionKey: item.optionKey,
        optionContent: item.optionContent,
        sort: item.sort
      }));
    } catch {
      // 选项兜底失败也要保证题干等基础信息能回显
      options = [];
    }
  }
  restoreAnswer(data.answer, options);
};

/* ------------------------------------ 提交 ------------------------------------ */

const buildAnswer = (): QuestionAnswerPayload => {
  switch (meta.value.answerMode) {
    case 'option':
      return { rightKeys: optionRows.value.map((row, index) => (row.isRight ? optionKeyOf(index) : '')).filter(Boolean) };
    case 'blank':
      return { blanks: blankRows.value.map((blank) => ({ answers: [...blank.answers] })) };
    case 'text':
      return { answer: answerText.value };
    case 'code':
      return { language: codeForm.language, answer: codeForm.answer, remark: codeForm.remark };
    case 'pairs':
      return { pairs: pairRows.value.map((pair) => ({ left: pair.left, right: pair.right })) };
    default:
      return {} as QuestionAnswerPayload;
  }
};

/** 答案区业务校验，返回第一个错误提示 */
const validateAnswer = (): string | null => {
  const { label, multiAnswer } = meta.value;
  switch (meta.value.answerMode) {
    case 'option': {
      if (optionRows.value.length < 2) return '至少需要两个选项';
      const emptyIndex = optionRows.value.findIndex((row) => !stripHtml(row.content));
      if (emptyIndex !== -1) return `选项 ${optionKeyOf(emptyIndex)} 的内容不能为空`;
      const rightCount = optionRows.value.filter((row) => row.isRight).length;
      if (!multiAnswer && rightCount !== 1) return `${label}必须且只能有一个正确答案`;
      if (multiAnswer && rightCount < 2) return `${label}至少需要两个正确答案`;
      return null;
    }
    case 'blank': {
      const emptyIndex = blankRows.value.findIndex((blank) => blank.answers.length === 0);
      return emptyIndex === -1 ? null : `第 ${emptyIndex + 1} 空还没有填写参考答案`;
    }
    case 'text':
      return stripHtml(answerText.value) ? null : '参考答案不能为空';
    case 'code':
      if (!codeForm.language) return '请选择编程语言';
      return codeForm.answer.trim() ? null : '参考实现不能为空';
    case 'pairs': {
      const badIndex = pairRows.value.findIndex((pair) => !pair.left.trim() || !pair.right.trim());
      return badIndex === -1 ? null : `第 ${badIndex + 1} 组匹配项还没有填写完整`;
    }
    default:
      return null;
  }
};

const validateForm = async (): Promise<boolean> => {
  if (!formRef.value) return false;
  try {
    await formRef.value.validate();
    return true;
  } catch {
    return false;
  }
};

const submitForm = async (status?: number) => {
  if (!stripHtml(form.title)) {
    proxy?.$modal.msgError('题干不能为空');
    return;
  }
  const answerError = validateAnswer();
  if (answerError) {
    proxy?.$modal.msgError(answerError);
    return;
  }
  if (!(await validateForm())) return;

  const payload: QuestionForm = {
    ...form,
    title: form.title,
    status: typeof status === 'number' ? status : form.status,
    answer: JSON.stringify(buildAnswer()),
    options: meta.value.hasOptions
      ? optionRows.value.map((row, index) => ({
          id: row.id,
          questionId: form.id,
          optionKey: optionKeyOf(index),
          optionContent: row.content,
          sort: index + 1,
          isRight: row.isRight
        }))
      : []
  };

  buttonLoading.value = true;
  try {
    if (form.id) {
      await updateQuestion(payload);
    } else {
      // 走「新增试题（含选项）」接口，试题与选项一次提交同时落库
      await createQuestion(payload);
    }
    proxy?.$modal.msgSuccess('保存成功');
    goBack();
  } finally {
    buttonLoading.value = false;
  }
};

const goBack = () => {
  // 从哪个页面进来就回哪个页面（组卷、预览等入口同样适用），并顺手关掉当前编辑页签
  const historyState = window.history.state as { back?: string } | null;
  const from = historyState?.back;
  if (from && !from.includes('/question/edit')) {
    proxy?.$tab.closePage(router.currentRoute.value);
    router.back();
    return;
  }
  // 直接打开本页（没有可回退的历史）时，关闭当前页签并回列表
  proxy?.$tab.closeOpenPage({ path: QUESTION_LIST_PATH });
};

onMounted(async () => {
  await loadBankList();
  if (questionId.value) {
    await loadDetail(questionId.value);
  } else {
    initAnswerArea();
  }
});
</script>

<style scoped lang="scss">
.code-textarea :deep(textarea) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
  line-height: 1.6;
}
</style>
