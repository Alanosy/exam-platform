<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2 min-w-0">
            <el-button link icon="ArrowLeft" @click="goBack">返回</el-button>
            <span class="font-medium">错题明细</span>
            <dict-tag :options="wrongSourceTypeOptions" :value="sourceType" />
            <dict-tag :options="examTypeOptions" :value="examType" />
            <span class="source-name" :title="sourceName">{{ sourceName || '全部来源' }}</span>
          </div>
          <div class="flex items-center gap-2">
            <!-- 有来源筛选时给个出口，否则「是真没有」还是「被条件筛没了」分不清 -->
            <el-button v-if="sourceId" plain @click="viewAllSources">查看全部来源</el-button>
            <el-button plain icon="MagicStick" :disabled="total === 0" @click="aiVisible = true">AI 诊断</el-button>
            <el-button plain icon="RefreshRight" :disabled="total === 0" @click="practiceAll">一键重刷</el-button>
            <el-button plain icon="Refresh" @click="getList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :model="queryParams" :inline="true" label-width="68px">
        <el-form-item label="掌握状态">
          <el-select v-model="queryParams.masterStatus" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in wrongMasterStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="queryParams.includeIgnored" label="含已忽略" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="题型" align="center" width="100">
          <template #default="{ row }">
            <dict-tag :options="questionTypeOptions" :value="row.questionType" />
          </template>
        </el-table-column>
        <el-table-column label="难度" align="center" width="90">
          <template #default="{ row }">
            <dict-tag :options="questionDifficultyOptions" :value="row.difficulty" />
          </template>
        </el-table-column>
        <el-table-column label="题干" min-width="280">
          <template #default="{ row }">
            <span class="question-title" :title="plainText(row.title)">{{ plainText(row.title) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.sourceName || '-' }}</template>
        </el-table-column>
        <el-table-column label="错 / 对" align="center" width="90">
          <template #default="{ row }">
            <span class="num-danger">{{ row.wrongCount ?? 0 }}</span>
            <span class="num-split"> / </span>
            <span class="num-success">{{ row.rightCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="掌握状态" align="center" width="100">
          <template #default="{ row }">
            <dict-tag :options="wrongMasterStatusOptions" :value="row.masterStatus" />
          </template>
        </el-table-column>
        <el-table-column label="最近答错" align="center" width="160">
          <template #default="{ row }">{{ formatTime(row.lastWrongTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="EditPen" @click="practiceOne(row.id)">重刷</el-button>
            <el-button v-if="row.masterStatus === 'IGNORED'" link type="warning" @click="handleRestore(row)">恢复</el-button>
            <el-button v-else-if="row.masterStatus !== 'MASTERED'" link type="success" @click="handleMaster(row)">已掌握</el-button>
            <el-button v-if="row.masterStatus !== 'IGNORED'" link type="info" @click="handleIgnore(row)">移出</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

      <el-empty v-if="!loading && total === 0" description="这个来源下没有错题" />
    </el-card>

    <!-- AI 错题诊断：只读结论，不动错题数据 -->
    <ai-diagnose-drawer v-model="aiVisible" :wrong-items="aiWrongItems" :mastered="aiMastered" />
  </div>
</template>

<script setup lang="ts" name="ExamWrongDetail">
import { getWrongList, masterWrong, ignoreWrong, restoreWrong } from '@/api/exam/wrong';
import type { WrongQuestionVO, WrongQuestionQuery } from '@/api/exam/wrong/types';
import { useExamDicts } from '@/hooks/useExamDicts';
import type { AiWrongItem } from '@/api/system/ai/types';
import AiDiagnoseDrawer from './AiDiagnoseDrawer.vue';

const route = useRoute();
const router = useRouter();

const { examTypeOptions, wrongSourceTypeOptions, wrongMasterStatusOptions, questionTypeOptions, questionDifficultyOptions } = useExamDicts();

const loading = ref(true);
const total = ref(0);
const list = ref<WrongQuestionVO[]>([]);

// 来源维度：从首页点「查看」带过来，决定这一页看的是哪一场考试 / 哪一份练习的错题
const sourceType = ref('');
const sourceId = ref('');
const examType = ref('');
const sourceName = ref('');

const queryParams = ref<WrongQuestionQuery>({ pageNum: 1, pageSize: 10, includeIgnored: false });

/** AI 诊断抽屉 */
const aiVisible = ref(false);
/** 错题转诊断入参：题干脱标签，知识点暂缺（题库还没打标），AI 从题干推断 */
const aiWrongItems = computed<AiWrongItem[]>(() =>
  list.value.map((row) => ({
    questionId: String(row.questionId ?? ''),
    questionType: row.questionType,
    stem: plainText(row.title),
    knowledgePoints: [],
    answerText: '',
    standardAnswer: row.standardAnswerText || row.standardAnswer,
    wrongCount: row.wrongCount
  }))
);
/** 已掌握的题不参与归因，避免把「会的」算成薄弱点 */
const aiMastered = computed<string[]>(() => []);

/**
 * 来源ID 只有合法正整数才作为过滤条件。
 *
 * 这里必须原样返回字符串：雪花 ID 是 19 位，超过 Number.MAX_SAFE_INTEGER，
 * 一旦用 Number() 转换就会被四舍五入（2105488649030184961 → 2105488649030185000），
 * 后端拿这个 ID 查询会一条都查不到。
 */
const parseSourceId = (value?: string): string | undefined => {
  if (!value) return undefined;
  const text = String(value).trim();
  return /^\d+$/.test(text) ? text : undefined;
};

const initFromQuery = () => {
  const q = route.query;
  sourceType.value = String(q.sourceType ?? '');
  sourceId.value = String(q.sourceId ?? '');
  examType.value = String(q.examType ?? '');
  sourceName.value = String(q.sourceName ?? '');
};

/** 富文本题干在表格里只显示纯文本，避免标签与图片撑爆行高 */
const plainText = (html?: string): string => {
  if (!html) return '-';
  const div = document.createElement('div');
  div.innerHTML = html;
  return (div.textContent ?? '').replace(/\s+/g, ' ').trim() || '-';
};

const pad = (n: number) => String(n).padStart(2, '0');

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await getWrongList({
      ...queryParams.value,
      sourceType: sourceType.value || undefined,
      sourceId: parseSourceId(sourceId.value)
    });
    list.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } catch {
    list.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryParams.value = { pageNum: 1, pageSize: 10, includeIgnored: false };
  getList();
};

const reload = async () => {
  await getList();
};

const handleMaster = async (row: WrongQuestionVO) => {
  try {
    await masterWrong(row.id);
    await reload();
  } catch {
    // 失败提示由全局拦截器统一弹出
  }
};

const handleIgnore = async (row: WrongQuestionVO) => {
  try {
    await ignoreWrong(row.id);
    await reload();
  } catch {
    // 同上
  }
};

const handleRestore = async (row: WrongQuestionVO) => {
  try {
    await restoreWrong(row.id);
    await reload();
  } catch {
    // 同上
  }
};

/** 重刷本来源的全部错题 */
const practiceAll = () => {
  router.push({
    path: '/exam/wrong/practice',
    query: {
      sourceType: sourceType.value,
      sourceId: sourceId.value,
      sourceName: sourceName.value
    }
  });
};

/** 只重刷这一道 */
const practiceOne = (wrongId: string) => {
  router.push({
    path: '/exam/wrong/practice',
    query: {
      sourceType: sourceType.value,
      sourceId: sourceId.value,
      sourceName: sourceName.value,
      wrongId: String(wrongId)
    }
  });
};

/** 清掉来源条件，看所有来源的错题（不带 sourceId，后端就不按来源过滤） */
const viewAllSources = () => router.push({ path: '/exam/wrong/detail' });

const goBack = () => router.push('/exam/wrong');

initFromQuery();
onMounted(getList);

// 同路由只改 query 时组件会被复用、onMounted 不再触发，
// 「查看全部来源」这种只清参数的跳转就靠这里刷新
watch(
  () => route.query,
  () => {
    initFromQuery();
    queryParams.value.pageNum = 1;
    getList();
  }
);
</script>

<style scoped lang="scss">
.source-name {
  overflow: hidden;
  font-size: 13px;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.question-title {
  display: -webkit-box;
  overflow: hidden;
  color: #303133;
  text-overflow: ellipsis;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.num-danger {
  font-weight: 600;
  color: #f56c6c;
}

.num-success {
  font-weight: 600;
  color: #67c23a;
}

.num-split {
  color: #909399;
}
</style>
