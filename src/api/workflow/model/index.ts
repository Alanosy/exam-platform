import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ModelForm, ModelQuery, ModelVO } from '@/api/workflow/model/types';

/**
 * 查询模型列表
 * @param query
 * @returns {*}
 */
export const listModel = (query: ModelQuery): AxiosPromise<ModelVO[]> => {
  return request({
    url: '/workflow/model/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询模型信息
 * @param query
 * @returns {*}
 */
export const getInfo = (id: string): AxiosPromise<ModelForm> => {
  return request({
    url: '/workflow/model/getInfo/' + id,
    method: 'get'
  });
};

/**
 * 新增模型
 * @param data
 * @returns {*}
 */
export const addModel = (data: ModelForm): AxiosPromise<void> => {
  return request({
    url: '/workflow/model/save',
    method: 'post',
    data: data
  });
};

/**
 * 修改模型信息
 * @param data
 * @returns {*}
 */
export function update(data: ModelForm): AxiosPromise<void> {
  return request({
    url: '/workflow/model/update',
    method: 'put',
    data: data
  });
}

/**
 * 修改模型信息
 * @param data
 * @returns {*}
 */
export function editModelXml(data: ModelForm): AxiosPromise<void> {
  return request({
    url: '/workflow/model/editModelXml',
    method: 'put',
    data: data
  });
}

/**
 * 按id删除模型
 * @returns {*}
 * @param id 模型id
 */
export function delModel(id: string | string[]): AxiosPromise<void> {
  return request({
    url: '/workflow/model/' + id,
    method: 'delete'
  });
}

/**
 * 模型部署
 * @returns {*}
 * @param id 模型id
 */
export const modelDeploy = (id: string): AxiosPromise<void> => {
  return request({
    url: `/workflow/model/modelDeploy/${id}`,
    method: 'post'
  });
};

/**
 * 复制模型
 * @param data
 * @returns {*}
 */
export const copyModel = (data: ModelForm): AxiosPromise<void> => {
  return request({
    url: '/workflow/model/copyModel',
    method: 'post',
    data: data
  });
};
