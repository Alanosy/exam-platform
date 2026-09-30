import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { BankVO, BankForm, BankQuery } from '@/api/system/bank/types';

/**
 * 查询题库列表
 * @param query
 * @returns {*}
 */

export const listBank = (query?: BankQuery): AxiosPromise<BankVO[]> => {
  return request({
    url: '/question/bank/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询题库详细
 * @param id
 */
export const getBank = (id: string | number): AxiosPromise<BankVO> => {
  return request({
    url: '/question/bank/' + id,
    method: 'get'
  });
};

/**
 * 新增题库
 * @param data
 */
export const addBank = (data: BankForm) => {
  return request({
    url: '/question/bank',
    method: 'post',
    data: data
  });
};

/**
 * 修改题库
 * @param data
 */
export const updateBank = (data: BankForm) => {
  return request({
    url: '/question/bank',
    method: 'put',
    data: data
  });
};

/**
 * 删除题库
 * @param id
 */
export const delBank = (id: string | number | Array<string | number>) => {
  return request({
    url: '/question/bank/' + id,
    method: 'delete'
  });
};
