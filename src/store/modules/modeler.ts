import type { Modeler, Modeling, Canvas, ElementRegistry, Moddle, BpmnFactory } from 'bpmn';

type ModelerStore = {
  modeler: Modeler | undefined;
  moddle: Moddle | undefined;
  modeling: Modeling | undefined;
  canvas: Canvas | undefined;
  elementRegistry: ElementRegistry | undefined;
  bpmnFactory: BpmnFactory | undefined;
  // 流程定义根节点信息
  procDefId: string | undefined;
  procDefName: string | undefined;
};

const defaultState: ModelerStore = {
  modeler: undefined,
  moddle: undefined,
  modeling: undefined,
  canvas: undefined,
  elementRegistry: undefined,
  bpmnFactory: undefined,
  procDefId: undefined,
  procDefName: undefined
};
export const useModelerStore = defineStore('modeler', () => {
  let modeler = defaultState.modeler;
  let moddle = defaultState.moddle;
  let modeling = defaultState.modeling;
  let canvas = defaultState.canvas;
  let elementRegistry = defaultState.elementRegistry;
  let bpmnFactory = defaultState.bpmnFactory;
  const procDefId = ref(defaultState.procDefId);
  const procDefName = ref(defaultState.procDefName);

  const getModeler = () => modeler;
  const getModdle = () => moddle;
  const getModeling = (): Modeling | undefined => modeling;
  const getCanvas = (): Canvas | undefined => canvas;
  const getElRegistry = (): ElementRegistry | undefined => elementRegistry;
  const getBpmnFactory = (): BpmnFactory | undefined => bpmnFactory;
  const getProcDefId = (): string | undefined => procDefId.value;
  const getProcDefName = (): string | undefined => procDefName.value;

  // 设置根节点
  const setModeler = (modelers: Modeler | undefined) => {
    if (modelers) {
      modeler = modelers;
      modeling = modelers.get<Modeling>('modeling');
      moddle = modelers.get<Moddle>('moddle');
      canvas = modelers.get<Canvas>('canvas');
      bpmnFactory = modelers.get<BpmnFactory>('bpmnFactory');
      elementRegistry = modelers.get<ElementRegistry>('elementRegistry');
    } else {
      modeling = moddle = canvas = elementRegistry = bpmnFactory = undefined;
    }
  };
  // 设置流程定义根节点信息
  const setProcDef = (modeler: Modeler | undefined) => {
    procDefId.value = modeler.get<Canvas>('canvas').getRootElement().businessObject.get('id');
    procDefName.value = modeler.get<Canvas>('canvas').getRootElement().businessObject.get('name');
  };

  return {
    getModeler,
    getModdle,
    getModeling,
    getCanvas,
    getElRegistry,
    getBpmnFactory,
    getProcDefId,
    getProcDefName,
    setModeler,
    setProcDef
  };
});
export default useModelerStore;
