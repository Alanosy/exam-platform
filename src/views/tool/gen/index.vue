<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="数据源" prop="dataName">
              <el-select v-model="queryParams.dataName" filterable clearable placeholder="请选择/输入数据源名称">
                <el-option key="" label="全部" value="" />
                <el-option v-for="item in dataNameList" :key="item" :label="item" :value="item"> </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="表名称" prop="tableName">
              <el-input v-model="queryParams.tableName" placeholder="请输入表名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="表描述" prop="tableComment">
              <el-input v-model="queryParams.tableComment" placeholder="请输入表描述" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="创建时间" style="width: 308px">
              <el-date-picker
                v-model="dateRange"
                value-format="YYYY-MM-DD"
                type="daterange"
                range-separator="-"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              ></el-date-picker>
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
            <el-button v-hasPermi="['tool:gen:code']" type="primary" plain icon="Download" @click="handleGenTable()">生成</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['tool:gen:import']" type="info" plain icon="Upload" @click="openImportTable">导入</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['tool:gen:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleEditTable()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['tool:gen:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">
              删除
            </el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="tableList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" align="center" width="55"></el-table-column>
        <el-table-column label="序号" type="index" width="50" align="center">
          <template #default="scope">
            <span>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数据源" align="center" prop="dataName" :show-overflow-tooltip="true" />
        <el-table-column label="表名称" align="center" prop="tableName" :show-overflow-tooltip="true" />
        <el-table-column label="表描述" align="center" prop="tableComment" :show-overflow-tooltip="true" />
        <el-table-column label="实体" align="center" prop="className" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
        <el-table-column label="更新时间" align="center" prop="updateTime" width="160" />
        <el-table-column label="操作" align="center" width="330" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="预览" placement="top">
              <el-button v-hasPermi="['tool:gen:preview']" link type="primary" icon="View" @click="handlePreview(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="编辑" placement="top">
              <el-button v-hasPermi="['tool:gen:edit']" link type="primary" icon="Edit" @click="handleEditTable(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['tool:gen:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="同步" placement="top">
              <el-button v-hasPermi="['tool:gen:edit']" link type="primary" icon="Refresh" @click="handleSynchDb(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="生成代码" placement="top">
              <el-button v-hasPermi="['tool:gen:code']" link type="primary" icon="Download" @click="handleGenTable(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 预览界面 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="80%" top="5vh" append-to-body class="scrollbar">
      <el-tabs v-model="preview.activeName">
        <el-tab-pane
          v-for="(value, key) in preview.data"
          :key="value"
          :label="key.substring(key.lastIndexOf('/') + 1, key.indexOf('.vm'))"
          :name="key.substring(key.lastIndexOf('/') + 1, key.indexOf('.vm'))"
        >
          <el-link v-copyText="value" v-copyText:callback="copyTextSuccess" :underline="false" icon="DocumentCopy" style="float: right">
            &nbsp;复制
          </el-link>
          <pre>{{ value }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
    <import-table ref="importRef" @ok="handleQuery" />
  </div>
</template>

<script setup name="Gen" lang="ts">
import { listTable, previewTable, delTable, genCode, synchDb, getDataNames } from '@/api/tool/gen';
import { TableQuery, TableVO } from '@/api/tool/gen/types';
import router from '@/router';
import ImportTable from './importTable.vue';

const route = useRoute();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const tableList = ref<TableVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const dateRange = ref<[DateModelType, DateModelType]>(['', '']);
const uniqueId = ref('');
const dataNameList = ref<Array<string>>([]);

const queryFormRef = ref<ElFormInstance>();
const importRef = ref<InstanceType<typeof ImportTable>>();

const queryParams = ref<TableQuery>({
  pageNum: 1,
  pageSize: 10,
  tableName: '',
  tableComment: '',
  dataName: ''
});

const preview = ref<{
  data: Record<string, string>;
  activeName: string;
}>({
  data: {},
  activeName: 'domain.java'
});
const dialog = reactive<DialogOption>({
  visible: false,
  title: '代码预览'
});

onActivated(() => {
  const time = route.query.t;
  if (time != null && time != uniqueId.value) {
    uniqueId.value = time as string;
    queryParams.value.pageNum = Number(route.query.pageNum);
    dateRange.value = ['', ''];
    queryFormRef.value?.resetFields();
    getList();
  }
});

/** 查询多数据源名称 */
const getDataNameList = async () => {
  const res = await getDataNames();
  dataNameList.value = res.data;
};

/** 查询表集合 */
const getList = async () => {
  loading.value = true;
  const res = await listTable(proxy?.addDateRange(queryParams.value, dateRange.value));
  tableList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
/** 生成代码操作 */
const handleGenTable = async (row?: TableVO) => {
  const tbIds = row?.tableId || ids.value;
  if (tbIds == '') {
    proxy?.$modal.msgError('请选择要生成的数据');
    return;
  }
  if (row?.genType === '1') {
    await genCode(row.tableId);
    proxy?.$modal.msgSuccess('成功生成到自定义路径：' + row.genPath);
  } else {
    proxy?.$download.zip('/tool/gen/batchGenCode?tableIdStr=' + tbIds, 'ruoyi.zip');
  }
};
/** 同步数据库操作 */
const handleSynchDb = async (row: TableVO) => {
  const tableId = row.tableId;
  await proxy?.$modal.confirm('确认要强制同步"' + row.tableName + '"表结构吗？');
  await synchDb(tableId);
  proxy?.$modal.msgSuccess('同步成功');
};
/** 打开导入表弹窗 */
const openImportTable = () => {
  importRef.value?.show(queryParams.value.dataName);
};
/** 重置按钮操作 */
const resetQuery = () => {
  dateRange.value = ['', ''];
  queryFormRef.value?.resetFields();
  handleQuery();
};
/** 预览按钮 */
const handlePreview = async (row: TableVO) => {
  const res = await previewTable(row.tableId);
  preview.value.data = res.data;
  dialog.visible = true;
  preview.value.activeName = 'domain.java';
};
/** 复制代码成功 */
const copyTextSuccess = () => {
  proxy?.$modal.msgSuccess('复制成功');
};
// 多选框选中数据
const handleSelectionChange = (selection: TableVO[]) => {
  ids.value = selection.map((item) => item.tableId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};
/** 修改按钮操作 */
const handleEditTable = (row?: TableVO) => {
  const tableId = row?.tableId || ids.value[0];
  router.push({ path: '/tool/gen-edit/index/' + tableId, query: { pageNum: queryParams.value.pageNum } });
};
/** 删除按钮操作 */
const handleDelete = async (row?: TableVO) => {
  const tableIds = row?.tableId || ids.value;
  await proxy?.$modal.confirm('是否确认删除表编号为"' + tableIds + '"的数据项？');
  await delTable(tableIds);
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

onMounted(() => {
  getList();
  getDataNameList();
});
</script>
