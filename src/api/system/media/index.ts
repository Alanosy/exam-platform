import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { MediaVO, MediaForm, MediaQuery } from '@/api/system/media/types';

/**
 * 查询试题多媒体附件列表
 * @param query
 * @returns {*}
 */

export const listMedia = (query?: MediaQuery): AxiosPromise<MediaVO[]> => {
  return request({
    url: '/question/media/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询试题多媒体附件详细
 * @param id
 */
export const getMedia = (id: string | number): AxiosPromise<MediaVO> => {
  return request({
    url: '/question/media/' + id,
    method: 'get'
  });
};

/**
 * 新增试题多媒体附件
 * @param data
 */
export const addMedia = (data: MediaForm) => {
  return request({
    url: '/question/media',
    method: 'post',
    data: data
  });
};

/**
 * 修改试题多媒体附件
 * @param data
 */
export const updateMedia = (data: MediaForm) => {
  return request({
    url: '/question/media',
    method: 'put',
    data: data
  });
};

/**
 * 删除试题多媒体附件
 * @param id
 */
export const delMedia = (id: string | number | Array<string | number>) => {
  return request({
    url: '/question/media/' + id,
    method: 'delete'
  });
};

/**
 * 登记一笔尚未挂到试题上的上传记录
 *
 * 富文本里插入图片时文件已经进了对象存储，但试题可能还没保存。
 * 这里先落一条 questionId 为空的记录，试题保存时回填；
 * 始终没回填的由后端清理任务按保留时长回收（连同对象存储里的文件）。
 * @param data 媒体附件信息
 */
export const draftMedia = (data: { mediaType: string; mediaUrl: string; mediaName?: string }) => {
  return request({
    url: '/question/media/draft',
    method: 'post',
    data: data
  });
};

/**
 * 清理长时间未挂到任何试题上的媒体附件与对象存储文件
 * @param retainHours 保留时长（小时），默认 24
 */
export const cleanUnusedMedia = (retainHours?: number) => {
  return request({
    url: '/question/media/cleanUnused',
    method: 'post',
    params: { retainHours }
  });
};
