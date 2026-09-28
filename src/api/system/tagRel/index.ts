import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TagRelVO, TagRelForm, TagRelQuery } from '@/api/tagRel/types';

/**
 * 查询试题标签关联列表
 * @param query
 * @returns {*}
 */

export const listTagRel = (query?: TagRelQuery): AxiosPromise<TagRelVO[]> => {
  return request({
    url: '/tagRel/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试题标签关联详细
 * @param id
 */
export const getTagRel = (id: string | number): AxiosPromise<TagRelVO> => {
  return request({
    url: '/tagRel/' + id,
    method: 'get'
  });
};

/**
 * 新增试题标签关联
 * @param data
 */
export const addTagRel = (data: TagRelForm) => {
  return request({
    url: '/tagRel',
    method: 'post',
    data: data
  });
};

/**
 * 修改试题标签关联
 * @param data
 */
export const updateTagRel = (data: TagRelForm) => {
  return request({
    url: '/tagRel',
    method: 'put',
    data: data
  });
};

/**
 * 删除试题标签关联
 * @param id
 */
export const delTagRel = (id: string | number | Array<string | number>) => {
  return request({
    url: '/tagRel/' + id,
    method: 'delete'
  });
};
