<template>
  <div>
    <vxe-toolbar>
      <template #buttons>
        <el-button type="primary" link size="small" @click="insertEvent">新增</el-button>
        <el-button type="primary" link size="small" @click="removeSelectRowEvent">删除</el-button>
      </template>
    </vxe-toolbar>
    <vxe-table
      ref="tableRef"
      size="mini"
      height="100px"
      border
      show-overflow
      keep-source
      :data="tableData"
      :menu-config="menuConfig"
      @cell-dblclick="cellDBLClickEvent"
      @menu-click="contextMenuClickEvent"
    >
      <vxe-column type="checkbox" width="40"></vxe-column>
      <vxe-column type="seq" width="40"></vxe-column>
      <vxe-column field="event" title="事件" min-width="100px">
        <template #default="slotParams">
          <span>{{ eventSelect.find((e) => e.value === slotParams.row.event)?.label }}</span>
        </template>
      </vxe-column>
      <vxe-column field="type" title="类型" min-width="100px">
        <template #default="slotParams">
          <span>{{ typeSelect.find((e) => e.value === slotParams.row.type)?.label }}</span>
        </template>
      </vxe-column>
      <vxe-column field="className" title="Java 类名" min-width="100px"> </vxe-column>
    </vxe-table>

    <el-dialog
      v-model="formDialog.visible.value"
      :title="formDialog.title.value"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      append-to-body
    >
      <el-form ref="formRef" :model="formData" :rules="tableRules" label-width="100px">
        <el-form-item label="事件" prop="event">
          <el-select v-model="formData.event">
            <el-option v-for="item in eventSelect" :key="item.id" :value="item.value" :label="item.label"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <template #label>
            <span>
              类型
              <el-tooltip placement="top">
                <el-icon><QuestionFilled /></el-icon>
                <template #content>
                  类：示例 com.company.MyCustomListener，自定义类必须实现 org.flowable.engine.delegate.TaskListener 接口<br />
                  表达式：示例 ${myObject.callMethod(task, task.eventName)}<br />
                  委托表达式：示例 ${myListenerSpringBean} ，该 springBean 需要实现 org.flowable.engine.delegate.TaskListener 接口
                </template>
              </el-tooltip>
            </span>
          </template>
          <el-select v-model="formData.type">
            <el-option v-for="item in typeSelect" :key="item.id" :value="item.value" :label="item.label"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          :label="typeSelect.filter((e) => e.value === formData.type)[0] ? typeSelect.filter((e) => e.value === formData.type)[0]?.label : '表达式'"
          prop="className"
        >
          <el-input v-model="formData.className" type="text"></el-input>
        </el-form-item>
      </el-form>
      <el-tabs type="border-card">
        <el-tab-pane label="参数">
          <ListenerParam ref="listenerParamRef" :table-data="formData.params" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formDialog.closeDialog">取 消</el-button>
          <el-button type="primary" @click="submitEvent">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import ListenerParam from './ListenerParam.vue';
import { VxeTableEvents, VxeTableInstance, VxeTablePropTypes } from 'vxe-table';
import type { ExecutionListenerVO } from 'bpmnDesign';
import type { Moddle, Modeler, ModdleElement } from 'bpmn';

import usePanel from '../../hooks/usePanel';
import useDialog from '@/hooks/useDialog';
import useModelerStore from '@/store/modules/modeler';

const emit = defineEmits(['close']);
interface PropType {
  element: ModdleElement;
}
const props = withDefaults(defineProps<PropType>(), {});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const selectRow = ref<ExecutionListenerVO | null>();
const formDialog = useDialog({
  title: selectRow.value ? '编辑&保存' : '新增&保存'
});

const { showConfig, elementType, updateProperties } = usePanel({
  element: toRaw(props.element)
});
const { getModdle } = useModelerStore();
const moddle = getModdle();

const listenerParamRef = ref<InstanceType<typeof ListenerParam>>();
const tableRef = ref<VxeTableInstance<ExecutionListenerVO>>();
const formRef = ref<ElFormInstance>();

const initData: ExecutionListenerVO = {
  event: '',
  type: '',
  className: '',
  params: []
};
const formData = ref<ExecutionListenerVO>({ ...initData });
const tableData = ref<ExecutionListenerVO[]>([]);
const tableRules = ref<ElFormRules>({
  event: [{ required: true, message: '请选择', trigger: 'blur' }],
  type: [{ required: true, message: '请选择', trigger: 'blur' }],
  className: [{ required: true, message: '请输入', trigger: 'blur' }]
});

const submitEvent = async () => {
  const error = await listenerParamRef.value.validate();
  await formRef.value.validate((validate) => {
    if (validate && !error) {
      const $table = tableRef.value;
      if ($table) {
        formData.value.params = listenerParamRef.value.getTableData();
        if (selectRow.value) {
          Object.assign(selectRow.value, formData.value);
        } else {
          $table.insertAt({ ...formData.value }, -1);
        }
        updateElement();
        formDialog.closeDialog();
      }
    }
  });
};

