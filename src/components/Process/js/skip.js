import { PolylineEdge, PolylineEdgeModel } from '@logicflow/core';

class SkipModel extends PolylineEdgeModel {
  setAttributes() {
    this.offset = 20;
  }

  getEdgeStyle() {
    const style = super.getEdgeStyle();
    const { properties } = this;
    if (properties.isActived) {
      style.strokeDasharray = '4 4';
    }
    return style;
  }

  /**
   * 重写此方法，使保存数据是能带上锚点数据。
   */
  getData() {
    const data = super.getData();
    data.sourceAnchorId = this.sourceAnchorId;
    data.targetAnchorId = this.targetAnchorId;
    return data;
  }
}

export default {
  type: 'skip',
  view: PolylineEdge,
  model: SkipModel
};
