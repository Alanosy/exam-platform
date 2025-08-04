import request from '@/utils/request';
import { FlowInstanceQuery, FlowInstanceVO } from '@/api/workflow/instance/types';
import { AxiosPromise } from 'axios';

/**
 * 查询运行中实例列表
 * @param query
 * @returns {*}
 */
export const pageByRunning = (query: FlowInstanceQuery): AxiosPromise<FlowInstanceVO[]> => {
  return request({
    url: '/workflow/instance/pageByRunning',
    method: 'get',
    params: query
  });
};

/**
 * 查询已完成实例列表
 * @param query
 * @returns {*}
 */
export const pageByFinish = (query: FlowInstanceQuery): AxiosPromise<FlowInstanceVO[]> => {
  return request({
    url: '/workflow/instance/pageByFinish',
    method: 'get',
    params: query
  });
};

/**
 * 通过业务id获取历史流程图
 */
export const flowHisTaskList = (businessId: string | number) => {
  return request({
    url: `/workflow/instance/flowHisTaskList/${businessId}` + '?t' + Math.random(),
    method: 'get'
  });
};

/**
 * 分页查询当前登录人单据
 * @param query
 * @returns {*}
 */
export const pageByCurrent = (query: FlowInstanceQuery): AxiosPromise<FlowInstanceVO[]> => {
  return request({
    url: '/workflow/instance/pageByCurrent',
    method: 'get',
    params: query
  });
};

/**
 * 撤销流程
 * @param data 参数
 * @returns
 */
export const cancelProcessApply = (data: any) => {
  return request({
    url: `/workflow/instance/cancelProcessApply`,
    method: 'put',
    data: data
  });
};

/**
 * 获取流程变量
 * @param instanceId 实例id
 * @returns
 */
export const instanceVariable = (instanceId: string | number) => {
  return request({
    url: `/workflow/instance/instanceVariable/${instanceId}`,
    method: 'get'
  });
};

/**
 * 删除
 * @param instanceIds 流程实例id
 * @returns
 */
export const deleteByInstanceIds = (instanceIds: Array<string | number> | string | number) => {
  return request({
    url: `/workflow/instance/deleteByInstanceIds/${instanceIds}`,
    method: 'delete'
  });
};

/**
 * 删除历史流程实例
 * @param instanceIds
 */
export const deleteHisByInstanceIds = (instanceIds: Array<string | number> | string | number) => {
  return request({
    url: `/workflow/instance/deleteHisByInstanceIds/${instanceIds}`,
    method: 'delete'
  });
};

/**
 * 作废流程
 * @param data 参数
 * @returns
 */
export const invalid = (data: any) => {
  return request({
    url: `/workflow/instance/invalid`,
    method: 'post',
    data: data
  });
};
/**
 * 修改流程变量
 * @param data 参数
 * @returns
 */
export const updateVariable = (data: any) => {
  return request({
    url: `/workflow/instance/updateVariable`,
    method: 'put',
    data: data
  });
};
