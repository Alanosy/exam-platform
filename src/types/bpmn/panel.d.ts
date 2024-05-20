declare module 'bpmnDesign' {
  import { AllocationTypeEnum, SpecifyDescEnum, MultiInstanceTypeEnum } from '@/enums/bpmn/IndexEnums';

  export interface ParamVO {
    type: string;
    name: string;
    value: string;
  }

  export interface TaskListenerVO {
    event: string;
    type: string;
    name: string;
    className: string;
    params: ParamVO[];
  }

  export interface ExecutionListenerVO {
    event: string;
    type: string;
    className: string;
    params: ParamVO[];
  }

  interface BasePanel {
    id: string;
    name: string;
  }
  export interface ProcessPanel extends BasePanel {}

  export interface TaskPanel extends BasePanel {
    allocationType: AllocationTypeEnum;
    specifyDesc: SpecifyDescEnum;
    multiInstanceType: MultiInstanceTypeEnum;
    async?: boolean;
    priority?: number;
    formKey?: string;
    skipExpression?: string;
    isForCompensation?: boolean;
    triggerServiceTask?: boolean;
    autoStoreVariables?: boolean;
    ruleVariablesInput?: string;
    excludeTaskListener?: boolean;
    exclude?: boolean;
    class?: string;
    dueDate?: string;
    fixedAssignee?: string;

    candidateUsers?: string;
    assignee?: string;
    candidateGroups?: string;
    collection?: string;
    elementVariable?: string;
    completionCondition?: string;
    isSequential?: boolean;

    loopCharacteristics?: {
      collection: string;
      elementVariable: string;
      isSequential: boolean;
      completionCondition: {
        body: string;
      };
    };
  }

  export interface StartEndPanel extends BasePanel {}
  export interface GatewayPanel extends BasePanel {}
  export interface SequenceFlowPanel extends BasePanel {
    conditionExpression: {
      body: string;
    };
    conditionExpressionValue: string;
    skipExpression: string;
  }

  export interface ParticipantPanel extends BasePanel {}
  export interface SubProcessPanel extends BasePanel {
    multiInstanceType: MultiInstanceTypeEnum;
    collection?: string;
    elementVariable?: string;
    completionCondition?: string;
    loopCharacteristics?: {
      collection: string;
      elementVariable: string;
      isSequential: boolean;
      completionCondition: {
        body: string;
      };
    };
  }
}
