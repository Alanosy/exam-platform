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
      <el-form-item label="附件" v-if="task.businessStatus === 'waiting'">
        <fileUpload v-model="form.fileId" :fileType="['doc', 'xls', 'ppt', 'txt', 'pdf', 'xlsx', 'docx', 'zip']" :fileSize="'20'"/>
      </el-form-item>
      <el-form-item label="抄送">
        <el-button type="primary" @click="openUserSelectCopy" icon="Plus" circle />
          <el-tag v-for="user in selectCopyUserList" :key="user.userId" closable style="margin: 2px" @close="handleCopyCloseTag(user)">
            {{ user.userName }}
          </el-tag>
      </el-form-item>
      <el-form-item label="审批意见" v-if="task.businessStatus === 'waiting'">
        <el-input v-model="form.message" type="textarea" resize="none" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button v-loading="buttonLoading" type="primary" @click="handleCompleteTask"> 提交 </el-button>
        <el-button v-if="task.businessStatus === 'waiting' && task.multiInstance" v-loading="buttonLoading" type="primary" @click="addMultiInstanceUser"> 加签 </el-button>
        <el-button v-if="task.businessStatus === 'waiting' && task.multiInstance" v-loading="buttonLoading" type="primary" @click="deleteMultiInstanceUser"> 减签 </el-button>
        <el-button v-if="task.businessStatus === 'waiting'" v-loading="buttonLoading" type="danger" @click="handleBackProcess"> 退回 </el-button>
        <el-button v-loading="buttonLoading" @click="cancel">取消</el-button>
      </span>
    </template>
    <UserSelect ref="userSelectCopyRef" :data="selectCopyUserIds" @confirm-call-back="userSelectCopyCallBack"></UserSelect>
    <!-- 加签组件 -->
    <multiInstanceUser ref="multiInstanceUserRef" :title="title" @submit-callback="handleQuery" />
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { ComponentInternalInstance } from 'vue';
import { ElForm } from 'element-plus';
import { completeTask, backProcess, getTaskById } from '@/api/workflow/task';
import UserSelect from '@/components/UserSelect';
import MultiInstanceUser from '@/components/Process/multiInstanceUser.vue';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
import { UserVO } from '@/api/system/user/types';
import { TaskVO } from '@/api/workflow/task/types';
const userSelectCopyRef = ref<InstanceType<typeof UserSelect>>();
  //加签组件
const multiInstanceUserRef = ref<InstanceType<typeof MultiInstanceUser>>();

const props = defineProps({
  taskVariables: {
    type: Object as () => Record<string, any>,
    default: {}
  }
});
//遮罩层
const loading = ref(true);
//按钮
const buttonLoading = ref(true);
//任务id
const taskId = ref<string>('');
//抄送人
const selectCopyUserList = ref<UserVO[]>([]);
//抄送人id
const selectCopyUserIds = ref<string>(undefined);
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
  multiInstance: undefined
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
//打开弹窗
const openDialog = (id?: string) => {
  selectCopyUserIds.value = undefined
  selectCopyUserList.value = []
  form.value.fileId = undefined
  taskId.value = id;
  form.value.message = undefined;
  dialog.visible = true;
  loading.value = true;
  buttonLoading.value = true;
  nextTick(() => {
    getTaskById(taskId.value).then((response) => {
      task.value = response.data;
      loading.value = false;
      buttonLoading.value = false;
    });
  });
};

onMounted(() => {});
const emits = defineEmits(['submitCallback', 'cancelCallback']);

/** 办理流程 */
const handleCompleteTask = async () => {
  form.value.taskId = taskId.value;
  form.value.taskVariables = props.taskVariables;
  if(selectCopyUserList && selectCopyUserList.value.length > 0){
    let wfCopyList = []
    selectCopyUserList.value.forEach( e=> {
      let copyUser = {
        userId: e.userId,
        userName: e.nickName
      }
      wfCopyList.push(copyUser)
    })
    form.value.wfCopyList = wfCopyList
  }
  await proxy?.$modal.confirm('是否确认提交？');
  loading.value = true;
  buttonLoading.value = true;
  await completeTask(form.value).finally(() => (loading.value = false));
  dialog.visible = false;
  emits('submitCallback');
  proxy?.$modal.msgSuccess('操作成功');
};

/** 驳回流程 */
const handleBackProcess = async () => {
  form.value.taskId = taskId.value;
  await proxy?.$modal.confirm('是否确认驳回到申请人？');
  loading.value = true;
  buttonLoading.value = true;
  await backProcess(form.value).finally(() => (loading.value = false));
  dialog.visible = false;
  emits('submitCallback');
  proxy?.$modal.msgSuccess('操作成功');
};
//取消
const cancel = async () => {
  dialog.visible = false;
  buttonLoading.value = false;
  emits('cancelCallback');
};
//打开抄送人员
const openUserSelectCopy = () => {
  userSelectCopyRef.value.open();
};
//确认抄送人员
const userSelectCopyCallBack = (data: UserVO[]) => {
  if(data && data.length > 0){
    selectCopyUserList.value = data
    selectCopyUserIds.value = selectCopyUserList.value.map((item) => item.userId).join(',');
  }
}
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
/**
 * 对外暴露子组件方法
 */
defineExpose({
  openDialog
});
</script>
