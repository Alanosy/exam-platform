import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TagRelVO, TagRelForm, TagRelQuery } from '@/api/system/tagRel/types';

/**
 * 查询试题标签关联列表
 * @param query
 * @returns {*}
 */

export const listTagRel = (query?: TagRelQuery): AxiosPromise<TagRelVO[]> => {
  return request({
    url: '/question/tagRel/list',
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
    url: '/question/tagRel/' + id,
    method: 'get'
  });
};

/**
 * 新增试题标签关联
 * @param data
 */
export const addTagRel = (data: TagRelForm) => {
  return request({
    url: '/question/tagRel',
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
    url: '/question/tagRel',
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
    url: '/question/tagRel/' + id,
    method: 'delete'
  });
};
