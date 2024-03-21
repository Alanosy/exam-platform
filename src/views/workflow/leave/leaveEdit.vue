<template>
  <div class="p-2">
    <el-card shadow="never">
        <div style="display: flex;justify-content: space-between;">
          <div>
            <el-button :loading="buttonLoading" 
            v-if="routeParams.type === 'add' || (routeParams.type === 'update' && form.processInstanceVo.businessStatus && (form.processInstanceVo.businessStatus === 'draft' || form.processInstanceVo.businessStatus === 'cancel' || form.processInstanceVo.businessStatus === 'back'))"
            type="info" @click="submitForm('draft')">暂存</el-button>
            <el-button :loading="buttonLoading" v-if="routeParams.type === 'add' || (routeParams.type === 'update' && form.processInstanceVo && (form.processInstanceVo.businessStatus === 'draft' || form.processInstanceVo.businessStatus === 'cancel' || form.processInstanceVo.businessStatus === 'back'))"
            type="primary" @click="submitForm('submit')">提 交</el-button>
            <el-button :loading="buttonLoading" v-if="routeParams.type === 'approval' && form.processInstanceVo && form.processInstanceVo.businessStatus === 'waiting'"
            type="primary" @click="approvalVerifyOpen">审批</el-button>
            <el-button @click="handleApprovalRecord" type="primary" v-if="processInstanceId">流程进度</el-button>
          </div>
          <div>
            <el-button style="float: right" @click="goBack()">返回</el-button>
          </div>
        </div>
    </el-card>
    <el-card shadow="never">
      <el-form ref="leaveFormRef" :disabled="routeParams.type ==='view'" v-loading="loading" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="请假时间">
          <el-date-picker
            v-model="leaveTime"
            type="daterange"
            range-separator="To"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            @change="changeLeaveTime()"
          />
        </el-form-item>
        <el-form-item label="请假天数" prop="leaveDays">
          <el-input v-model="form.leaveDays" disabled type="number" placeholder="请输入请假天数" />
        </el-form-item>
        <el-form-item label="请假原因" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入请假原因" />
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 提交组件 -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup name="Leave" lang="ts">
import { addLeave, getLeave, updateLeave } from '@/api/workflow/leave';
import { LeaveForm, LeaveQuery, LeaveVO } from '@/api/workflow/leave/types';
import { startWorkFlow } from '@/api/workflow/task';
import SubmitVerify from '@/components/Process/submitVerify.vue';
import ApprovalRecord from '@/components/Process/approvalRecord.vue';
import { AxiosResponse } from 'axios';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const buttonLoading = ref(false);
const loading = ref(true);
const leaveTime = ref<Array<string>>([]);
//流程实例id
const processInstanceId = ref('');
//路由参数 
const routeParams = ref<Record<string, any>>({})
const options = [
  {
    value: '1',
    label: '事假'
  },
  {
    value: '2',
    label: '调休'
  },
  {
    value: '3',
    label: '病假'
  },
  {
    value: '4',
    label: '婚假'
  }
];
//提交组件
const submitVerifyRef = ref<InstanceType<typeof SubmitVerify>>();
//审批记录组件
const approvalRecordRef = ref<InstanceType<typeof ApprovalRecord>>();

const leaveFormRef = ref<ElFormInstance>();

const submitFormData = ref<Record<string, any>>({
  businessKey: '',
  processKey: '',
  variables: {}
});
const taskVariables = ref<Record<string, any>>({});


const initFormData: LeaveForm = {
  id: undefined,
  leaveType: undefined,
  startDate: undefined,
  endDate: undefined,
  leaveDays: undefined,
  remark: undefined,
  processInstanceVo: {}
};
const data = reactive<PageData<LeaveForm, LeaveQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    startLeaveDays: undefined,
    endLeaveDays: undefined
  },
  rules: {
    id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
    leaveType: [{ required: true, message: '请假类型不能为空', trigger: 'blur' }],
    leaveTime: [{ required: true, message: '请假时间不能为空', trigger: 'blur' }],
    leaveDays: [{ required: true, message: '请假天数不能为空', trigger: 'blur' }]
  }
});

const { form, rules } = toRefs(data);


/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  leaveTime.value = [];
  leaveFormRef.value?.resetFields();
};

const changeLeaveTime = () => {
  const startDate = new Date(leaveTime.value[0]).getTime();
  const endDate = new Date(leaveTime.value[1]).getTime();
  const diffInMilliseconds = endDate - startDate;
  form.value.leaveDays = Math.floor(diffInMilliseconds / (1000 * 60 * 60 * 24));
};
/** 获取详情 */
const getInfo = () => {
  loading.value = true
  buttonLoading.value = false;
  nextTick(async () => {
    const res = await getLeave(routeParams.value.id);
    Object.assign(form.value, res.data);
    leaveTime.value = [];
    leaveTime.value.push(form.value.startDate);
    leaveTime.value.push(form.value.endDate);
    if(form.value.processInstanceVo){
      processInstanceId.value = form.value.processInstanceVo.id
    }
    loading.value = false
    buttonLoading.value = false;
  });
};

/** 提交按钮 */
const submitForm = (status: string) => {
  if (leaveTime.value.length === 0) {
    proxy?.$modal.msgError('请假时间不能为空');
    return;
  }
  leaveFormRef.value?.validate(async (valid: boolean) => {
    form.value.startDate = leaveTime.value[0];
    form.value.endDate = leaveTime.value[1];
    if (valid) {
      buttonLoading.value = true;
      let res: AxiosResponse<LeaveVO>;
      if (form.value.id) {
        res = await updateLeave(form.value);
      } else {
        res = await addLeave(form.value);
      }
      form.value = res.data;
      if (status === 'draft') {
        buttonLoading.value = false;
        proxy?.$modal.msgSuccess('暂存成功');
        proxy.$tab.closePage(proxy.$route);
        proxy.$router.go(-1)
      } else {
        await handleStartWorkFlow(res.data);
      }
    }
  });
};

//提交申请
const handleStartWorkFlow = async (data: LeaveVO) => {
  submitFormData.value.processKey = 'test';
  submitFormData.value.businessKey = data.id;
  //流程变量
  taskVariables.value = {
    entity: data,
    leaveDays: data.leaveDays,
    userList: [1, 2],
    userList2: [1, 2]
  };
  submitFormData.value.variables = taskVariables.value;
  const resp = await startWorkFlow(submitFormData.value);
  if (submitVerifyRef.value) {
    buttonLoading.value = false;
    submitVerifyRef.value.openDialog(resp.data.taskId);
  }
};
//审批记录
const handleApprovalRecord = () => {
  approvalRecordRef.value.init(processInstanceId.value);
};
//提交回调
const submitCallback = async () => {
  proxy.$tab.closePage(proxy.$route);
  proxy.$router.go(-1)
};

//返回
const goBack = () => {
  proxy.$tab.closePage(proxy.$route);
  proxy.$router.go(-1)
}
//审批
const approvalVerifyOpen = async () => {
  submitVerifyRef.value.openDialog(proxy.$route.query.taskId);
};
onMounted(() => {
  nextTick(async () => {
      routeParams.value = proxy?.$route.params
      reset();
      if (routeParams.value.type === 'update' || routeParams.value.type === 'view' || routeParams.value.type === 'approval') {
        getInfo()
      }
  })
});
</script>
