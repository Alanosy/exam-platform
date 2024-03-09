<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="search">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="68px">
          <el-form-item label="请假天数" prop="startLeaveDays">
            <el-input v-model="queryParams.startLeaveDays" placeholder="请输入请假天数" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item prop="endLeaveDays"> 至 </el-form-item>
          <el-form-item prop="endLeaveDays">
            <el-input v-model="queryParams.endLeaveDays" placeholder="请输入请假天数" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['demo:leave:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['demo:leave:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="leaveList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column v-if="false" label="主键" align="center" prop="id" />
        <el-table-column label="请假类型" align="center">
          <template #default="scope">
            <el-tag>{{ options.find((e) => e.value === scope.row.leaveType)?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" align="center" prop="startDate">
          <template #default="scope">
            <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结束时间" align="center" prop="endDate">
          <template #default="scope">
            <span>{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="请假天数" align="center" prop="leaveDays" />
        <el-table-column label="请假原因" align="center" prop="remark" />
        <el-table-column align="center" prop="businessStatusName" label="流程状态" min-width="70">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.processInstanceVo.businessStatusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip
              v-if="
                scope.row.processInstanceVo.businessStatus === 'draft' ||
                scope.row.processInstanceVo.businessStatus === 'cancel' ||
                scope.row.processInstanceVo.businessStatus === 'back'
              "
              content="修改"
              placement="top"
            >
              <el-button v-hasPermi="['demo:leave:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip
              v-if="
                scope.row.processInstanceVo.businessStatus === 'draft' ||
                scope.row.processInstanceVo.businessStatus === 'cancel' ||
                scope.row.processInstanceVo.businessStatus === 'back'
              "
              content="删除"
              placement="top"
            >
              <el-button v-hasPermi="['demo:leave:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip v-if="scope.row.processInstanceVo.businessStatus === 'waiting'" content="撤销" placement="top">
              <el-button link type="primary" icon="Notification" @click="handleCancelProcessApply(scope.row.processInstanceVo.id)"></el-button>
            </el-tooltip>
            <el-tooltip v-if="scope.row.processInstanceVo.businessStatus === 'waiting' || 
            scope.row.processInstanceVo.businessStatus === 'finish'||
            scope.row.processInstanceVo.businessStatus === 'termination'||
            scope.row.processInstanceVo.businessStatus === 'invalid'" content="审批记录" placement="top">
              <el-button link type="primary" icon="Document" @click="handleApprovalRecord(scope.row.processInstanceVo.id)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改请假对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="800px" append-to-body>
      <el-form ref="leaveFormRef" v-loading="loading" :model="form" :rules="rules" label-width="80px">
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
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="info" @click="submitForm('draft')">暂 存</el-button>
          <el-button :loading="buttonLoading" type="primary" @click="submitForm('submit')">提 交</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 提交组件 -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup name="Leave" lang="ts">
import { addLeave, delLeave, getLeave, listLeave, updateLeave } from '@/api/workflow/leave';
import { cancelProcessApply } from '@/api/workflow/processInstance';
import { LeaveForm, LeaveQuery, LeaveVO } from '@/api/workflow/leave/types';
import { startWorkFlow } from '@/api/workflow/task';
import SubmitVerify from '@/components/Process/submitVerify.vue';
import ApprovalRecord from '@/components/Process/approvalRecord.vue';
import { AxiosResponse } from 'axios';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const leaveList = ref<LeaveVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const leaveTime = ref<Array<string>>([]);
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

const queryFormRef = ref<ElFormInstance>();
const leaveFormRef = ref<ElFormInstance>();

const submitFormData = ref<Record<string, any>>({
  businessKey: '',
  processKey: '',
  variables: {}
});
const taskVariables = ref<Record<string, any>>({});

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: LeaveForm = {
  id: undefined,
  leaveType: undefined,
  startDate: undefined,
  endDate: undefined,
  leaveDays: undefined,
  remark: undefined
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

const { queryParams, form, rules } = toRefs(data);

/** 查询请假列表 */
const getList = async () => {
  loading.value = true;
  const res = await listLeave(queryParams.value);
  leaveList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  leaveTime.value = [];
  leaveFormRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: LeaveVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  dialog.visible = true;
  dialog.title = '添加请假申请';
  nextTick(() => {
    reset();
  });
};

const changeLeaveTime = () => {
  const startDate = new Date(leaveTime.value[0]).getTime();
  const endDate = new Date(leaveTime.value[1]).getTime();
  const diffInMilliseconds = endDate - startDate;
  form.value.leaveDays = Math.floor(diffInMilliseconds / (1000 * 60 * 60 * 24));
};
/** 修改按钮操作 */
const handleUpdate = (row?: LeaveVO) => {
  buttonLoading.value = false;
  dialog.visible = true;
  dialog.title = '修改请假申请';
  nextTick(async () => {
    reset();
    const _id = row?.id || ids.value[0];
    const res = await getLeave(_id);
    Object.assign(form.value, res.data);
    leaveTime.value = [];
    leaveTime.value.push(form.value.startDate);
    leaveTime.value.push(form.value.endDate);
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
        dialog.visible = false;
        await getList();
      } else {
        await handleStartWorkFlow(res.data);
      }
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: LeaveVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除请假编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delLeave(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'demo/leave/export',
    {
      ...queryParams.value
    },
    `leave_${new Date().getTime()}.xlsx`
  );
};

//提交申请
const handleStartWorkFlow = async (data: LeaveVO) => {
  submitFormData.value.processKey = 'leave1';
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
const handleApprovalRecord = (id: string) => {
  if (approvalRecordRef.value) {
    approvalRecordRef.value.init(id);
  }
};
//提交回调
const submitCallback = async () => {
  dialog.visible = false;
  handleQuery();
};
/** 撤销按钮操作 */
const handleCancelProcessApply = async (id: string) => {
  await proxy?.$modal.confirm('是否确认撤销当前单据？');
  loading.value = true;
  await cancelProcessApply(id).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('撤销成功');
};
onMounted(() => {
  getList();
});
</script>
