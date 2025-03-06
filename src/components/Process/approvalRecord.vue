<template>
  <div class="container">
    <el-dialog v-model="visible" draggable title="审批记录" :width="props.width" :height="props.height" :close-on-click-modal="false">
      <el-tabs v-model="tabActiveName" class="demo-tabs">
        <el-tab-pane v-loading="loading" label="流程图" name="image" style="height: 68vh">
          <div
            ref="imageWrapperRef"
            class="image-wrapper"
            @wheel="handleMouseWheel"
            @mousedown="handleMouseDown"
            @mousemove="handleMouseMove"
            @mouseup="handleMouseUp"
            @mouseleave="handleMouseLeave"
            @dblclick="resetTransform"
            :style="transformStyle"
          >
            <el-card class="box-card">
              <el-image :src="imgUrl" class="scalable-image" />
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane v-loading="loading" label="审批信息" name="info">
          <div>
            <el-table :data="historyList" style="width: 100%" border fit>
              <el-table-column type="index" label="序号" align="center" width="60"></el-table-column>
              <el-table-column prop="nodeName" label="任务名称" sortable align="center"></el-table-column>
              <el-table-column prop="approveName" :show-overflow-tooltip="true" label="办理人" sortable align="center">
                <template #default="scope">
                  <template v-if="scope.row.approveName">
                    <el-tag v-for="(item, index) in scope.row.approveName.split(',')" :key="index" type="success">{{ item }}</el-tag>
                  </template>
                  <template v-else> <el-tag type="success">无</el-tag></template>
                </template>
              </el-table-column>
              <el-table-column prop="flowStatus" label="状态" width="80" sortable align="center">
                <template #default="scope">
                  <dict-tag :options="wf_task_status" :value="scope.row.flowStatus"></dict-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="审批意见" :show-overflow-tooltip="true" sortable align="center"></el-table-column>
              <el-table-column prop="createTime" label="开始时间" width="160" :show-overflow-tooltip="true" sortable align="center"></el-table-column>
              <el-table-column prop="updateTime" label="结束时间" width="160" :show-overflow-tooltip="true" sortable align="center"></el-table-column>
              <el-table-column
                prop="runDuration"
                label="运行时长"
                width="140"
                :show-overflow-tooltip="true"
                sortable
                align="center"
              ></el-table-column>
              <el-table-column prop="attachmentList" width="120" label="附件" align="center">
                <template #default="scope">
                  <el-popover v-if="scope.row.attachmentList && scope.row.attachmentList.length > 0" placement="right" :width="310" trigger="click">
                    <template #reference>
                      <el-button type="primary" style="margin-right: 16px">附件</el-button>
                    </template>
                    <el-table border :data="scope.row.attachmentList">
                      <el-table-column prop="originalName" width="202" :show-overflow-tooltip="true" label="附件名称"></el-table-column>
                      <el-table-column prop="name" width="80" align="center" :show-overflow-tooltip="true" label="操作">
                        <template #default="tool">
                          <el-button type="text" @click="handleDownload(tool.row.ossId)">下载</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-popover>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { flowImage } from '@/api/workflow/instance';
import { propTypes } from '@/utils/propTypes';
import { listByIds } from '@/api/system/oss';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_task_status } = toRefs<any>(proxy?.useDict('wf_task_status'));
const props = defineProps({
  width: propTypes.string.def('80%'),
  height: propTypes.string.def('100%')
});
const loading = ref(false);
const visible = ref(false);
const historyList = ref<Array<any>>([]);
const tabActiveName = ref('image');
const imgUrl = ref('');

//初始化查询审批记录
const init = async (businessId: string | number) => {
  visible.value = true;
  loading.value = true;
  tabActiveName.value = 'image';
  historyList.value = [];
  flowImage(businessId).then((resp) => {
    if (resp.data) {
      historyList.value = resp.data.list;
      imgUrl.value = 'data:image/gif;base64,' + resp.data.image;
      if (historyList.value.length > 0) {
        historyList.value.forEach((item) => {
          if (item.ext) {
            getIds(item.ext).then((res) => {
              item.attachmentList = res.data;
            });
          } else {
            item.attachmentList = [];
          }
        });
      }
      loading.value = false;
    }
  });
};
const getIds = async (ids: string | number) => {
  const res = await listByIds(ids);
  return res;
};

/** 下载按钮操作 */
const handleDownload = (ossId: string) => {
  proxy?.$download.oss(ossId);
};

