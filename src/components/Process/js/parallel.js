import { h, PolygonNode, PolygonNodeModel } from '@logicflow/core';

class ParallelModel extends PolygonNodeModel {
  static extendKey = 'ParallelModel';
  constructor(data, graphModel) {
    if (!data.text) {
      data.text = '';
    }
    if (data.text && typeof data.text === 'string') {
      data.text = {
        value: data.text,
        x: data.x,
        y: data.y + 40
      };
    }
    super(data, graphModel);
    this.points = [
      [25, 0],
      [50, 25],
      [25, 50],
      [0, 25]
    ];
  }
}

class ParallelView extends PolygonNode {
  static extendKey = 'ParallelNode';
  getShape() {
    const { model } = this.props;
    const { x, y, width, height, points } = model;
    const style = model.getNodeStyle();
    return h(
      'g',
      {
        transform: `matrix(1 0 0 1 ${x - width / 2} ${y - height / 2})`
      },
      h('polygon', {
        ...style,
        x,
        y,
        points
      }),
      h('path', {
        d: 'm 23,10 0,12.5 -12.5,0 0,5 12.5,0 0,12.5 5,0 0,-12.5 12.5,0 0,-5 -12.5,0 0,-12.5 -5,0 z',
        ...style
      })
    );
  }
}

export default {
  type: 'parallel',
  view: ParallelView,
  model: ParallelModel
};
