<template>
  <vxe-toolbar>
    <template #buttons>
      <el-button icon="Plus" @click="insertRow">新增</el-button>
    </template>
  </vxe-toolbar>
  <vxe-table
    ref="tableRef"
    :height="height"
    border
    show-overflow
    keep-source
    :data="tableData"
    :edit-rules="tableRules"
    :edit-config="{ trigger: 'click', mode: 'row', showStatus: true }"
  >
    <vxe-column type="seq" width="40"></vxe-column>
    <vxe-column field="type" title="类型" :edit-render="{}">
      <template #default="slotParams">
        <span>{{ typeSelect.find((e) => e.value === slotParams.row.type)?.label }}</span>
      </template>
      <template #edit="slotParams">
        <vxe-select v-model="slotParams.row.type">
          <vxe-option v-for="item in typeSelect" :key="item.id" :value="item.value" :label="item.label"></vxe-option>
        </vxe-select>
      </template>
    </vxe-column>
    <vxe-column field="name" title="名称" :edit-render="{}">
      <template #edit="slotParams">
        <vxe-input v-model="slotParams.row.name" type="text"></vxe-input>
      </template>
    </vxe-column>
    <vxe-column field="value" title="值" :edit-render="{}">
      <template #edit="slotParams">
        <vxe-input v-model="slotParams.row.value" type="text"></vxe-input>
      </template>
    </vxe-column>
    <vxe-column title="操作" width="100" show-overflow align="center">
      <template #default="slotParams">
        <el-tooltip content="删除" placement="top">
          <el-button link type="danger" icon="Delete" @click="removeRow(slotParams.row)"></el-button>
        </el-tooltip>
      </template>
    </vxe-column>
  </vxe-table>
</template>

<script setup lang="ts">
import { VXETable, VxeTableInstance, VxeTablePropTypes } from 'vxe-table';
import type { ParamVO } from 'bpmnDesign';
import useDialog from '@/hooks/useDialog';

interface PropType {
  height?: string;
  tableData?: ParamVO[];
}

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const props = withDefaults(defineProps<PropType>(), {
  height: '200px',
  tableData: () => []
});

const tableRules = ref<VxeTablePropTypes.EditRules>({
  type: [{ required: true, message: '请选择', trigger: 'blur' }],
  name: [{ required: true, message: '请输入', trigger: 'blur' }],
  value: [{ required: true, message: '请输入', trigger: 'blur' }]
});

const { title, visible, openDialog, closeDialog } = useDialog({
  title: '监听器参数'
});
const typeSelect = [
  { id: '742fdeb7-23b4-416b-ac66-cd4ec8b901b7', label: '字符串', value: 'stringValue' },
  { id: '660c9c46-8fae-4bae-91a0-0335420019dc', label: '表达式', value: 'expression' }
];

const tableRef = ref<VxeTableInstance<ParamVO>>();

const getTableData = () => {
  const $table = tableRef.value;
  if ($table) {
    return $table.getTableData().fullData;
  }
  return [];
};

const insertRow = async () => {
  const $table = tableRef.value;
  if ($table) {
    const { row: newRow } = await $table.insertAt({}, -1);
    // 插入一条数据并触发校验
    await $table.validate(newRow);
  }
};

const removeRow = async (row: ParamVO) => {
  await proxy?.$modal.confirm('您确定要删除该数据?');
  const $table = tableRef.value;
  if ($table) {
    await $table.remove(row);
  }
};

const validate = async () => {
  const $table = tableRef.value;
  if ($table) {
    return await $table.validate(true);
  }
};

defineExpose({
  closeDialog,
  openDialog,
  validate,
  getTableData
});
</script>

<style scoped lang="scss"></style>
