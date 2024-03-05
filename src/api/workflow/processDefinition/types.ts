export interface ProcessDefinitionQuery extends PageQuery {
  key?: string;
  name?: string;
  categoryCode?: string;
}

export interface ProcessDefinitionVO extends BaseEntity {
  id: string;
  name: string;
  key: string;
  version: number;
  suspensionState: number;
  resourceName: string;
  diagramResourceName: string;
  deploymentId: string;
  deploymentTime: string;
}

export interface ProcessDefinitionXmlVO {
  xml: string[];
  xmlStr: string;
}
