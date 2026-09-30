import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ClientVO, ClientForm, ClientQuery } from '@/api/system/client/types';

/**
 * 查询客户端管理列表
 * @param query
 * @returns {*}
 */

export const listClient = (query?: ClientQuery): AxiosPromise<ClientVO[]> => {
  return request({
    url: '/system/client/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询客户端管理详细
 * @param id
 */
export const getClient = (id: string | number): AxiosPromise<ClientVO> => {
  return request({
    url: '/system/client/' + id,
    method: 'get'
  });
};

/**
 * 新增客户端管理
 * @param data
 */
export const addClient = (data: ClientForm) => {
  return request({
    url: '/system/client',
    method: 'post',
    data: data
  });
};

/**
 * 修改客户端管理
 * @param data
 */
export const updateClient = (data: ClientForm) => {
  return request({
    url: '/system/client',
    method: 'put',
    data: data
  });
};

/**
 * 删除客户端管理
 * @param id
 */
export const delClient = (id: string | number | Array<string | number>) => {
  return request({
    url: '/system/client/' + id,
    method: 'delete'
  });
};

/**
 * 状态修改
 * @param clientId 客户端id
 * @param status 状态
 */
export function changeStatus(clientId: string, status: string) {
  const data = {
    clientId,
    status
  };
  return request({
    url: '/system/client/changeStatus',
    method: 'put',
    data: data
  });
}
