import request from '@/utils/request';
import type { AxiosPromise } from 'axios';
import type {
  CertIssueBO,
  CertificateBO,
  CertificateOptionVO,
  CertificateQuery,
  CertificateRecordQuery,
  CertificateRecordVO,
  CertificateVO
} from './types';

/** 证书模板分页 */
export function listCertificates(params: CertificateQuery): AxiosPromise<{ rows: CertificateVO[]; total: number }> {
  return request({
    url: '/cert/list',
    method: 'get',
    params
  });
}

/** 启用中的证书模板下拉（考试配置页选「及格证书」用） */
export function listCertificateOptions(): AxiosPromise<CertificateOptionVO[]> {
  return request({
    url: '/cert/option/list',
    method: 'get'
  });
}

/** 证书模板详情 */
export function getCertificate(certId: string): AxiosPromise<CertificateVO> {
  return request({
    url: `/cert/${certId}`,
    method: 'get'
  });
}

/** 新增证书模板 */
export function addCertificate(data: CertificateBO): AxiosPromise<string> {
  return request({
    url: '/cert',
    method: 'post',
    data
  });
}

/** 修改证书模板 */
export function updateCertificate(data: CertificateBO): AxiosPromise<null> {
  return request({
    url: '/cert',
    method: 'put',
    data
  });
}

/** 删除证书模板 */
export function delCertificate(certIds: string | string[]): AxiosPromise<null> {
  const ids = Array.isArray(certIds) ? certIds.join(',') : certIds;
  return request({
    url: `/cert/${ids}`,
    method: 'delete'
  });
}

/** 证书颁发记录分页 */
export function listCertificateRecords(params: CertificateRecordQuery): AxiosPromise<{ rows: CertificateRecordVO[]; total: number }> {
  return request({
    url: '/cert/record/list',
    method: 'get',
    params
  });
}

/** 某场考试已颁发的证书数量 */
export function countCertificate(examId: string): AxiosPromise<number> {
  return request({
    url: '/cert/record/count',
    method: 'get',
    params: { examId }
  });
}

/** 手工补发证书（成绩由后端去答题服务拉，不靠前端填） */
export function issueCertificate(data: CertIssueBO): AxiosPromise<CertificateRecordVO> {
  return request({
    url: '/cert/record/issue',
    method: 'post',
    data
  });
}

/** 吊销证书 */
export function revokeCertificate(id: string, reason?: string): AxiosPromise<null> {
  return request({
    url: `/cert/record/revoke/${id}`,
    method: 'put',
    params: { reason }
  });
}

/** 我的证书列表（考生端） */
export function listMyCertificates(): AxiosPromise<CertificateRecordVO[]> {
  return request({
    url: '/cert/mine/list',
    method: 'get'
  });
}

/** 我的证书：按答卷ID查（成绩页与考试记录详情用） */
export function getMyCertificateByRecord(recordId: string): AxiosPromise<CertificateRecordVO> {
  return request({
    url: `/cert/mine/record/${recordId}`,
    method: 'get'
  });
}