const imageWrapperRef = ref<HTMLElement | null>(null);
const scale = ref(1); // 初始缩放比例
const maxScale = 3; // 最大缩放比例
const minScale = 0.5; // 最小缩放比例

let isDragging = false;
let startX = 0;
let startY = 0;
let currentTranslateX = 0;
let currentTranslateY = 0;

const handleMouseWheel = (event: WheelEvent) => {
  event.preventDefault();
  let newScale = scale.value - event.deltaY / 1000;
  newScale = Math.max(minScale, Math.min(newScale, maxScale));
  if (newScale !== scale.value) {
    scale.value = newScale;
    resetDragPosition(); // 重置拖拽位置，使图片居中
  }
};

const handleMouseDown = (event: MouseEvent) => {
  if (scale.value > 1) {
    event.preventDefault(); // 阻止默认行为，防止拖拽
    isDragging = true;
    startX = event.clientX;
    startY = event.clientY;
  }
};

const handleMouseMove = (event: MouseEvent) => {
  if (!isDragging || !imageWrapperRef.value) return;

  const deltaX = event.clientX - startX;
  const deltaY = event.clientY - startY;
  startX = event.clientX;
  startY = event.clientY;

  currentTranslateX += deltaX;
  currentTranslateY += deltaY;

  // 边界检测，防止图片被拖出容器
  const bounds = getBounds();
  if (currentTranslateX > bounds.maxTranslateX) {
    currentTranslateX = bounds.maxTranslateX;
  } else if (currentTranslateX < bounds.minTranslateX) {
    currentTranslateX = bounds.minTranslateX;
  }

  if (currentTranslateY > bounds.maxTranslateY) {
    currentTranslateY = bounds.maxTranslateY;
  } else if (currentTranslateY < bounds.minTranslateY) {
    currentTranslateY = bounds.minTranslateY;
  }

  applyTransform();
};

const handleMouseUp = () => {
  isDragging = false;
};

const handleMouseLeave = () => {
  isDragging = false;
};

const resetTransform = () => {
  scale.value = 1;
  currentTranslateX = 0;
  currentTranslateY = 0;
  applyTransform();
};

const resetDragPosition = () => {
  currentTranslateX = 0;
  currentTranslateY = 0;
  applyTransform();
};

const applyTransform = () => {
  if (imageWrapperRef.value) {
    imageWrapperRef.value.style.transform = `translate(${currentTranslateX}px, ${currentTranslateY}px) scale(${scale.value})`;
  }
};

const getBounds = () => {
  if (!imageWrapperRef.value) return { minTranslateX: 0, maxTranslateX: 0, minTranslateY: 0, maxTranslateY: 0 };

  const imgRect = imageWrapperRef.value.getBoundingClientRect();
  const containerRect = imageWrapperRef.value.parentElement?.getBoundingClientRect() ?? imgRect;

  const minTranslateX = (containerRect.width - imgRect.width * scale.value) / 2;
  const maxTranslateX = -(containerRect.width - imgRect.width * scale.value) / 2;
  const minTranslateY = (containerRect.height - imgRect.height * scale.value) / 2;
  const maxTranslateY = -(containerRect.height - imgRect.height * scale.value) / 2;

  return { minTranslateX, maxTranslateX, minTranslateY, maxTranslateY };
};

const transformStyle = computed(() => ({
  transition: isDragging ? 'none' : 'transform 0.2s ease'
}));

/**
 * 对外暴露子组件方法
 */
defineExpose({
  init
});
</script>
<style lang="scss" scoped>
.triangle {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  border-radius: 6px;
}

.triangle::after {
  content: ' ';
  position: absolute;
  top: 8em;
  right: 215px;
  border: 15px solid;
  border-color: transparent #fff transparent transparent;
}

.container {
  :deep(.el-dialog .el-dialog__body) {
    max-height: calc(100vh - 170px) !important;
    min-height: calc(100vh - 170px) !important;
  }
}

.image-wrapper {
  width: 100%;
  overflow: hidden;
  position: relative;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: center;
  user-select: none; /* 禁用文本选择 */
  cursor: grab; /* 设置初始鼠标指针为可拖动 */
}

.image-wrapper:active {
  cursor: grabbing; /* 当正在拖动时改变鼠标指针 */
}

.scalable-image {
  object-fit: contain;
  width: 100%;
  padding: 15px;
}
</style>
