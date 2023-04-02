export interface DemoVO extends BaseEntity {
  id: number | string;
  deptId: number | string;
  userId: number | string;
  orderNum: number;
  testKey: string;
  value: string;
  createByName: string;
  updateByName?: any;
}

export interface DemoQuery extends PageQuery {
  testKey: string;
  value: string;
  createTime: string;
}
export interface DemoForm {
  id: string | number | undefined;
  deptId: string | number | undefined;
  userId: string | number | undefined;
  orderNum: number;
  testKey: string;
  value: string;
  version: string;
  ossConfigId: string | number | undefined;
  createTime?: string;
}

export interface DemoTreeVO extends BaseEntity {
  id: number | string;
  parentId: number | string;
  deptId: number | string;
  userId: number | string;
  treeName: string;
  children?: DemoTreeVO[];
}

export interface DemoTreeQuery {
  treeName: string;
  createTime: string;
}

export interface DemoTreeForm {
  id: string | number | undefined;
  parentId: string | number | undefined;
  deptId: string | number | undefined;
  userId: string | number | undefined;
  treeName: string;
}

export interface DemoTreeOptionsType {
  id: string | number;
  treeName: string;
  children?: DemoTreeOptionsType[];
}
