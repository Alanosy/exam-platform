import request from '@/utils/request';
import { DbTableQuery, DbTableVO, TableQuery, TableVO, GenTableVO, DbTableForm } from './types';
import { AxiosPromise } from 'axios';

// 查询生成表数据
export const listTable = (query: TableQuery): AxiosPromise<TableVO[]> => {
  return request({
    url: '/tool/gen/list',
    method: 'get',
    params: query
  });
};
// 查询db数据库列表
export const listDbTable = (query: DbTableQuery): AxiosPromise<DbTableVO[]> => {
  return request({
    url: '/tool/gen/db/list',
    method: 'get',
    params: query
  });
};

// 查询表详细信息
export const getGenTable = (tableId: string | number): AxiosPromise<GenTableVO> => {
  return request({
    url: '/tool/gen/' + tableId,
    method: 'get'
  });
};

// 修改代码生成信息
export const updateGenTable = (data: DbTableForm) => {
  return request({
    url: '/tool/gen',
    method: 'put',
    data: data
  });
};

// 导入表
export const importTable = (data: { tables: string; dataName: string }) => {
  return request({
    url: '/tool/gen/importTable',
    method: 'post',
    params: data
  });
};

// 预览生成代码
export const previewTable = (tableId: string | number) => {
  return request({
    url: '/tool/gen/preview/' + tableId,
    method: 'get'
  });
};

// 删除表数据
export const delTable = (tableId: string | number | Array<string | number>) => {
  return request({
    url: '/tool/gen/' + tableId,
    method: 'delete'
  });
};

// 生成代码（自定义路径）
export const genCode = (tableId: string | number) => {
  return request({
    url: '/tool/gen/genCode/' + tableId,
    method: 'get'
  });
};

// 同步数据库
export const synchDb = (tableId: string | number) => {
  return request({
    url: '/tool/gen/synchDb/' + tableId,
    method: 'get'
  });
};

// 获取数据源名称
export const getDataNames = () => {
  return request({
    url: '/tool/gen/getDataNames',
    method: 'get'
  });
};
