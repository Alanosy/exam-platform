<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="考试名称" prop="examName">
              <el-input v-model="queryParams.examName" placeholder="请输入考试名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="考试类型" prop="examType">
              <el-select v-model="queryParams.examType" placeholder="请选择考试类型" clearable>
                <el-option v-for="item in examTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
                <el-option v-for="item in examStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="参加方式" prop="participantType">
              <el-select v-model="queryParams.participantType" placeholder="请选择参加方式" clearable>
                <el-option v-for="item in examParticipantTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:exam:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:exam:edit']">配置</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:exam:remove']"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:exam:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="examList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" width="60" align="center">
          <template #default="scope">
            <span>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="考试名称" align="center" prop="examName" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column label="关联试卷" align="center" min-width="160" :show-overflow-tooltip="true">
          <template #default="scope">
            <span>{{ paperNameOf(scope.row.paperId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="考试类型" align="center" prop="examType" width="110">
          <template #default="scope">
            <dict-tag :options="examTypeOptions" :value="scope.row.examType" />
          </template>
        </el-table-column>
        <el-table-column label="考试时间" align="center" min-width="300">
          <template #default="scope">
            <span>{{ formatTime(scope.row.startTime) }} ~ {{ formatTime(scope.row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="限时" align="center" prop="duration" width="110">
          <template #default="scope">
            <span>{{ scope.row.duration ? scope.row.duration + ' 分钟' : '不限时' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="参加方式" align="center" prop="participantType" width="110">
          <template #default="scope">
            <dict-tag :options="examParticipantTypeOptions" :value="scope.row.participantType" />
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="scope">
            <dict-tag :options="examStatusOptions" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建人" align="center" width="120" :show-overflow-tooltip="true">
          <template #default="scope">
            <span>{{ scope.row.creatorName || scope.row.creatorId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="200" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="配置考试" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:exam:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:exam:remove']"></el-button>
            </el-tooltip>
            <!-- 只有公开考试的链接发出去才有用：白名单考试别人凭链接也进不来 -->
            <template v-if="isPublicExam(scope.row)">
              <el-tooltip content="复制考试链接" placement="top">
                <el-button link type="success" icon="Link" @click="copyText(joinLinkOf(scope.row), '考试链接已复制')"></el-button>
              </el-tooltip>
              <el-tooltip :content="scope.row.joinPassword ? '复制参与密码' : '该考试未设置参与密码'" placement="top">
                <el-button
                  link
                  type="warning"
                  icon="Key"
                  :disabled="!scope.row.joinPassword"
                  @click="copyText(scope.row.joinPassword, '参与密码已复制')"
                ></el-button>
              </el-tooltip>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="Exam" lang="ts">
import { useRouter } from 'vue-router';
import { parseTime } from '@/utils/ruoyi';
import { listExam, delExam } from '@/api/system/exam';
import { ExamVO, ExamQuery } from '@/api/system/exam/types';
import { listPaper } from '@/api/system/paper';
import { PaperVO } from '@/api/system/paper/types';
import { useExamDicts } from '@/hooks/useExamDicts';

/** 状态 / 参加方式 / 考试类型都走字典，文案与配色在「字典管理」里改即可 */
const { examTypeOptions, examStatusOptions, examParticipantTypeOptions } = useExamDicts();

/** 考生端加入考试的页面路径，与路由 /exam/join/:code、配置页 JOIN_PATH 保持一致 */
const JOIN_PATH = '/exam/join/';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

/** 只有公开考试（有加入码）才给复制链接与密码 */
const isPublicExam = (row: ExamVO) => row.participantType === 'public' && !!row.joinCode;

/** 拼成可直接发出去的完整链接：带上部署时的上下文路径 */
const joinLinkOf = (row: ExamVO) => `${window.location.origin}${import.meta.env.VITE_APP_CONTEXT_PATH}${JOIN_PATH}${row.joinCode}`;

/**
 * 复制到剪贴板。
 * navigator.clipboard 只在 HTTPS / localhost 安全上下文可用，
 * 开发环境用 http://ip 访问时会静默失败，这里退回 execCommand 兜底。
 */
const copyText = async (text: string, tip: string) => {
  if (!text) return;
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
    } else {
      const input = document.createElement('textarea');
      input.value = text;
      input.style.position = 'fixed';
      input.style.opacity = '0';
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
    }
    proxy?.$modal.msgSuccess(tip);
  } catch {
    proxy?.$modal.msgError('复制失败，请手动复制');
  }
};

const examList = ref<ExamVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();

/** 试卷下拉，用于列表里把 paperId 显示成试卷名 */
const paperOptions = ref<PaperVO[]>([]);
const paperNameOf = (paperId?: string | number) => {
  if (paperId === undefined || paperId === null || paperId === '') return '-';
  return paperOptions.value.find((item) => String(item.id) === String(paperId))?.paperName ?? `#${paperId}`;
};

const queryParams = ref<ExamQuery>({
  pageNum: 1,
  pageSize: 10,
  examName: undefined,
  participantType: undefined,
  examType: undefined,
  status: undefined
});

const formatTime = (value?: string) => (value ? parseTime(value, '{y}-{m}-{d} {h}:{i}') : '-');

/** 查询考试列表 */
const getList = async () => {
  loading.value = true;
  const res = await listExam(queryParams.value);
  examList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

/** 加载试卷下拉 */
const loadPaperOptions = async () => {
  try {
    const res = await listPaper({ pageNum: 1, pageSize: 500 });
    paperOptions.value = res.rows ?? [];
  } catch {
    paperOptions.value = [];
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
const handleSelectionChange = (selection: ExamVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作：跳转到整页考试配置 */
const handleAdd = () => {
  router.push({ name: 'ExamEdit' });
};

/** 修改按钮操作：跳转到整页考试配置 */
const handleUpdate = (row?: ExamVO) => {
  const _id = row?.id || ids.value[0];
  router.push({ name: 'ExamEdit', params: { examId: _id } });
};

/** 删除按钮操作 */
const handleDelete = async (row?: ExamVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除考试编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delExam(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'exam/export',
    {
      ...queryParams.value
    },
    `exam_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  loadPaperOptions();
  getList();
});

/** 从考试配置页返回列表时刷新数据 */
onActivated(() => {
  getList();
});
</script>
