import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import type {
  WrongOverviewVO,
  WrongSourceVO,
  WrongSourceQuery,
  WrongQuestionVO,
  WrongQuestionQuery,
  WrongReviewRecordVO,
  WrongReviewResultVO,
  WrongReviewForm,
  WrongNoteForm
} from '@/api/exam/wrong/types';

/**
 * 错题本总览统计
 */
export const getWrongOverview = (): AxiosPromise<WrongOverviewVO> => {
  return request({
    url: '/practice/wrong/overview',
    method: 'get'
  });
};

/**
 * 按来源分组统计（不分页，用于下拉等小数据量场景）
 */
export const getWrongSources = (): AxiosPromise<WrongSourceVO[]> => {
  return request({
    url: '/practice/wrong/sources',
    method: 'get'
  });
};

/**
 * 按来源分页：首页只列到「哪场考试错了多少题」，点进去才看明细
 * @param query 查询条件 + 分页参数
 */
export const getWrongSourcesPage = (query: WrongSourceQuery): AxiosPromise<WrongSourceVO[]> => {
  return request({
    url: '/practice/wrong/sources/page',
    method: 'get',
    params: query
  });
};

/**
 * 我的错题（分页）
 * @param query 查询条件 + 分页参数
 */
export const getWrongList = (query: WrongQuestionQuery): AxiosPromise<WrongQuestionVO[]> => {
  return request({
    url: '/practice/wrong/list',
    method: 'get',
    params: query
  });
};

/**
 * 错题详情
 * @param id 错题记录ID
 */
export const getWrongDetail = (id: string | number): AxiosPromise<WrongQuestionVO> => {
  return request({
    url: `/practice/wrong/${id}`,
    method: 'get'
  });
};

/**
 * 某道错题的重做历史
 * @param id 错题记录ID
 */
export const getWrongReviews = (id: string | number): AxiosPromise<WrongReviewRecordVO[]> => {
  return request({
    url: `/practice/wrong/${id}/reviews`,
    method: 'get'
  });
};

/**
 * 重做错题：判分并返回正确答案与解析
 * @param id 错题记录ID
 * @param data 本次作答（answerContent 为 JSON 字符串）
 */
export const reviewWrong = (id: string | number, data: WrongReviewForm): AxiosPromise<WrongReviewResultVO> => {
  return request({
    url: `/practice/wrong/${id}/review`,
    method: 'post',
    data: data
  });
};

/**
 * 保存错题笔记
 * @param id 错题记录ID
 * @param data 笔记内容
 */
export const saveWrongNote = (id: string | number, data: WrongNoteForm): AxiosPromise<void> => {
  return request({
    url: `/practice/wrong/${id}/note`,
    method: 'put',
    data: data
  });
};

/**
 * 标记为已掌握
 * @param id 错题记录ID
 */
export const masterWrong = (id: string | number): AxiosPromise<void> => {
  return request({
    url: `/practice/wrong/${id}/master`,
    method: 'put'
  });
};

/**
 * 移出错题本（标记已忽略）
 * @param id 错题记录ID
 */
export const ignoreWrong = (id: string | number): AxiosPromise<void> => {
  return request({
    url: `/practice/wrong/${id}/ignore`,
    method: 'put'
  });
};

/**
 * 恢复已忽略的错题
 * @param id 错题记录ID
 */
export const restoreWrong = (id: string | number): AxiosPromise<void> => {
  return request({
    url: `/practice/wrong/${id}/restore`,
    method: 'put'
  });
};

/**
 * 彻底删除错题
 * @param id 错题记录ID
 */
export const deleteWrong = (id: string | number): AxiosPromise<void> => {
  return request({
    url: `/practice/wrong/${id}`,
    method: 'delete'
  });
};
