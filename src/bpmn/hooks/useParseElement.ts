import type { ModdleElement } from 'bpmn';

interface Options {
  element: ModdleElement;
}

interface Data {
  id: string;
}

export default (ops: Options) => {
  const { element } = ops;

  const parseData = <T>(): T => {
    const result = {
      ...element.businessObject,
      ...element.businessObject.$attrs
    };

    // 移除flowable前缀，格式化数组
    for (const key in result) {
      if (key.indexOf('flowable:') === 0) {
        const newKey = key.replace('flowable:', '');
        result[newKey] = result[key];
        delete result[key];
      }
    }
    return { ...result } as T;
  };

  return {
    parseData
  };
};
