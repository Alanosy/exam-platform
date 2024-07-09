<template>
  <el-dialog v-model="dialog.visible" :title="dialog.title" width="50%" draggable :before-close="cancel" center :close-on-click-modal="false">
    <el-form v-loading="loading" :model="form" label-width="120px">
      <el-form-item label="消息提醒">
        <el-checkbox-group v-model="form.messageType">
          <el-checkbox label="1" name="type" disabled>站内信</el-checkbox>
          <el-checkbox label="2" name="type">邮件</el-checkbox>
          <el-checkbox label="3" name="type">短信</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item v-if="task.businessStatus === 'waiting'" label="附件">
        <fileUpload v-model="form.fileId" :file-type="['doc', 'xls', 'ppt', 'txt', 'pdf', 'xlsx', 'docx', 'zip']" :file-size="'20'" />
      </el-form-item>
      <el-form-item label="抄送">
        <el-button type="primary" icon="Plus" circle @click="openUserSelectCopy" />
        <el-tag v-for="user in selectCopyUserList" :key="user.userId" closable style="margin: 2px" @close="handleCopyCloseTag(user)">
          {{ user.userName }}
        </el-tag>
      </el-form-item>
      <el-form-item v-if="task.businessStatus === 'waiting'" label="审批意见">
        <el-input v-model="form.message" type="textarea" resize="none" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button :disabled="buttonDisabled" type="primary" @click="handleCompleteTask"> 提交 </el-button>
        <el-button v-if="task.businessStatus === 'waiting'" :disabled="buttonDisabled" type="primary" @click="openDelegateTask"> 委托 </el-button>
        <el-button v-if="task.businessStatus === 'waiting'" :disabled="buttonDisabled" type="primary" @click="openTransferTask"> 转办 </el-button>
        <el-button
          v-if="task.businessStatus === 'waiting' && task.multiInstance"
          :disabled="buttonDisabled"
          type="primary"
          @click="addMultiInstanceUser"
        >
          加签
        </el-button>
        <el-button
          v-if="task.businessStatus === 'waiting' && task.multiInstance"
          :disabled="buttonDisabled"
          type="primary"
          @click="deleteMultiInstanceUser"
        >
          减签
        </el-button>
        <el-button v-if="task.businessStatus === 'waiting'" :disabled="buttonDisabled" type="danger" @click="handleTerminationTask"> 终止 </el-button>
        <el-button v-if="task.businessStatus === 'waiting'" :disabled="buttonDisabled" type="danger" @click="handleBackProcessOpen"> 退回 </el-button>
        <el-button :disabled="buttonDisabled" @click="cancel">取消</el-button>
      </span>
    </template>
    <!-- 抄送 -->
    <UserSelect ref="userSelectCopyRef" :multiple="true" :data="selectCopyUserIds" @confirm-call-back="userSelectCopyCallBack"></UserSelect>
    <!-- 转办 -->
    <UserSelect ref="transferTaskRef" :multiple="false" @confirm-call-back="handleTransferTask"></UserSelect>
    <!-- 委托 -->
    <UserSelect ref="delegateTaskRef" :multiple="false" @confirm-call-back="handleDelegateTask"></UserSelect>
    <!-- 加签组件 -->
    <multiInstanceUser ref="multiInstanceUserRef" :title="title" @submit-callback="closeDialog" />

    <!-- 驳回开始 -->
    <el-dialog v-model="backVisible" draggable title="驳回" width="40%" :close-on-click-modal="false">
      <el-form v-if="task.businessStatus === 'waiting'" v-loading="backLoading" :model="backForm" label-width="120px">
        <el-form-item label="驳回节点">
          <el-select v-model="backForm.targetActivityId" clearable placeholder="请选择" style="width: 300px">
            <el-option v-for="item in taskNodeList" :key="item.nodeId" :label="item.nodeName" :value="item.nodeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="消息提醒">
          <el-checkbox-group v-model="backForm.messageType">
            <el-checkbox label="1" name="type" disabled>站内信</el-checkbox>
            <el-checkbox label="2" name="type">邮件</el-checkbox>
            <el-checkbox label="3" name="type">短信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="backForm.message" type="textarea" resize="none" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer" style="float: right; padding-bottom: 20px">
          <el-button :disabled="backButtonDisabled" type="primary" @click="handleBackProcess">确认</el-button>
          <el-button :disabled="backButtonDisabled" @click="backVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 驳回结束 -->
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { ComponentInternalInstance } from 'vue';
import { ElForm } from 'element-plus';
import { completeTask, backProcess, getTaskById, transferTask, terminationTask, getTaskNodeList, delegateTask } from '@/api/workflow/task';
import UserSelect from '@/components/UserSelect';
import MultiInstanceUser from '@/components/Process/multiInstanceUser.vue';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
import { UserVO } from '@/api/system/user/types';
import { TaskVO } from '@/api/workflow/task/types';
const userSelectCopyRef = ref<InstanceType<typeof UserSelect>>();
const transferTaskRef = ref<InstanceType<typeof UserSelect>>();
const delegateTaskRef = ref<InstanceType<typeof UserSelect>>();

