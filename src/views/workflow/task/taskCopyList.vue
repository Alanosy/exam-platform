<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" label-width="68px">
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
        <el-table-column fixed align="center" type="index" label="序号" width="60"></el-table-column>
        <el-table-column fixed align="center" prop="processDefinitionName" label="流程定义名称"></el-table-column>
        <el-table-column fixed align="center" prop="processDefinitionKey" label="流程定义KEY"></el-table-column>
        <el-table-column fixed align="center" prop="name" label="任务名称"></el-table-column>
        <el-table-column fixed align="center" prop="assigneeName" label="办理人">
          <template #default="scope">
            <template v-if="scope.row.participantVo && scope.row.assignee === null">
              <el-tag v-for="(item, index) in scope.row.participantVo.candidateName" :key="index" type="success">
                {{ item }}
              </el-tag>
            </template>
            <template v-else>
              <el-tag type="success">
                {{ scope.row.assigneeName }}
              </el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="businessStatusName" label="流程状态" min-width="70">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.businessStatusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" size="small" icon="Document" @click="handleApprovalRecord(scope.row)">审批记录</el-button>
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
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script lang="ts" setup>
import { getPageByTaskCopy} from '@/api/workflow/task';
import ApprovalRecord from '@/components/Process/approvalRecord.vue';
import { TaskQuery, TaskVO } from '@/api/workflow/task/types';
//审批记录组件
const approvalRecordRef = ref<InstanceType<typeof ApprovalRecord>>();
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
onMounted(() => {
  getTaskCopyList();
});
//审批记录
const handleApprovalRecord = (row: TaskVO) => {
  if (approvalRecordRef.value) {
    approvalRecordRef.value.init(row.processInstanceId);
  }
};
/** 搜索按钮操作 */
const handleQuery = () => {
  getTaskCopyList();
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
//分页
const getTaskCopyList = () => {
  loading.value = true;
  getPageByTaskCopy(queryParams.value).then((resp) => {
    taskList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};

</script>
