<template>
  <div v-loading="loading" class="bpmnDialogContainers">
    <el-header style="border-bottom: 1px solid rgb(218 218 218); height: auto">
      <div class="header-div">
        <div>
          <el-tooltip effect="dark" content="自适应屏幕" placement="bottom">
            <el-button size="small" icon="Rank" @click="fitViewport" />
          </el-tooltip>
          <el-tooltip effect="dark" content="放大" placement="bottom">
            <el-button size="small" icon="ZoomIn" @click="zoomViewport(true)" />
          </el-tooltip>
          <el-tooltip effect="dark" content="缩小" placement="bottom">
            <el-button size="small" icon="ZoomOut" @click="zoomViewport(false)" />
          </el-tooltip>
        </div>
        <div>
          <div class="tips-label">
            <div class="un-complete">未完成</div>
            <div class="in-progress">进行中</div>
            <div class="complete">已完成</div>
          </div>
        </div>
      </div>
    </el-header>
    <div class="flow-containers">
      <el-container class="bpmn-el-container" style="align-items: stretch">
        <el-main style="padding: 0">
          <div ref="canvas" class="canvas" />
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script lang="ts" setup>
import BpmnViewer from 'bpmn-js/lib/Viewer';
import MoveCanvasModule from 'diagram-js/lib/navigation/movecanvas';
import ZoomScrollModule from 'diagram-js/lib/navigation/zoomscroll';
import { ModuleDeclaration } from 'didi';
import type { Canvas, ModdleElement } from 'bpmn';
import EventBus from 'diagram-js/lib/core/EventBus';
import Overlays from 'diagram-js/lib/features/overlays/Overlays';
import processApi from '@/api/workflow/processInstance/index';

const canvas = ref<HTMLElement>();
const modeler = ref<BpmnViewer>();
const taskList = ref([]);
const zoom = ref(1);
const xml = ref('');
const loading = ref(false);
const bpmnVisible = ref(true);
const historyList = ref([]);

const init = (businessKey) => {
  loading.value = true;
  bpmnVisible.value = true;
  nextTick(async () => {
    if (modeler.value) modeler.value.destroy();
    modeler.value = new BpmnViewer({
      container: canvas.value,
      additionalModules: [
        {
          //禁止滚轮滚动
          zoomScroll: ['value', '']
        },
        ZoomScrollModule,
        MoveCanvasModule
      ] as ModuleDeclaration[]
    });
    const resp = await processApi.getHistoryList(businessKey);
    xml.value = resp.data.xml;
    taskList.value = resp.data.taskList;
    historyList.value = resp.data.historyList;
    await createDiagram(xml.value);
    loading.value = false;
  });
};

const initXml = (xmlStr: string) => {
  loading.value = true;
  bpmnVisible.value = true;
  nextTick(async () => {
    if (modeler.value) modeler.value.destroy();
    modeler.value = new BpmnViewer({
      container: canvas.value,
      additionalModules: [
        {
          //禁止滚轮滚动
          zoomScroll: ['value', '']
        },
        ZoomScrollModule,
        MoveCanvasModule
      ] as ModuleDeclaration[]
    });
    xml.value = xmlStr;
    await createDiagram(xml.value);
    loading.value = false;
  });
};