//加签组件
const multiInstanceUserRef = ref<InstanceType<typeof MultiInstanceUser>>();

const props = defineProps({
  taskVariables: {
    type: Object as () => Record<string, any>,
    default: () => {}
  }
});
//遮罩层
const loading = ref(true);
//按钮
const buttonDisabled = ref(true);
//任务id
const taskId = ref<string>('');
//抄送人
const selectCopyUserList = ref<UserVO[]>([]);
//抄送人id
const selectCopyUserIds = ref<string>(undefined);
// 驳回是否显示
const backVisible = ref(false);
const backLoading = ref(true);
const backButtonDisabled = ref(true);
// 可驳回得任务节点
const taskNodeList = ref([]);
//任务
const task = ref<TaskVO>({
  id: undefined,
  name: undefined,
  description: undefined,
  priority: undefined,
  owner: undefined,
  assignee: undefined,
  assigneeName: undefined,
  processInstanceId: undefined,
  executionId: undefined,
  taskDefinitionId: undefined,
  processDefinitionId: undefined,
  endTime: undefined,
  taskDefinitionKey: undefined,
  dueDate: undefined,
  category: undefined,
  parentTaskId: undefined,
  tenantId: undefined,
  claimTime: undefined,
  businessStatus: undefined,
  businessStatusName: undefined,
  processDefinitionName: undefined,
  processDefinitionKey: undefined,
  participantVo: undefined,
  multiInstance: undefined,
  businessKey: undefined,
  wfNodeConfigVo: undefined
});
//加签 减签标题
const title = ref('');
const dialog = reactive<DialogOption>({
  visible: false,
  title: '提示'
});

const form = ref<Record<string, any>>({
  taskId: undefined,
  message: undefined,
  variables: {},
  messageType: ['1'],
  wfCopyList: []
});
const backForm = ref<Record<string, any>>({
  taskId: undefined,
  targetActivityId: undefined,
  message: undefined,
  variables: {},
  messageType: ['1']
});
const closeDialog = () => {
  dialog.visible = false;
};
//打开弹窗
const openDialog = (id?: string) => {
  selectCopyUserIds.value = undefined;
  selectCopyUserList.value = [];
  form.value.fileId = undefined;
  taskId.value = id;
  form.value.message = undefined;
  dialog.visible = true;
  loading.value = true;
  buttonDisabled.value = true;
  nextTick(() => {
    getTaskById(taskId.value).then((response) => {
      task.value = response.data;
      loading.value = false;
      buttonDisabled.value = false;
    });
  });
};

onMounted(() => {});
const emits = defineEmits(['submitCallback', 'cancelCallback']);

/** 办理流程 */
const handleCompleteTask = async () => {
  form.value.taskId = taskId.value;
  form.value.taskVariables = props.taskVariables;
  if (selectCopyUserList.value && selectCopyUserList.value.length > 0) {
    let wfCopyList = [];
    selectCopyUserList.value.forEach((e) => {
      let copyUser = {
        userId: e.userId,
        userName: e.nickName
      };
      wfCopyList.push(copyUser);
    });
    form.value.wfCopyList = wfCopyList;
  }
  await proxy?.$modal.confirm('是否确认提交？');
  loading.value = true;
  buttonDisabled.value = true;
  try {
    await completeTask(form.value);
    dialog.visible = false;
    emits('submitCallback');
    proxy?.$modal.msgSuccess('操作成功');
  } finally {
    loading.value = false;
    buttonDisabled.value = false;
  }
};

