<template>
  <el-dialog v-model="visible" draggable title="审批记录" :width="props.width" :height="props.height" append-to-body
    :close-on-click-modal="false">
    <div v-loading="loading">
      <div style="width: 100%;height: 300px;overflow: auto;position: relative;">
        <div v-for="(graphic, index) in graphicInfoVos" :key="index" :style="{
          position: 'absolute',
          left: `${graphic.x}px`,
          top: `${graphic.y}px`,
          width: `${graphic.width}px`,
          height: `${graphic.height}px`,
          cursor: 'pointer',
          zIndex: 99
        }" @mouseover="handleMouseOver(graphic)" @mouseleave="handleMouseLeave()"></div>
        <!-- 弹出的 div 元素 -->
        <div v-show="popupVisible" class="triangle" :style="{
          position: 'absolute',
          left: `${graphicX}px`,
          top: `${graphicY}px`,
          backgroundColor: '#fff',
          padding: '10px',
          zIndex: 100
        }">
          <p>审批人员: {{ nodeInfo.nickName }}</p>
          <p>节点状态：{{ nodeInfo.status }}</p>
          <p>开始时间：{{ nodeInfo.startTime }}</p>
          <p>结束时间：{{ nodeInfo.endTime }}</p>
          <p>审批耗时：{{ nodeInfo.runDuration }}</p>
        </div>
        <el-image :src="src" />
      </div>
      <div>
        <el-table :data="historyList" style="width: 100%" border fit max-height="570">
          <el-table-column label="流程审批历史记录" align="center">
            <el-table-column type="index" label="序号" align="center" width="50"></el-table-column>
            <el-table-column prop="name" label="任务名称" sortable align="center"></el-table-column>
            <el-table-column prop="nickName" label="办理人" sortable align="center"></el-table-column>
            <el-table-column label="状态" sortable align="center">
              <template #default="scope">
                <el-tag type="success">{{ scope.row.statusName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="审批意见" sortable align="center"></el-table-column>
            <el-table-column prop="attachmentList" label="附件" sortable align="center">
              <template #default="scope">
                <el-popover placement="right"  v-if="scope.row.attachmentList && scope.row.attachmentList.length > 0" :width="310" trigger="click">
                    <template #reference>
                      <el-button style="margin-right: 16px">附件</el-button>
                    </template>
                    <el-table border :data="scope.row.attachmentList">
                      <el-table-column prop="name" width="202" :show-overflow-tooltip="true" label="附件名称"></el-table-column>
                      <el-table-column prop="name" width="80" align="center" :show-overflow-tooltip="true" label="操作">
                        <template #default="tool">
                          <el-button type="text" @click="handleDownload(tool.row.contentId)">下载</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                    
                  </el-popover>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" sortable align="center"></el-table-column>
            <el-table-column prop="endTime" label="结束时间" sortable align="center"></el-table-column>
            <el-table-column prop="runDuration" label="运行时长" sortable align="center"></el-table-column>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-dialog>
</template>
<script lang="ts" setup>
import { getHistoryProcessImage, getHistoryRecord } from '@/api/workflow/processInstance';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
import { ref } from 'vue';
const props = defineProps({
  width: {
    type: String,
    default: '70%'
  },
  height: {
    type: String,
    default: '100%'
  }
});
const loading = ref(false);
const src = ref('');
const visible = ref(false);
const historyList = ref<Array<any>>([]);
const deleteReason = ref<string>('');
const graphicInfoVos = ref<Array<any>>([]);
const nodeListInfo = ref<Array<any>>([]);
const popupVisible = ref(false);
const nodeInfo = ref<any>({});
const graphicX = ref<number | string>(0);
const graphicY = ref<number | string>(0);
//初始化查询审批记录
const init = async (processInstanceId: string) => {
  visible.value = true;
  loading.value = true;
  historyList.value = [];
  graphicInfoVos.value = [];
  getHistoryProcessImage(processInstanceId).then((res) => {
    src.value = 'data:image/png;base64,' + res.data
  });
  getHistoryRecord(processInstanceId).then((response) => {
    historyList.value = response.data.historyRecordList;
    graphicInfoVos.value = response.data.graphicInfoVos;
    nodeListInfo.value = response.data.nodeListInfo;
    deleteReason.value = response.data.deleteReason;
    loading.value = false;
  });
};
//悬浮事件
const handleMouseOver = async (graphic: any) => {
  graphicX.value = graphic.x + graphic.width + 10;
  graphicY.value = graphic.y - graphic.height + -10;
  nodeInfo.value = {};
  if (nodeListInfo.value && nodeListInfo.value.length > 0) {
    let info = nodeListInfo.value.find((e: any) => e.taskDefinitionKey == graphic.nodeId);
    if (info) {
      nodeInfo.value = {
        nickName: info.nickName,
        status: info.status,
        startTime: info.startTime,
        endTime: info.endTime,
        runDuration: info.runDuration
      };
      popupVisible.value = true;
    }
  }
};
//关闭
const handleMouseLeave = async () => {
  popupVisible.value = false;
};

/** 下载按钮操作 */
const handleDownload = (ossId: string) => {
  proxy?.$download.oss(ossId);
};
/**
 * 对外暴露子组件方法
 */
defineExpose({
  init
});
</script>
<style scoped>
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
</style>
