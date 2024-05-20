import { assign } from 'min-dash';
import PaletteProvider from 'bpmn-js/lib/features/palette/PaletteProvider';
import ElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory';
import Create from 'diagram-js/lib/features/create/Create';
import SpaceTool from 'diagram-js/lib/features/space-tool/SpaceTool';
import LassoTool from 'diagram-js/lib/features/lasso-tool/LassoTool';
import HandTool from 'diagram-js/lib/features/hand-tool/HandTool';
import GlobalConnect from 'diagram-js/lib/features/global-connect/GlobalConnect';
import Palette from 'diagram-js/lib/features/palette/Palette';
import modeler from '@/store/modules/modeler';
import BpmnFactory from 'bpmn-js/lib/features/modeling/BpmnFactory';

// @Description: 增强左侧面板
class CustomPaletteProvider extends PaletteProvider {
  private readonly _palette: Palette;
  private readonly _create: Create;
  private readonly _elementFactory: ElementFactory;
  private readonly _spaceTool: SpaceTool;
  private readonly _lassoTool: LassoTool;
  private readonly _handTool: HandTool;
  private readonly _globalConnect: GlobalConnect;
  private readonly _translate: any;

  constructor(palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect, translate) {
    super(palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect, translate);
    this._palette = palette;
    this._create = create;
    this._elementFactory = elementFactory;
    this._spaceTool = spaceTool;
    this._lassoTool = lassoTool;
    this._handTool = handTool;
    this._globalConnect = globalConnect;
    this._translate = translate;
  }

  getPaletteEntries() {
    const actions = {},
      create = this._create,
      elementFactory = this._elementFactory,
      translate = this._translate;

    function createAction(type: string, group: string, className: string, title: string, options?: object) {
      function createListener(event) {
        const shape = elementFactory.createShape(assign({ type: type }, options));
        if (options) {
          !shape.businessObject.di && (shape.businessObject.di = {});
          shape.businessObject.di.isExpanded = (options as { [key: string]: any }).isExpanded;
        }
        create.start(event, shape, null);
      }
      const shortType = type.replace(/^bpmn:/, '');
      return {
        group: group,
        className: className,
        title: title || translate('Create {type}', { type: shortType }),
        action: {
          dragstart: createListener,
          click: createListener
        }
      };
    }

    function createMultiInstanceUserTask(event) {
      const bpmnFactory: BpmnFactory | undefined = modeler().getBpmnFactory();
      // 创建一个 bpmn:UserTask
      const userTask = bpmnFactory.create('bpmn:UserTask', {
        // name: '多实例用户任务', // 在画板中显示字段
        isForCompensation: false
      });
      // 将多实例属性分配给 bpmn:UserTask 的 loopCharacteristics
      userTask.loopCharacteristics = bpmnFactory.create('bpmn:MultiInstanceLoopCharacteristics');
      const customUserTask = elementFactory.createShape({
        type: 'bpmn:UserTask',
        businessObject: userTask // 分配创建的 userTask 到 businessObject
      });
      create.start(event, customUserTask, {});
    }

    assign(actions, {
      'create.parallel-gateway': createAction('bpmn:ParallelGateway', 'gateway', 'bpmn-icon-gateway-parallel', '并行网关'),
      'create.event-base-gateway': createAction('bpmn:EventBasedGateway', 'gateway', 'bpmn-icon-gateway-eventbased', '事件网关'),
      // 分组线
      'gateway-separator': {
        group: 'gateway',
        separator: true
      },
      'create.user-task': createAction('bpmn:UserTask', 'activity', 'bpmn-icon-user-task', '创建用户任务'),
      'create.multi-instance-user-task': {
        group: 'activity',
        type: 'bpmn:UserTask',
        className: 'bpmn-icon-user task-multi-instance',
        title: '创建多实例用户任务',
        action: {
          click: createMultiInstanceUserTask,
          dragstart: createMultiInstanceUserTask
        }
      },
      'task-separator': {
        group: 'activity',
        separator: true
      }
    });
    return actions;
  }
}

CustomPaletteProvider['$inject'] = ['palette', 'create', 'elementFactory', 'spaceTool', 'lassoTool', 'handTool', 'globalConnect', 'translate'];

export default CustomPaletteProvider;
