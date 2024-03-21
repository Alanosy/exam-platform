import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { FormDefinitionVO, FormDefinitionForm, FormDefinitionQuery } from '@/api/workflow/formDefinition/types';


/**
 * 查询表单配置详细
 * @param id
 */
export const getByDefId = (definitionId: string | number): AxiosPromise<FormDefinitionVO> => {
  return request({
    url: '/workflow/formDefinition/getByDefId/' + definitionId,
    method: 'get'
  });
};

/**
 * 新增表单配置
 * @param data
 */
export const saveOrUpdate = (data: FormDefinitionForm) => {
  return request({
    url: '/workflow/formDefinition/saveOrUpdate',
    method: 'post',
    data: data
  });
};

/**
 * 删除表单配置
 * @param id
 */
export const delFormDefinition = (id: string | number | Array<string | number>) => {
  return request({
    url: '/workflow/formDefinition/' + id,
    method: 'delete'
  });
};
