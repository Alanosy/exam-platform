<template>
  <el-dialog v-model="data.visible" title="预览" width="70%" append-to-body>
    <div v-if="data.type === 'png' && data.xmlStr" style="align: center">
      <BpmnViewer ref="bpmnViewerRef"></BpmnViewer>
    </div>
    <div v-if="data.type === 'xml'" class="xml-data">
      <div v-for="(xml, index) in data.url" :key="index">
        <pre class="font">{{ xml }}</pre>
      </div>
    </div>
    <template #footer>
      <span v-if="data.type === 'xml'" class="dialog-footer"> </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import BpmnViewer from '@/components/BpmnView/index.vue';

const data = reactive({
  visible: false,
  url: new Array<string>(),
  type: '',
  xmlStr: ''
});

const bpmnViewerRef = ref<InstanceType<typeof BpmnViewer>>();
//打开
const openDialog = (url: string[], type: string) => {
  data.visible = true;
  data.url = url;
  data.type = type;
  /** 流程图 */
  if (type === 'png') {
    data.xmlStr = url.join('\n');
    /** 必须放在nextTick 否则第一次打开为空 */
    nextTick(() => {
      bpmnViewerRef.value?.initXml(data.xmlStr);
    });
  }
};
/**
 * 对外暴露子组件方法
 */
defineExpose({
  openDialog
});
</script>
<style>
.xml-data {
  background-color: #2b2b2b;
  border-radius: 5px;
  color: #c6c6c6;
  word-break: break-all;
  overflow-y: scroll;
  overflow-x: hidden;
  box-sizing: border-box;
  padding: 8px 0px;
  height: 500px;
  width: inherit;
  line-height: 1px;
  overflow: auto;
}
.font {
  font-family: '幼圆';
  font-weight: 500;
}
</style>
