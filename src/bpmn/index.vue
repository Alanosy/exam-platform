<template>
  <div class="containers-bpmn">
    <!-- dark模式下 连接线的箭头样式 -->
    <svg width="0" height="0" style="position: absolute">
      <defs>
        <marker id="markerArrow-dark-mode" viewBox="0 0 20 20" refX="11" refY="10" markerWidth="10" markerHeight="10" orient="auto">
          <path d="M 1 5 L 11 10 L 1 15 Z" class="arrow-dark" />
        </marker>
      </defs>
    </svg>
    <div v-loading="loading" class="app-containers-bpmn">
      <el-container class="h-full">
        <el-container style="align-items: stretch">
          <el-header>
            <div class="process-toolbar">
              <el-space wrap :size="10">
                <el-tooltip effect="dark" content="自适应屏幕" placement="bottom">
                  <el-button size="small" icon="Rank" @click="fitViewport" />
                </el-tooltip>
                <el-tooltip effect="dark" content="放大" placement="bottom">
                  <el-button size="small" icon="ZoomIn" @click="zoomViewport(true)" />
                </el-tooltip>
                <el-tooltip effect="dark" content="缩小" placement="bottom">
                  <el-button size="small" icon="ZoomOut" @click="zoomViewport(false)" />
                </el-tooltip>
                <el-tooltip effect="dark" content="后退" placement="bottom">
                  <el-button size="small" icon="Back" @click="bpmnModeler.get('commandStack').undo()" />
                </el-tooltip>
                <el-tooltip effect="dark" content="前进" placement="bottom">
                  <el-button size="small" icon="Right" @click="bpmnModeler.get('commandStack').redo()" />
                </el-tooltip>
              </el-space>
              <el-space wrap :size="10" style="float: right; padding-right: 10px">
                <el-button size="small" type="primary" @click="saveXml">保 存</el-button>
                <el-dropdown size="small">
                  <el-button size="small" type="primary"> 预 览 </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item icon="Document" @click="previewXML">XML预览</el-dropdown-item>
                      <el-dropdown-item icon="View" @click="previewSVG"> SVG预览</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-dropdown size="small">
                  <el-button size="small" type="primary"> 下 载 </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item icon="Download" @click="downloadXML">下载XML</el-dropdown-item>
                      <el-dropdown-item icon="Download" @click="downloadSVG"> 下载SVG</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </el-space>
            </div>
          </el-header>
          <div ref="canvas" class="canvas" />
        </el-container>
        <div :class="{ 'process-panel': true, 'hide': panelFlag }">
          <div class="process-panel-bar" @click="panelBarClick">
            <div class="open-bar">
              <el-link type="default" :underline="false">
                <svg-icon class-name="open-bar" :icon-class="panelFlag ? 'caret-back' : 'caret-forward'"></svg-icon>
              </el-link>
            </div>
          </div>
          <transition enter-active-class="animate__animated animate__fadeIn">
            <div v-show="showPanel" v-if="bpmnModeler" class="panel-content">
              <PropertyPanel :modeler="bpmnModeler" />
            </div>
          </transition>
        </div>
      </el-container>
    </div>
  </div>
  <div>
    <el-dialog v-model="perviewXMLShow" title="XML预览" width="80%" append-to-body>
      <highlightjs :code="xmlStr" language="XML" />
    </el-dialog>
  </div>
  <div>
    <el-dialog v-model="perviewSVGShow" title="SVG预览" width="80%" append-to-body>
      <div style="text-align: center" v-html="svgData" />
    </el-dialog>
  </div>
</template>

<script lang="ts" setup name="BpmnDesign">
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import './assets/style/index.scss';
import type { Canvas, Modeler } from 'bpmn';
import PropertyPanel from './panel/index.vue';
import BpmnModeler from 'bpmn-js/lib/Modeler.js';
import defaultXML from './assets/defaultXML';
import flowableModdle from './assets/moddle/flowable';
import Modules from './assets/module/index';
import useModelerStore from '@/store/modules/modeler';
import useDialog from '@/hooks/useDialog';

const emit = defineEmits(['closeCallBack', 'saveCallBack']);

const { visible, title, openDialog, closeDialog } = useDialog({
  title: '编辑流程'
});
const modelerStore = useModelerStore();

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const panelFlag = ref(false);
const showPanel = ref(true);
const canvas = ref<HTMLDivElement>();
const panel = ref<HTMLDivElement>();
const bpmnModeler = ref<Modeler>();
const zoom = ref(1);
const perviewXMLShow = ref(false);
const perviewSVGShow = ref(false);
const xmlStr = ref('');
const svgData = ref('');
const loading = ref(false);

