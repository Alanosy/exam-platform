import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { KnowledgePointVO, KnowledgePointForm, KnowledgePointQuery, QuestionKnowledgeVO } from '@/api/system/knowledge/types';

/**
 * 查询知识点列表（平铺分页）
 */
export const listKnowledge = (query?: KnowledgePointQuery): AxiosPromise<KnowledgePointVO[]> => {
  return request({
    url: '/question/knowledge/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询知识点树（章节 → 知识点），管理页树表格与各级选择器都用它
 */
export const treeKnowledge = (query?: KnowledgePointQuery): AxiosPromise<KnowledgePointVO[]> => {
  return request({
    url: '/question/knowledge/tree',
    method: 'get',
    params: query
  });
};

/**
 * 按试题ID批量取知识点关联（错题诊断入参拼装、详情页回显）
 *
 * @param ids 试题ID，逗号分隔
 */
export const listQuestionKnowledge = (ids: string): AxiosPromise<QuestionKnowledgeVO[]> => {
  return request({
    url: '/question/knowledge/byQuestions',
    method: 'get',
    params: { ids }
  });
};

/**
 * 查询知识点详细
 */
export const getKnowledge = (id: string | number): AxiosPromise<KnowledgePointVO> => {
  return request({
    url: '/question/knowledge/' + id,
    method: 'get'
  });
};

/**
 * 新增知识点（parentId 传 0 或不传 = 建章节）
 */
export const addKnowledge = (data: KnowledgePointForm) => {
  return request({
    url: '/question/knowledge',
    method: 'post',
    data: data
  });
};

/**
 * 修改知识点
 */
export const updateKnowledge = (data: KnowledgePointForm) => {
  return request({
    url: '/question/knowledge',
    method: 'put',
    data: data
  });
};

/**
 * 删除知识点（有子节点或已被试题引用时后端会拒绝）
 */
export const delKnowledge = (id: string | number | Array<string | number>) => {
  return request({
    url: '/question/knowledge/' + id,
    method: 'delete'
  });
};
