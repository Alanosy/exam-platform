import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { FormManageVO, FormManageForm, FormManageQuery } from '@/api/workflow/formManage/types';

/**
 * 查询表单管理列表
 * @param query
 * @returns {*}
 */

export const listFormManage = (query?: FormManageQuery): AxiosPromise<FormManageVO[]> => {
  return request({
    url: '/workflow/formManage/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询表单管理列表
 * @param query
 * @returns {*}
 */

export const selectListFormManage = (): AxiosPromise<FormManageVO[]> => {
  return request({
    url: '/workflow/formManage/list/selectList',
    method: 'get'
  });
};

/**
 * 查询表单管理详细
 * @param id
 */
export const getFormManage = (id: string | number): AxiosPromise<FormManageVO> => {
  return request({
    url: '/workflow/formManage/' + id,
    method: 'get'
  });
};

/**
 * 新增表单管理
 * @param data
 */
export const addFormManage = (data: FormManageForm) => {
  return request({
    url: '/workflow/formManage',
    method: 'post',
    data: data
  });
};

/**
 * 修改表单管理
 * @param data
 */
export const updateFormManage = (data: FormManageForm) => {
  return request({
    url: '/workflow/formManage',
    method: 'put',
    data: data
  });
};

/**
 * 删除表单管理
 * @param id
 */
export const delFormManage = (id: string | number | Array<string | number>) => {
  return request({
    url: '/workflow/formManage/' + id,
    method: 'delete'
  });
};
