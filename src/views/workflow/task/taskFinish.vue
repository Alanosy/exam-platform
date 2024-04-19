<template>
  <div class="p-2">
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
          <template #default="scope">
            <el-tag type="success">
              {{ scope.row.assigneeName || '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="startTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" icon="View" @click="handleView(scope.row)">查看</el-button>
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
  </div>
</template>

<script lang="ts" setup>
import { getPageByTaskFinish } from '@/api/workflow/task';
import { TaskQuery, TaskVO } from '@/api/workflow/task/types';
import workflowCommon from '@/api/workflow/workflowCommon';
import { RouterJumpVo } from '@/api/workflow/workflowCommon/types';
//审批记录组件
const queryFormRef = ref<ElFormInstance>();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
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
// 查询参数
const queryParams = ref<TaskQuery>({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  processDefinitionName: undefined,
  processDefinitionKey: undefined
});
/** 搜索按钮操作 */
const handleQuery = () => {
  getFinishList();
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
const getFinishList = () => {
  loading.value = true;
  getPageByTaskFinish(queryParams.value).then((resp) => {
    taskList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};
/** 查看按钮操作 */
const handleView = (row: TaskVO) => {
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
  getFinishList();
});
</script>
