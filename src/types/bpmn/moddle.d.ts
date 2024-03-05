declare module 'moddle' {
  import type { Element as element } from 'bpmn-js/lib/model/Types';

  export type Element = {
    get<T>(name: string): T;

    set(name: string, value: any): void;
  } & element;

  export interface ModdleElement extends Element {
    $model: Moddle;
    readonly $type: string;
    $attrs: object | {};
    $parent: any;
    businessObject: ModdleElement;
    type: string;

    [field: string]: any;

    hasType(element: ModdleElement, type?: string): boolean;
  }

  export interface Package {
    name: string;
    prefix: string;
  }

  export interface Moddle {
    typeCache: Record<string, ModdleElement>;

    getPackage: typeof Registry.prototype.getPackage;

    getPackages: typeof Registry.prototype.getPackages;

    create(type: string, attrs?: any): ModdleElement;
  }
}
