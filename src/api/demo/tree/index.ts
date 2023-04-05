import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TreeVO, TreeForm, TreeQuery } from '@/api/demo/tree/types';

/**
 * 查询测试树列表
 * @param query
 * @returns {*}
 */
export const listTree = (query?: TreeQuery): AxiosPromise<TreeVO[]> => {
  return request({
    url: '/demo/tree/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询测试树详细
 * @param id
 */
export const getTree = (id: string | number): AxiosPromise<TreeVO> => {
  return request({
    url: '/demo/tree/' + id,
    method: 'get'
  });
};

/**
 * 新增测试树
 * @param data
 */
export const addTree = (data: TreeForm) => {
  return request({
    url: '/demo/tree',
    method: 'post',
    data: data
  });
};

/**
 * 修改测试树
 * @param data
 */
export const updateTree = (data: TreeForm) => {
  return request({
    url: '/demo/tree',
    method: 'put',
    data: data
  });
};

/**
 * 删除测试树
 * @param id
 */
export const delTree = (id: string | number | Array<string | number>) => {
  return request({
    url: '/demo/tree/' + id,
    method: 'delete'
  });
};
