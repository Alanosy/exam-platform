import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TaskQuery, TaskVO } from '@/api/workflow/task/types';

/**
 * 查询待办列表
 * @param query
 * @returns {*}
 */
export const getPageByTaskWait = (query: TaskQuery): AxiosPromise<TaskVO[]> => {
  return request({
    url: '/workflow/task/getPageByTaskWait',
    method: 'get',
    params: query
  });
};

/**
 * 查询已办列表
 * @param query
 * @returns {*}
 */
export const getPageByTaskFinish = (query: TaskQuery): AxiosPromise<TaskVO[]> => {
  return request({
    url: '/workflow/task/getPageByTaskFinish',
    method: 'get',
    params: query
  });
};

/**
 * 查询当前用户的抄送列表
 * @param query
 * @returns {*}
 */
export const getPageByTaskCopy = (query: TaskQuery): AxiosPromise<TaskVO[]> => {
  return request({
    url: '/workflow/task/getPageByTaskCopy',
    method: 'get',
    params: query
  });
};

/**
 * 当前租户所有待办任务
 * @param query
 * @returns {*}
 */
export const getPageByAllTaskWait = (query: TaskQuery): AxiosPromise<TaskVO[]> => {
  return request({
    url: '/workflow/task/getPageByAllTaskWait',
    method: 'get',
    params: query
  });
};

/**
 * 当前租户所有已办任务
 * @param query
 * @returns {*}
 */
export const getPageByAllTaskFinish = (query: TaskQuery): AxiosPromise<TaskVO[]> => {
  return request({
    url: '/workflow/task/getPageByAllTaskFinish',
    method: 'get',
    params: query
  });
};

/**
 * 启动流程
 * @param data
 * @returns {*}
 */
export const startWorkFlow = (data: object): any => {
  return request({
    url: '/workflow/task/startWorkFlow',
    method: 'post',
    data: data
  });
};

/**
 * 办理流程
 * @param data
 * @returns {*}
 */
export const completeTask = (data: object) => {
  return request({
    url: '/workflow/task/completeTask',
    method: 'post',
    data: data
  });
};

/**
 * 认领任务
 * @param taskId
 * @returns {*}
 */
export const claim = (taskId: string): any => {
  return request({
    url: '/workflow/task/claim/' + taskId,
    method: 'post'
  });
};

/**
 * 归还任务
 * @param taskId
 * @returns {*}
 */
export const returnTask = (taskId: string): any => {
  return request({
    url: '/workflow/task/returnTask/' + taskId,
    method: 'post'
  });
};

/**
 * 任务驳回
 * @param data
 * @returns {*}
 */
export const backProcess = (data: any): any => {
  return request({
    url: '/workflow/task/backProcess',
    method: 'post',
    data: data
  });
};

/**
 * 获取当前任务
 * @param taskId
 * @returns
 */
export const getTaskById = (taskId: string) => {
  return request({
    url: '/workflow/task/getTaskById/' + taskId,
    method: 'get'
  });
};

/**
 * 加签
 * @param data
 * @returns
 */
export const addMultiInstanceExecution = (data: any) => {
  return request({
    url: '/workflow/task/addMultiInstanceExecution',
    method: 'post',
    data: data
  });
};

/**
 * 减签
 * @param data
 * @returns
 */
export const deleteMultiInstanceExecution = (data: any) => {
  return request({
    url: '/workflow/task/deleteMultiInstanceExecution',
    method: 'post',
    data: data
  });
};

/**
 * 修改任务办理人
 * @param taskIds
 * @param userId
 * @returns
 */
export const updateAssignee = (taskIds: Array<string>, userId: string) => {
  return request({
    url: `/workflow/task/updateAssignee/${taskIds}/${userId}`,
    method: 'put'
  });
};

/**
 * 转办任务
 * @returns
 */
export const transferTask = (data: any) => {
  return request({
    url: `/workflow/task/transferTask`,
    method: 'post',
    data: data
  });
};

/**
 * 终止任务
 * @returns
 */
export const terminationTask = (data: any) => {
  return request({
    url: `/workflow/task/terminationTask`,
    method: 'post',
    data: data
  });
};

/**
 * 查询流程变量
 * @returns
 */
export const getInstanceVariable = (taskId: string) => {
  return request({
    url: `/workflow/task/getInstanceVariable/${taskId}`,
    method: 'get'
  });
};

/**
 * 获取可驳回得任务节点
 * @returns
 */
export const getTaskNodeList = (processInstanceId: string) => {
  return request({
    url: `/workflow/task/getTaskNodeList/${processInstanceId}`,
    method: 'get'
  });
};

/**
 * 委托任务
 * @returns
 */
export const delegateTask = (data: any) => {
  return request({
    url: `/workflow/task/delegateTask`,
    method: 'post',
    data: data
  });
};

/**
 * 查询工作流任务用户选择加签人员
 * @param taskId
 * @returns {*}
 */
export const getTaskUserIdsByAddMultiInstance = (taskId: string) => {
  return request({
    url: '/workflow/task/getTaskUserIdsByAddMultiInstance/' + taskId,
    method: 'get'
  });
};

/**
 * 查询工作流选择减签人员
 * @param taskId
 * @returns {*}
 */
export const getListByDeleteMultiInstance = (taskId: string) => {
  return request({
    url: '/workflow/task/getListByDeleteMultiInstance/' + taskId,
    method: 'get'
  });
};
