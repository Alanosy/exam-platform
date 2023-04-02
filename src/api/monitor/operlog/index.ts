import request from '@/utils/request';
import { OperLogQuery, OperLogVO } from './types';
import { AxiosPromise } from 'axios';

// 查询操作日志列表
export function list(query: OperLogQuery): AxiosPromise<OperLogVO[]> {
  return request({
    url: '/monitor/operlog/list',
    method: 'get',
    params: query
  });
}

// 删除操作日志
export function delOperlog(operId: string | number | Array<string | number>) {
  return request({
    url: '/monitor/operlog/' + operId,
    method: 'delete'
  });
}

// 清空操作日志
export function cleanOperlog() {
  return request({
    url: '/monitor/operlog/clean',
    method: 'delete'
  });
}
