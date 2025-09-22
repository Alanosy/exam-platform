<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item>
              <el-badge :value="userSelectCount" :max="10" class="item">
                <el-button type="primary" @click="openUserSelect">选择申请人</el-button>
              </el-badge>
            </el-form-item>
            <el-form-item label="任务名称" prop="nodeName">
              <el-input v-model="queryParams.nodeName" placeholder="请输入任务名称" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="流程定义名称" label-width="100" prop="flowName">
              <el-input v-model="queryParams.flowName" placeholder="请输入流程定义名称" @keyup.enter="handleQuery" />
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
          <el-col :span="1.5" v-if="tab === 'waiting'">
            <el-button type="primary" plain icon="Edit" :disabled="multiple" @click="handleUserOpen()">修改办理人 </el-button>
            <el-button type="primary" plain icon="Bell" :disabled="multiple" @click="handleUrgeTaskOpen()">催办 </el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="handleQuery"></right-toolbar>
        </el-row>
      </template>
      <el-tabs v-model="tab" @tab-click="changeTab">
        <el-tab-pane name="waiting" label="待办任务"> </el-tab-pane>
        <el-tab-pane name="finish" label="已办任务"> </el-tab-pane>
        <el-table v-loading="loading" border :data="taskList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column align="center" type="index" label="序号" width="60"></el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="businessCode" align="center" label="业务编码"></el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="businessTitle" align="center" label="业务标题"></el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="flowName" align="center" width="120" label="流程定义名称"></el-table-column>
          <el-table-column align="center" prop="flowCode" width="120" label="流程定义编码"></el-table-column>
          <el-table-column align="center" prop="categoryName" label="流程分类"></el-table-column>
          <el-table-column align="center" prop="version" label="版本号" width="90">
            <template #default="scope"> v{{ scope.row.version }}.0</template>
          </el-table-column>
          <el-table-column align="center" prop="nodeName" :show-overflow-tooltip="true" label="任务名称"></el-table-column>
          <el-table-column align="center" prop="createByName" :show-overflow-tooltip="true" label="申请人"></el-table-column>
          <el-table-column align="center" label="办理人">
            <template #default="scope">
              <template v-if="tab === 'waiting'">
                <template v-if="scope.row.assigneeNames">
                  <el-tag v-for="(name, index) in scope.row.assigneeNames.split(',')" :key="index" type="success">
                    {{ name }}
                  </el-tag>
                </template>
                <template v-else>
                  <el-tag type="success"> 无</el-tag>
                </template>
              </template>
              <template v-else>
                <el-tag type="success"> {{ scope.row.approveName }}</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column align="center" label="流程状态" prop="flowStatus" min-width="70">
            <template #default="scope">
              <dict-tag :options="wf_business_status" :value="scope.row.flowStatus"></dict-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="tab === 'finish'" align="center" label="任务状态" prop="flowTaskStatus" min-width="70">
            <template #default="scope">
              <dict-tag :options="wf_task_status" :value="scope.row.flowTaskStatus"></dict-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" prop="createTime" label="创建时间" width="160"></el-table-column>
          <el-table-column label="操作" align="center" :width="tab === 'finish' ? '88' : '188'">
            <template #default="scope">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5" v-if="tab === 'waiting' || tab === 'finish'">
                  <el-button type="primary" size="small" icon="View" @click="handleView(scope.row)">查看</el-button>
                </el-col>
                <el-col :span="1.5" v-if="tab === 'waiting'">
                  <el-button type="primary" size="small" icon="Setting" @click="handleMeddle(scope.row)">流程干预 </el-button>
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
      </el-tabs>
    </el-card>
    <!-- 选人组件 -->
    <UserSelect ref="userSelectRef" :multiple="userMultiple" @confirm-call-back="submitCallback"></UserSelect>
    <!-- 流程干预组件 -->
    <processMeddle ref="processMeddleRef" @submitCallback="getWaitingList"></processMeddle>
    <!-- 申请人 -->
    <UserSelect ref="applyUserSelectRef" :multiple="true" :data="selectUserIds" @confirm-call-back="userSelectCallBack"></UserSelect>
    <!-- 流程干预组件 -->
    <messageType ref="messageTypeRef" @submitCallback="handleUserTask"></messageType>
  </div>
