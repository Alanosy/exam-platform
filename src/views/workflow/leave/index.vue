<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="search">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
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
            <el-button v-hasPermi="['workflow:leave:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['workflow:leave:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
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
        <el-table-column align="center" label="流程状态" min-width="70">
          <template #default="scope">
            <dict-tag :options="wf_business_status" :value="scope.row.status"></dict-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'draft' || scope.row.status === 'cancel' || scope.row.status === 'back'"
              v-hasPermi="['workflow:leave:edit']"
              size="small"
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(scope.row)"
              >修改</el-button
            >
            <el-button
              v-if="scope.row.status === 'draft' || scope.row.status === 'cancel' || scope.row.status === 'back'"
              v-hasPermi="['workflow:leave:remove']"
              size="small"
              link
              type="primary"
              icon="Delete"
              @click="handleDelete(scope.row)"
              >删除</el-button
            >
            <el-button link type="primary" size="small" icon="View" @click="handleView(scope.row)">查看</el-button>
            <el-button
              v-if="scope.row.status === 'waiting'"
              link
              size="small"
              type="primary"
              icon="Notification"
              @click="handleCancelProcessApply(scope.row.id)"
              >撤销</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="Leave" lang="ts">
import { delLeave, listLeave } from '@/api/workflow/leave';
import { cancelProcessApply } from '@/api/workflow/processInstance';
import { LeaveForm, LeaveQuery, LeaveVO } from '@/api/workflow/leave/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_business_status } = toRefs<any>(proxy?.useDict('wf_business_status'));
const leaveList = ref<LeaveVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
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

const queryFormRef = ref<ElFormInstance>();

const data = reactive<PageData<LeaveForm, LeaveQuery>>({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    startLeaveDays: undefined,
    endLeaveDays: undefined
  },
  rules: {}
});

const { queryParams } = toRefs(data);

/** 查询请假列表 */
const getList = async () => {
  loading.value = true;
  const res = await listLeave(queryParams.value);
  leaveList.value = res.rows;
  total.value = res.total;
  loading.value = false;
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
  proxy.$tab.closePage(proxy.$route);
  proxy.$router.push(`/workflow/leaveEdit/index/add/add`);
  proxy.$router.push({
    path: `/workflow/leaveEdit/index`,
    query: {
      type: 'add'
    }
  });
};

/** 修改按钮操作 */
const handleUpdate = (row?: LeaveVO) => {
  proxy.$tab.closePage(proxy.$route);
  proxy.$router.push({
    path: `/workflow/leaveEdit/index`,
    query: {
      id: row.id,
      type: 'update'
    }
  });
};

/** 查看按钮操作 */
const handleView = (row?: LeaveVO) => {
  proxy.$tab.closePage(proxy.$route);
  proxy.$router.push({
    path: `/workflow/leaveEdit/index`,
    query: {
      id: row.id,
      type: 'view'
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
    'workflow/leave/export',
    {
      ...queryParams.value
    },
    `leave_${new Date().getTime()}.xlsx`
  );
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
