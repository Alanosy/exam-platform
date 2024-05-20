import BaseRenderer from 'diagram-js/lib/draw/BaseRenderer';
import {
  append as svgAppend,
  attr as svgAttr,
  create as svgCreate,
  select as svgSelect,
  selectAll as svgSelectAll,
  clone as svgClone,
  clear as svgClear,
  remove as svgRemove
} from 'tiny-svg';

const HIGH_PRIORITY = 1500;
export default class CustomRenderer extends BaseRenderer {
  bpmnRenderer: BaseRenderer;
  modeling: any;
  constructor(eventBus, bpmnRenderer, modeling) {
    super(eventBus, HIGH_PRIORITY);
    this.bpmnRenderer = bpmnRenderer;
    this.modeling = modeling;
  }
  canRender(element) {
    // ignore labels
    return !element.labelTarget;
  }

  /**
   * 自定义节点图形
   * @param {*} parentNode 当前元素的svgNode
   * @param {*} element
   * @returns
   */
  drawShape(parentNode, element) {
    const shape = this.bpmnRenderer.drawShape(parentNode, element);
    const { type, width, height } = element;
    // 开始 填充绿色
    if (type === 'bpmn:StartEvent') {
      svgAttr(shape, { fill: '#77DF6D' });
      return shape;
    }
    if (type === 'bpmn:EndEvent') {
      svgAttr(shape, { fill: '#EE7B77' });
      return shape;
    }
    if (type === 'bpmn:UserTask') {
      svgAttr(shape, { fill: '#A9C4F8' });
      return shape;
    }
    return shape;
  }

  getShapePath(shape) {
    return this.bpmnRenderer.getShapePath(shape);
  }
}
CustomRenderer['$inject'] = ['eventBus', 'bpmnRenderer'];
