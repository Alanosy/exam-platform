declare module 'bpmn' {
  import type modeler from 'bpmn-js/lib/Modeler';
  import type modeling from 'bpmn-js/lib/features/modeling/Modeling';
  import type canvas from 'diagram-js/lib/core/Canvas';
  import type elementRegistry from 'diagram-js/lib/core/ElementRegistry';
  import type bpmnFactory from 'bpmn-js/lib/features/modeling/BpmnFactory';

  export type Modeler = modeler;
  export type Modeling = modeling;
  export type Canvas = canvas;
  export type ElementRegistry = elementRegistry;
  export type Moddle = import('moddle').Moddle;
  export type ModdleElement = import('moddle').ModdleElement;
  export type BpmnFactory = bpmnFactory;
}