const createDiagram = async (data) => {
  try {
    await modeler.value.importXML(data);
    fitViewport();
    fillColor();
    loading.value = false;
    addEventBusListener();
  } catch (err) {
    console.log(err);
  }
};
const addEventBusListener = () => {
  const eventBus = modeler.value.get<EventBus>('eventBus');
  const overlays = modeler.value.get<Overlays>('overlays');
  eventBus.on<ModdleElement>('element.hover', (e) => {
    let data = historyList.value.find((t) => t.taskDefinitionKey === e.element.id);
    if (e.element.type === 'bpmn:UserTask' && data) {
      setTimeout(() => {
        genNodeDetailBox(e, overlays, data);
      }, 10);
    }
  });
  eventBus.on('element.out', (e) => {
    overlays.clear();
  });
};
const genNodeDetailBox = (e, overlays, data) => {
  overlays.add(e.element.id, {
    position: { top: e.element.height, left: 0 },
    html: `<div class="verlays">
                    <p>审批人员: ${data.nickName || ''}<p/>
                    <p>节点状态：${data.status || ''}</p>
                    <p>开始时间：${data.startTime || ''}</p>
                    <p>结束时间：${data.endTime || ''}</p>
                    <p>审批耗时：${data.runDuration || ''}</p>
                    <p>流程版本：v${data.version || ''}</p>
                   </div>`
  });
};
// 让图能自适应屏幕
const fitViewport = () => {
  zoom.value = modeler.value.get<Canvas>('canvas').zoom('fit-viewport');
  const bbox = document.querySelector<SVGGElement>('.flow-containers .viewport').getBBox();
  const currentViewBox = modeler.value.get('canvas').viewbox();
  const elementMid = {
    x: bbox.x + bbox.width / 2 - 65,
    y: bbox.y + bbox.height / 2
  };
  modeler.value.get<Canvas>('canvas').viewbox({
    x: elementMid.x - currentViewBox.width / 2,
    y: elementMid.y - currentViewBox.height / 2,
    width: currentViewBox.width,
    height: currentViewBox.height
  });
  zoom.value = (bbox.width / currentViewBox.width) * 1.8;
};
// 放大缩小
const zoomViewport = (zoomIn = true) => {
  zoom.value = modeler.value.get<Canvas>('canvas').zoom();
  zoom.value += zoomIn ? 0.1 : -0.1;
  modeler.value.get<Canvas>('canvas').zoom(zoom.value);
};
//上色
const fillColor = () => {
  const canvas = modeler.value.get<Canvas>('canvas');
  bpmnNodeList(modeler.value._definitions.rootElements[0].flowElements, canvas);
};
//递归上色
const bpmnNodeList = (flowElements, canvas) => {
  flowElements.forEach((n) => {
    if (n.$type === 'bpmn:UserTask') {
      const completeTask = taskList.value.find((m) => m.key === n.id);
      if (completeTask) {
        canvas.addMarker(n.id, completeTask.completed ? 'highlight' : 'highlight-todo');
        n.outgoing?.forEach((nn) => {
          const targetTask = taskList.value.find((m) => m.key === nn.targetRef.id);
          if (targetTask) {
            canvas.addMarker(nn.id, targetTask.completed ? 'highlight' : 'highlight-todo');
          } else if (nn.targetRef.$type === 'bpmn:ExclusiveGateway') {
            canvas.addMarker(nn.id, completeTask.completed ? 'highlight' : 'highlight-todo');
            canvas.addMarker(nn.targetRef.id, completeTask.completed ? 'highlight' : 'highlight-todo');
            nn.targetRef.outgoing.forEach((e) => {
              gateway(e.id, e.targetRef.$type, e.targetRef.id, canvas, completeTask.completed);
            });
          } else if (nn.targetRef.$type === 'bpmn:ParallelGateway') {
            canvas.addMarker(nn.id, completeTask.completed ? 'highlight' : 'highlight-todo');
            canvas.addMarker(nn.targetRef.id, completeTask.completed ? 'highlight' : 'highlight-todo');
            nn.targetRef.outgoing.forEach((e) => {
              gateway(e.id, e.targetRef.$type, e.targetRef.id, canvas, completeTask.completed);
            });
          } else if (nn.targetRef.$type === 'bpmn:InclusiveGateway') {
            canvas.addMarker(nn.id, completeTask.completed ? 'highlight' : 'highlight-todo');
            canvas.addMarker(nn.targetRef.id, completeTask.completed ? 'highlight' : 'highlight-todo');
            nn.targetRef.outgoing.forEach((e) => {
              gateway(e.id, e.targetRef.$type, e.targetRef.id, canvas, completeTask.completed);
            });
          }
        });
      }
    } else if (n.$type === 'bpmn:ExclusiveGateway') {
      n.outgoing.forEach((nn) => {
        const targetTask = taskList.value.find((m) => m.key === nn.targetRef.id);
        if (targetTask) {
          canvas.addMarker(nn.id, targetTask.completed ? 'highlight' : 'highlight-todo');
        }
      });
    } else if (n.$type === 'bpmn:ParallelGateway') {
      n.outgoing.forEach((nn) => {
        const targetTask = taskList.value.find((m) => m.key === nn.targetRef.id);
        if (targetTask) {
          canvas.addMarker(nn.id, targetTask.completed ? 'highlight' : 'highlight-todo');
        }
      });
    } else if (n.$type === 'bpmn:InclusiveGateway') {
      n.outgoing.forEach((nn) => {
        const targetTask = taskList.value.find((m) => m.key === nn.targetRef.id);
        if (targetTask) {
          canvas.addMarker(nn.id, targetTask.completed ? 'highlight' : 'highlight-todo');
        }
      });
    } else if (n.$type === 'bpmn:SubProcess') {
      const completeTask = taskList.value.find((m) => m.key === n.id);
      if (completeTask) {
        canvas.addMarker(n.id, completeTask.completed ? 'highlight' : 'highlight-todo');
      }
      bpmnNodeList(n.flowElements, canvas);
    } else if (n.$type === 'bpmn:StartEvent') {
      canvas.addMarker(n.id, 'startEvent');
      if (n.outgoing) {
        n.outgoing.forEach((nn) => {
          const completeTask = taskList.value.find((m) => m.key === nn.targetRef.id);
          if (completeTask) {
            canvas.addMarker(nn.id, 'highlight');
            canvas.addMarker(n.id, 'highlight');
          }
        });
      }
    } else if (n.$type === 'bpmn:EndEvent') {
      canvas.addMarker(n.id, 'endEvent');
      const completeTask = taskList.value.find((m) => m.key === n.id);
      if (completeTask) {
        canvas.addMarker(completeTask.key, 'highlight');
        canvas.addMarker(n.id, 'highlight');
        return;
      }
    }
  });
};
const gateway = (id, targetRefType, targetRefId, canvas, completed) => {
  if (targetRefType === 'bpmn:ExclusiveGateway') {
    canvas.addMarker(id, completed ? 'highlight' : 'highlight-todo');
    canvas.addMarker(targetRefId, completed ? 'highlight' : 'highlight-todo');
  }
  if (targetRefType === 'bpmn:ParallelGateway') {
    canvas.addMarker(id, completed ? 'highlight' : 'highlight-todo');
    canvas.addMarker(targetRefId, completed ? 'highlight' : 'highlight-todo');
  }
  if (targetRefType === 'bpmn:InclusiveGateway') {
    canvas.addMarker(id, completed ? 'highlight' : 'highlight-todo');
    canvas.addMarker(targetRefId, completed ? 'highlight' : 'highlight-todo');
  }
};
defineExpose({
  init,
  initXml
});
</script>

