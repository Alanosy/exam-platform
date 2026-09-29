<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="85%" top="5vh" append-to-body class="bank-question-manage-dialog">
    <el-card shadow="hover" class="mb-[10px]">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="题干" prop="title">
          <el-input v-model="queryParams.title" placeholder="请输入题干关键词" clearable @keyup.enter="handleQuery" />
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

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:question:add']">新增试题</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Promotion" :disabled="multiple" @click="handleMoveBank" v-hasPermi="['system:question:edit']"
              >切换题库</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:question:remove']"
              >移除试题</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:question:export']">导出</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Refresh" @click="getList">刷新</el-button>
          </el-col>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="questionList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" align="center" width="60" type="index" :index="indexMethod" />
        <el-table-column label="题干" prop="title" min-width="280" show-overflow-tooltip>
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
        <el-table-column label="操作" align="center" fixed="right" width="140" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['system:question:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="切换题库" placement="top">
              <el-button
                link
                type="primary"
                icon="Promotion"
                @click="handleMoveBank(scope.row.id)"
                v-hasPermi="['system:question:edit']"
              ></el-button>
            </el-tooltip>
            <el-tooltip content="移除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:question:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 切换题库 -->
    <el-dialog v-model="moveDialog.visible" title="切换题库" width="500px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="目标题库">
          <el-select v-model="moveDialog.bankId" filterable placeholder="请选择目标题库" class="w-full">
            <el-option v-for="item in bankOptions" :key="item.id" :label="item.bankName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="已选试题">
          <span>{{ moveDialog.ids.length }} 条</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="moveLoading" type="primary" @click="submitMoveBank">确 定</el-button>
          <el-button @click="moveDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup name="BankQuestionManage" lang="ts">
import { useRouter } from 'vue-router';
import { listQuestion, delQuestion, changeQuestionBank } from '@/api/system/question';
import { QuestionVO, QuestionQuery } from '@/api/system/question/types';
import { listBank } from '@/api/system/bank';
import { BankVO } from '@/api/system/bank/types';
import { DIFFICULTY_OPTIONS, QUESTION_TYPES, STATUS_OPTIONS, getDifficultyLabel, getQuestionTypeLabel, getStatusLabel } from '@/views/system/question/questionMeta';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const visible = defineModel<boolean>('visible', { default: false });

const props = withDefaults(
  defineProps<{
    /** 当前题库ID */
    bankId?: string | number;
    /** 当前题库名称，用于标题展示 */
    bankName?: string;
  }>(),
  {
    bankId: undefined,
    bankName: ''
  }
);

const questionList = ref<QuestionVO[]>([]);
const bankOptions = ref<BankVO[]>([]);
const loading = ref(false);
const moveLoading = ref(false);
const total = ref(0);
const ids = ref<Array<string | number>>([]);
const multiple = ref(true);

const queryFormRef = ref<ElFormInstance>();

const dialogTitle = computed(() => {
  return props.bankName ? `题库「${props.bankName}」试题管理` : '题库试题管理';
});

const queryParams = ref<QuestionQuery>({
  pageNum: 1,
  pageSize: 10,
  bankId: props.bankId,
  title: undefined,
  questionType: undefined,
  difficulty: undefined,
  status: undefined,
  params: {}
});

/** 表格序号（跨页连续自增） */
const indexMethod = (index: number) => {
  return (queryParams.value.pageNum - 1) * queryParams.value.pageSize + index + 1;
};

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

/** 查询当前题库下的试题 */
const getList = async () => {
  if (!props.bankId) {
    questionList.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  try {
    queryParams.value.bankId = props.bankId;
    const res = await listQuestion(queryParams.value);
    questionList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

/** 加载题库下拉（切换题库用），排除当前题库 */
const loadBankOptions = async () => {
  try {
    const res = await listBank({ pageNum: 1, pageSize: 500 });
    bankOptions.value = (res.rows ?? []).filter((item) => String(item.id) !== String(props.bankId));
  } catch {
    bankOptions.value = [];
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
  multiple.value = !selection.length;
};

/** 新增试题：跳到整页编辑并带上当前题库 */
const handleAdd = () => {
  visible.value = false;
  router.push({ name: 'QuestionEdit', query: { bankId: String(props.bankId ?? '') } });
};

/** 编辑试题：跳到整页编辑 */
const handleEdit = (row: QuestionVO) => {
  visible.value = false;
  router.push({ name: 'QuestionEdit', params: { questionId: row.id } });
};

const moveDialog = reactive<{ visible: boolean; bankId?: string | number; ids: Array<string | number> }>({
  visible: false,
  bankId: undefined,
  ids: []
});

/** 打开切换题库弹窗（传 id 则只移动该条，否则移动表格勾选项） */
const handleMoveBank = (id?: string | number) => {
  const targetIds = id ? [id] : ids.value;
  if (!targetIds.length) {
    proxy?.$modal.msgWarning('请先选择试题');
    return;
  }
  moveDialog.ids = targetIds;
  moveDialog.bankId = undefined;
  moveDialog.visible = true;
};

/** 提交切换题库 */
const submitMoveBank = async () => {
  if (!moveDialog.bankId) {
    proxy?.$modal.msgWarning('请选择目标题库');
    return;
  }
  moveLoading.value = true;
  try {
    await changeQuestionBank(moveDialog.ids, moveDialog.bankId);
    proxy?.$modal.msgSuccess('切换成功');
    moveDialog.visible = false;
    await getList();
  } finally {
    moveLoading.value = false;
  }
};

/** 移除试题（从题库中删除，只有勾选/传入的行会被删） */
const handleDelete = async (row?: QuestionVO) => {
  const _ids = row?.id ? [row.id] : ids.value;
  if (!_ids.length) {
    proxy?.$modal.msgWarning('请先选择试题');
    return;
  }
  await proxy?.$modal.confirm('是否确认移除试题编号为"' + _ids + '"的数据项？');
  await delQuestion(_ids);
  proxy?.$modal.msgSuccess('移除成功');
  await getList();
};

/** 导出按钮操作（导出当前筛选结果） */
const handleExport = () => {
  proxy?.download(
    'question/export',
    {
      ...queryParams.value
    },
    `question_${new Date().getTime()}.xlsx`
  );
};

/** 弹窗打开/题库切换时重新加载数据 */
watch(
  () => [visible.value, props.bankId],
  async ([isVisible]) => {
    if (!isVisible) return;
    queryParams.value = {
      pageNum: 1,
      pageSize: 10,
      bankId: props.bankId,
      title: undefined,
      questionType: undefined,
      difficulty: undefined,
      status: undefined,
      params: {}
    };
    ids.value = [];
    multiple.value = true;
    await loadBankOptions();
    await getList();
  },
  { immediate: true }
);
</script>
