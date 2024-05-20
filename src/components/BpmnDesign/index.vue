<template>
  <div class="design">
    <el-dialog v-model="visible" width="100%" fullscreen :title="title">
      <div class="modeler">
        <bpmn-design ref="bpmnDesignRef" @save-call-back="saveCallBack"></bpmn-design>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup name="Design">
import { getInfo, editModelXml } from '@/api/workflow/model';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

import { ModelForm } from '@/api/workflow/model/types';
import BpmnDesign from '@/bpmn/index.vue';
import useDialog from '@/hooks/useDialog';
const bpmnDesignRef = ref<InstanceType<typeof BpmnDesign>>();
const modelForm = ref<ModelForm>();
const emit = defineEmits(['closeCallBack']);
const { visible, title } = useDialog({
  title: '编辑流程'
});
const modelId = ref('');
const open = async (id) => {
  visible.value = true;
  modelId.value = id;
  const { data } = await getInfo(id);
  modelForm.value = data;
  bpmnDesignRef.value.initDiagram(modelForm.value.xml);
};
//保存模型
const saveCallBack = async (data) => {
  await proxy?.$modal.confirm('是否确认保存？');
  data.loading.value = true;
  modelForm.value.id = modelId.value;
  modelForm.value.xml = data.xml;
  modelForm.value.svg = data.svg;
  modelForm.value.key = data.key;
  modelForm.value.name = data.name;
  editModelXml(modelForm.value).then((res) => {
    if (res.code === 200) {
      visible.value = false;
      proxy?.$modal.msgSuccess('保存成功');
      emit('closeCallBack', data);
    }
  });
  data.loading.value = false;
};

/**
 * 对外暴露子组件方法
 */
defineExpose({
  open
});
</script>

<style lang="scss" scoped>
.design {
  :deep(.el-dialog .el-dialog__body) {
    max-height: 100% !important;
    min-height: calc(100vh - 80px);
    padding: 10px 0 10px 0 !important;
  }
  :deep(.el-dialog__header) {
    padding: 0 0 5px 0 !important;
  }
}
</style>
