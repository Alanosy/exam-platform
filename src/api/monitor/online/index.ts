import request from '@/utils/request';
import { OnlineQuery, OnlineVO } from './types';
import { AxiosPromise } from 'axios';

// 查询在线用户列表
export function list(query: OnlineQuery): AxiosPromise<OnlineVO[]> {
  return request({
    url: '/monitor/online/list',
    method: 'get',
    params: query
  });
}

// 强退用户
export function forceLogout(tokenId: string) {
  return request({
    url: '/monitor/online/' + tokenId,
    method: 'delete'
  });
}
