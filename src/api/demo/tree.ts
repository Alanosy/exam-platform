import { AxiosPromise } from 'axios';
import request from '@/utils/request';
import { DemoTreeForm, DemoTreeVO, DemoTreeQuery } from './types';

// 查询测试树表列表
export function listTree(query?: DemoTreeQuery): AxiosPromise<DemoTreeVO[]> {
	return request({
		url: '/demo/tree/list',
		method: 'get',
		params: query
	});
}

// 查询测试树表详细
export function getTree(id: string | number): AxiosPromise<DemoTreeVO> {
	return request({
		url: '/demo/tree/' + id,
		method: 'get'
	});
}

// 新增测试树表
export function addTree(data: DemoTreeForm) {
	return request({
		url: '/demo/tree',
		method: 'post',
		data: data
	});
}

// 修改测试树表
export function updateTree(data: DemoTreeForm) {
	return request({
		url: '/demo/tree',
		method: 'put',
		data: data
	});
}

// 删除测试树表
export function delTree(id: string | number | Array<string | number>) {
	return request({
		url: '/demo/tree/' + id,
		method: 'delete'
	});
}
