import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { LeaveVO, LeaveQuery, LeaveForm } from '@/api/workflow/leave/types';

/**
 * 查询请假列表
 * @param query
 * @returns {*}
 */

export const listLeave = (query?: LeaveQuery): AxiosPromise<LeaveVO[]> => {
  return request({
    url: '/demo/leave/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询请假详细
 * @param id
 */
export const getLeave = (id: string | number): AxiosPromise<LeaveVO> => {
  return request({
    url: '/demo/leave/' + id,
    method: 'get'
  });
};

/**
 * 新增请假
 * @param data
 */
export const addLeave = (data: LeaveForm): AxiosPromise<LeaveVO> => {
  return request({
    url: '/demo/leave',
    method: 'post',
    data: data
  });
};

/**
 * 修改请假
 * @param data
 */
export const updateLeave = (data: LeaveForm): AxiosPromise<LeaveVO> => {
  return request({
    url: '/demo/leave',
    method: 'put',
    data: data
  });
};

/**
 * 删除请假
 * @param id
 */
export const delLeave = (id: string | number | Array<string | number>) => {
  return request({
    url: '/demo/leave/' + id,
    method: 'delete'
  });
};
