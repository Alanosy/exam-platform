/**
 * 考试域字典：考试管理 / 阅卷 / 错题本 / 考试中心 / 考试记录
 *
 * 与试题模块的 useQuestionDict 同一套约定：
 *   1. 字典有数据 → 用字典（文案、排序、标签颜色都能在「系统管理 → 字典管理」里改，不用改代码发版）
 *   2. 字典没配 / 接口还没回来 → 用下面的兜底值，页面不至于开天窗
 *
 * 字典 SQL 见 exam-back/script/sql/update/update_exam_dict.sql
 *
 * 用法：
 *   const { examTypeOptions, examTypeLabel, examTypeTagType } = useExamDicts();
 */

import { computed, getCurrentInstance, toRefs } from 'vue';
// 题型 / 难度的兜底值以 questionMeta.ts 为唯一来源，避免两处各写一份
import { DIFFICULTY_OPTIONS, QUESTION_TYPES } from '@/views/system/question/questionMeta';

export interface ExamDictOption {
  label: string;
  value: string;
  /** 对应 sys_dict_data.list_class，用于 el-tag 的 type */
  elTagType?: ElTagType;
  elTagClass?: string;
}

/** 兜底选项：取值与后端各实体里的常量一一对应 */
const FALLBACK_OPTIONS: Record<string, ExamDictOption[]> = {
  exam_type: [
    { value: '1', label: '正式考试' },
    { value: '2', label: '练习考试' }
  ],
  exam_status: [
    { value: 'not_start', label: '未开始' },
    { value: 'ongoing', label: '进行中' },
    { value: 'finished', label: '已结束' },
    { value: 'archived', label: '已归档' }
  ],
  exam_participant_type: [
    { value: 'white', label: '白名单' },
    { value: 'public', label: '公开链接' }
  ],
  exam_show_answer_mode: [
    { value: 'none', label: '不展示' },
    { value: 'after_submit', label: '交卷后展示' },
    { value: 'after_exam', label: '考试结束后展示' },
    { value: 'immediate', label: '立即展示（刷题即时判题）' }
  ],
  exam_my_status: [
    { value: 'not_start', label: '未开始' },
    { value: 'pending', label: '待考试' },
    { value: 'answering', label: '答题中' },
    { value: 'submitted', label: '已交卷' },
    { value: 'ended', label: '已结束' },
    { value: 'late', label: '迟到不可参加' },
    { value: 'blocked', label: '不可参加' }
  ],
  exam_record_status: [
    { value: 'answering', label: '答题中' },
    { value: 'submitted', label: '已交卷' },
    { value: 'expired', label: '已过期' }
  ],
  mark_task_status: [
    { value: 'pending', label: '待阅' },
    { value: 'marking', label: '阅卷中' },
    { value: 'finished', label: '已阅完' }
  ],
  mark_log_action: [
    { value: 'create', label: '建任务' },
    { value: 'score', label: '打分' },
    { value: 'rescore', label: '改分' },
    { value: 'ai', label: 'AI 预评' },
    { value: 'finish', label: '完成阅卷' }
  ],
  wrong_source_type: [
    { value: 'EXAM', label: '考试' },
    { value: 'PAPER_PRACTICE', label: '试卷练习' }
  ],
  wrong_master_status: [
    { value: 'NOT_MASTER', label: '未掌握' },
    { value: 'MASTERED', label: '已掌握' },
    { value: 'IGNORED', label: '已忽略' }
  ],
  question_type: QUESTION_TYPES.map((item) => ({ value: item.value, label: item.label })),
  question_difficulty: DIFFICULTY_OPTIONS.map((item) => ({ value: item.value, label: item.label }))
};

/** 兜底标签色：字典没配 list_class 时沿用原来的配色 */
const FALLBACK_TAG: Record<string, Record<string, ElTagType>> = {
  exam_type: { '1': 'warning', '2': 'success' },
  exam_status: { not_start: 'info', ongoing: 'success', finished: 'info', archived: 'warning' },
  exam_my_status: {
    not_start: 'info',
    pending: 'primary',
    answering: 'warning',
    submitted: 'success',
    ended: 'info',
    late: 'danger',
    blocked: 'danger'
  },
  exam_record_status: { answering: 'warning', submitted: 'success', expired: 'info' },
  mark_task_status: { pending: 'warning', marking: 'primary', finished: 'success' },
  wrong_source_type: { EXAM: 'warning', PAPER_PRACTICE: 'info' },
  wrong_master_status: { NOT_MASTER: 'danger', MASTERED: 'success', IGNORED: 'info' },
  question_difficulty: { easy: 'success', medium: 'warning', hard: 'danger' }
};

/** 一次拉齐，useDict 内部有 store 缓存，换页不会重复请求 */
const DICT_TYPES = [
  'exam_type',
  'exam_status',
  'exam_participant_type',
  'exam_show_answer_mode',
  'exam_my_status',
  'exam_record_status',
  'mark_task_status',
  'mark_log_action',
  'wrong_source_type',
  'wrong_master_status',
  'question_type',
  'question_difficulty'
];

const textOf = (value?: string | number): string => (value === undefined || value === null ? '' : String(value));

const labelOf = (options: ExamDictOption[], value?: string | number, fallback = ''): string => {
  const val = textOf(value);
  if (!val) return fallback;
  return options.find((item) => item.value === val)?.label ?? fallback;
};

const tagTypeOf = (options: ExamDictOption[], dictType: string, value?: string | number): ElTagType => {
  const val = textOf(value);
  return options.find((item) => item.value === val)?.elTagType ?? FALLBACK_TAG[dictType]?.[val] ?? 'info';
};

