export interface DictDataQuery extends PageQuery {
  dictName: string;
  dictType: string;
  status: string;
  dictLabel: string;
}

export interface DictDataVO extends BaseEntity {
  dictCode: string;
  dictLabel: string;
  dictValue: string;
  cssClass: string;
  listClass: ElTagType;
  dictSort: number;
  status: string;
  remark: string;
}

export interface DictDataForm {
  dictType?: string;
  dictCode: string | undefined;
  dictLabel: string;
  dictValue: string;
  cssClass: string;
  listClass: ElTagType;
  dictSort: number;
  status: string;
  remark: string;
}
