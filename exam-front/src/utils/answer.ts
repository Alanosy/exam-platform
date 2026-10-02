/**
 * 答案展示工具
 *
 * <p>题库里存的是 JSON 字符串（`question.answer` 参考答案、`exam_answer.answer_content`
 * 考生作答），直接贴到页面上就是 `{"answer":"..."}` 这种谁也看不懂的东西。
 * 这里按题型把 JSON 翻译成人能看的内容，凡是「要显示答案」的页面统一走这两个函数：
 *
 * <ul>
 *   <li>{@link formatAnswer} —— 返回可直接 v-html 的片段</li>
 *   <li>{@link answerKeys}   —— 客观题取选项标识（A/C），用于高亮正确项</li>
 * </ul>
 *
 * <h3>两种 JSON 家族（键名不一样，别混）</h3>
 * <pre>
 * 参考答案 question.answer（由出题页 / 导入写入）
 *   单选/多选/判断 : { "rightKeys": ["A", "C"] }        判断题 A=正确 B=错误
 *   填空           : { "blanks": [{ "answers": ["北京", "北平"] }] }
 *   简答/论述/上传 : { "answer": "&lt;p&gt;富文本要点&lt;/p&gt;" }
 *   代码题         : { "language": "java", "answer": "...", "remark": "..." }
 *   匹配题         : { "pairs": [{ "left": "CPU", "right": "中央处理器" }] }
 *
 * 考生作答 exam_answer.answer_content（由答题页 / 错题重刷写入）
 *   单选/判断      : { "choices": ["A"] }
 *   多选           : { "choices": ["A", "C"] }
 *   填空           : { "blanks": [{ "text": "北京" }] }
 *   主观题         : { "text": "&lt;p&gt;考生作答&lt;/p&gt;" }
 * </pre>
 *
 * 两个家族的键名不冲突，所以取值时**两种都认**（`rightKeys ?? choices`），
 * 即使调用方把 standard 传反了也不会显示成一串 JSON。
 *
 * @author ruoyi
 */

import { getQuestionTypeMeta } from '@/utils/questionMeta';

export interface AnswerOptionLike {
  /** 选项标识 A/B/C... */
  optionKey?: string;
  /** 选项内容（富文本） */
  optionContent?: string;
}

export interface FormatAnswerOptions {
  /** 是否是参考答案（默认 false，即考生作答） */
  standard?: boolean;
  /** 本题选项，传了就把「A」显示成「A. 选项内容」 */
  options?: AnswerOptionLike[];
  /** 没有答案时的占位，默认空串（页面自己决定「未作答」还是「-」） */
  empty?: string;
}

/** 转义 HTML：考生填的纯文本可能带尖括号，不能直接 v-html */
const escapeHtml = (text: string): string => text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

/** 内容里是不是已经有 HTML 标签 */
const hasHtmlTag = (text: string): boolean => /<\/?[a-zA-Z][^>]*>/.test(text);

/** 纯文本 → 安全 HTML：转义 + 换行转 <br> */
const textToHtml = (text?: string): string => {
  if (!text) return '';
  return escapeHtml(text).replace(/\r?\n/g, '<br>');
};

/**
 * 富文本字段：本来就是 HTML 就原样渲染，不是（历史数据存了纯文本）就转义后渲染
 */
const richToHtml = (text?: string): string => {
  if (!text) return '';
  return hasHtmlTag(text) ? text : textToHtml(text);
};

/** 安全解析答案 JSON：不是 JSON 或解析出来不是对象都返回 null */
export const parseAnswerJson = (raw?: string | null): Record<string, any> | null => {
  if (!raw) return null;
  if (typeof raw === 'object') return raw as Record<string, any>;
  const text = String(raw).trim();
  if (!text.startsWith('{') && !text.startsWith('[')) return null;
  try {
    const parsed = JSON.parse(text);
    return parsed && typeof parsed === 'object' ? parsed : null;
  } catch {
    return null;
  }
};

/** 取数组字段，兼容两种 JSON 家族 */
const pickList = (obj: Record<string, any> | null, ...names: string[]): any[] => {
  if (!obj) return [];
  for (const name of names) {
    const value = obj[name];
    if (Array.isArray(value)) return value;
  }
  return [];
};

/** 取字符串字段 */
const pickText = (obj: Record<string, any> | null, ...names: string[]): string => {
  if (!obj) return '';
  for (const name of names) {
    const value = obj[name];
    if (typeof value === 'string' && value.trim()) return value;
    if (typeof value === 'number') return String(value);
  }
  return '';
};

