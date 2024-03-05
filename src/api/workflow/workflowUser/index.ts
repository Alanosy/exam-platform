import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { UserVO } from '@/api/system/user/types';

/**
 * 分页查询工作流选择加签人员
 * @param query
 * @returns {*}
 */
export const getWorkflowAddMultiListByPage = (query: object) => {
  return request({
    url: '/workflow/user/getWorkflowAddMultiListByPage',
    method: 'get',
    params: query
  });
};

/**
 * 查询工作流选择减签人员
 * @param query
 * @returns {*}
 */
export const getWorkflowDeleteMultiInstanceList = (taskId: string) => {
  return request({
    url: '/workflow/user/getWorkflowDeleteMultiInstanceList/' + taskId,
    method: 'get'
  });
};

/**
 * 按照用户id查询用户
 * @param userIdList
 * @returns {*}
 */
export const getUserListByIds = (userIdList: any[]): AxiosPromise<UserVO[]> => {
  return request({
    url: '/workflow/user/getUserListByIds/' + userIdList,
    method: 'get'
  });
};

/**
 * 分页查询用户
 * @param query
 * @returns {*}
 */
export const getUserListByPage = (query: object) => {
  return request({
    url: '/workflow/user/getUserListByPage',
    method: 'get',
    params: query
  });
};
