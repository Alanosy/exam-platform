import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ModelConfigVO, ModelConfigForm, ModelConfigQuery } from '@/api/system/modelConfig/types';

/**
 * 查询AI大模型配置列表
 * @param query
 * @returns {*}
 */

export const listModelConfig = (query?: ModelConfigQuery): AxiosPromise<ModelConfigVO[]> => {
  return request({
    url: '/system/modelConfig/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询AI大模型配置详细
 * @param id
 */
export const getModelConfig = (id: string | number): AxiosPromise<ModelConfigVO> => {
  return request({
    url: '/system/modelConfig/' + id,
    method: 'get'
  });
};

/**
 * 新增AI大模型配置
 * @param data
 */
export const addModelConfig = (data: ModelConfigForm) => {
  return request({
    url: '/system/modelConfig',
    method: 'post',
    data: data
  });
};

/**
 * 修改AI大模型配置
 * @param data
 */
export const updateModelConfig = (data: ModelConfigForm) => {
  return request({
    url: '/system/modelConfig',
    method: 'put',
    data: data
  });
};

/**
 * 删除AI大模型配置
 * @param id
 */
export const delModelConfig = (id: string | number | Array<string | number>) => {
  return request({
    url: '/system/modelConfig/' + id,
    method: 'delete'
  });
};
