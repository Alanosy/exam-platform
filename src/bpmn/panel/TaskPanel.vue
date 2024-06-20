<template>
  <div>
    <el-form ref="formRef" size="default" :model="formData" :rules="formRules" label-width="100px">
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
            <el-form-item prop="id" label="节点 ID">
              <el-input v-model="formData.id" @change="idChange"> </el-input>
            </el-form-item>
            <el-form-item prop="name" label="节点名称">
              <el-input v-model="formData.name" @change="nameChange"> </el-input>
            </el-form-item>
            <el-form-item v-if="showConfig.skipExpression" prop="skipExpression" label="跳过表达式">
              <el-input v-model="formData.skipExpression" @change="skipExpressionChange"> </el-input>
            </el-form-item>
            <el-form-item v-loading="formManageListLoading" prop="formKey" label="表单地址">
              <el-select v-model="formData.formKey" clearable filterable placeholder="请选择表单" style="width: 260px" @change="formKeyChange">
                <el-option
                  v-for="item in formManageList"
                  :key="item.id"
                  :label="item.formTypeName + ':' + item.formName"
                  :value="item.formType + ':' + item.id"
                />
              </el-select>
            </el-form-item>
          </div>
        </el-collapse-item>
        <el-collapse-item name="2">
          <template #title>
            <div class="collapse__title">
              <el-icon>
                <Checked />
              </el-icon>
              任务
            </div>
          </template>
          <div>
            <el-form-item v-if="showConfig.async" prop="sync" label="是否异步">
              <el-switch v-model="formData.async" inline-prompt active-text="是" inactive-text="否" @change="syncChange" />
            </el-form-item>

            <el-tabs tab-position="left" class="demo-tabs">
              <el-tab-pane label="身份存储">
                <el-form-item label="分配人员">
                  <el-input v-model="formData.assignee" @blur="blurAssignee(formData.assignee)">
                    <template #append>
                      <el-button icon="Search" type="primary" @click="openSingleUserSelect" />
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="候选人员">
                  <el-badge :value="selectUserLength" :max="99">
                    <el-button size="small" type="primary" @click="openUserSelect">选择人员</el-button>
                  </el-badge>
                </el-form-item>
                <el-form-item label="候选组">
                  <el-badge :value="selectRoleLength" :max="99">
                    <el-button size="small" type="primary" @click="openRoleSelect">选择组</el-button>
                  </el-badge>
                </el-form-item>
              </el-tab-pane>

              <!-- <el-tab-pane label="固定值">
                <el-form-item prop="auditUserType" label="分配类型">
                  <el-select v-model="formData.allocationType">
                    <el-option v-for="item in AllocationTypeSelect" :key="item.id" :value="item.value" :label="item.label"> </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item v-if="formData.allocationType === AllocationTypeEnum.USER" label="分配人员">
                  <el-input v-model="formData.assignee">
                    <template #append>
                      <el-button icon="Search" type="primary" @click="openSingleUserSelect" />
                    </template>
                  </el-input>
                </el-form-item>
                <div v-if="formData.allocationType === AllocationTypeEnum.CANDIDATE">
                  <el-form-item label="候选人员">
                    <el-badge :value="selectUserLength" :max="99">
                      <el-button size="small" type="primary" @click="openUserSelect">选择人员</el-button>
                    </el-badge>
                  </el-form-item>
                  <el-form-item label="候选组">
                    <el-badge :value="selectRoleLength" :max="99">
                      <el-button size="small" type="primary" @click="openRoleSelect">选择组</el-button>
                    </el-badge>
                  </el-form-item>
                </div>
                <el-form-item v-if="formData.allocationType === AllocationTypeEnum.SPECIFY && showConfig.specifyDesc" style="">
                  <el-radio-group v-model="formData.specifyDesc" class="ml-4">
                    <el-radio v-for="item in SpecifyDesc" :key="item.id" :value="item.value" size="large">{{ item.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-tab-pane> -->
            </el-tabs>

            <el-form-item v-if="showConfig.dueDate" prop="dueDate" label="到期时间">
              <el-input v-model="formData.dueDate" clearable @change="dueDateChange" @click="openDueDate">
                <template #append>
                  <el-button icon="Search" type="primary" @click="openDueDate" />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item v-if="showConfig.priority" prop="priority" label="优先级">
              <el-input-number v-model="formData.priority" :min="0" @change="priorityChange"> </el-input-number>
            </el-form-item>
          </div>
        </el-collapse-item>
        <el-collapse-item name="3">
          <template #title>
            <div class="collapse__title">
              <el-icon>
                <HelpFilled />
              </el-icon>
              多实例
            </div>
          </template>
          <div>
            <el-form-item label="多实例类型">
              <el-select v-model="formData.multiInstanceType" @change="multiInstanceTypeChange">
                <el-option v-for="item in constant.MultiInstanceType" :key="item.id" :value="item.value" :label="item.label"> </el-option>
              </el-select>
            </el-form-item>

            <div v-if="formData.multiInstanceType !== MultiInstanceTypeEnum.NONE">
              <el-form-item label="集合">
                <template #label>
                  <span>
                    集合
                    <el-tooltip placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                      <template #content>
                        属性会作为表达式进行解析。如果表达式解析为字符串而不是一个集合，<br />
                        不论是因为本身配置的就是静态字符串值，还是表达式计算结果为字符串，<br />
                        这个字符串都会被当做变量名，并从流程变量中用于获取实际的集合。
                      </template>
                    </el-tooltip>
                  </span>
                </template>
                <el-input v-model="formData.collection" @change="collectionChange"></el-input>
              </el-form-item>
              <el-form-item label="元素变量">
                <template #label>
                  <span>
                    元素变量
                    <el-tooltip placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                      <template #content>
                        每创建一个用户任务前，先以该元素变量为label，集合中的一项为value，<br />
                        创建（局部）流程变量，该局部流程变量被用于指派用户任务。<br />
                        一般来说，该字符串应与指定人员变量相同。
                      </template>
                    </el-tooltip>
                  </span>
                </template>
                <el-input v-model="formData.elementVariable" @change="elementVariableChange"> </el-input>
              </el-form-item>
              <el-form-item label="完成条件">
                <template #label>
                  <span>
                    完成条件
                    <el-tooltip placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                      <template #content>
                        多实例活动在所有实例都完成时结束，然而也可以指定一个表达式，在每个实例<br />
                        结束时进行计算。当表达式计算为true时，将销毁所有剩余的实例，并结束多实例<br />
                        活动，继续执行流程。例如 ${nrOfCompletedInstances/nrOfInstances >= 0.6 }，<br />
                        表示当任务完成60%时，该节点就算完成
                      </template>
                    </el-tooltip>
                  </span>
                </template>
                <el-input v-model="formData.completionCondition" @change="completionConditionChange"> </el-input>
              </el-form-item>
            </div>
          </div>
        </el-collapse-item>
        <el-collapse-item v-if="showConfig.taskListener" name="4">
          <template #title>
            <div class="collapse__title">
              <el-icon>
                <BellFilled />
              </el-icon>
              任务监听器
            </div>
          </template>
          <div>
            <TaskListener v-if="showConfig.taskListener" :element="element"></TaskListener>
          </div>
        </el-collapse-item>
        <el-collapse-item v-if="showConfig.executionListener" name="5">
          <template #title>
            <div class="collapse__title">
              <el-icon>
                <BellFilled />
              </el-icon>
              执行监听器
            </div>
          </template>
          <div>
            <ExecutionListener v-if="showConfig.executionListener" :element="element"></ExecutionListener>
          </div>
        </el-collapse-item>

        <el-form-item v-if="showConfig.isForCompensation" prop="isForCompensation" label="是否为补偿">
          <el-switch v-model="formData.isForCompensation" inline-prompt active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item v-if="showConfig.triggerServiceTask" prop="triggerServiceTask" label="服务任务可触发">
          <el-switch v-model="formData.triggerServiceTask" inline-prompt active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item v-if="showConfig.autoStoreVariables" prop="autoStoreVariables" label="自动存储变量">
          <el-switch v-model="formData.autoStoreVariables" inline-prompt active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item v-if="showConfig.ruleVariablesInput" prop="skipExpression" label="输入变量">
          <el-input v-model="formData.ruleVariablesInput"> </el-input>
        </el-form-item>
        <el-form-item v-if="showConfig.exclude" prop="exclude" label="排除">
          <el-switch v-model="formData.exclude" inline-prompt active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item v-if="showConfig.class" prop="class" label="类">
          <el-input v-model="formData.class"> </el-input>
        </el-form-item>
      </el-collapse>
    </el-form>
    <UserSelect ref="userSelectRef" :data="formData.candidateUsers" @confirm-call-back="userSelectCallBack"></UserSelect>
    <UserSelect ref="singleUserSelectRef" :data="formData.assignee" :multiple="false" @confirm-call-back="singleUserSelectCallBack"></UserSelect>
    <RoleSelect ref="roleSelectRef" :data="formData.candidateGroups" @confirm-call-back="roleSelectCallBack"></RoleSelect>
    <DueDate ref="dueDateRef" v-model="formData.dueDate" :data="formData.dueDate" @confirm-call-back="dueDateCallBack"></DueDate>
  </div>
</template>
<script setup lang="ts">
import useParseElement from '../hooks/useParseElement';
import usePanel from '../hooks/usePanel';
import UserSelect from '@/components/UserSelect';
import RoleSelect from '@/components/RoleSelect';
import ExecutionListener from './property/ExecutionListener.vue';
import TaskListener from './property/TaskListener.vue';
import DueDate from './property/DueDate.vue';
import type { ModdleElement } from 'bpmn';
import type { TaskPanel } from 'bpmnDesign';
import { AllocationTypeEnum, MultiInstanceTypeEnum, SpecifyDescEnum } from '@/enums/bpmn/IndexEnums';
import { UserVO } from '@/api/system/user/types';
import { RoleVO } from '@/api/system/role/types';
import { selectListFormManage } from '@/api/workflow/formManage';
import { FormManageVO } from '@/api/workflow/formManage/types';
const formManageList = ref<FormManageVO[]>([]);
const formManageListLoading = ref(false);
interface PropType {
  element: ModdleElement;
}
const props = withDefaults(defineProps<PropType>(), {});
const { showConfig, nameChange, formKeyChange, idChange, updateProperties, getExtensionElements, createModdleElement, constant } = usePanel({
  element: toRaw(props.element)
});
const { parseData } = useParseElement({
  element: toRaw(props.element)
});

const initFormData = {
  id: '',
  name: '',
  dueDate: '',
  multiInstanceType: MultiInstanceTypeEnum.NONE,
  allocationType: AllocationTypeEnum.USER,
  specifyDesc: SpecifyDescEnum.SPECIFY_SINGLE
};
const formData = ref({ ...initFormData, ...parseData<TaskPanel>() });
const assignee = ref<Partial<UserVO>>({
  userName: ''
});
const currentCollapseItem = ref(['1', '2']);
const userSelectRef = ref<InstanceType<typeof UserSelect>>();
const singleUserSelectRef = ref<InstanceType<typeof UserSelect>>();
const roleSelectRef = ref<InstanceType<typeof RoleSelect>>();
const dueDateRef = ref<InstanceType<typeof DueDate>>();

const openUserSelect = () => {
  userSelectRef.value.open();
};
const openSingleUserSelect = () => {
  if (formData.value.assignee?.includes('$')) {
    formData.value.assignee = '';
  }
  singleUserSelectRef.value.open();
};
const openRoleSelect = () => {
  roleSelectRef.value.open();
};
const openDueDate = (e) => {
  dueDateRef.value.openDialog();
};
const blurAssignee = (assignee) => {
  updateProperties({ 'flowable:assignee': assignee ? assignee : undefined });
};
const singleUserSelectCallBack = (data: UserVO[]) => {
  const user: UserVO = data.length !== 0 ? data[0] : undefined;
  updateProperties({ 'flowable:assignee': user?.userId });
  assignee.value = user ? user : { userName: '' };
  formData.value.assignee = String(user?.userId);
  let extensionElements = getExtensionElements();
  extensionElements.values = extensionElements.get('values').filter((item) => item.$type !== 'flowable:extAssignee');
  if (user) {
    const extAssigneeElement = createModdleElement('flowable:extAssignee', { body: '' }, extensionElements);
    extensionElements.get('values').push(extAssigneeElement);
    extAssigneeElement.body = JSON.stringify({ userName: user.userName, userId: user.userId });
  }
  if (extensionElements.values.length === 0) {
    extensionElements = undefined;
  }
  updateProperties({ extensionElements: extensionElements });
};
const userSelectCallBack = (data: UserVO[]) => {
  let extensionElements = getExtensionElements();
  extensionElements.values = extensionElements.values.filter((item) => item.$type !== 'flowable:extCandidateUsers');
  if (data.length === 0) {
    formData.value.candidateUsers = undefined;
    updateProperties({ 'flowable:candidateUsers': undefined });
  } else {
    const userIds = data.map((item) => item.userId).join(',');
    formData.value.candidateUsers = userIds;
    updateProperties({ 'flowable:candidateUsers': userIds });
    const extCandidateUsersElement = createModdleElement('flowable:extCandidateUsers', { body: '' }, extensionElements);
    extensionElements.values.push(extCandidateUsersElement);
    const users = data.map((item) => {
      return {
        userId: item.userId,
        userName: item.userName
      };
    });
    extCandidateUsersElement.body = JSON.stringify(users);
  }
  if (extensionElements.values.length === 0) {
    extensionElements = undefined;
  }
  updateProperties({ extensionElements: extensionElements });
};
const roleSelectCallBack = (data: RoleVO[]) => {
  if (data.length === 0) {
    formData.value.candidateGroups = '';
    updateProperties({ 'flowable:candidateGroups': undefined });
  } else {
    const roleIds = data.map((item) => item.roleId).join(',');
    formData.value.candidateGroups = roleIds;
    updateProperties({ 'flowable:candidateGroups': roleIds });
  }
};
const dueDateCallBack = (data: string) => {
  updateProperties({ 'flowable:dueDate': data });
};

const taskTabClick = (e) => {
  formData.value.candidateGroups = '';
  formData.value.candidateUsers = '';
  formData.value.assignee = '';
  // formData.value.fixedAssignee = '';
  assignee.value = {};
};

const syncChange = (newVal) => {
  updateProperties({ 'flowable:async': newVal });
};
const skipExpressionChange = (newVal) => {
  updateProperties({ 'flowable:skipExpression': newVal && newVal.length > 0 ? newVal : undefined });
};
const priorityChange = (newVal) => {
  updateProperties({ 'flowable:priority': newVal });
};
const fixedAssigneeChange = (newVal) => {
  updateProperties({ 'flowable:assignee': newVal && newVal.length > 0 ? newVal : undefined });
};
const multiInstanceTypeChange = (newVal) => {
  if (newVal !== MultiInstanceTypeEnum.NONE) {
    let loopCharacteristics = props.element.businessObject.get('loopCharacteristics');
    if (!loopCharacteristics) {
      loopCharacteristics = createModdleElement('bpmn:MultiInstanceLoopCharacteristics', {}, props.element.businessObject);
    }
    loopCharacteristics.isSequential = newVal === MultiInstanceTypeEnum.SERIAL;
    updateProperties({ loopCharacteristics: loopCharacteristics });
  } else {
    updateProperties({ loopCharacteristics: undefined });
  }
};
const collectionChange = (newVal) => {
  let loopCharacteristics = props.element.businessObject.get('loopCharacteristics');
  if (!loopCharacteristics) {
    loopCharacteristics = createModdleElement('bpmn:MultiInstanceLoopCharacteristics', {}, props.element.businessObject);
  }
  loopCharacteristics.collection = newVal && newVal.length > 0 ? newVal : undefined;
  updateProperties({ loopCharacteristics: loopCharacteristics });
};
const elementVariableChange = (newVal) => {
  let loopCharacteristics = props.element.businessObject.get('loopCharacteristics');
  if (!loopCharacteristics) {
    loopCharacteristics = createModdleElement('bpmn:MultiInstanceLoopCharacteristics', {}, props.element.businessObject);
  }
  loopCharacteristics.elementVariable = newVal && newVal.length > 0 ? newVal : undefined;
  updateProperties({ loopCharacteristics: loopCharacteristics });
};
const completionConditionChange = (newVal) => {
  let loopCharacteristics = props.element.businessObject.get<ModdleElement>('loopCharacteristics');
  if (!loopCharacteristics) {
    loopCharacteristics = createModdleElement('bpmn:MultiInstanceLoopCharacteristics', {}, props.element.businessObject);
  }
  if (newVal && newVal.length > 0) {
    if (!loopCharacteristics.completionCondition) {
      loopCharacteristics.completionCondition = createModdleElement('bpmn:Expression', { body: newVal }, loopCharacteristics);
    } else {
      loopCharacteristics.completionCondition.body = newVal;
    }
  } else {
    loopCharacteristics.completionCondition = undefined;
  }
  updateProperties({ loopCharacteristics: loopCharacteristics });
};
const dueDateChange = (newVal) => {
  updateProperties({ 'flowable:dueDate': newVal && newVal.length > 0 ? newVal : undefined });
};
const selectUserLength = computed(() => {
  if (formData.value.candidateUsers) {
    return formData.value.candidateUsers.split(',').length;
  } else {
    return 0;
  }
});
const selectRoleLength = computed(() => {
  if (formData.value.candidateGroups) {
    return formData.value.candidateGroups.split(',').length;
  } else {
    return 0;
  }
});

onBeforeMount(() => {
  const extensionElements = getExtensionElements(false);
  if (extensionElements && extensionElements.get('values')) {
    let extAssigneeElement = extensionElements.get('values').find((item) => item.$type === 'flowable:extAssignee');
    if (extAssigneeElement) {
      assignee.value = JSON.parse(extAssigneeElement.body);
    }
  }

  if (formData.value.loopCharacteristics) {
    const loopCharacteristics = formData.value.loopCharacteristics;
    formData.value.collection = loopCharacteristics.collection || '';
    formData.value.elementVariable = loopCharacteristics.elementVariable || '';
    formData.value.completionCondition = loopCharacteristics.completionCondition?.body || '';
    formData.value.multiInstanceType = loopCharacteristics.isSequential ? MultiInstanceTypeEnum.SERIAL : MultiInstanceTypeEnum.PARALLEL;
  }

  if (formData.value.assignee) {
    formData.value.fixedAssignee = formData.value.assignee;
  }
});

const formRules = ref<ElFormRules>({
  id: [{ required: true, message: '请输入', trigger: 'blur' }],
  name: [{ required: true, message: '请输入', trigger: 'blur' }]
});

const AllocationTypeSelect = [
  { id: 'b9cdf970-dd91-47c0-819f-42a7010ca2a6', label: '指定人员', value: AllocationTypeEnum.USER },
  { id: '3f7ccbcd-c464-4602-bb9d-e96649d10585', label: '候选人员', value: AllocationTypeEnum.CANDIDATE },
  { id: 'c49065e0-7f2d-4c09-aedb-ab2d47d9a454', label: '发起人自己', value: AllocationTypeEnum.YOURSELF },
  { id: '6ef40a03-7e9a-4898-89b2-c88fe9064542', label: '发起人指定', value: AllocationTypeEnum.SPECIFY }
];
const SpecifyDesc = [
  { id: 'fa253b34-4335-458c-b1bc-b039e2a2b7a6', label: '指定一个人', value: 'specifySingle' },
  { id: '7365ff54-2e05-4312-9bfb-0b8edd779c5b', label: '指定多个人', value: 'specifyMultiple' }
];

const listFormManage = async () => {
  formManageListLoading.value = true;
  const res = await selectListFormManage();
  formManageList.value = res.data;
  formManageListLoading.value = false;
};
onMounted(() => {
  nextTick(() => {
    listFormManage();
  });
});
</script>

<style lang="scss" scoped></style>
