<template>
  <div>
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
      <el-form-item prop="id" label="节点 ID">
        <el-input v-model="formData.id" @change="idChange"> </el-input>
      </el-form-item>
      <el-form-item prop="name" label="节点名称">
        <el-input v-model="formData.name" @change="nameChange"> </el-input>
      </el-form-item>
      <el-form-item label="执行监听器" style="margin-bottom: 0"> </el-form-item>
      <ExecutionListener :element="element"></ExecutionListener>
    </el-form>
  </div>
</template>
<script setup lang="ts">
import useParseElement from '@/components/BpmnDesign/hooks/useParseElement';
import usePanel from '@/components/BpmnDesign/hooks/usePanel';
import { ModdleElement } from 'bpmn';
import { StartEndPanel } from 'bpmnDesign';

interface PropType {
  element: ModdleElement;
}
const props = withDefaults(defineProps<PropType>(), {});
const { nameChange, idChange } = usePanel({
  element: toRaw(props.element)
});
const { parseData } = useParseElement({
  element: toRaw(props.element)
});

const formData = ref(parseData<StartEndPanel>());

const formRules = ref<ElFormRules>({
  id: [{ required: true, message: '请输入', trigger: 'blur' }],
  name: [{ required: true, message: '请输入', trigger: 'blur' }]
});
</script>

<style lang="scss" scoped></style>
