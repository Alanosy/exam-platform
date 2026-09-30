import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { BankCategoryVO, BankCategoryForm, BankCategoryQuery, BankCategoryTreeVO } from '@/api/system/bankCategory/types';

/**
 * 查询题库分类目录列表
 * @param query
 * @returns {*}
 */

export const listBankCategory = (query?: BankCategoryQuery): AxiosPromise<BankCategoryVO[]> => {
  return request({
    url: '/question/bankCategory/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询题库分类目录树（全量，用于树表格与树选择）
 * @param query
 */
export const treeBankCategory = (query?: BankCategoryQuery): AxiosPromise<BankCategoryTreeVO[]> => {
  return request({
    url: '/question/bankCategory/tree',
    method: 'get',
    params: query
  });
};

/**
 * 查询题库分类目录详细
 * @param id
 */
export const getBankCategory = (id: string | number): AxiosPromise<BankCategoryVO> => {
  return request({
    url: '/question/bankCategory/' + id,
    method: 'get'
  });
};

/**
 * 新增题库分类目录
 * @param data
 */
export const addBankCategory = (data: BankCategoryForm) => {
  return request({
    url: '/question/bankCategory',
    method: 'post',
    data: data
  });
};

/**
 * 修改题库分类目录
 * @param data
 */
export const updateBankCategory = (data: BankCategoryForm) => {
  return request({
    url: '/question/bankCategory',
    method: 'put',
    data: data
  });
};

/**
 * 删除题库分类目录
 * @param id
 */
export const delBankCategory = (id: string | number | Array<string | number>) => {
  return request({
    url: '/question/bankCategory/' + id,
    method: 'delete'
  });
};
