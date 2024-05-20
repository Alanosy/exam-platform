import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { CategoryVO, CategoryForm, CategoryQuery } from '@/api/workflow/category/types';

/**
 * 查询流程分类列表
 * @param query
 * @returns {*}
 */

export const listCategory = (query?: CategoryQuery): AxiosPromise<CategoryVO[]> => {
  return request({
    url: '/workflow/category/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询流程分类详细
 * @param id
 */
export const getCategory = (id: string | number): AxiosPromise<CategoryVO> => {
  return request({
    url: '/workflow/category/' + id,
    method: 'get'
  });
};

/**
 * 新增流程分类
 * @param data
 */
export const addCategory = (data: CategoryForm) => {
  return request({
    url: '/workflow/category',
    method: 'post',
    data: data
  });
};

/**
 * 修改流程分类
 * @param data
 */
export const updateCategory = (data: CategoryForm) => {
  return request({
    url: '/workflow/category',
    method: 'put',
    data: data
  });
};

/**
 * 删除流程分类
 * @param id
 */
export const delCategory = (id: string | number | Array<string | number>) => {
  return request({
    url: '/workflow/category/' + id,
    method: 'delete'
  });
};