const removeSelectRowEvent = async () => {
  const $table = tableRef.value;
  if ($table) {
    const selectCount = $table.getCheckboxRecords().length;
    if (selectCount === 0) {
      proxy?.$modal.msgWarning('请选择行');
    } else {
      await $table.removeCheckboxRow();
      updateElement();
    }
  }
};
const insertEvent = async () => {
  Object.assign(formData.value, initData);
  selectRow.value = null;
  formDialog.openDialog();
};

const editEvent = (row: ExecutionListenerVO) => {
  Object.assign(formData.value, row);
  selectRow.value = row;
  formDialog.openDialog();
};

const removeEvent = async (row: ExecutionListenerVO) => {
  await proxy?.$modal.confirm('您确定要删除该数据?');
  const $table = tableRef.value;
  if ($table) {
    await $table.remove(row);
    updateElement();
  }
};
const updateElement = () => {
  const $table = tableRef.value;
  const data = $table.getTableData().fullData;
  if (data.length) {
    let extensionElements = props.element.businessObject.get('extensionElements');
    if (!extensionElements) {
      extensionElements = moddle.create('bpmn:ExtensionElements');
    }
    // 清除旧值
    extensionElements.values = extensionElements.values?.filter((item) => item.$type !== 'flowable:ExecutionListener') ?? [];
    data.forEach((item) => {
      const executionListener = moddle.create('flowable:ExecutionListener');
      executionListener['event'] = item.event;
      executionListener[item.type] = item.className;
      if (item.params && item.params.length) {
        item.params.forEach((field) => {
          const fieldElement = moddle.create('flowable:Field');
          fieldElement['name'] = field.name;
          fieldElement[field.type] = field.value;
          executionListener.get('fields').push(fieldElement);
        });
      }
      extensionElements.get('values').push(executionListener);
    });
    updateProperties({ extensionElements: extensionElements });
  } else {
    const extensionElements = props.element.businessObject[`extensionElements`];
    if (extensionElements) {
      extensionElements.values = extensionElements.values?.filter((item) => item.$type !== 'flowable:ExecutionListener') ?? [];
    }
  }
};

const cellDBLClickEvent: VxeTableEvents.CellDblclick<ExecutionListenerVO> = ({ row }) => {
  editEvent(row);
};

const menuConfig = reactive<VxeTablePropTypes.MenuConfig<ExecutionListenerVO>>({
  body: {
    options: [
      [
        { code: 'edit', name: '编辑', prefixIcon: 'vxe-icon-edit', disabled: false },
        { code: 'remove', name: '删除', prefixIcon: 'vxe-icon-delete', disabled: false }
      ]
    ]
  },
  visibleMethod({ options, column }) {
    const isDisabled = !column;
    options.forEach((list) => {
      list.forEach((item) => {
        item.disabled = isDisabled;
      });
    });
    return true;
  }
});
const contextMenuClickEvent: VxeTableEvents.MenuClick<ExecutionListenerVO> = ({ menu, row, column }) => {
  const $table = tableRef.value;
  if ($table) {
    switch (menu.code) {
      case 'edit':
        editEvent(row);
        break;
      case 'remove':
        removeEvent(row);
        break;
    }
  }
};

const initTableData = () => {
  tableData.value =
    props.element.businessObject.extensionElements?.values
      .filter((item) => item.$type === 'flowable:ExecutionListener')
      .map((item) => {
        let type;
        if ('class' in item) type = 'class';
        if ('expression' in item) type = 'expression';
        if ('delegateExpression' in item) type = 'delegateExpression';
        return {
          event: item.event,
          type: type,
          className: item[type],
          params:
            item.fields?.map((field) => {
              let fieldType;
              if ('stringValue' in field) fieldType = 'stringValue';
              if ('expression' in field) fieldType = 'expression';
              return {
                name: field.name,
                type: fieldType,
                value: field[fieldType]
              };
            }) ?? []
        };
      }) ?? [];
};

onMounted(() => {
  initTableData();
});

const typeSelect = [
  { id: '742fdeb7-23b4-416b-ac66-cd4ec8b901b7', label: '类', value: 'class' },
  { id: '660c9c46-8fae-4bae-91a0-0335420019dc', label: '表达式', value: 'expression' },
  { id: '4b8135ab-6bc3-4a0f-80be-22f58bc6c5fd', label: '委托表达式', value: 'delegateExpression' }
];
const eventSelect = [
  { id: 'e6e0a51a-2d5d-4dc4-b847-b5c14f43a6ab', label: '开始', value: 'start' },
  { id: '6da97c1e-15fc-4445-8943-75d09f49778e', label: '结束', value: 'end' },
  { id: '6a2cbcec-e026-4f11-bef7-fff0b5c871e2', label: '启用', value: 'take' }
];
</script>

<style scoped lang="scss">
.el-badge {
  :deep(.el-badge__content) {
    top: 10px;
  }
}
</style>
