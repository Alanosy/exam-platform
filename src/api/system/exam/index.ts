import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ExamVO, ExamForm, ExamQuery } from '@/api/system/exam/types';

/**
 * 查询考试主列表
 * @param query
 * @returns {*}
 */

export const listExam = (query?: ExamQuery): AxiosPromise<ExamVO[]> => {
  return request({
    url: '/exam/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询考试主详细
 * @param id
 */
export const getExam = (id: string | number): AxiosPromise<ExamVO> => {
  return request({
    url: '/exam/' + id,
    method: 'get'
  });
};

/**
 * 新增考试主
 * @param data
 */
export const addExam = (data: ExamForm) => {
  return request({
    url: '/exam',
    method: 'post',
    data: data
  });
};

/**
 * 修改考试主
 * @param data
 */
export const updateExam = (data: ExamForm) => {
  return request({
    url: '/exam',
    method: 'put',
    data: data
  });
};

/**
 * 重新生成公开考试的加入码，原加入链接立即失效
 * @param id
 */
export const refreshJoinCode = (id: string | number): AxiosPromise<string> => {
  return request({
    url: `/exam/${id}/joinCode/refresh`,
    method: 'post'
  });
};

/**
 * 删除考试主
 * @param id
 */
export const delExam = (id: string | number | Array<string | number>) => {
  return request({
    url: '/exam/' + id,
    method: 'delete'
  });
};
