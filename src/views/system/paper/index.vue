<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="试卷名称" prop="paperName">
              <el-input v-model="queryParams.paperName" placeholder="请输入试卷名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="组卷模式" prop="paperType">
              <el-select v-model="queryParams.paperType" placeholder="请选择组卷模式" clearable>
                <el-option v-for="item in paperTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="可见性" prop="visibility">
              <el-select v-model="queryParams.visibility" placeholder="请选择可见性" clearable>
                <el-option v-for="item in visibilityOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:paper:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:paper:edit']">组卷</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:paper:remove']"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:paper:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="paperList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" width="60" align="center">
          <template #default="scope">
            <span>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="试卷名称" align="center" prop="paperName" :show-overflow-tooltip="true" />
        <el-table-column label="试卷描述" align="center" prop="paperDesc" :show-overflow-tooltip="true" />
        <el-table-column label="组卷模式" align="center" prop="paperType">
          <template #default="scope">
            <el-tag :type="scope.row.paperType === 'RANDOM' ? 'warning' : 'info'">
              {{ getOptionLabel(paperTypeOptions, scope.row.paperType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="试卷总分" align="center" prop="totalScore" />
        <el-table-column label="及格分数" align="center" prop="passScore" />
        <el-table-column label="考试时长" align="center" prop="timeLimit">
          <template #default="scope">
            <span>{{ scope.row.timeLimit === 0 || scope.row.timeLimit === null ? '不限时' : scope.row.timeLimit + ' 分钟' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getOptionLabel(statusOptions, scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可见性" align="center" prop="visibility">
          <template #default="scope">
            <el-tag :type="scope.row.visibility === 'public' ? 'success' : 'info'">
              {{ getOptionLabel(visibilityOptions, scope.row.visibility) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分享过期时间" align="center" prop="shareExpireTime" width="180">
          <template #default="scope">
            <span>{{ scope.row.shareExpireTime ? parseTime(scope.row.shareExpireTime, '{y}-{m}-{d}') : '永久有效' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建人" align="center" prop="creatorName" width="120" :show-overflow-tooltip="true">
          <template #default="scope">
            <span>{{ scope.row.creatorName || scope.row.creatorId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="组卷" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:paper:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:paper:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="Paper" lang="ts">
import { useRouter } from 'vue-router';
import { listPaper, delPaper } from '@/api/system/paper';
import { PaperVO, PaperQuery } from '@/api/system/paper/types';

type Option = { label: string; value: string };

/** 组卷模式：MANUAL手动选题 / RANDOM随机抽题 */
const paperTypeOptions: Option[] = [
  { label: '手动选题', value: 'MANUAL' },
  { label: '随机抽题', value: 'RANDOM' }
];
/** 试卷状态：draft草稿 / ready已组卷 / archived归档 */
const statusOptions: Option[] = [
  { label: '草稿', value: 'draft' },
  { label: '已组卷', value: 'ready' },
  { label: '归档', value: 'archived' }
];
/** 可见性：private私有 / public公开 */
const visibilityOptions: Option[] = [
  { label: '私有', value: 'private' },
  { label: '公开', value: 'public' }
];

const getOptionLabel = (options: Option[], value: string) => options.find((item) => item.value === value)?.label ?? value;

const getStatusTagType = (status: string) => {
  if (status === 'ready') return 'success';
  if (status === 'archived') return 'info';
  return 'warning';
};

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const paperList = ref<PaperVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();

const queryParams = ref<PaperQuery>({
  pageNum: 1,
  pageSize: 10,
  paperName: undefined,
  paperType: undefined,
  status: undefined,
  visibility: undefined,
  params: {}
});

/** 查询试卷列表 */
const getList = async () => {
  loading.value = true;
  const res = await listPaper(queryParams.value);
  paperList.value = res.rows;
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
const handleSelectionChange = (selection: PaperVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作：跳转到整页组卷 */
const handleAdd = () => {
  router.push({ name: 'PaperEdit' });
};

/** 修改按钮操作：跳转到整页组卷 */
const handleUpdate = (row?: PaperVO) => {
  const _id = row?.id || ids.value[0];
  router.push({ name: 'PaperEdit', params: { paperId: _id } });
};

/** 删除按钮操作 */
const handleDelete = async (row?: PaperVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除试卷编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delPaper(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'paper/export',
    {
      ...queryParams.value
    },
    `paper_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});

/** 从组卷页返回列表时刷新数据 */
onActivated(() => {
  getList();
});
</script>
