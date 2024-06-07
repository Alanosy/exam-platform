<template>
  <div class="container">
    <el-dialog v-model="visible" draggable title="审批记录" :width="props.width" :height="props.height" :close-on-click-modal="false">
      <el-tabs v-model="tabActiveName" class="demo-tabs">
        <el-tab-pane label="流程图" name="bpmn">
          <BpmnView ref="bpmnViewRef"></BpmnView>
        </el-tab-pane>
        <el-tab-pane v-loading="loading" label="审批信息" name="info">
          <div>
            <el-table :data="historyList" style="width: 100%" border fit>
              <el-table-column type="index" label="序号" align="center" width="60"></el-table-column>
              <el-table-column prop="name" label="任务名称" sortable align="center"></el-table-column>
              <el-table-column prop="nickName" :show-overflow-tooltip="true" label="办理人" sortable align="center">
                <template #default="scope">
                  <el-tag type="success">{{ scope.row.nickName || '无' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" sortable align="center">
                <template #default="scope">
                  <el-tag type="success">{{ scope.row.statusName }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="comment" label="审批意见" sortable align="center"></el-table-column>
              <el-table-column prop="startTime" label="开始时间" sortable align="center"></el-table-column>
              <el-table-column prop="endTime" label="结束时间" sortable align="center"></el-table-column>
              <el-table-column prop="runDuration" label="运行时长" sortable align="center"></el-table-column>
              <el-table-column prop="attachmentList" label="附件" sortable align="center">
                <template #default="scope">
                  <el-popover v-if="scope.row.attachmentList && scope.row.attachmentList.length > 0" placement="right" :width="310" trigger="click">
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
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
import BpmnView from '@/components/BpmnView/index.vue';
import processApi from '@/api/workflow/processInstance';
import { propTypes } from '@/utils/propTypes';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const props = defineProps({
  width: propTypes.string.def('70%'),
  height: propTypes.string.def('100%')
});
const loading = ref(false);
const visible = ref(false);
const historyList = ref<Array<any>>([]);
const tabActiveName = ref('bpmn');

const bpmnViewRef = ref<BpmnView>();

//初始化查询审批记录
const init = async (businessKey: string | number) => {
  visible.value = true;
  loading.value = true;
  tabActiveName.value = 'bpmn';
  historyList.value = [];
  processApi.getHistoryRecord(businessKey).then((resp) => {
    historyList.value = resp.data;
    loading.value = false;
  });
  await nextTick(() => {
    bpmnViewRef.value.init(businessKey);
  });
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
</style>
