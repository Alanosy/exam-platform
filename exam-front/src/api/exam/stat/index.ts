import request from '@/utils/request';
import type { AxiosPromise } from 'axios';
import type {
  HomeStatVO,
  StatAnswerVO,
  StatDashboardVO,
  StatExamQuery,
  StatExamRowVO,
  StatKnowledgeVO,
  StatOverviewVO,
  StatQuestionQuery,
  StatQuestionVO,
  StatSegmentVO,
  StatUserQuery,
  StatUserVO
} from '@/api/exam/stat/types';

/**
 * 首页总览
 *
 * 一次把 Dashboard 要的数字全部带回来，避免为了一张页面打七八个接口。
 * 需要 system:exam:list 权限（能看考试列表的人才能看全局总览），
 * 考生不要调这个接口，前端直接走「我的」视图。
 */
export const getHomeOverview = (): AxiosPromise<HomeStatVO> => {
  return request({
    url: '/stat/home/overview',
    method: 'get'
  });
};

/* ====================== 考试统计 ====================== */

/** 大盘概览 */
export const getStatDashboard = (params: StatExamQuery): AxiosPromise<StatDashboardVO> => {
  return request({ url: '/stat/dashboard', method: 'get', params });
};

/** 考试统计列表 */
export const listStatExam = (params: StatExamQuery): AxiosPromise<{ rows: StatExamRowVO[]; total: number }> => {
  return request({ url: '/stat/exam/page', method: 'get', params });
};

/** 单场考试详情 */
export const getStatOverview = (examId: string): AxiosPromise<StatOverviewVO> => {
  return request({ url: `/stat/exam/${examId}/overview`, method: 'get' });
};

/** 分数段分布 */
export const listStatSegment = (examId: string): AxiosPromise<StatSegmentVO[]> => {
  return request({ url: `/stat/exam/${examId}/segment`, method: 'get' });
};

/** 考生成绩 */
export const listStatUser = (examId: string, params: Omit<StatUserQuery, 'examId'>): AxiosPromise<{ rows: StatUserVO[]; total: number }> => {
  return request({ url: `/stat/exam/${examId}/users`, method: 'get', params });
};

/** 考生答卷明细 */
export const getStatUserDetail = (examId: string, userId: string, attemptNo?: number): AxiosPromise<StatAnswerVO> => {
  return request({
    url: `/stat/exam/${examId}/user/${userId}/detail`,
    method: 'get',
    params: attemptNo ? { attemptNo } : {}
  });
};

/** 试题分析 */
export const listStatQuestion = (examId: string, params: Omit<StatQuestionQuery, 'examId'>): AxiosPromise<{ rows: StatQuestionVO[]; total: number }> => {
  return request({ url: `/stat/exam/${examId}/questions`, method: 'get', params });
};

/** 知识点薄弱分析 */
export const listStatKnowledge = (examId: string): AxiosPromise<StatKnowledgeVO[]> => {
  return request({ url: `/stat/exam/${examId}/knowledge`, method: 'get' });
};

/**
 * 重新计算
 *
 * 交卷 / 阅卷会自动触发，这个接口是给「改完分发现数字不对」时手动刷一次用的。
 */
export const recalcStat = (examId: string): AxiosPromise<null> => {
  return request({ url: `/stat/exam/${examId}/recalc`, method: 'post' });
};

/** 标记 / 取消作废：作弊、缺考等不该进统计的答卷 */
export const markStatExcluded = (examId: string, recordId: string, excluded: boolean): AxiosPromise<null> => {
  return request({
    url: `/stat/exam/${examId}/record/${recordId}/exclude`,
    method: 'put',
    params: { excluded }
  });
};
