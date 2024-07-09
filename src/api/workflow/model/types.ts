export interface ModelForm {
  id: string;
  name: string;
  key: string;
  categoryCode: string;
  xml: string;
  svg: string;
  description: string;
}

export interface ModelQuery extends PageQuery {
  name?: string;
  key?: string;
  categoryCode?: string;
}

export interface OriginalPersistentState {
  metaInfo: string;
  editorSourceValueId: string;
  createTime: string;
  deploymentId?: string;
  name: string;
  tenantId: string;
  category?: string;
  version: number;
  editorSourceExtraValueId?: string;
  key: string;
  lastUpdateTime: string;
}

export interface PersistentState {
  metaInfo: string;
  editorSourceValueId: string;
  createTime: string;
  deploymentId?: string;
  name: string;
  tenantId: string;
  category?: string;
  version: number;
  editorSourceExtraValueId?: string;
  key: string;
  lastUpdateTime: string;
}

export interface ModelVO {
  id: string;
  revision: number;
  originalPersistentState: OriginalPersistentState;
  name: string;
  key: string;
  category?: string;
  createTime: string;
  lastUpdateTime: string;
  version: number;
  metaInfo: string;
  deploymentId?: string;
  editorSourceValueId: string;
  editorSourceExtraValueId?: string;
  tenantId: string;
  persistentState: PersistentState;
  revisionNext: number;
  idPrefix: string;
  inserted: boolean;
  updated: boolean;
  deleted: boolean;
}
