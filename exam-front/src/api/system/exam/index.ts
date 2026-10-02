import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  ExamVO,
  ExamForm,
  ExamQuery,
  ExamJoinVO,
  ExamWhiteUserVO,
  ExamSituationVO,
  ExamSituationOverviewVO,
  ExamSituationQuery
} from '@/api/system/exam/types';

/**
 * 查询考试主列表
 * @param query
 * @returns {*}
 */

export const listExam = (query?: ExamQuery): AxiosPromise<ExamVO[]> => {
  return request({
    url: '/exam/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询考试主详细
 * @param id
 */
export const getExam = (id: string | number): AxiosPromise<ExamVO> => {
  return request({
    url: '/exam/' + id,
    method: 'get'
  });
};

/**
 * 新增考试主
 * @param data
 */
export const addExam = (data: ExamForm) => {
  return request({
    url: '/exam',
    method: 'post',
    data: data
  });
};

/**
 * 修改考试主
 * @param data
 */
export const updateExam = (data: ExamForm) => {
  return request({
    url: '/exam',
    method: 'put',
    data: data
  });
};

/**
 * 重新生成公开考试的加入码，原加入链接立即失效
 * @param id
 */
export const refreshJoinCode = (id: string | number): AxiosPromise<string> => {
  return request({
    url: `/exam/${id}/joinCode/refresh`,
    method: 'post'
  });
};

/**
 * 按加入码查询公开考试的加入信息
 * @param code 加入码
 */
export const getExamJoinInfo = (code: string): AxiosPromise<ExamJoinVO> => {
  return request({
    url: `/exam/join/${code}`,
    method: 'get'
  });
};

/**
 * 通过加入码加入公开考试，需要参与密码时传 password
 * @param code 加入码
 * @param password 参与密码
 */
export const joinExam = (code: string, password?: string): AxiosPromise<number> => {
  return request({
    url: `/exam/join/${code}`,
    method: 'post',
    data: { password }
  });
};

/**
 * 查询考试白名单考生
 * @param examId 考试ID
 */
export const listExamWhiteUsers = (examId: string | number): AxiosPromise<ExamWhiteUserVO[]> => {
  return request({
    url: `/exam/${examId}/whiteUsers`,
    method: 'get'
  });
};

/**
 * 保存考试白名单（整体覆盖：不在列表里的原有考生会被移出）
 * @param examId 考试ID
 * @param userIds 考生用户ID列表
 */
export const saveExamWhiteUsers = (examId: string | number, userIds: Array<string | number>) => {
  return request({
    url: `/exam/${examId}/whiteUsers`,
    method: 'put',
    data: { examId, userIds }
  });
};

/**
 * 查询某场考试的概览：应考 / 参考 / 已交卷 / 待阅 / 平均分 / 及格率
 * @param examId 考试ID
 */
export const getExamSituationOverview = (examId: string | number): AxiosPromise<ExamSituationOverviewVO> => {
  return request({
    url: `/exam/${examId}/situation/overview`,
    method: 'get'
  });
};

/**
 * 查询某场考试的参考名单与成绩
 * @param examId 考试ID
 * @param query 筛选条件（关键词 / 状态 / 是否及格 / 只看待阅）
 */
export const listExamSituation = (examId: string | number, query: ExamSituationQuery): AxiosPromise<ExamSituationVO[]> => {
  return request({
    url: `/exam/${examId}/situation/records`,
    method: 'get',
    params: query
  });
};

/**
 * 删除考试主
 * @param id
 */
export const delExam = (id: string | number | Array<string | number>) => {
  return request({
    url: '/exam/' + id,
    method: 'delete'
  });
};
