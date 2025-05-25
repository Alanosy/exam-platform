<template>
  <div>
    <el-header style="border-bottom: 1px solid rgb(218 218 218); height: auto">
      <div style="display: flex; padding: 10px 0px; justify-content: space-between">
        <div>
          <el-tooltip effect="dark" content="自适应屏幕" placement="bottom">
            <el-button size="small" icon="Rank" @click="zoomViewport(1)">自适应屏幕</el-button>
          </el-tooltip>
          <el-tooltip effect="dark" content="放大" placement="bottom">
            <el-button size="small" icon="ZoomIn" @click="zoomViewport(true)">放大</el-button>
          </el-tooltip>
          <el-tooltip effect="dark" content="缩小" placement="bottom">
            <el-button size="small" icon="ZoomOut" @click="zoomViewport(false)">缩小</el-button>
          </el-tooltip>
        </div>
        <div>
          <el-button size="small" style="border: 1px solid #000">未完成</el-button>
          <el-button size="small" style="background-color: #fff8dc; border: 1px solid #ffcd17">进行中</el-button>
          <el-button size="small" style="background-color: #f0ffd9; border: 1px solid #9dff00">已完成</el-button>
        </div>
      </div>
    </el-header>
    <div class="container" ref="container"></div>
  </div>
</template>

<script setup lang="ts">
import LogicFlow from '@logicflow/core';
import '@logicflow/core/lib/style/index.css';
import Start from './js/start.js';
import Between from './js/between.js';
import Serial from './js/serial.js';
import Parallel from './js/parallel.js';
import End from './js/end.js';
import Skip from './js/skip.js';
import { json2LogicFlowJson } from './js/tool.js';

// Props 定义方式变化
const props = defineProps({
  defJson: {
    type: Object,
    default: () => ({})
  }
});

const container = ref(null);
const lf = ref(null);
const register = () => {
  lf.value.register(Start);
  lf.value.register(Between);
  lf.value.register(Serial);
  lf.value.register(Parallel);
  lf.value.register(End);
  lf.value.register(Skip);
};
const zoomViewport = async (zoom) => {
  lf.value.zoom(zoom);
  // 将内容平移至画布中心
  lf.value.translateCenter();
};

onMounted(async () => {
  if (props.defJson) {
    const data = json2LogicFlowJson(props.defJson);
    lf.value = new LogicFlow({
      container: container.value,
      grid: false,
      isSilentMode: true,
      textEdit: false
    });
    register();
    lf.value.render(data);
    lf.value.translateCenter();
  }
});
</script>

<style scoped>
/* 样式部分保持不变 */
.container {
  width: 100%;
  height: 500px;
}
</style>
