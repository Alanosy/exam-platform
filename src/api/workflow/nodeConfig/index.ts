import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { NodeConfigVO, NodeConfigForm, NodeConfigQuery } from '@/api/workflow/nodeConfig/types';

/**
 * 查询节点配置列表
 * @param query
 * @returns {*}
 */

export const listNodeConfig = (query?: NodeConfigQuery): AxiosPromise<NodeConfigVO[]> => {
  return request({
    url: '/workflow/nodeConfig/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询节点配置详细
 * @param id
 */
export const getNodeConfig = (id: string | number): AxiosPromise<NodeConfigVO> => {
  return request({
    url: '/workflow/nodeConfig/' + id,
    method: 'get'
  });
};

/**
 * 新增节点配置
 * @param data
 */
export const addNodeConfig = (data: NodeConfigForm) => {
  return request({
    url: '/workflow/nodeConfig',
    method: 'post',
    data: data
  });
};

/**
 * 修改节点配置
 * @param data
 */
export const updateNodeConfig = (data: NodeConfigForm) => {
  return request({
    url: '/workflow/nodeConfig',
    method: 'put',
    data: data
  });
};

/**
 * 删除节点配置
 * @param id
 */
export const delNodeConfig = (id: string | number | Array<string | number>) => {
  return request({
    url: '/workflow/nodeConfig/' + id,
    method: 'delete'
  });
};
