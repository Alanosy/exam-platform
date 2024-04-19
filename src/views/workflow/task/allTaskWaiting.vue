<template>
  <div class="p-2">
    <div class="mb-[10px]">
      <el-card shadow="hover" class="text-center">
        <el-radio-group v-model="tab" @change="changeTab(tab)">
          <el-radio-button value="waiting">待办任务</el-radio-button>
          <el-radio-button value="finish">已办任务</el-radio-button>
        </el-radio-group>
      </el-card>
    </div>
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="任务名称" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入任务名称" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="流程定义名称" label-width="100" prop="processDefinitionName">
              <el-input v-model="queryParams.processDefinitionName" placeholder="请输入流程定义名称" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="流程定义KEY" label-width="100" prop="processDefinitionKey">
              <el-input v-model="queryParams.processDefinitionKey" placeholder="请输入流程定义KEY" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Edit" :disabled="multiple" @click="handleUpdate">修改办理人</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @query-table="handleQuery"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="taskList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column align="center" type="index" label="序号" width="60"></el-table-column>
        <el-table-column :show-overflow-tooltip="true" align="center" label="流程定义名称">
          <template #default="scope">
            <span>{{ scope.row.processDefinitionName }}v{{ scope.row.processDefinitionVersion }}.0</span>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="processDefinitionKey" label="流程定义KEY"></el-table-column>
        <el-table-column align="center" prop="name" label="任务名称"></el-table-column>
        <el-table-column align="center" prop="assigneeName" label="办理人">
          <template v-if="tab === 'waiting'" #default="scope">
            <template v-if="scope.row.participantVo && scope.row.assignee === null">
              <el-tag v-for="(item, index) in scope.row.participantVo.candidateName" :key="index" type="success">
                {{ item }}
              </el-tag>
            </template>
            <template v-else>
              <el-tag type="success">
                {{ scope.row.assigneeName || '无' }}
              </el-tag>
            </template>
          </template>
          <template v-else-if="tab === 'finish'" #default="scope">
            <el-tag type="success">
              {{ scope.row.assigneeName || '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="流程状态" min-width="70">
          <template #default="scope">
            <dict-tag v-if="tab === 'waiting'" :options="wf_business_status" :value="scope.row.businessStatus"></dict-tag>
            <el-tag v-else type="success">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="tab === 'waiting'" align="center" prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column v-if="tab === 'finish'" align="center" prop="startTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" align="center" :width="tab === 'finish' ? '80' : '151'">
          <template #default="scope">
            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button link type="primary" size="small" icon="View" @click="handleView(scope.row)">查看</el-button>
              </el-col>
              <el-col v-if="tab === 'waiting'" :span="1.5">
                <el-button link type="primary" size="small" icon="Document" @click="handleInstanceVariable(scope.row)">流程变量</el-button>
              </el-col>
            </el-row>
            <el-row v-if="scope.row.multiInstance" :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button link type="primary" size="small" icon="Remove" @click="deleteMultiInstanceUser(scope.row)">减签</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button link type="primary" size="small" icon="CirclePlus" @click="addMultiInstanceUser(scope.row)">加签</el-button>
              </el-col>
            </el-row>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="handleQuery"
      />
    </el-card>
    <!-- 加签组件 -->
    <multiInstanceUser ref="multiInstanceUserRef" :title="title" @submit-callback="handleQuery" />
    <!-- 选人组件 -->
    <UserSelect ref="userSelectRef" :multiple="false" @confirm-call-back="submitCallback"></UserSelect>
    <!-- 流程变量开始 -->
    <el-dialog v-model="variableVisible" draggable title="流程变量" width="60%" :close-on-click-modal="false">
      <el-card v-loading="variableLoading" class="box-card">
        <template #header>
          <div class="clearfix">
            <span
              >流程定义名称：<el-tag>{{ processDefinitionName }}</el-tag></span
            >
          </div>
        </template>
        <div v-for="(v, index) in variableList" :key="index">
          <el-form v-if="v.key !== '_FLOWABLE_SKIP_EXPRESSION_ENABLED'" :label-position="'right'" label-width="150px">
            <el-form-item :label="v.key + '：'">
              {{ v.value }}
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </el-dialog>
    <!-- 流程变量结束 -->
  </div>
</template>

<script lang="ts" setup>
import { getPageByAllTaskWait, getPageByAllTaskFinish, updateAssignee, getInstanceVariable } from '@/api/workflow/task';
import MultiInstanceUser from '@/components/Process/multiInstanceUser.vue';
import UserSelect from '@/components/UserSelect';
import { TaskQuery, TaskVO, VariableVo } from '@/api/workflow/task/types';
import workflowCommon from '@/api/workflow/workflowCommon';
import { RouterJumpVo } from '@/api/workflow/workflowCommon/types';
//审批记录组件
//加签组件
const multiInstanceUserRef = ref<InstanceType<typeof MultiInstanceUser>>();
//选人组件
const userSelectRef = ref<InstanceType<typeof UserSelect>>();

const queryFormRef = ref<ElFormInstance>();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_business_status } = toRefs<any>(proxy?.useDict('wf_business_status'));
// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref<Array<any>>([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 模型定义表格数据
const taskList = ref([]);
const title = ref('');
// 流程变量是否显示
const variableVisible = ref(false);
const variableLoading = ref(true);
// 流程变量
const variableList = ref<VariableVo>({
  key: '',
  value: ''
});
//流程定义名称
const processDefinitionName = ref();
// 查询参数
const queryParams = ref<TaskQuery>({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  processDefinitionName: undefined,
  processDefinitionKey: undefined
});
const tab = ref('waiting');

//加签
const addMultiInstanceUser = (row: TaskVO) => {
  if (multiInstanceUserRef.value) {
    title.value = '加签人员';
    multiInstanceUserRef.value.getAddMultiInstanceList(row.id, []);
  }
};
//减签
const deleteMultiInstanceUser = (row: TaskVO) => {
  if (multiInstanceUserRef.value) {
    title.value = '减签人员';
    multiInstanceUserRef.value.getDeleteMultiInstanceList(row.id);
  }
};
/** 搜索按钮操作 */
const handleQuery = () => {
  if ('waiting' === tab.value) {
    getWaitingList();
  } else {
    getFinishList();
  }
};
/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  queryParams.value.pageNum = 1;
  queryParams.value.pageSize = 10;
  handleQuery();
};
// 多选框选中数据
const handleSelectionChange = (selection: any) => {
  ids.value = selection.map((item: any) => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};
const changeTab = async (data: string) => {
  taskList.value = [];
  queryParams.value.pageNum = 1;
  if ('waiting' === data) {
    getWaitingList();
  } else {
    getFinishList();
  }
};
//分页
const getWaitingList = () => {
  loading.value = true;
  getPageByAllTaskWait(queryParams.value).then((resp) => {
    taskList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};
const getFinishList = () => {
  loading.value = true;
  getPageByAllTaskFinish(queryParams.value).then((resp) => {
    taskList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};
//打开修改选人
const handleUpdate = () => {
  userSelectRef.value.open();
};
//修改办理人
const submitCallback = async (data) => {
  if (data && data.length > 0) {
    await proxy?.$modal.confirm('是否确认提交？');
    loading.value = true;
    await updateAssignee(ids.value, data[0].userId);
    handleQuery();
    proxy?.$modal.msgSuccess('操作成功');
  } else {
    proxy?.$modal.msgWarning('请选择用户！');
  }
};
//查询流程变量
const handleInstanceVariable = async (row: TaskVO) => {
  variableLoading.value = true;
  variableVisible.value = true;
  processDefinitionName.value = row.processDefinitionName;
  let data = await getInstanceVariable(row.id);
  variableList.value = data.data;
  variableLoading.value = false;
};
/** 查看按钮操作 */
const handleView = (row) => {
  const routerJumpVo = reactive<RouterJumpVo>({
    wfDefinitionConfigVo: row.wfDefinitionConfigVo,
    wfNodeConfigVo: row.wfNodeConfigVo,
    businessKey: row.businessKey,
    taskId: row.id,
    type: 'view'
  });
  workflowCommon.routerJump(routerJumpVo, proxy);
};
onMounted(() => {
  getWaitingList();
});
</script>
