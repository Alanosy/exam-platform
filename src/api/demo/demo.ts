import request from '@/utils/request';
import { DemoForm, DemoQuery, DemoVO } from './types';
import { AxiosPromise } from 'axios';

// 查询测试单表列表
export function listDemo(query: DemoQuery): AxiosPromise<DemoVO[]> {
	return request({
		url: '/demo/demo/list',
		method: 'get',
		params: query
	});
}

// 自定义分页接口
export function pageDemo(query: DemoQuery): AxiosPromise<DemoVO[]> {
	return request({
		url: '/demo/demo/page',
		method: 'get',
		params: query
	});
}

// 查询测试单表详细
export function getDemo(id: string | number): AxiosPromise<DemoVO> {
	return request({
		url: '/demo/demo/' + id,
		method: 'get'
	});
}

// 新增测试单表
export function addDemo(data: DemoForm) {
	return request({
		url: '/demo/demo',
		method: 'post',
		data: data
	});
}

// 修改测试单表
export function updateDemo(data: DemoForm) {
	return request({
		url: '/demo/demo',
		method: 'put',
		data: data
	});
}

// 删除测试单表
export function delDemo(id: string | number | Array<string | number>) {
	return request({
		url: '/demo/demo/' + id,
		method: 'delete'
	});
}
