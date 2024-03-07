import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { UserVO } from '@/api/system/user/types';

/**
 * 分页查询工作流选择加签人员
 * @param query
 * @returns {*}
 */
export const getPageByAddMultiInstance = (query: object) => {
  return request({
    url: '/workflow/user/getPageByAddMultiInstance',
    method: 'get',
    params: query
  });
};

/**
 * 查询工作流选择减签人员
 * @param query
 * @returns {*}
 */
export const getListByDeleteMultiInstance = (taskId: string) => {
  return request({
    url: '/workflow/user/getListByDeleteMultiInstance/' + taskId,
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
export const getPageByUserList = (query: object) => {
  return request({
    url: '/workflow/user/getPageByUserList',
    method: 'get',
    params: query
  });
};
