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
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
            <el-form-item prop="id" label="节点 ID">
              <el-input v-model="formData.id" @change="idChange"></el-input>
            </el-form-item>
            <el-form-item prop="name" label="节点名称">
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
import useParseElement from '../hooks/useParseElement';
import usePanel from '../hooks/usePanel';
import ExecutionListener from './property/ExecutionListener.vue';
import type { ModdleElement } from 'bpmn';
import type { ParticipantPanel } from 'bpmnDesign';

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

const formData = ref(parseData<ParticipantPanel>());
const currentCollapseItem = ref(['1', '2']);
const formRules = ref<ElFormRules>({
  id: [{ required: true, message: '请输入', trigger: 'blur' }],
  name: [{ required: true, message: '请输入', trigger: 'blur' }]
});
</script>

<style lang="scss" scoped></style>