const panelBarClick = () => {
  // 延迟执行，否则会导致面板收起时，属性面板不显示
  panelFlag.value = !panelFlag.value;
  setTimeout(() => {
    showPanel.value = !panelFlag.value;
  }, 100);
};

/**
 * 初始化Canvas
 */
const initCanvas = () => {
  bpmnModeler.value = new BpmnModeler({
    container: canvas.value,
    // 键盘
    keyboard: {
      bindTo: window // 或者window，注意与外部表单的键盘监听事件是否冲突
    },
    propertiesPanel: {
      parent: panel.value
    },
    additionalModules: Modules,
    moddleExtensions: {
      flowable: flowableModdle
    }
  });
};

/**
 * 初始化Model
 */
const initModel = () => {
  if (modelerStore.getModeler()) {
    modelerStore.getModeler().destroy();
    modelerStore.setModeler(undefined);
  }
  modelerStore.setModeler(bpmnModeler.value);
};

/**
 * 新建
 */
const newDiagram = async () => {
  await proxy?.$modal.confirm('是否确认新建');
  initDiagram();
};

/**
 * 初始化
 */
const initDiagram = (xml?: string) => {
  if (!xml) xml = defaultXML;
  bpmnModeler.value.importXML(xml);
};

/**
 * 自适应屏幕
 */
const fitViewport = () => {
  zoom.value = bpmnModeler.value.get<Canvas>('canvas').zoom('fit-viewport');
  const bbox = document.querySelector<SVGGElement>('.app-containers-bpmn .viewport').getBBox();
  const currentViewBox = bpmnModeler.value.get<Canvas>('canvas').viewbox();
  const elementMid = {
    x: bbox.x + bbox.width / 2 - 65,
    y: bbox.y + bbox.height / 2
  };
  bpmnModeler.value.get<Canvas>('canvas').viewbox({
    x: elementMid.x - currentViewBox.width / 2,
    y: elementMid.y - currentViewBox.height / 2,
    width: currentViewBox.width,
    height: currentViewBox.height
  });
  zoom.value = (bbox.width / currentViewBox.width) * 1.8;
};
/**
 * 放大或者缩小
 * @param zoomIn true 放大 | false 缩小
 */
const zoomViewport = (zoomIn = true) => {
  zoom.value = bpmnModeler.value.get<Canvas>('canvas').zoom();
  zoom.value += zoomIn ? 0.1 : -0.1;
  bpmnModeler.value.get<Canvas>('canvas').zoom(zoom.value);
};

/**
 * 下载XML
 */
const downloadXML = async () => {
  try {
    const { xml } = await bpmnModeler.value.saveXML({ format: true });
    downloadFile(`${getProcessElement().name}.bpmn20.xml`, xml, 'application/xml');
  } catch (e) {
    proxy?.$modal.msgError(e);
  }
};

/**
 * 下载SVG
 */
const downloadSVG = async () => {
  try {
    const { svg } = await bpmnModeler.value.saveSVG();
    downloadFile(getProcessElement().name, svg, 'image/svg+xml');
  } catch (e) {
    proxy?.$modal.msgError(e);
  }
};

/**
 * XML预览
 */
const previewXML = async () => {
  try {
    const { xml } = await bpmnModeler.value.saveXML({ format: true });
    xmlStr.value = xml;
    perviewXMLShow.value = true;
  } catch (e) {
    proxy?.$modal.msgError(e);
  }
};

/**
 * SVG预览
 */
const previewSVG = async () => {
  try {
    const { svg } = await bpmnModeler.value.saveSVG();
    svgData.value = svg;
    perviewSVGShow.value = true;
  } catch (e) {
    proxy?.$modal.msgError(e);
  }
};

const curNodeInfo = reactive({
  curType: '', // 任务类型 用户任务
  curNode: '',
  expValue: '' //多用户和部门角色实现
});

const downloadFile = (fileName: string, data: any, type: string) => {
  const a = document.createElement('a');
  const url = window.URL.createObjectURL(new Blob([data], { type: type }));
  a.href = url;
  a.download = fileName;
  a.click();
  window.URL.revokeObjectURL(url);
};

const getProcessElement = () => {
  const rootElements = bpmnModeler.value?.getDefinitions().rootElements;
  for (let i = 0; i < rootElements.length; i++) {
    if (rootElements[i].$type === 'bpmn:Process') return rootElements[i];
  }
};

const getProcess = () => {
  const element = getProcessElement();
  return {
    id: element.id,
    name: element.name
  };
};

const saveXml = async () => {
  const { xml } = await bpmnModeler.value.saveXML({ format: true });
  const { svg } = await bpmnModeler.value.saveSVG();
  const process = getProcess();
  let data = {
    xml: xml,
    svg: svg,
    key: process.id,
    name: process.name,
    loading: loading
  };
  emit('saveCallBack', data);
};

