import ContextPadProvider from 'bpmn-js/lib/features/context-pad/ContextPadProvider';
import { Injector } from 'didi';
import EventBus from 'diagram-js/lib/core/EventBus';
import ContextPad from 'diagram-js/lib/features/context-pad/ContextPad';
import Modeling from 'bpmn-js/lib/features/modeling/Modeling.js';
import ElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory';
import Connect from 'diagram-js/lib/features/connect/Connect';
import Create from 'diagram-js/lib/features/create/Create';
import PopupMenu from 'diagram-js/lib/features/popup-menu/PopupMenu';
import Canvas from 'diagram-js/lib/core/Canvas';
import Rules from 'diagram-js/lib/features/rules/Rules';
import { Element, Shape } from 'diagram-js/lib/model/Types';
import BpmnFactory from 'bpmn-js/lib/features/modeling/BpmnFactory';
import modeler from '@/store/modules/modeler';

// @Description: 增强元素连线事件

class CustomContextPadProvider extends ContextPadProvider {
  private _contextPad: ContextPad;
  private _modeling: Modeling;
  private _elementFactory: ElementFactory;
  private _autoPlace: any;
  private _connect: Connect;
  private _create: Create;
  private _popupMenu: PopupMenu;
  private _canvas: Canvas;
  private _rules: Rules;

  constructor(
    config: any,
    injector: Injector,
    eventBus: EventBus,
    contextPad: ContextPad,
    modeling: Modeling,
    elementFactory: ElementFactory,
    connect: Connect,
    create: Create,
    popupMenu: PopupMenu,
    canvas: Canvas,
    rules: Rules,
    translate
  ) {
    // @ts-expect-error 忽略异常
    super(config, injector, eventBus, contextPad, modeling, elementFactory, connect, create, popupMenu, canvas, rules, translate);

    this._contextPad = contextPad;
    this._modeling = modeling;
    this._elementFactory = elementFactory;
    this._connect = connect;
    this._create = create;
    this._popupMenu = popupMenu;
    this._canvas = canvas;
    this._rules = rules;

    this._autoPlace = injector.get('autoPlace', false);
  }

  getContextPadEntries(element: Element) {
    const actions: Record<string, any> = {};

    const appendUserTask = (event: Event, element: Shape) => {
      const shape = this._elementFactory.createShape({ type: 'bpmn:UserTask' });
      this._create.start(event, shape, {
        source: element
      });
    };

    const appendMultiInstanceUserTask = (event: Event, element: Shape) => {
      const store = modeler();
      const bpmnFactory = store.getModeler().get('bpmnFactory') as BpmnFactory;
      const businessObject = bpmnFactory.create('bpmn:UserTask', {
        // name: '多实例用户任务',
        isForCompensation: false
      });
      businessObject.loopCharacteristics = bpmnFactory.create('bpmn:MultiInstanceLoopCharacteristics');
      // 创建 Shape
      const shape = this._elementFactory.createShape({
        type: 'bpmn:UserTask',
        businessObject: businessObject
      });
      this._create.start(event, shape, { source: element });
    };

    const appendTask = this._autoPlace
      ? (event, element) => {
          const bpmnFactory: BpmnFactory | undefined = modeler().getModeler().get('bpmnFactory');
          const businessObject = bpmnFactory.create('bpmn:UserTask', {
            // name: '多实例用户任务',// 右键创建显示
            isForCompensation: false
          });

          // 创建多实例属性并分配给用户任务的 loopCharacteristics
          businessObject.loopCharacteristics = bpmnFactory.create('bpmn:MultiInstanceLoopCharacteristics');

          // 创建 Shape
          const shape = this._elementFactory.createShape({
            type: 'bpmn:UserTask',
            businessObject: businessObject
          });

          this._autoPlace.append(element, shape);
        }
      : appendMultiInstanceUserTask;

    const append = this._autoPlace
      ? (event: Event, element: Shape) => {
          const shape = this._elementFactory.createShape({ type: 'bpmn:UserTask' });
          this._autoPlace.append(element, shape);
        }
      : appendUserTask;

    // // 添加创建用户任务按钮
    actions['append.append-user-task'] = {
      group: 'model',
      className: 'bpmn-icon-user-task',
      title: '用户任务',
      action: {
        dragstart: appendUserTask,
        click: append
      }
    };

    // 添加创建多实例用户任务按钮
    actions['append.append-multi-instance-user-task'] = {
      group: 'model',
      className: 'bpmn-icon-user', // 你可以使用多实例用户任务的图标  bpmn-icon-user   bpmn-icon-user-task
      title: '多实例用户任务',
      action: {
        dragstart: appendMultiInstanceUserTask,
        click: appendTask
      }
    };

    return actions;
  }
}

export default CustomContextPadProvider;
