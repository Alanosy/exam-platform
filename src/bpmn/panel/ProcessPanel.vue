<template>
  <div>
    <el-collapse v-model="currentCollapseItem">
      <el-collapse-item name="1">
        <template #title>
          <div class="collapse__title">
            <el-icon>
              <InfoFilled />
            </el-icon>
            常规
          </div>
        </template>
        <div>
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
            <el-form-item label="流程标识" prop="id">
              <el-input v-model="formData.id" @change="idChange"></el-input>
            </el-form-item>
            <el-form-item label="流程名称" prop="name">
              <el-input v-model="formData.name" @change="nameChange"></el-input>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>

      <el-collapse-item name="2">
        <template #title>
          <div class="collapse__title">
            <el-icon>
              <BellFilled />
            </el-icon>
            执行监听器
          </div>
        </template>
        <div>
          <ExecutionListener :element="element"></ExecutionListener>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import ExecutionListener from './property/ExecutionListener.vue';
import useParseElement from '../hooks/useParseElement';
import usePanel from '../hooks/usePanel';
import type { Modeler, ModdleElement } from 'bpmn';
import type { ProcessPanel } from 'bpmnDesign';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

interface PropType {
  element: ModdleElement;
}
const props = withDefaults(defineProps<PropType>(), {});

const { parseData } = useParseElement({
  element: toRaw(props.element)
});
const { idChange, nameChange } = usePanel({
  element: toRaw(props.element)
});
const currentCollapseItem = ref(['1', '2']);
const formData = ref<ProcessPanel>(parseData<ProcessPanel>());

const formRules = ref<ElFormRules>({
  id: [{ required: true, message: '请输入', trigger: 'blur' }],
  name: [{ required: true, message: '请输入', trigger: 'blur' }]
});
</script>

<style scoped lang="scss"></style>
