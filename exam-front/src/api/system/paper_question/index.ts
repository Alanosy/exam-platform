import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { QuestionVO, QuestionForm, QuestionQuery } from '@/api/paper/question/types';

/**
 * 查询试卷-试题中间列表
 * @param query
 * @returns {*}
 */

export const listQuestion = (query?: QuestionQuery): AxiosPromise<QuestionVO[]> => {
  return request({
    url: '/paper/question/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试卷-试题中间详细
 * @param id
 */
export const getQuestion = (id: string | number): AxiosPromise<QuestionVO> => {
  return request({
    url: '/paper/question/' + id,
    method: 'get'
  });
};

/**
 * 新增试卷-试题中间
 * @param data
 */
export const addQuestion = (data: QuestionForm) => {
  return request({
    url: '/paper/question',
    method: 'post',
    data: data
  });
};

/**
 * 修改试卷-试题中间
 * @param data
 */
export const updateQuestion = (data: QuestionForm) => {
  return request({
    url: '/paper/question',
    method: 'put',
    data: data
  });
};

/**
 * 删除试卷-试题中间
 * @param id
 */
export const delQuestion = (id: string | number | Array<string | number>) => {
  return request({
    url: '/paper/question/' + id,
    method: 'delete'
  });
};
