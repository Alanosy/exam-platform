import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { PaperVO, PaperForm, PaperQuery } from '@/api/system/paper/types';

/**
 * 查询试卷主列表
 * @param query
 * @returns {*}
 */

export const listPaper = (query?: PaperQuery): AxiosPromise<PaperVO[]> => {
  return request({
    url: '/system/paper/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试卷主详细
 * @param id
 */
export const getPaper = (id: string | number): AxiosPromise<PaperVO> => {
  return request({
    url: '/system/paper/' + id,
    method: 'get'
  });
};

/**
 * 新增试卷主
 * @param data
 */
export const addPaper = (data: PaperForm) => {
  return request({
    url: '/system/paper',
    method: 'post',
    data: data
  });
};

/**
 * 修改试卷主
 * @param data
 */
export const updatePaper = (data: PaperForm) => {
  return request({
    url: '/system/paper',
    method: 'put',
    data: data
  });
};

/**
 * 删除试卷主
 * @param id
 */
export const delPaper = (id: string | number | Array<string | number>) => {
  return request({
    url: '/system/paper/' + id,
    method: 'delete'
  });
};
