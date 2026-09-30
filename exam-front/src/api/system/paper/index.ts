import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { PaperVO, PaperForm, PaperQuery } from '@/api/system/paper/types';

/**
 * 查询试卷列表
 * @param query
 * @returns {*}
 */

export const listPaper = (query?: PaperQuery): AxiosPromise<PaperVO[]> => {
  return request({
    url: '/paper/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试卷详细（含已选试题明细）
 * @param id
 */
export const getPaper = (id: string | number): AxiosPromise<PaperVO> => {
  return request({
    url: '/paper/' + id,
    method: 'get'
  });
};

/**
 * 新增试卷
 * @param data
 */
export const addPaper = (data: PaperForm) => {
  return request({
    url: '/paper',
    method: 'post',
    data: data
  });
};

/**
 * 组卷保存：试卷信息 + 已选试题一次提交
 *
 * 有 id 走修改、无 id 走新增；试题明细按传入顺序全量覆盖写入 paper_question。
 * @param data 试卷信息（含 questions）
 * @returns 试卷主键ID
 */
export const savePaper = (data: PaperForm): AxiosPromise<number | string> => {
  return request({
    url: '/paper/save',
    method: 'post',
    data: data
  });
};

/**
 * 修改试卷
 * @param data
 */
export const updatePaper = (data: PaperForm) => {
  return request({
    url: '/paper',
    method: 'put',
    data: data
  });
};

/**
 * 删除试卷
 * @param id
 */
export const delPaper = (id: string | number | Array<string | number>) => {
  return request({
    url: '/paper/' + id,
    method: 'delete'
  });
};