<style lang="scss" scoped>
.canvas {
  width: 100%;
  height: 100%;
}

.header-div {
  display: flex;
  padding: 10px 0;
  justify-content: space-between;

  .tips-label {
    display: flex;
    div {
      margin-right: 10px;
      padding: 5px;
      font-size: 12px;
    }
    .un-complete {
      border: 1px solid #000;
    }
    .in-progress {
      background-color: rgb(255, 237, 204);
      border: 1px dashed orange;
    }
    .complete {
      background-color: rgb(204, 230, 204);
      border: 1px solid green;
    }
  }
}

.view-mode {
  .el-header,
  .el-aside,
  .djs-palette,
  .bjs-powered-by {
    display: none;
  }
  .el-loading-mask {
    background-color: initial;
  }
  .el-loading-spinner {
    display: none;
  }
}
.bpmn-el-container {
  height: calc(100vh - 350px);
}
.flow-containers {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  .canvas {
    width: 100%;
    height: 100%;
  }
  .load {
    margin-right: 10px;
  }
  :deep(.el-form-item__label) {
    font-size: 13px;
  }

  :deep(.djs-palette) {
    left: 0 !important;
    top: 0;
    border-top: none;
  }

  :deep(.djs-container svg) {
    min-height: 650px;
  }

  :deep(.startEvent.djs-shape .djs-visual > :nth-child(1)) {
    fill: #77df6d !important;
  }
  :deep(.endEvent.djs-shape .djs-visual > :nth-child(1)) {
    fill: #ee7b77 !important;
  }
  :deep(.highlight.djs-shape .djs-visual > :nth-child(1)) {
    fill: green !important;
    stroke: green !important;
    fill-opacity: 0.2 !important;
  }
  :deep(.highlight.djs-shape .djs-visual > :nth-child(2)) {
    fill: green !important;
  }
  :deep(.highlight.djs-shape .djs-visual > path) {
    fill: green !important;
    fill-opacity: 0.2 !important;
    stroke: green !important;
  }
  :deep(.highlight.djs-connection > .djs-visual > path) {
    stroke: green !important;
  }

  // 边框滚动动画
  @keyframes path-animation {
    from {
      stroke-dashoffset: 100%;
    }

    to {
      stroke-dashoffset: 0%;
    }
  }

  :deep(.highlight-todo.djs-connection > .djs-visual > path) {
    animation: path-animation 60s;
    animation-timing-function: linear;
    animation-iteration-count: infinite;
    stroke-dasharray: 4px !important;
    stroke: orange !important;
    fill-opacity: 0.2 !important;
    marker-end: url('#sequenceflow-end-_E7DFDF-_E7DFDF-803g1kf6zwzmcig1y2ulm5egr');
  }

  :deep(.highlight-todo.djs-shape .djs-visual > :nth-child(1)) {
    animation: path-animation 60s;
    animation-timing-function: linear;
    animation-iteration-count: infinite;
    stroke-dasharray: 4px !important;
    stroke: orange !important;
    fill: orange !important;
    fill-opacity: 0.2 !important;
  }
}
:deep(.verlays) {
  width: 250px;
  background: rgb(102, 102, 102);
  border-radius: 4px;
  border: 1px solid #ebeef5;
  color: #fff;
  padding: 15px 10px;
  p {
    line-height: 28px;
    margin: 0;
    padding: 0;
  }
  cursor: pointer;
}
</style>
