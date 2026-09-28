import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { OptionVO, OptionForm, OptionQuery } from '@/api/option/types';

/**
 * 查询试题选项列表
 * @param query
 * @returns {*}
 */

export const listOption = (query?: OptionQuery): AxiosPromise<OptionVO[]> => {
  return request({
    url: '/option/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试题选项详细
 * @param id
 */
export const getOption = (id: string | number): AxiosPromise<OptionVO> => {
  return request({
    url: '/option/' + id,
    method: 'get'
  });
};

/**
 * 新增试题选项
 * @param data
 */
export const addOption = (data: OptionForm) => {
  return request({
    url: '/option',
    method: 'post',
    data: data
  });
};

/**
 * 修改试题选项
 * @param data
 */
export const updateOption = (data: OptionForm) => {
  return request({
    url: '/option',
    method: 'put',
    data: data
  });
};

/**
 * 删除试题选项
 * @param id
 */
export const delOption = (id: string | number | Array<string | number>) => {
  return request({
    url: '/option/' + id,
    method: 'delete'
  });
};
