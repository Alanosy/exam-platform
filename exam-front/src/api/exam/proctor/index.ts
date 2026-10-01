import request from '@/utils/request';
import type { AxiosPromise } from 'axios';
import type {
  ProctorEventBO,
  ProctorEventQuery,
  ProctorEventVO,
  ProctorExamGroupQuery,
  ProctorExamGroupVO,
  ProctorOverviewVO,
  ProctorReportVO,
  ProctorSessionQuery,
  ProctorSessionVO,
  ProctorSnapshotVO
} from './types';

/** 进入答题页：开启监考会话，拿回本场生效的防作弊规则 */
export function startProctor(data: { examId: string | number; recordId: string | number; device?: string }): AxiosPromise<ProctorSessionVO> {
  return request({
    url: '/proctor/session/start',
    method: 'post',
    data
  });
}

/** 批量上报防作弊事件 */
export function reportProctorEvents(sessionId: string, events: ProctorEventBO[]): AxiosPromise<ProctorReportVO> {
  return request({
    url: '/proctor/event/report',
    method: 'post',
    data: { sessionId, events }
  });
}

/** 心跳：顺带把服务端最新的计数带回来，刷新页面后靠它把本地计数拉回真实值 */
export function proctorHeartbeat(sessionId: string): AxiosPromise<ProctorReportVO> {
  return request({
    url: `/proctor/session/heartbeat/${sessionId}`,
    method: 'post'
  });
}

/** 结束监考会话 */
export function finishProctor(recordId: string | number, status = 'submitted'): AxiosPromise<null> {
  return request({
    url: `/proctor/session/finish/${recordId}`,
    method: 'post',
    params: { status }
  });
}

/** 上传摄像头抓拍 */
export function uploadProctorSnapshot(sessionId: string, file: Blob, eventType = 'periodic'): AxiosPromise<ProctorSnapshotVO> {
  const formData = new FormData();
  formData.append('file', file, `capture-${Date.now()}.jpg`);
  formData.append('eventType', eventType);
  return request({
    url: `/proctor/snapshot/upload?sessionId=${sessionId}`,
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: formData
  });
}

/* ------------------------------ 监考端（发布者） ------------------------------ */

/** 按考试分组的监考汇总：先按考试看，再点进去看考生 */
export function listProctorExamGroups(params: ProctorExamGroupQuery): AxiosPromise<ProctorExamGroupVO[]> {
  return request({
    url: '/proctor/exam/list',
    method: 'get',
    params
  });
}

/** 监考会话分页 */
export function listProctorSessions(params: ProctorSessionQuery): AxiosPromise<{ rows: ProctorSessionVO[]; total: number }> {
  return request({
    url: '/proctor/session/list',
    method: 'get',
    params
  });
}

/** 监考概览 */
export function getProctorOverview(examId: string | number): AxiosPromise<ProctorOverviewVO> {
  return request({
    url: '/proctor/overview',
    method: 'get',
    params: { examId }
  });
}

/** 会话详情 */
export function getProctorSession(sessionId: string | number): AxiosPromise<ProctorSessionVO> {
  return request({
    url: `/proctor/session/${sessionId}`,
    method: 'get'
  });
}

/** 事件流水 */
export function listProctorEvents(params: ProctorEventQuery): AxiosPromise<{ rows: ProctorEventVO[]; total: number }> {
  return request({
    url: '/proctor/event/list',
    method: 'get',
    params
  });
}

/** 摄像头抓拍列表 */
export function listProctorSnapshots(sessionId: string | number): AxiosPromise<ProctorSnapshotVO[]> {
  return request({
    url: '/proctor/snapshot/list',
    method: 'get',
    params: { sessionId }
  });
}
