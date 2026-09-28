import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { MediaVO, MediaForm, MediaQuery } from '@/api/system/media/types';

/**
 * 查询试题多媒体附件列表
 * @param query
 * @returns {*}
 */

export const listMedia = (query?: MediaQuery): AxiosPromise<MediaVO[]> => {
  return request({
    url: '/question/media/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试题多媒体附件详细
 * @param id
 */
export const getMedia = (id: string | number): AxiosPromise<MediaVO> => {
  return request({
    url: '/question/media/' + id,
    method: 'get'
  });
};

/**
 * 新增试题多媒体附件
 * @param data
 */
export const addMedia = (data: MediaForm) => {
  return request({
    url: '/question/media',
    method: 'post',
    data: data
  });
};

/**
 * 修改试题多媒体附件
 * @param data
 */
export const updateMedia = (data: MediaForm) => {
  return request({
    url: '/question/media',
    method: 'put',
    data: data
  });
};

/**
 * 删除试题多媒体附件
 * @param id
 */
export const delMedia = (id: string | number | Array<string | number>) => {
  return request({
    url: '/question/media/' + id,
    method: 'delete'
  });
};
