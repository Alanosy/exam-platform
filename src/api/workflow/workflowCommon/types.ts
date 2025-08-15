export interface RouterJumpVo {
  businessId: string;
  taskId: string | number;
  type: string;
  formCustom: string;
  formPath: string;
}

export interface StartProcessBo {
  businessId: string | number;
  flowCode: string;
  variables: any;
  flowInstanceBizExtBo: any;
}
