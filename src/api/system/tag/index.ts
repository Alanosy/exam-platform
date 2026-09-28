import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TagVO, TagForm, TagQuery } from '@/api/system/tag/types';

/**
 * 查询试题标签列表
 * @param query
 * @returns {*}
 */

export const listTag = (query?: TagQuery): AxiosPromise<TagVO[]> => {
  return request({
    url: '/question/tag/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试题标签详细
 * @param id
 */
export const getTag = (id: string | number): AxiosPromise<TagVO> => {
  return request({
    url: '/question/tag/' + id,
    method: 'get'
  });
};

/**
 * 新增试题标签
 * @param data
 */
export const addTag = (data: TagForm) => {
  return request({
    url: '/question/tag',
    method: 'post',
    data: data
  });
};

/**
 * 修改试题标签
 * @param data
 */
export const updateTag = (data: TagForm) => {
  return request({
    url: '/question/tag',
    method: 'put',
    data: data
  });
};

/**
 * 删除试题标签
 * @param id
 */
export const delTag = (id: string | number | Array<string | number>) => {
  return request({
    url: '/question/tag/' + id,
    method: 'delete'
  });
};