</template>

<script setup lang="ts">
import { pageByAllTaskWait, pageByAllTaskFinish, updateAssignee, urgeTask } from '@/api/workflow/task';
import UserSelect from '@/components/UserSelect';
import { TaskQuery } from '@/api/workflow/task/types';
import workflowCommon from '@/api/workflow/workflowCommon';
import { RouterJumpVo } from '@/api/workflow/workflowCommon/types';
import processMeddle from '@/components/Process/processMeddle';
import messageType from '@/components/Process/MessageType';
import { UserVO } from '@/api/system/user/types';
import { TabsPaneContext } from 'element-plus';
//选人组件
const userSelectRef = ref<InstanceType<typeof UserSelect>>();
//流程干预组件
const processMeddleRef = ref<InstanceType<typeof processMeddle>>();
//选人组件
const applyUserSelectRef = ref<InstanceType<typeof UserSelect>>();
//消息组件
const messageTypeRef = ref<InstanceType<typeof messageType>>();
const queryFormRef = ref<ElFormInstance>();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_business_status } = toRefs<any>(proxy?.useDict('wf_business_status'));
const { wf_task_status } = toRefs<any>(proxy?.useDict('wf_task_status'));
// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref<Array<any>>([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
const userMultiple = ref(false);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 模型定义表格数据
const taskList = ref([]);
const buttonType = ref('');
//申请人id
const selectUserIds = ref<Array<number | string>>([]);
//申请人选择数量
const userSelectCount = ref(0);
// 查询参数
const queryParams = ref<TaskQuery>({
  pageNum: 1,
  pageSize: 10,
  nodeName: undefined,
  flowName: undefined,
  flowCode: undefined,
  createByIds: []
});
const tab = ref('waiting');

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
  queryParams.value.createByIds = [];
  userSelectCount.value = 0;
  selectUserIds.value = [];
  handleQuery();
};
// 多选框选中数据
const handleSelectionChange = (selection: any) => {
  ids.value = selection.map((item: any) => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};
const changeTab = async (data: TabsPaneContext) => {
  taskList.value = [];
  queryParams.value.pageNum = 1;
  if ('waiting' === data.paneName) {
    getWaitingList();
  } else {
    getFinishList();
  }
};
//分页
const getWaitingList = () => {
  loading.value = true;
  pageByAllTaskWait(queryParams.value).then((resp) => {
    taskList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};
const getFinishList = () => {
  loading.value = true;
  pageByAllTaskFinish(queryParams.value).then((resp) => {
    taskList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};
// 打开催办
const handleUrgeTaskOpen = () => {
  messageTypeRef.value.open();
};
//打开修改选人
const handleUserOpen = () => {
  userSelectRef.value.open();
};

//打开修改选人
const handleUserTask = async (data) => {
  await proxy?.$modal.confirm('是否确认提交？');
  data.taskIdList = ids.value;
  await urgeTask(data);
  messageTypeRef.value.close();
  proxy?.$modal.msgSuccess('操作成功');
  handleQuery();
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
/** 查看按钮操作 */
const handleView = (row) => {
  const routerJumpVo = reactive<RouterJumpVo>({
    businessId: row.businessId,
    taskId: row.id,
    type: 'view',
    formCustom: row.formCustom,
    formPath: row.formPath
  });
  workflowCommon.routerJump(routerJumpVo, proxy);
};
const handleMeddle = (row) => {
  processMeddleRef.value.open(row.id);
};
//打开申请人选择
const openUserSelect = () => {
  applyUserSelectRef.value.open();
};
//确认选择申请人
const userSelectCallBack = (data: UserVO[]) => {
  userSelectCount.value = 0;
  selectUserIds.value = [];
  queryParams.value.createByIds = [];

  if (data && data.length > 0) {
    userSelectCount.value = data.length;
    selectUserIds.value = data.map((item) => item.userId);
    queryParams.value.createByIds = selectUserIds.value;
  }
};
onMounted(() => {
  getWaitingList();
});
</script>
