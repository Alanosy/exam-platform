import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ExamCenterVO, ExamPaperVO, ExamResultVO, AnswerSaveForm } from '@/api/exam/answer/types';

/**
 * 我的考试列表（考试中心）
 */
export const listMyExams = (): AxiosPromise<ExamCenterVO[]> => {
  return request({
    url: '/answer/record/center',
    method: 'get'
  });
};

/**
 * 开始 / 继续考试，返回答卷ID
 * @param examId 考试ID
 */
export const startExam = (examId: string | number): AxiosPromise<number> => {
  return request({
    url: `/answer/record/start/${examId}`,
    method: 'post'
  });
};

/**
 * 取答题页数据
 * @param recordId 答卷ID
 */
export const getExamPaper = (recordId: string | number): AxiosPromise<ExamPaperVO> => {
  return request({
    url: `/answer/record/${recordId}/paper`,
    method: 'get'
  });
};

/**
 * 保存单题作答
 * @param recordId 答卷ID
 * @param data 作答内容
 */
export const saveAnswer = (recordId: string | number, data: AnswerSaveForm) => {
  return request({
    url: `/answer/record/${recordId}/answer`,
    method: 'post',
    data: data
  });
};

/**
 * 交卷
 * @param recordId 答卷ID
 */
export const submitExam = (recordId: string | number): AxiosPromise<ExamResultVO> => {
  return request({
    url: `/answer/record/${recordId}/submit`,
    method: 'post'
  });
};

/**
 * 查询成绩
 * @param recordId 答卷ID
 */
export const getExamResult = (recordId: string | number): AxiosPromise<ExamResultVO> => {
  return request({
    url: `/answer/record/${recordId}/result`,
    method: 'get'
  });
};
