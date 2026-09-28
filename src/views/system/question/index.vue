<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="所属题库" prop="bankId">
              <el-select v-model="queryParams.bankId" filterable clearable placeholder="请选择题库" class="w-[200px]">
                <el-option v-for="item in bankList" :key="item.id" :label="item.bankName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="题干" prop="title">
              <el-input v-model="queryParams.title" placeholder="请输入题干" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="题型" prop="questionType">
              <el-select v-model="queryParams.questionType" clearable placeholder="请选择题型" class="w-[160px]">
                <el-option v-for="item in QUESTION_TYPES" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="queryParams.difficulty" clearable placeholder="请选择难度" class="w-[140px]">
                <el-option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" clearable placeholder="请选择状态" class="w-[120px]">
                <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:question:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:question:edit']"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:question:remove']"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:question:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="questionList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="主键ID" align="center" prop="id" v-if="true" width="90" />
        <el-table-column label="所属题库" align="center" prop="bankId" min-width="140">
          <template #default="{ row }">
            <span>{{ bankNameMap[row.bankId] ?? row.bankId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题干" prop="title" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ plainText(row.title) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题型" align="center" prop="questionType" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ getQuestionTypeLabel(row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" align="center" prop="difficulty" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="difficultyTagType(row.difficulty)" effect="light">{{ getDifficultyLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分值" align="center" prop="score" width="80" />
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)" effect="light">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="120" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:question:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:question:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="Question" lang="ts">
import { useRouter } from 'vue-router';
import { listQuestion, delQuestion } from '@/api/system/question';
import { QuestionVO, QuestionQuery } from '@/api/system/question/types';
import { listBank } from '@/api/system/bank';
import { BankVO } from '@/api/system/bank/types';
import { DIFFICULTY_OPTIONS, QUESTION_TYPES, STATUS_OPTIONS, getDifficultyLabel, getQuestionTypeLabel, getStatusLabel } from './questionMeta';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const questionList = ref<QuestionVO[]>([]);
const bankList = ref<BankVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();

const queryParams = ref<QuestionQuery>({
  pageNum: 1,
  pageSize: 10,
  bankId: undefined,
  title: undefined,
  questionType: undefined,
  difficulty: undefined,
  status: undefined,
  params: {}
});

const bankNameMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {};
  bankList.value.forEach((item) => {
    map[String(item.id)] = item.bankName;
  });
  return map;
});

/** 富文本转纯文本，用于列表展示 */
const plainText = (html?: string): string => {
  if (!html) return '';
  return html
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim();
};

const difficultyTagType = (value?: string) => {
  if (value === 'easy') return 'success';
  if (value === 'medium') return 'warning';
  if (value === 'hard') return 'danger';
  return 'info';
};

const statusTagType = (value?: number) => {
  if (value === 1) return 'success';
  if (value === 2) return 'info';
  return 'warning';
};

/** 查询试题主列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listQuestion(queryParams.value);
    questionList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

/** 加载题库下拉 */
const loadBankList = async () => {
  try {
    const res = await listBank({ pageNum: 1, pageSize: 500 });
    bankList.value = res.rows ?? [];
  } catch {
    bankList.value = [];
  }
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
const handleSelectionChange = (selection: QuestionVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作：跳转到整页编辑 */
const handleAdd = () => {
  router.push({ name: 'QuestionEdit' });
};

/** 修改按钮操作：跳转到整页编辑 */
const handleUpdate = (row?: QuestionVO) => {
  const _id = row?.id || ids.value[0];
  router.push({ name: 'QuestionEdit', params: { questionId: _id } });
};

/** 删除按钮操作 */
const handleDelete = async (row?: QuestionVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除试题主编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delQuestion(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'question/export',
    {
      ...queryParams.value
    },
    `question_${new Date().getTime()}.xlsx`
  );
};

onMounted(async () => {
  await loadBankList();
  await getList();
});

/** 从编辑页返回列表时刷新数据 */
onActivated(() => {
  getList();
});
</script>
