<template>
  <div class="p-[16px]" v-loading="pageLoading">
    <!-- 概览：一眼看懂这场考试考得怎么样 -->
    <el-card shadow="never" class="mb-[16px]">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2 min-w-0">
            <el-button link icon="ArrowLeft" @click="goBack">返回</el-button>
            <span class="font-medium">考试情况</span>
            <span class="text-gray-500 truncate" :title="overview.examName">{{ overview.examName || '-' }}</span>
            <dict-tag :options="examStatusOptions" :value="overview.status" />
          </div>
          <div class="flex items-center gap-2">
            <el-button plain icon="Refresh" @click="() => loadAll()">刷新</el-button>
            <el-button type="primary" plain icon="Download" :disabled="!examId" @click="() => handleExport()"
                       v-hasPermi="['system:exam:export']">导出考试情况</el-button>
          </div>
        </div>
      </template>

      <!-- 有主观题没阅完：成绩还没定统计也不准，必须显式提醒并给出入口 -->
      <el-alert v-if="(overview.pendingMarkCount ?? 0) > 0" type="warning" :closable="false" show-icon class="mb-[16px]">
        <template #title>
          还有 {{ overview.pendingMarkCount }} 份答卷的主观题没有阅完，这些考生的成绩暂时没显示。
          <el-button link type="primary" @click="() => goMark()">去阅卷</el-button>
        </template>
      </el-alert>

      <el-descriptions :column="4" border class="mb-[16px]">
        <el-descriptions-item label="考试类型">
          <dict-tag :options="examTypeOptions" :value="overview.examType" />
        </el-descriptions-item>
        <el-descriptions-item label="及格分">{{ fmt(overview.passScore) }}</el-descriptions-item>
        <el-descriptions-item label="考试时间">{{ overview.startTime || '-' }} ~ {{ overview.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="考试限时">{{ overview.duration ?? '-' }} 分钟</el-descriptions-item>
      </el-descriptions>

      <el-row :gutter="10">
        <el-col v-for="kpi in kpis" :key="kpi.label" :span="3">
          <el-card shadow="never" class="kpi-card" :class="{ 'kpi-warn': kpi.warn }">
            <div class="text-[13px] text-gray-500">{{ kpi.label }}</div>
            <div class="text-[20px] font-semibold mt-[4px]">{{ kpi.value }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 参考名单：谁参考了、考了多少分 -->
    <el-card shadow="never">
      <el-form :model="queryParams" :inline="true" label-width="68px">
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="姓名 / 账号 / 部门" clearable style="width: 180px"
                    @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option v-for="item in examRecordStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否及格" prop="passed">
          <el-select v-model="queryParams.passed" placeholder="全部" clearable style="width: 110px">
            <el-option label="及格" :value="1" />
            <el-option label="不及格" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="只看待阅" prop="pendingMark">
          <el-switch v-model="pendingMarkOnly" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="考生" min-width="160" fixed="left">
          <template #default="{ row }">
            <span>{{ row.nickName || row.account || '-' }}</span>
            <span v-if="row.nickName && row.account" class="text-gray-400">（{{ row.account }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="部门" min-width="140" prop="deptName" show-overflow-tooltip />
        <el-table-column label="次数" align="center" width="80">
          <template #default="{ row }">第 {{ row.attemptNo ?? 1 }} 次</template>
        </el-table-column>
        <el-table-column label="客观题" align="center" width="90">
          <template #default="{ row }">{{ fmt(row.objectiveScore) }}</template>
        </el-table-column>
        <el-table-column label="主观题" align="center" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.pendingMark" type="warning" size="small" effect="plain">待阅</el-tag>
            <span v-else>{{ fmt(row.subjectiveScore) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总分" align="center" width="90">
          <template #default="{ row }">
            <span v-if="row.pendingMark" class="text-gray-400">-</span>
            <span v-else class="font-medium">{{ fmt(row.totalScore) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="及格" align="center" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.pendingMark" type="info" size="small" effect="plain">未出分</el-tag>
            <el-tag v-else :type="passedTagType(row)" size="small" effect="plain">{{ row.passed === 1 ? '及格' : '不及格' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="答题进度" align="center" width="100">
          <template #default="{ row }">{{ row.answeredCount ?? 0 }} / {{ row.questionCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="用时" align="center" width="100">
          <template #default="{ row }">{{ row.usedSeconds ? row.usedTimeLabel : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="{ row }">
            <dict-tag :options="examRecordStatusOptions" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="交卷时间" align="center" width="170">
          <template #default="{ row }">{{ row.submitTime || '-' }}</template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="() => getList()"
      />

      <el-empty v-if="!loading && total === 0" description="还没有人参加这场考试" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="SystemExamSituation">
import { ComponentInternalInstance, computed, getCurrentInstance } from 'vue';
import { getExamSituationOverview, listExamSituation } from '@/api/system/exam';
import type { ExamSituationOverviewVO, ExamSituationQuery, ExamSituationVO } from '@/api/system/exam/types';
import { useExamDicts } from '@/hooks/useExamDicts';

const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const { examStatusOptions, examTypeOptions, examRecordStatusOptions } = useExamDicts();

const pageLoading = ref(true);
const loading = ref(false);
const total = ref(0);
const list = ref<ExamSituationVO[]>([]);
const overview = ref<ExamSituationOverviewVO>({} as ExamSituationOverviewVO);

/** 考试ID 全程字符串透传：19 位雪花 ID 经 Number() 转换会丢精度 */
const examId = ref('');
const examName = ref('');

const queryParams = ref<ExamSituationQuery>({ pageNum: 1, pageSize: 10 });
/** 开关用布尔值，提交时再转成后端要的 1 / undefined */
const pendingMarkOnly = ref(false);

const buildQuery = (): ExamSituationQuery => ({
  ...queryParams.value,
  pendingMark: pendingMarkOnly.value ? 1 : undefined
});

const kpis = computed(() => [
  { label: '应考人数', value: overview.value.invitedCount ?? 0 },
  { label: '参考人数', value: overview.value.joinedCount ?? 0 },
  { label: '已交卷', value: overview.value.submittedCount ?? 0 },
  { label: '答题中', value: overview.value.answeringCount ?? 0 },
  { label: '待阅卷', value: overview.value.pendingMarkCount ?? 0, warn: (overview.value.pendingMarkCount ?? 0) > 0 },
  { label: '及格率', value: `${fmt(overview.value.passRate)}%` },
  { label: '平均分', value: fmt(overview.value.avgScore) },
  { label: '最高分', value: fmt(overview.value.maxScore) }
]);

const fmt = (value?: string | number): string => {
  if (value === undefined || value === null || value === '') return '-';
  const num = Number(value);
  // 小数位统一保留两位，整数直接显示，避免 60.00 这种冗余
  return Number.isNaN(num) ? String(value) : Number.isInteger(num) ? String(num) : num.toFixed(2);
};

/** el-tag 的 type 只接受字面量联合，返回 string 会让 vue-tsc 报 TS2322 */
const passedTagType = (row: ExamSituationVO): 'success' | 'danger' => (row.passed === 1 ? 'success' : 'danger');

const loadOverview = async () => {
  if (!examId.value) {
    return;
  }
  const res: any = await getExamSituationOverview(examId.value);
  overview.value = (res?.data ?? {}) as ExamSituationOverviewVO;
  examName.value = overview.value.examName || examName.value;
};

const getList = async () => {
  if (!examId.value) {
    list.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  try {
    const res: any = await listExamSituation(examId.value, buildQuery());
    list.value = res?.rows ?? [];
    total.value = res?.total ?? 0;
  } catch {
    list.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const loadAll = async () => {
  pageLoading.value = true;
  try {
    await loadOverview();
    await getList();
  } finally {
    pageLoading.value = false;
  }
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryParams.value = { pageNum: 1, pageSize: 10 };
  pendingMarkOnly.value = false;
  getList();
};

const handleExport = () => {
  proxy?.download(
    `exam/${examId.value}/situation/export`,
    buildQuery(),
    `${examName.value || '考试'}_考试情况_${new Date().getTime()}.xlsx`
  );
};

/** 跳这场考试的阅卷列表：'/system/mark/record' 才认 examId，阅卷首页不认 */
const goMark = () => {
  router.push({
    path: '/system/mark/record',
    query: { examId: examId.value, examName: examName.value }
  });
};

const goBack = () => router.push('/system/exam');

onMounted(() => {
  const q = route.query;
  examId.value = String(q.examId ?? '');
  examName.value = String(q.examName ?? '');
  loadAll();
});
</script>

<style scoped lang="scss">
.kpi-card {
  text-align: center;
  padding: 6px 0;
}

.kpi-warn {
  background: #fdf6ec;
  border-color: #f3d19e;
}
</style>