export const useExamDicts = () => {
  const { proxy } = getCurrentInstance() as ComponentInternalInstance;
  // toRefs 保证字典异步加载完成后页面自动刷新（与项目里 bank_status 等写法一致）
  const dicts = toRefs<any>(proxy?.useDict(...DICT_TYPES));

  /** 字典优先，兜底其次 */
  const optionsOf = (dictType: string) =>
    computed<ExamDictOption[]>(() => {
      const dict = dicts[dictType]?.value as DictDataOption[] | undefined;
      if (dict && dict.length > 0) {
        return dict.map((item) => ({
          label: item.label,
          value: item.value,
          elTagType: item.elTagType,
          elTagClass: item.elTagClass
        }));
      }
      return FALLBACK_OPTIONS[dictType] ?? [];
    });

  /* --------------------------------- 下拉选项 --------------------------------- */

  const examTypeOptions = optionsOf('exam_type');
  const examStatusOptions = optionsOf('exam_status');
  const examParticipantTypeOptions = optionsOf('exam_participant_type');
  const examShowAnswerModeOptions = optionsOf('exam_show_answer_mode');
  const examMyStatusOptions = optionsOf('exam_my_status');
  const examRecordStatusOptions = optionsOf('exam_record_status');
  const markTaskStatusOptions = optionsOf('mark_task_status');
  const markLogActionOptions = optionsOf('mark_log_action');
  const wrongSourceTypeOptions = optionsOf('wrong_source_type');
  const wrongMasterStatusOptions = optionsOf('wrong_master_status');
  const questionTypeOptions = optionsOf('question_type');
  const questionDifficultyOptions = optionsOf('question_difficulty');

  /* ---------------------------------- 文案回显 ---------------------------------- */

  /** 兜底给 value 本身：未知编码至少能让用户看到原始值，不至于空白 */
  const examTypeLabel = (value?: string | number): string => labelOf(examTypeOptions.value, value, textOf(value));
  const examStatusLabel = (value?: string | number): string => labelOf(examStatusOptions.value, value, textOf(value));
  const examParticipantTypeLabel = (value?: string | number): string => labelOf(examParticipantTypeOptions.value, value, textOf(value));
  const examShowAnswerModeLabel = (value?: string | number): string => labelOf(examShowAnswerModeOptions.value, value, textOf(value));
  const examMyStatusLabel = (value?: string | number): string => labelOf(examMyStatusOptions.value, value, textOf(value));
  const examRecordStatusLabel = (value?: string | number): string => labelOf(examRecordStatusOptions.value, value, textOf(value));
  const markTaskStatusLabel = (value?: string | number): string => labelOf(markTaskStatusOptions.value, value, textOf(value));
  const markLogActionLabel = (value?: string | number): string => labelOf(markLogActionOptions.value, value, textOf(value));
  const wrongSourceTypeLabel = (value?: string | number): string => labelOf(wrongSourceTypeOptions.value, value, textOf(value));
  const wrongMasterStatusLabel = (value?: string | number): string => labelOf(wrongMasterStatusOptions.value, value, textOf(value));
  const questionTypeLabel = (value?: string | number): string => labelOf(questionTypeOptions.value, value, '问答题');
  const questionDifficultyLabel = (value?: string | number): string => labelOf(questionDifficultyOptions.value, value, textOf(value));

  /* --------------------------------- 标签配色 --------------------------------- */

  const examTypeTagType = (value?: string | number): ElTagType => tagTypeOf(examTypeOptions.value, 'exam_type', value);
  const examStatusTagType = (value?: string | number): ElTagType => tagTypeOf(examStatusOptions.value, 'exam_status', value);
  const examMyStatusTagType = (value?: string | number): ElTagType => tagTypeOf(examMyStatusOptions.value, 'exam_my_status', value);
  const examRecordStatusTagType = (value?: string | number): ElTagType => tagTypeOf(examRecordStatusOptions.value, 'exam_record_status', value);
  const markTaskStatusTagType = (value?: string | number): ElTagType => tagTypeOf(markTaskStatusOptions.value, 'mark_task_status', value);
  const wrongSourceTypeTagType = (value?: string | number): ElTagType => tagTypeOf(wrongSourceTypeOptions.value, 'wrong_source_type', value);
  const wrongMasterStatusTagType = (value?: string | number): ElTagType => tagTypeOf(wrongMasterStatusOptions.value, 'wrong_master_status', value);
  const questionDifficultyTagType = (value?: string | number): ElTagType => tagTypeOf(questionDifficultyOptions.value, 'question_difficulty', value);

  return {
    examTypeOptions,
    examStatusOptions,
    examParticipantTypeOptions,
    examShowAnswerModeOptions,
    examMyStatusOptions,
    examRecordStatusOptions,
    markTaskStatusOptions,
    markLogActionOptions,
    wrongSourceTypeOptions,
    wrongMasterStatusOptions,
    questionTypeOptions,
    questionDifficultyOptions,
    examTypeLabel,
    examStatusLabel,
    examParticipantTypeLabel,
    examShowAnswerModeLabel,
    examMyStatusLabel,
    examRecordStatusLabel,
    markTaskStatusLabel,
    markLogActionLabel,
    wrongSourceTypeLabel,
    wrongMasterStatusLabel,
    questionTypeLabel,
    questionDifficultyLabel,
    examTypeTagType,
    examStatusTagType,
    examMyStatusTagType,
    examRecordStatusTagType,
    markTaskStatusTagType,
    wrongSourceTypeTagType,
    wrongMasterStatusTagType,
    questionDifficultyTagType
  };
};
