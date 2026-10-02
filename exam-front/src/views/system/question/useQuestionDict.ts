/**
 * 试题相关字典：question_type 题型 / question_difficulty 难度 / question_status 状态 / code_languages 代码语言
 *
 * 统一在这里取字典，避免每个页面各写一份。约定：
 * 1. 字典有数据 → 用字典（编码 + 名称 + 标签样式都可以在「字典管理」里改，不用改代码）
 * 2. 字典还没配 / 接口没返回 → 回退到 questionMeta.ts 里的写死默认值，页面不至于空选项
 */

import { computed, getCurrentInstance, toRefs } from 'vue';
import {
  CODE_LANGUAGES,
  DIFFICULTY_OPTIONS,
  QUESTION_TYPES,
  STATUS_OPTIONS,
  getDifficultyLabel,
  getQuestionTypeLabel,
  getStatusLabel
} from '@/utils/questionMeta';

export interface QuestionDictOption {
  label: string;
  value: string | number;
  /** 字典的 list_class，用于 el-tag 的 type */
  elTagType?: ElTagType;
  elTagClass?: string;
}

/** 状态类字典在库里存的是数字，字典值本身是字符串，需要转回数字才能和表单 / 后端对齐 */
const toNumberIfNumeric = (value: string): string | number => {
  return /^-?\d+$/.test(value) ? Number(value) : value;
};

const normalize = (dict: DictDataOption[] | undefined, numeric: boolean): QuestionDictOption[] => {
  if (!dict || !dict.length) {
    return [];
  }
  return dict.map((item) => ({
    label: item.label,
    value: numeric ? toNumberIfNumeric(item.value) : item.value,
    elTagType: item.elTagType,
    elTagClass: item.elTagClass
  }));
};

const findOption = (options: QuestionDictOption[], value?: string | number): QuestionDictOption | undefined => {
  if (value === undefined || value === null || value === '') return undefined;
  return options.find((item) => String(item.value) === String(value));
};

export const useQuestionDicts = () => {
  const { proxy } = getCurrentInstance() as ComponentInternalInstance;
  // toRefs 保证字典异步加载完成后页面自动刷新（与项目里 bank_status 等写法一致）
  const dicts = toRefs<any>(proxy?.useDict('question_type', 'question_difficulty', 'question_status', 'code_languages'));

  /** 题型选项：只有编码和名称走字典，答案录入形态仍然由 questionMeta.ts 决定 */
  const questionTypeOptions = computed<QuestionDictOption[]>(() => {
    const dict = normalize(dicts.question_type?.value, false);
    if (dict.length) return dict;
    return QUESTION_TYPES.map((item) => ({ value: item.value, label: item.label }));
  });

  /** 难度选项 */
  const questionDifficultyOptions = computed<QuestionDictOption[]>(() => {
    const dict = normalize(dicts.question_difficulty?.value, false);
    if (dict.length) return dict;
    return DIFFICULTY_OPTIONS.map((item) => ({ value: item.value, label: item.label }));
  });

  /** 状态选项：字典值转成数字，和后端 status 字段类型保持一致 */
  const questionStatusOptions = computed<QuestionDictOption[]>(() => {
    const dict = normalize(dicts.question_status?.value, true);
    if (dict.length) return dict;
    return STATUS_OPTIONS.map((item) => ({ value: item.value, label: item.label }));
  });

  /** 代码题可选语言 */
  const codeLanguageOptions = computed<QuestionDictOption[]>(() => {
    const dict = normalize(dicts.code_languages?.value, false);
    if (dict.length) return dict;
    return CODE_LANGUAGES.map((item) => ({ value: item.value, label: item.label }));
  });

  /* --------------------------------- 名称回显 --------------------------------- */

  const questionTypeLabel = (value?: string): string => {
    return findOption(questionTypeOptions.value, value)?.label ?? getQuestionTypeLabel(value);
  };

  const questionDifficultyLabel = (value?: string): string => {
    return findOption(questionDifficultyOptions.value, value)?.label ?? getDifficultyLabel(value);
  };

  const questionStatusLabel = (value?: string | number): string => {
    return findOption(questionStatusOptions.value, value)?.label ?? getStatusLabel(Number(value));
  };

  const codeLanguageLabel = (value?: string): string => {
    return findOption(codeLanguageOptions.value, value)?.label ?? value ?? '';
  };

  /* ------------------------------ el-tag 标签样式 ------------------------------ */

  /** 优先用字典的 list_class，没配时沿用原来的写死配色 */
  const tagType = (options: QuestionDictOption[], value: string | number | undefined, fallback: () => ElTagType): ElTagType => {
    const dictType = findOption(options, value)?.elTagType;
    if (dictType) return dictType;
    return fallback();
  };

  const questionDifficultyTagType = (value?: string): ElTagType => {
    return tagType(questionDifficultyOptions.value, value, () => {
      if (value === 'easy') return 'success';
      if (value === 'medium') return 'warning';
      if (value === 'hard') return 'danger';
      return 'info';
    });
  };

  const questionStatusTagType = (value?: string | number): ElTagType => {
    return tagType(questionStatusOptions.value, value, () => {
      const num = Number(value);
      if (num === 1) return 'success';
      if (num === 2) return 'info';
      return 'warning';
    });
  };

  return {
    questionTypeOptions,
    questionDifficultyOptions,
    questionStatusOptions,
    codeLanguageOptions,
    questionTypeLabel,
    questionDifficultyLabel,
    questionStatusLabel,
    codeLanguageLabel,
    questionDifficultyTagType,
    questionStatusTagType
  };
};
