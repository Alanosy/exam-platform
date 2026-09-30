import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { InviteVO, InviteForm, InviteQuery } from '@/api/invite/types';

/**
 * 查询考试邀请记录列表
 * @param query
 * @returns {*}
 */

export const listInvite = (query?: InviteQuery): AxiosPromise<InviteVO[]> => {
  return request({
    url: '/invite/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询考试邀请记录详细
 * @param id
 */
export const getInvite = (id: string | number): AxiosPromise<InviteVO> => {
  return request({
    url: '/invite/' + id,
    method: 'get'
  });
};

/**
 * 新增考试邀请记录
 * @param data
 */
export const addInvite = (data: InviteForm) => {
  return request({
    url: '/invite',
    method: 'post',
    data: data
  });
};

/**
 * 修改考试邀请记录
 * @param data
 */
export const updateInvite = (data: InviteForm) => {
  return request({
    url: '/invite',
    method: 'put',
    data: data
  });
};

/**
 * 删除考试邀请记录
 * @param id
 */
export const delInvite = (id: string | number | Array<string | number>) => {
  return request({
    url: '/invite/' + id,
    method: 'delete'
  });
};
