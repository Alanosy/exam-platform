import request from '@/utils/request';
import type { AxiosPromise } from 'axios';
import type { HomeStatVO } from '@/api/exam/stat/types';

/**
 * 首页总览
 *
 * 一次把 Dashboard 要的数字全部带回来，避免为了一张页面打七八个接口。
 * 需要 system:exam:list 权限（能看考试列表的人才能看全局总览），
 * 考生不要调这个接口，前端直接走「我的」视图。
 */
export const getHomeOverview = (): AxiosPromise<HomeStatVO> => {
  return request({
    url: '/stat/home/overview',
    method: 'get'
  });
};