/** 选项「A」→「A. 内容」，没传选项就只显示标识 */
const optionLabel = (key: string, options?: AnswerOptionLike[]): string => {
  const content = options?.find((item) => String(item.optionKey ?? '') === key)?.optionContent ?? '';
  return content ? `${key}. ${content}` : key;
};

/** 一个填空的展示文本：标准答案取 answers（多种可接受写法），考生作答取 text */
const blankText = (item: any, standard: boolean): string => {
  if (!item) return '';
  if (typeof item === 'string') return item;
  const answers = Array.isArray(item.answers) ? item.answers.map((v: any) => String(v ?? '')).filter(Boolean) : [];
  const text = String(item.text ?? '').trim();
  if (standard) {
    return answers.length ? answers.join(' / ') : text;
  }
  return text || answers.join(' / ');
};

/**
 * 答案 → 可直接 v-html 的片段
 *
 * @param raw          答案原文（JSON 字符串，也可能是历史遗留的纯文本）
 * @param questionType 题型，用来决定按哪种结构解析
 * @param opts         standard 参考答案 / options 选项（用于把 A 显示成「A. 内容」）/ empty 占位
 */
export const formatAnswer = (raw?: string | null, questionType?: string, opts: FormatAnswerOptions = {}): string => {
  if (raw === null || raw === undefined || !String(raw).trim()) {
    return opts.empty ?? '';
  }
  const text = String(raw);
  const obj = parseAnswerJson(text);
  const standard = opts.standard === true;
  const mode = getQuestionTypeMeta(questionType).answerMode;

  // 不是 JSON（后端已经转成人话，或历史数据就是纯文本）：原样渲染，绝不把 JSON 贴出来
  if (!obj) {
    return richToHtml(text);
  }

  switch (mode) {
    case 'option': {
      const keys = pickList(obj, standard ? 'rightKeys' : 'choices', standard ? 'choices' : 'rightKeys')
        .map((k) => String(k ?? '').trim())
        .filter(Boolean);
      if (keys.length === 0) return opts.empty ?? '';
      return keys.map((key) => optionLabel(key, opts.options)).join('、');
    }
    case 'blank': {
      const blanks = pickList(obj, 'blanks');
      if (blanks.length === 0) return opts.empty ?? '';
      return blanks
        .map((item, index) => {
          const value = blankText(item, standard);
          return `第${index + 1}空：${value ? escapeHtml(value) : '未作答'}`;
        })
        .join('；');
    }
    case 'pairs': {
      const pairs = pickList(obj, 'pairs');
      if (pairs.length === 0) return opts.empty ?? '';
      return pairs
        .map((item) => {
          const left = String(item?.left ?? '').trim();
          const right = String(item?.right ?? '').trim();
          return `${escapeHtml(left)} → ${escapeHtml(right)}`;
        })
        .join('；');
    }
    case 'code': {
      const language = pickText(obj, 'language');
      const code = pickText(obj, 'answer', 'text');
      const remark = pickText(obj, 'remark');
      const parts: string[] = [];
      if (language) parts.push(`<span class="answer-code-lang">${escapeHtml(language)}</span>`);
      if (code) parts.push(`<pre class="answer-code">${escapeHtml(code)}</pre>`);
      if (remark) parts.push(`<div class="answer-remark">${richToHtml(remark)}</div>`);
      return parts.length ? parts.join('') : (opts.empty ?? '');
    }
    default: {
      // 简答 / 论述 / 文件上传：富文本要点，两种家族的键都认
      const value = pickText(obj, standard ? 'answer' : 'text', standard ? 'text' : 'answer');
      return value ? richToHtml(value) : (opts.empty ?? '');
    }
  }
};

/**
 * 客观题的选项标识（A / A、C），主观题返回空数组
 *
 * <p>页面高亮「正确答案」「我选错了」时需要拿到标识本身，而 formatAnswer 可能已经把
 * 它拼成了「A. 内容」，所以单独提供一个取标识的函数。
 */
export const answerKeys = (raw?: string | null, questionType?: string): string[] => {
  if (!raw || !String(raw).trim()) return [];
  if (getQuestionTypeMeta(questionType).answerMode !== 'option') return [];
  const obj = parseAnswerJson(raw);
  if (obj) {
    const keys = pickList(obj, 'rightKeys', 'choices', 'keys');
    if (keys.length) {
      return keys.map((k) => String(k ?? '').trim()).filter(Boolean);
    }
    const single = pickText(obj, 'rightKey', 'choice');
    return single ? [single] : [];
  }
  // 后端给的是「A、B」这类文本时兜底拆一下
  return String(raw)
    .split(/[^A-Za-z0-9]+/)
    .map((item) => item.toUpperCase())
    .filter((item) => /^[A-Z]{1,2}$/.test(item));
};
