/**
 * 试题题型元数据
 *
 * 这个文件是整个试题编辑页的「行为开关」：加一种新题型，只需要在这里追加一项，
 * 编辑页的答题区会自动切换到对应的录入形态，不需要再改 edit.vue 的分支。
 */

/** 参考答案的录入形态 */
export type AnswerMode = 'option' | 'blank' | 'text' | 'code' | 'pairs';

export interface QuestionTypeMeta {
  /** 与后端字典一致的类型编码 */
  value: string;
  /** 展示名称 */
  label: string;
  /** 答案录入形态 */
  answerMode: AnswerMode;
  /** 是否维护候选选项（会落到 option 表） */
  hasOptions: boolean;
  /** 是否允许多个正确答案 */
  multiAnswer: boolean;
  /** 切换题型时默认生成的条目数（选项数 / 填空数 / 匹配对数） */
  defaultCount: number;
  /** 该题型只允许固定选项内容（判断题），为 true 时选项内容不可编辑且不可增删 */
  fixedOptions?: boolean;
  /** 答题区提示文案 */
  tip: string;
}

export const QUESTION_TYPES: QuestionTypeMeta[] = [
  {
    value: 'SINGLE',
    label: '单选题',
    answerMode: 'option',
    hasOptions: true,
    multiAnswer: false,
    defaultCount: 4,
    tip: '勾选唯一正确答案，选项内容支持富文本'
  },
  {
    value: 'MULTIPLE',
    label: '多选题',
    answerMode: 'option',
    hasOptions: true,
    multiAnswer: true,
    defaultCount: 4,
    tip: '至少勾选两个正确答案，支持部分给分时请按后端规则配置分值'
  },
  {
    value: 'JUDGE',
    label: '判断题',
    answerMode: 'option',
    hasOptions: true,
    multiAnswer: false,
    defaultCount: 2,
    fixedOptions: true,
    tip: '固定为「正确 / 错误」两项，勾选正确答案'
  },
  {
    value: 'BLANK',
    label: '填空题',
    answerMode: 'blank',
    hasOptions: false,
    multiAnswer: true,
    defaultCount: 1,
    tip: '按空依次录入参考答案，每个空可以填多个可接受的写法'
  },
  {
    value: 'SHORT_ANSWER',
    label: '简答题',
    answerMode: 'text',
    hasOptions: false,
    multiAnswer: false,
    defaultCount: 0,
    tip: '录入参考答案要点，支持富文本'
  },
  {
    value: 'ESSAY',
    label: '论述题',
    answerMode: 'text',
    hasOptions: false,
    multiAnswer: false,
    defaultCount: 0,
    tip: '录入参考答案要点，支持富文本'
  },
  {
    value: 'CODE',
    label: '代码题',
    answerMode: 'code',
    hasOptions: false,
    multiAnswer: false,
    defaultCount: 0,
    tip: '指定编程语言并录入参考实现，可补充评分说明'
  },
  {
    value: 'UPLOAD_FILE',
    label: '文件上传题',
    answerMode: 'text',
    hasOptions: false,
    multiAnswer: false,
    defaultCount: 0,
    tip: '录入评分要点 / 材料要求'
  },
  {
    value: 'MATCH',
    label: '匹配题',
    answerMode: 'pairs',
    hasOptions: false,
    multiAnswer: true,
    defaultCount: 3,
    tip: '按行录入左右两列，同一行的两项互为正确答案'
  }
];

/** 兜底元数据，避免后端返回未知类型时页面崩掉 */
const FALLBACK_META: QuestionTypeMeta = {
  value: '',
  label: '未知题型',
  answerMode: 'text',
  hasOptions: false,
  multiAnswer: false,
  defaultCount: 0,
  tip: '未知题型，按文本参考答案处理'
};

export const getQuestionTypeMeta = (value?: string): QuestionTypeMeta => {
  return QUESTION_TYPES.find((item) => item.value === value) ?? FALLBACK_META;
};

export const getQuestionTypeLabel = (value?: string): string => {
  return getQuestionTypeMeta(value).label;
};

/**
 * 难度 easy简单 medium中等 hard困难
 *
 * 仅作为 question_difficulty 字典未配置时的兜底，正常以字典为准，见 useQuestionDict.ts
 */
export const DIFFICULTY_OPTIONS = [
  { value: 'easy', label: '简单' },
  { value: 'medium', label: '中等' },
  { value: 'hard', label: '困难' }
];

export const getDifficultyLabel = (value?: string): string => {
  return DIFFICULTY_OPTIONS.find((item) => item.value === value)?.label ?? value ?? '';
};

/**
 * 状态 0草稿 1启用 2废弃
 *
 * 仅作为 question_status 字典未配置时的兜底，正常以字典为准，见 useQuestionDict.ts
 */
export const STATUS_OPTIONS = [
  { value: 0, label: '草稿' },
  { value: 1, label: '启用' },
  { value: 2, label: '废弃' }
];

export const getStatusLabel = (value?: number): string => {
  return STATUS_OPTIONS.find((item) => item.value === value)?.label ?? String(value ?? '');
};

/**
 * 代码题可选语言
 *
 * 仅作为 code_languages 字典未配置时的兜底，正常以字典为准，见 useQuestionDict.ts
 */
export const CODE_LANGUAGES = [
  { value: 'java', label: 'Java' },
  { value: 'python', label: 'Python' },
  { value: 'cpp', label: 'C++' },
  { value: 'c', label: 'C' },
  { value: 'csharp', label: 'C#' },
  { value: 'go', label: 'Go' },
  { value: 'javascript', label: 'JavaScript' },
  { value: 'typescript', label: 'TypeScript' },
  { value: 'sql', label: 'SQL' },
  { value: 'shell', label: 'Shell' }
];

/** 选项标识 A/B/C/D... */
export const OPTION_KEYS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];

export const optionKeyOf = (index: number): string => OPTION_KEYS[index] ?? `O${index + 1}`;

/**
 * question.answer 字段的数据结构
 *
 * 不同题型结构不同，统一约定如下，后端按 answerMode 解析：
 * - option: { rightKeys: ['A', 'C'] }
 * - blank : { blanks: [{ answers: ['张三'] }, { answers: [' Beijing', '北京'] }] }
 * - text  : { answer: '<p>富文本参考答案</p>' }
 * - code  : { language: 'java', answer: '...', remark: '...' }
 * - pairs : { pairs: [{ left: 'CPU', right: '中央处理器' }] }
 */
export interface OptionAnswer {
  rightKeys: string[];
}

export interface BlankAnswer {
  blanks: Array<{ answers: string[] }>;
}

export interface TextAnswer {
  answer: string;
}

export interface CodeAnswer {
  language: string;
  answer: string;
  remark?: string;
}

export interface PairAnswer {
  pairs: Array<{ left: string; right: string }>;
}

export type QuestionAnswerPayload = OptionAnswer | BlankAnswer | TextAnswer | CodeAnswer | PairAnswer;

/** 安全解析 answer 字段，后端返回 null / 非法 JSON 时不至于白屏 */
export const parseAnswer = (answer?: string): any => {
  if (!answer) return {};
  if (typeof answer === 'object') return answer as any;
  try {
    const parsed = JSON.parse(answer);
    return parsed && typeof parsed === 'object' ? parsed : {};
  } catch {
    // 历史数据可能直接存了纯文本答案
    return { answer };
  }
};
