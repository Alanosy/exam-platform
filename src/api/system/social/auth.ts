import request from '@/utils/request';

// 绑定账号
export function authBinding(source: string) {
  return request({
    url: '/auth/binding/' + source,
    method: 'get'
  });
}

// 解绑账号
export function authUnlock(authId: string) {
  return request({
    url: '/auth/unlock/' + authId,
    method: 'delete'
  });
}
//获取授权列表
export function getAuthList() {
  return request({
    url: '/system/social/list',
    method: 'get'
  });
}
