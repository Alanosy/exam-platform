import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { DefinitionConfigVO, DefinitionConfigForm } from '@/api/workflow/definitionConfig/types';

/**
 * 查询表单配置详细
 * @param definitionId
 */
export const getByDefId = (definitionId: string | number): AxiosPromise<DefinitionConfigVO> => {
  return request({
    url: '/workflow/definitionConfig/getByDefId/' + definitionId,
    method: 'get'
  });
};

/**
 * 新增表单配置
 * @param data
 */
export const saveOrUpdate = (data: DefinitionConfigForm) => {
  return request({
    url: '/workflow/definitionConfig/saveOrUpdate',
    method: 'post',
    data: data
  });
};

/**
 * 删除表单配置
 * @param id
 */
export const deldefinitionConfig = (id: string | number | Array<string | number>) => {
  return request({
    url: '/workflow/definitionConfig/' + id,
    method: 'delete'
  });
};

/**
 * 查询流程定义配置排除当前查询的流程定义
 * @param tableName
 * @param definitionId
 */
export const getByTableNameNotDefId = (tableName: string, definitionId: string | number) => {
  return request({
    url: `/workflow/definitionConfig/getByTableNameNotDefId/${tableName}/${definitionId}`,
    method: 'get'
  });
};
