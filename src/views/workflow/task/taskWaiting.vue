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
            <el-form-item label="流程定义编码" label-width="100" prop="flowCode">
              <el-input v-model="queryParams.flowCode" placeholder="请输入流程定义编码" @keyup.enter="handleQuery" />
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
          <right-toolbar v-model:show-search="showSearch" @query-table="handleQuery"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="taskList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column align="center" type="index" label="序号" width="60"></el-table-column>
        <el-table-column :show-overflow-tooltip="true" prop="businessCode" align="center" label="业务编码"></el-table-column>
        <el-table-column :show-overflow-tooltip="true" prop="businessTitle" align="center" label="业务标题"></el-table-column>
        <el-table-column :show-overflow-tooltip="true" prop="flowName" align="center" label="流程定义名称"></el-table-column>
        <el-table-column align="center" prop="flowCode" label="流程定义编码"></el-table-column>
        <el-table-column align="center" prop="categoryName" label="流程分类"></el-table-column>
        <el-table-column align="center" prop="nodeName" label="任务名称"></el-table-column>
        <el-table-column align="center" prop="createByName" label="申请人"></el-table-column>
        <el-table-column align="center" label="办理人">
          <template #default="scope">
            <template v-if="scope.row.assigneeNames">
              <el-tag v-for="(name, index) in scope.row.assigneeNames.split(',')" :key="index" type="success">
                {{ name }}
              </el-tag>
            </template>
            <template v-else>
              <el-tag type="success"> 无</el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column align="center" label="流程状态" prop="flowStatusName" min-width="70">
          <template #default="scope">
            <dict-tag :options="wf_business_status" :value="scope.row.flowStatus"></dict-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" icon="Edit" @click="handleOpen(scope.row)">办理</el-button>
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
    <!-- 申请人 -->
    <UserSelect ref="userSelectRef" :multiple="true" :data="selectUserIds" @confirm-call-back="userSelectCallBack"></UserSelect>
  </div>
</template>

<script setup lang="ts">
import { pageByTaskWait } from '@/api/workflow/task';
import { TaskQuery, FlowTaskVO } from '@/api/workflow/task/types';
import workflowCommon from '@/api/workflow/workflowCommon';
import { RouterJumpVo } from '@/api/workflow/workflowCommon/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_business_status } = toRefs<any>(proxy?.useDict('wf_business_status'));
import UserSelect from '@/components/UserSelect';
import { ref } from 'vue';
import { UserVO } from '@/api/system/user/types';

const userSelectRef = ref<InstanceType<typeof UserSelect>>();
//提交组件
const queryFormRef = ref<ElFormInstance>();
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
onMounted(() => {
  getWaitingList();
});
/** 搜索按钮操作 */
const handleQuery = () => {
  getWaitingList();
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
//分页
const getWaitingList = () => {
  loading.value = true;
  pageByTaskWait(queryParams.value).then((resp) => {
    taskList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};
//办理
const handleOpen = async (row: FlowTaskVO) => {
  const routerJumpVo = reactive<RouterJumpVo>({
    businessId: row.businessId,
    taskId: row.id,
    type: 'approval',
    formCustom: row.formCustom,
    formPath: row.formPath
  });
  workflowCommon.routerJump(routerJumpVo, proxy);
};
//打开申请人选择
const openUserSelect = () => {
  userSelectRef.value.open();
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
</script>
