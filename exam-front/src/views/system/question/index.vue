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
                <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="queryParams.difficulty" clearable placeholder="请选择难度" class="w-[140px]">
                <el-option v-for="item in questionDifficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" clearable placeholder="请选择状态" class="w-[120px]">
                <el-option v-for="item in questionStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
            <el-button type="info" plain icon="Top" @click="handleImport" v-hasPermi="['system:question:add']">导入</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:question:export']">导出</el-button>
          </el-col>
          <el-col :span="1.5">
            <!-- AI 出题：服务不可用时按钮保留，点了会明确提示未启动，而不是静默失败 -->
            <el-button type="primary" plain icon="MagicStick" @click="aiGenVisible = true" v-hasPermi="['system:question:add']">AI 出题</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="questionList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" align="center" width="60" type="index" :index="indexMethod" />
        <el-table-column label="所属题库" align="center" prop="bankId" min-width="140">
          <template #default="{ row }">
            <span>{{ row.bankName || bankNameMap[row.bankId] || row.bankId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题干" prop="title" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ plainText(row.title) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题型" align="center" prop="questionType" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ questionTypeLabel(row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" align="center" prop="difficulty" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="questionDifficultyTagType(row.difficulty)" effect="light">{{
              questionDifficultyLabel(row.difficulty)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分值" align="center" prop="score" width="80" />
        <el-table-column label="知识点" align="center" min-width="160">
          <template #default="{ row }">
            <template v-if="row.knowledgeNames && row.knowledgeNames.length > 0">
              <el-tag v-for="name in row.knowledgeNames" :key="name" size="small" effect="plain" class="mr-1 mb-1">{{ name }}</el-tag>
            </template>
            <span v-else class="text-[#c0c4cc]">未标注</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="questionStatusTagType(row.status)" effect="light">{{ questionStatusLabel(row.status) }}</el-tag>
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

    <!-- 试题导入对话框 -->
    <el-dialog v-model="upload.open" :title="upload.title" width="640px" append-to-body>
      <el-form label-width="92px">
        <el-form-item label="默认题库">
          <el-select v-model="upload.bankId" filterable clearable placeholder="Excel 未填写题库名称时，导入到这个题库" class="w-full">
            <el-option v-for="item in bankList" :key="item.id" :label="item.bankName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-alert type="info" :closable="false" class="mb-[10px]">
        <div class="text-[12px] leading-[20px]">
          <div>1. 每行一道题。<b>题库名称</b>填中文名称即可，后端自动翻译成题库ID；每行可以填不同题库，留空则落到上面选的默认题库。</div>
          <div>2. 单选 / 多选：填「选项A~选项F」，正确答案填选项标识，多个用英文逗号分隔，如 <b>A,C</b>。</div>
          <div>3. 判断题：不用填选项，正确答案填 <b>正确</b> 或 <b>错误</b>。</div>
          <div>4. 填空题：多个空用 <b>|</b> 分隔，同一个空的多种可接受写法用 <b>;</b> 分隔。</div>
          <div>5. 匹配题：多组用 <b>;</b> 分隔，每组按 <b>左项=右项</b> 填写。</div>
          <div>6. 简答 / 论述 / 文件上传 / 代码题：正确答案列直接填参考答案文本。</div>
          <div>7. 任意一行校验不通过会整批回滚，按提示改完重新上传即可，不会产生半份数据。</div>
        </div>
      </el-alert>

      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="uploadAction"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload">
          <i-ep-upload-filled />
        </el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="text-center el-upload__tip">
            <span>仅允许导入 xls、xlsx 格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- AI 出题：生成结果可逐题编辑后再入库，AI 不直接写题库 -->
    <ai-generate-drawer v-model="aiGenVisible" :bank-list="bankList" :default-bank-id="queryParams.bankId" @saved="getList" />
  </div>
</template>

<script setup name="Question" lang="ts">
import { useRouter } from 'vue-router';
import { listQuestion, delQuestion } from '@/api/system/question';
import { QuestionVO, QuestionQuery } from '@/api/system/question/types';
import { listBank } from '@/api/system/bank';
import { BankVO } from '@/api/system/bank/types';
import { globalHeaders } from '@/utils/request';
import { useQuestionDicts } from './useQuestionDict';
import AiGenerateDrawer from './AiGenerateDrawer.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

// 题型 / 难度 / 状态统一走字典（question_type / question_difficulty / question_status）
const {
  questionTypeOptions,
  questionDifficultyOptions,
  questionStatusOptions,
  questionTypeLabel,
  questionDifficultyLabel,
  questionStatusLabel,
  questionDifficultyTagType,
  questionStatusTagType
} = useQuestionDicts();

const questionList = ref<QuestionVO[]>([]);
const bankList = ref<BankVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
/** AI 出题抽屉 */
const aiGenVisible = ref(false);
const uploadRef = ref<ElUploadInstance>();

/** 试题导入参数 */
const upload = reactive<ImportOption>({
  // 是否显示弹出层（试题导入）
  open: false,
  // 弹出层标题
  title: '',
  // 是否禁用上传
  isUploading: false,
  updateSupport: 0,
  // Excel 里没填题库名称时落到的默认题库
  bankId: undefined,
  // 设置上传的请求头部
  headers: globalHeaders(),
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + '/question/importData'
});

/** 上传地址带上默认题库，用户改题库后下一次提交即生效 */
const uploadAction = computed(() => {
  return upload.bankId ? `${upload.url}?bankId=${upload.bankId}` : upload.url;
});

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

/** 表格序号（跨页连续自增） */
const indexMethod = (index: number) => {
  return (queryParams.value.pageNum - 1) * queryParams.value.pageSize + index + 1;
};

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

/** 打开导入对话框 */
const handleImport = () => {
  upload.title = '试题导入';
  upload.open = true;
};

/** 下载导入模板 */
const importTemplate = () => {
  proxy?.download('question/importTemplate', {}, `question_template_${new Date().getTime()}.xlsx`);
};

/** 文件上传中处理 */
const handleFileUploadProgress = () => {
  upload.isUploading = true;
};

/** 文件上传成功处理：导入是整批校验整批入库，失败时把每一行的原因都展示出来 */
const handleFileSuccess = (response: any, file: UploadFile) => {
  upload.isUploading = false;
  uploadRef.value?.handleRemove(file);
  const result = (response?.msg ?? '').toString();
  if (response?.code === 200) {
    upload.open = false;
    ElMessageBox.alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + result + '</div>', '导入结果', {
      dangerouslyUseHTMLString: true
    });
    getList();
    return;
  }
  ElMessageBox.alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + result + '</div>', '导入失败', {
    dangerouslyUseHTMLString: true
  });
};

/** 提交上传文件 */
const submitFileForm = () => {
  uploadRef.value?.submit();
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