const open = (xml?: string) => {
  openDialog();
  nextTick(() => {
    initDiagram(xml);
  });
};
const close = () => {
  closeDialog();
};

onMounted(() => {
  nextTick(() => {
    initCanvas();
    initModel();
  });
});

/**
 * 对外暴露子组件方法
 */
defineExpose({
  initDiagram,
  saveXml,
  open,
  close
});
</script>

<style lang="scss">
/** 夜间模式 线条的颜色 */
$stroke-color-dark: white;
$bpmn-font-size: 12px;
/** 日间模式 字体颜色 */
$bpmn-font-color-dark: white;
/** 夜间模式 字体颜色 */
$bpmn-font-color-light: #222;

/* 背景网格 */
@mixin djs-container {
  background-image: linear-gradient(90deg, hsl(0deg 0% 78.4% / 15%) 10%, transparent 0), linear-gradient(hsl(0deg 0% 78.4% / 15%) 10%, transparent 0) !important;
  background-size: 10px 10px !important;
}

html[class='light'] {
  /** 从左侧拖动时的背景图 */
  svg.new-parent {
    @include djs-container;
  }

  /** 双击编辑元素时样式保持一致 */
  div.djs-direct-editing-parent {
    border-radius: 10px;
    background-color: transparent !important;
    color: $bpmn-font-color-light;
  }

  g.djs-visual {
    .djs-label {
      fill: $bpmn-font-color-light !important;
      font-size: $bpmn-font-size !important;
    }
  }
}

html[class='dark'] {
  /** dark模式下 连接线的箭头样式 */
  .arrow-dark {
    stroke-width: 1px;
    stroke-linecap: round;
    stroke: $stroke-color-dark;
    fill: $stroke-color-dark;
    stroke-linejoin: round;
  }

  /** 从左侧拖动时的背景图 */
  svg.new-parent {
    background-color: black !important;
    @include djs-container;
  }

  /** 双击编辑元素时样式保持一致 */
  div.djs-direct-editing-parent {
    border-radius: 10px;
    background-color: transparent !important;
    color: $bpmn-font-color-dark;
  }

  /** 元素相关设置 */
  g.djs-visual {
    /** 元素边框 需要去除文字(.djs-label) */
    & > *:first-child:not(.djs-label) {
      stroke: $stroke-color-dark !important;
    }

    /** 字体颜色 */
    .djs-label {
      fill: $bpmn-font-color-dark !important;
      font-size: $bpmn-font-size !important;
    }

    /* 连接线样式 */
    path[data-corner-radius] {
      stroke: $stroke-color-dark !important;
      marker-end: url('#markerArrow-dark-mode') !important;
    }
  }
}

.containers-bpmn {
  height: 100%;
  .app-containers-bpmn {
    width: 100%;
    height: 100%;
    .canvas {
      width: 100%;
      height: 100%;
      @include djs-container;
    }
    .el-header {
      height: 35px;
      padding: 0;
    }

    .process-panel {
      transition: width 0.25s ease-in;
      .process-panel-bar {
        width: 34px;
        height: 40px;
        .open-bar {
          width: 34px;
          line-height: 40px;
        }
      }
      // 收起面板样式
      &.hide {
        width: 34px;
        overflow: hidden;
        padding: 0;
        .process-panel-bar {
          width: 34px;
          height: 100%;
          box-sizing: border-box;
          display: block;
          text-align: left;
          line-height: 34px;
        }
        .process-panel-bar:hover {
          background-color: var(--bpmn-panel-bar-background-color);
        }
      }
    }
  }
}
pre {
  margin: 0;
  height: 100%;
  max-height: calc(80vh - 32px);
  overflow-x: hidden;
  overflow-y: auto;
  .hljs {
    word-break: break-word;
    white-space: pre-wrap;
    padding: 0.5em;
  }
}

.open-bar {
  font-size: 20px;
  cursor: pointer;
  text-align: center;
}
.process-panel {
  box-sizing: border-box;
  padding: 0 8px 0 8px;
  border-left: 1px solid var(--bpmn-panel-border);
  box-shadow: var(--bpmn-panel-box-shadow) 0 0 8px;
  max-height: 100%;
  width: 25%;
  height: calc(100vh - 100px);
  .el-collapse {
    height: calc(100vh - 182px);
    overflow: auto;
  }
}

// 任务栏 透明度
//:deep(.djs-palette) {
//  opacity: 0.3;
//  transition: all 1s;
//}
//
//:deep(.djs-palette:hover) {
//  opacity: 1;
//  transition: all 1s;
//}
</style>
