import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import type {
  AiEnabledVO,
  AiModelVO,
  AiMarkScoreForm,
  AiMarkScoreVO,
  AiQuestionGenForm,
  AiQuestionGenVO,
  AiQuestionAuditVO,
  AiPaperReviewForm,
  AiPaperReviewVO,
  AiDiagnoseForm,
  AiDiagnoseVO,
  AiSkillRunForm,
  AiSkillRunVO
} from '@/api/system/ai/types';

/**
 * AI 能力是否可用
 *
 * 页面渲染 AI 入口前先问一次：agent 没起来时不该给用户一个点了必报错的按钮。
 */
export const getAiEnabled = (): AxiosPromise<AiEnabledVO> => {
  return request({
    url: '/ai/enabled',
    method: 'get'
  });
};

/**
 * 模型清单（脱敏，不含密钥）
 */
export const getAiModelList = (): AxiosPromise<AiModelVO[]> => {
  return request({
    url: '/ai/model/list',
    method: 'get'
  });
};

/**
 * 主观题 AI 评分：只给建议分，不写库
 */
export const aiMarkScore = (data: AiMarkScoreForm): AxiosPromise<AiMarkScoreVO> => {
  return request({
    url: '/ai/mark/score',
    method: 'post',
    data: data
  });
};

/**
 * 主观题 AI 批量评分
 */
export const aiMarkScoreBatch = (data: AiMarkScoreForm[]): AxiosPromise<AiMarkScoreVO[]> => {
  return request({
    url: '/ai/mark/batch',
    method: 'post',
    data: data
  });
};

/**
 * 智能出题
 */
export const aiGenerateQuestions = (data: AiQuestionGenForm): AxiosPromise<AiQuestionGenVO[]> => {
  return request({
    url: '/ai/question/generate',
    method: 'post',
    data: data
  });
};

/**
 * 试题质检
 */
export const aiAuditQuestion = (data: AiQuestionGenVO): AxiosPromise<AiQuestionAuditVO> => {
  return request({
    url: '/ai/question/audit',
    method: 'post',
    data: data
  });
};

/**
 * 试卷审查
 */
export const aiReviewPaper = (data: AiPaperReviewForm): AxiosPromise<AiPaperReviewVO> => {
  return request({
    url: '/ai/paper/review',
    method: 'post',
    data: data
  });
};

/**
 * 错题归因
 */
export const aiDiagnose = (data: AiDiagnoseForm): AxiosPromise<AiDiagnoseVO> => {
  return request({
    url: '/ai/learn/diagnose',
    method: 'post',
    data: data
  });
};

/**
 * 通用 Skill 执行（运维自测与临时调用新能力用）
 */
export const aiRunSkill = (data: AiSkillRunForm): AxiosPromise<AiSkillRunVO> => {
  return request({
    url: '/ai/skill/run',
    method: 'post',
    data: data
  });
};