/** 驳回弹窗打开 */
const handleBackProcessOpen = async () => {
  backForm.value = {};
  backForm.value.messageType = ['1'];
  backVisible.value = true;
  backLoading.value = true;
  backButtonDisabled.value = true;
  let data = await getTaskNodeList(task.value.processInstanceId);
  taskNodeList.value = data.data;
  backLoading.value = false;
  backButtonDisabled.value = false;
  backForm.value.targetActivityId = taskNodeList.value[0].nodeId;
};
/** 驳回流程 */
const handleBackProcess = async () => {
  backForm.value.taskId = taskId.value;
  await proxy?.$modal.confirm('是否确认驳回到申请人？');
  loading.value = true;
  backLoading.value = true;
  backButtonDisabled.value = true;
  await backProcess(backForm.value).finally(() => (loading.value = false));
  dialog.visible = false;
  backLoading.value = false;
  backButtonDisabled.value = false;
  emits('submitCallback');
  proxy?.$modal.msgSuccess('操作成功');
};
//取消
const cancel = async () => {
  dialog.visible = false;
  buttonDisabled.value = false;
  emits('cancelCallback');
};
//打开抄送人员
const openUserSelectCopy = () => {
  userSelectCopyRef.value.open();
};
//确认抄送人员
const userSelectCopyCallBack = (data: UserVO[]) => {
  if (data && data.length > 0) {
    selectCopyUserList.value = data;
    selectCopyUserIds.value = selectCopyUserList.value.map((item) => item.userId).join(',');
  }
};
//删除抄送人员
const handleCopyCloseTag = (user: UserVO) => {
  const userId = user.userId;
  // 使用split删除用户
  const index = selectCopyUserList.value.findIndex((item) => item.userId === userId);
  selectCopyUserList.value.splice(index, 1);
  selectCopyUserIds.value = selectCopyUserList.value.map((item) => item.userId).join(',');
};
//加签
const addMultiInstanceUser = () => {
  if (multiInstanceUserRef.value) {
    title.value = '加签人员';
    multiInstanceUserRef.value.getAddMultiInstanceList(taskId.value, []);
  }
};
//减签
const deleteMultiInstanceUser = () => {
  if (multiInstanceUserRef.value) {
    title.value = '减签人员';
    multiInstanceUserRef.value.getDeleteMultiInstanceList(taskId.value);
  }
};
//打开转办
const openTransferTask = () => {
  transferTaskRef.value.open();
};
//转办
const handleTransferTask = async (data) => {
  if (data && data.length > 0) {
    let params = {
      taskId: taskId.value,
      userId: data[0].userId,
      comment: form.value.message
    };
    await proxy?.$modal.confirm('是否确认提交？');
    loading.value = true;
    buttonDisabled.value = true;
    await transferTask(params).finally(() => (loading.value = false));
    dialog.visible = false;
    emits('submitCallback');
    proxy?.$modal.msgSuccess('操作成功');
  } else {
    proxy?.$modal.msgWarning('请选择用户！');
  }
};

//打开委托
const openDelegateTask = () => {
  delegateTaskRef.value.open();
};
//委托
const handleDelegateTask = async (data) => {
  if (data && data.length > 0) {
    let params = {
      taskId: taskId.value,
      userId: data[0].userId,
      nickName: data[0].nickName
    };
    await proxy?.$modal.confirm('是否确认提交？');
    loading.value = true;
    buttonDisabled.value = true;
    await delegateTask(params).finally(() => (loading.value = false));
    dialog.visible = false;
    emits('submitCallback');
    proxy?.$modal.msgSuccess('操作成功');
  } else {
    proxy?.$modal.msgWarning('请选择用户！');
  }
};
//终止任务
const handleTerminationTask = async (data) => {
  let params = {
    taskId: taskId.value,
    comment: form.value.message
  };
  await proxy?.$modal.confirm('是否确认终止？');
  loading.value = true;
  buttonDisabled.value = true;
  await terminationTask(params).finally(() => (loading.value = false));
  dialog.visible = false;
  emits('submitCallback');
  proxy?.$modal.msgSuccess('操作成功');
};

/**
 * 对外暴露子组件方法
 */
defineExpose({
  openDialog
});
</script>
