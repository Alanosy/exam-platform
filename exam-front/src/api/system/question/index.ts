import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { QuestionVO, QuestionForm, QuestionQuery } from '@/api/system/question/types';

/** 随机抽题条件（组卷使用） */
export interface QuestionRandomQuery {
  /** 限定题库，为空表示全部题库 */
  bankId?: string | number;
  /** 限定题型，为空表示不限 */
  questionType?: string;
  /** 限定难度，为空表示不限 */
  difficulty?: string;
  /** 抽取数量 */
  count?: number;
  /** 需要排除的试题ID */
  excludeIds?: Array<string | number>;
}

/**
 * 查询试题主列表
 * @param query
 * @returns {*}
 */

export const listQuestion = (query?: QuestionQuery): AxiosPromise<QuestionVO[]> => {
  return request({
    url: '/question/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试题主详细
 * @param id
 */
export const getQuestion = (id: string | number): AxiosPromise<QuestionVO> => {
  return request({
    url: '/question/' + id,
    method: 'get'
  });
};

/**
 * 新增试题主
 *
 * ⚠️ 走的是 POST /question → insertByBo：**不写选项、不填 create_user、不兜底 status**。
 * 页面新增试题请用 {@link createQuestion}（POST /question/create），
 * 否则会报「0草稿 1启用 2废弃不能为空」，且选项不会入库。
 * @param data
 */
export const addQuestion = (data: QuestionForm) => {
  return request({
    url: '/question',
    method: 'post',
    data: data
  });
};

/**
 * 新增试题（含选项）
 *
 * 试题与选项一次提交同时落库，返回新建试题ID。
 * 答案为空时后端会按选项的 isRight 反推正确答案。
 * @param data
 */
export const createQuestion = (data: QuestionForm) => {
  return request({
    url: '/question/create',
    method: 'post',
    data: data
  });
};

/**
 * 修改试题主
 * @param data
 */
export const updateQuestion = (data: QuestionForm) => {
  return request({
    url: '/question',
    method: 'put',
    data: data
  });
};

/**
 * 批量切换试题所属题库
 * @param ids 试题主键集合
 * @param bankId 目标题库ID
 */
export const changeQuestionBank = (ids: Array<string | number>, bankId: string | number) => {
  return request({
    url: '/question/changeBank',
    method: 'put',
    params: { bankId },
    data: ids
  });
};

/**
 * 删除试题主
 * @param id
 */
export const delQuestion = (id: string | number | Array<string | number>) => {
  return request({
    url: '/question/' + id,
    method: 'delete'
  });
};

/**
 * 随机抽题（组卷使用）
 *
 * 按题库 / 题型 / 难度筛选后随机抽取，候选题不足时返回实际能抽到的全部试题。
 * @param query 抽题条件
 */
export const randomQuestion = (query: QuestionRandomQuery): AxiosPromise<QuestionVO[]> => {
  return request({
    url: '/question/random',
    method: 'get',
    params: query
  });
};

/**
 * 按ID批量查询试题（组卷回显使用），返回顺序与传入的 ids 一致
 * @param ids 试题ID集合
 */
export const listQuestionByIds = (ids: Array<string | number>): AxiosPromise<QuestionVO[]> => {
  return request({
    url: '/question/listByIds',
    method: 'post',
    data: { ids }
  });
};
