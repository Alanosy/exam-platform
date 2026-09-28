import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { QuestionVO, QuestionForm, QuestionQuery } from '@/api/system/question/types';

/**
 * 查询试题主列表
 * @param query
 * @returns {*}
 */

export const listQuestion = (query?: QuestionQuery): AxiosPromise<QuestionVO[]> => {
  return request({
    url: '/question/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试题主详细
 * @param id
 */
export const getQuestion = (id: string | number): AxiosPromise<QuestionVO> => {
  return request({
    url: '/question/' + id,
    method: 'get'
  });
};

/**
 * 新增试题主
 * @param data
 */
export const addQuestion = (data: QuestionForm) => {
  return request({
    url: '/question',
    method: 'post',
    data: data
  });
};

/**
 * 新增试题（含选项）
 *
 * 试题与选项一次提交同时落库，返回新建试题ID。
 * 答案为空时后端会按选项的 isRight 反推正确答案。
 * @param data
 */
export const createQuestion = (data: QuestionForm) => {
  return request({
    url: '/question/create',
    method: 'post',
    data: data
  });
};

/**
 * 修改试题主
 * @param data
 */
export const updateQuestion = (data: QuestionForm) => {
  return request({
    url: '/question',
    method: 'put',
    data: data
  });
};

/**
 * 删除试题主
 * @param id
 */
export const delQuestion = (id: string | number | Array<string | number>) => {
  return request({
    url: '/question/' + id,
    method: 'delete'
  });
};
