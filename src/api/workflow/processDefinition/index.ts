import request from '@/utils/request';
import { ProcessDefinitionQuery, ProcessDefinitionVO, definitionXmlVO } from '@/api/workflow/processDefinition/types';
import { AxiosPromise } from 'axios';

/**
 * 获取流程定义列表
 * @param query 流程实例id
 * @returns
 */
export const listProcessDefinition = (query: ProcessDefinitionQuery): AxiosPromise<ProcessDefinitionVO[]> => {
  return request({
    url: `/workflow/processDefinition/list`,
    method: 'get',
    params: query
  });
};
/**
 * 按照流程定义key获取流程定义
 * @param processInstanceId 流程实例id
 * @returns
 */
export const getListByKey = (key: string) => {
  return request({
    url: `/workflow/processDefinition/getListByKey/${key}`,
    method: 'get'
  });
};

/**
 * 通过流程定义id获取流程图
 */
export const definitionImage = (processDefinitionId: string): AxiosPromise<any> => {
  return request({
    url: `/workflow/processDefinition/definitionImage/${processDefinitionId}` + '?t' + Math.random(),
    method: 'get'
  });
};

/**
 * 通过流程定义id获取xml
 * @param processDefinitionId 流程定义id
 * @returns
 */
export const definitionXml = (processDefinitionId: string): AxiosPromise<definitionXmlVO> => {
  return request({
    url: `/workflow/processDefinition/definitionXml/${processDefinitionId}`,
    method: 'get'
  });
};

/**
 * 删除流程定义
 * @param deploymentId 部署id
 * @param processDefinitionId 流程定义id
 * @returns
 */
export const deleteProcessDefinition = (deploymentId: string | string[], processDefinitionId: string | string[]) => {
  return request({
    url: `/workflow/processDefinition/${deploymentId}/${processDefinitionId}`,
    method: 'delete'
  });
};

/**
 * 挂起/激活
 * @param processDefinitionId 流程定义id
 * @returns
 */
export const updateDefinitionState = (processDefinitionId: string) => {
  return request({
    url: `/workflow/processDefinition/updateDefinitionState/${processDefinitionId}`,
    method: 'put'
  });
};

/**
 * 流程定义转换为模型
 * @param processDefinitionId 流程定义id
 * @returns
 */
export const convertToModel = (processDefinitionId: string) => {
  return request({
    url: `/workflow/processDefinition/convertToModel/${processDefinitionId}`,
    method: 'put'
  });
};

/**
 * 通过zip或xml部署流程定义
 * @returns
 */
export function deployProcessFile(data: any) {
  return request({
    url: '/workflow/processDefinition/deployByFile',
    method: 'post',
    data: data,
    headers: {
      repeatSubmit: false
    }
  });
}

/**
 * 迁移流程
 * @param currentProcessDefinitionId
 * @param fromProcessDefinitionId
 * @returns
 */
export const migrationDefinition = (currentProcessDefinitionId: string, fromProcessDefinitionId: string) => {
  return request({
    url: `/workflow/processDefinition/migrationDefinition/${currentProcessDefinitionId}/${fromProcessDefinitionId}`,
    method: 'put'
  });
};
