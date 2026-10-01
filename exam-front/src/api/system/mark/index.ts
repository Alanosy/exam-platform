import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import type { MarkExamVO, MarkExamQuery, MarkTaskVO, MarkTaskQuery, MarkQuestionVO, MarkLogVO, MarkScoreForm } from '@/api/system/mark/types';

/**
 * 阅卷列表：按考试聚合，只统计正式考试
 * @param query 查询条件 + 分页参数
 */
export const getMarkExamList = (query: MarkExamQuery): AxiosPromise<MarkExamVO[]> => {
  return request({
    url: '/mark/exam/list',
    method: 'get',
    params: query
  });
};

/**
 * 某场考试下的答卷列表
 * @param query 查询条件 + 分页参数
 */
export const getMarkTaskList = (query: MarkTaskQuery): AxiosPromise<MarkTaskVO[]> => {
  return request({
    url: '/mark/task/list',
    method: 'get',
    params: query
  });
};

/**
 * 阅卷页：主观题明细
 * @param taskId 阅卷任务ID
 */
export const getMarkQuestions = (taskId: string | number): AxiosPromise<MarkQuestionVO[]> => {
  return request({
    url: `/mark/task/${taskId}/questions`,
    method: 'get'
  });
};

/**
 * 阅卷操作日志
 * @param taskId 阅卷任务ID
 */
export const getMarkLogs = (taskId: string | number): AxiosPromise<MarkLogVO[]> => {
  return request({
    url: `/mark/task/${taskId}/logs`,
    method: 'get'
  });
};

/**
 * 单题打分
 * @param data 打分入参
 */
export const saveMarkScore = (data: MarkScoreForm): AxiosPromise<void> => {
  return request({
    url: '/mark/score',
    method: 'post',
    data: data
  });
};

/**
 * 确认完成阅卷
 * @param taskId 阅卷任务ID
 */
export const finishMarkTask = (taskId: string | number): AxiosPromise<void> => {
  return request({
    url: `/mark/task/${taskId}/finish`,
    method: 'post'
  });
};

/**
 * AI 批量预评：只给建议分与理由，教师确认后才作为最终得分
 * @param taskId 阅卷任务ID
 */
export const aiPreviewMarkTask = (taskId: string | number): AxiosPromise<void> => {
  return request({
    url: `/mark/task/${taskId}/ai`,
    method: 'post'
  });
};
