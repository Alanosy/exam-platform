import request from '@/utils/request';
import { ProcessInstanceQuery, ProcessInstanceVO } from '@/api/workflow/processInstance/types';
import { AxiosPromise } from 'axios';

/**
 * 查询运行中实例列表
 * @param query
 * @returns {*}
 */
export const getPageByRunning = (query: ProcessInstanceQuery): AxiosPromise<ProcessInstanceVO[]> => {
  return request({
    url: '/workflow/processInstance/getPageByRunning',
    method: 'get',
    params: query
  });
};

/**
 * 查询已完成实例列表
 * @param query
 * @returns {*}
 */
export const getPageByFinish = (query: ProcessInstanceQuery): AxiosPromise<ProcessInstanceVO[]> => {
  return request({
    url: '/workflow/processInstance/getPageByFinish',
    method: 'get',
    params: query
  });
};

/**
 * 通过流程实例id获取历史流程图
 */
export const getHistoryImage = (processInstanceId: string) => {
  return request({
    url: `/workflow/processInstance/getHistoryImage/${processInstanceId}` + '?t' + Math.random(),
    method: 'get'
  });
};

/**
 * 通过流程实例id获取历史流程图运行中，历史等节点
 */
export const getHistoryList = (instanceId: string): AxiosPromise<Record<string, any>> => {
  return request({
    url: `/workflow/processInstance/getHistoryList/${instanceId}` + '?t' + Math.random(),
    method: 'get'
  });
};

/**
 * 获取审批记录
 * @param processInstanceId 流程实例id
 * @returns
 */
export const getHistoryRecord = (processInstanceId: string) => {
  return request({
    url: `/workflow/processInstance/getHistoryRecord/${processInstanceId}`,
    method: 'get'
  });
};

/**
 * 作废
 * @param data 参数
 * @returns
 */
export const deleteRunInstance = (data: object) => {
  return request({
    url: `/workflow/processInstance/deleteRunInstance`,
    method: 'post',
    data: data
  });
};

/**
 * 运行中的实例 删除程实例，删除历史记录，删除业务与流程关联信息
 * @param processInstanceId 流程实例id
 * @returns
 */
export const deleteRunAndHisInstance = (processInstanceId: string | string[]) => {
  return request({
    url: `/workflow/processInstance/deleteRunAndHisInstance/${processInstanceId}`,
    method: 'delete'
  });
};

/**
 * 已完成的实例 删除程实例，删除历史记录，删除业务与流程关联信息
 * @param processInstanceId 流程实例id
 * @returns
 */
export const deleteFinishAndHisInstance = (processInstanceId: string | string[]) => {
  return request({
    url: `/workflow/processInstance/deleteFinishAndHisInstance/${processInstanceId}`,
    method: 'delete'
  });
};

/**
 * 分页查询当前登录人单据
 * @param query
 * @returns {*}
 */
export const getPageByCurrent = (query: ProcessInstanceQuery): AxiosPromise<ProcessInstanceVO[]> => {
  return request({
    url: '/workflow/processInstance/getPageByCurrent',
    method: 'get',
    params: query
  });
};

/**
 * 撤销流程
 * @param processInstanceId 流程实例id
 * @returns
 */
export const cancelProcessApply = (processInstanceId: string) => {
  return request({
    url: `/workflow/processInstance/cancelProcessApply/${processInstanceId}`,
    method: 'post'
  });
};

export default {
  getPageByRunning,
  getPageByFinish,
  getHistoryImage,
  getHistoryList,
  getHistoryRecord,
  deleteRunInstance,
  deleteRunAndHisInstance,
  deleteFinishAndHisInstance,
  getPageByCurrent,
  cancelProcessApply
};
