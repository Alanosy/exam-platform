<template>
  <div class="p-4" v-loading="loading">
    <el-card shadow="never" class="mb-4">
      <template #header>
        <div class="card-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/system/stat/dashboard' }">考试统计</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/system/stat/exam', query: { examId } }">考试详情</el-breadcrumb-item>
            <el-breadcrumb-item>{{ detail.userName || detail.account || '考生明细' }}</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button @click="() => goBack()">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="5" border class="mb-4">
        <el-descriptions-item label="姓名">{{ detail.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ detail.account || '-' }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detail.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="第几次">第 {{ detail.attemptNo ?? 1 }} 次</el-descriptions-item>
        <el-descriptions-item label="用时">{{ fmtDuration(detail.usedSeconds) }}</el-descriptions-item>
        <el-descriptions-item label="开考">{{ '-' }}</el-descriptions-item>
        <el-descriptions-item label="交卷">{{ detail.submitTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statStatusType(detail.statStatus)" size="small">{{ statStatusText(detail.statStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="排名">
          第 {{ detail.rankNo ?? '-' }} 名
          <span v-if="detail.beatRate">（超过 {{ fmt(detail.beatRate) }}%）</span>
        </el-descriptions-item>
        <el-descriptions-item label="是否及格">
          <el-tag :type="detail.passed === 1 ? 'success' : 'danger'" size="small">{{ detail.passed === 1 ? '及格' : '不及格' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-row :gutter="12" class="mb-4">
        <el-col :span="4"><el-card shadow="never" class="kpi"><div class="kpi-label">总分</div><div class="kpi-value">{{ fmt(detail.totalScore) }}</div><div class="kpi-sub">满分 {{ fmt(detail.fullScore) }}</div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never" class="kpi"><div class="kpi-label">客观题分</div><div class="kpi-value">{{ fmt(detail.objectiveScore) }}</div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never" class="kpi"><div class="kpi-label">主观题分</div><div class="kpi-value">{{ fmt(detail.subjectiveScore) }}</div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never" class="kpi"><div class="kpi-label">及格分</div><div class="kpi-value">{{ fmt(detail.passScore) }}</div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never" class="kpi"><div class="kpi-label">用时对比</div><div class="kpi-value">{{ fmtDuration(detail.usedSeconds) }}</div><div class="kpi-sub">平均 {{ fmtDuration(detail.avgUsedSeconds) }}</div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never" class="kpi"><div class="kpi-label">得分率</div><div class="kpi-value">{{ scoreRate }}%</div></el-card></el-col>
      </el-row>

      <!-- 重考切换 -->
      <div v-if="(detail.attempts?.length ?? 0) > 1" class="mb-4">
        <el-radio-group v-model="attemptNo" @change="() => loadDetail()">
          <el-radio-button v-for="a in detail.attempts" :key="a.recordId" :value="a.attemptNo">
            第 {{ a.attemptNo }} 次（{{ a.statStatus === 'PENDING_MARK' ? '待阅' : fmt(a.totalScore) }}）
          </el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>答卷明细</span>
          <el-radio-group v-model="filter" @change="() => onFilterChange()">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="wrong">仅错题</el-radio-button>
            <el-radio-button value="subjective">仅主观题</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="filteredItems" border stripe>
        <el-table-column label="题号" width="60" prop="sort" align="center" />
        <el-table-column label="题型" width="100" prop="questionType" align="center" />
        <el-table-column label="题干" min-width="240" show-overflow-tooltip>
          <template #default="{ row }"><span v-html="row.title" /></template>
        </el-table-column>
        <el-table-column label="考生作答" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.answerText || '未作答' }}</template>
        </el-table-column>
        <el-table-column label="正确答案" min-width="140" prop="standardAnswerText" show-overflow-tooltip />
        <el-table-column label="满分" width="70" prop="fullScore" align="center" />
        <el-table-column label="得分" width="70" prop="score" align="center" />
        <el-table-column label="结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="resultType(row.result)" size="small">{{ row.result }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阅卷人" width="100" prop="markerName" align="center" />
        <el-table-column label="评语" min-width="160" prop="markComment" show-overflow-tooltip />
      </el-table>
      <template v-if="!filteredItems.length"><el-empty description="没有符合条件的题目" /></template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getStatUserDetail } from '@/api/exam/stat';
import type { StatAnswerItemVO, StatAnswerVO } from '@/api/exam/stat/types';

defineOptions({ name: 'StatUserDetail' });

const route = useRoute();
const router = useRouter();
const examId = ref<string>(String(route.query.examId ?? ''));
const userId = ref<string>(String(route.query.userId ?? ''));
const loading = ref(false);
const detail = ref<StatAnswerVO>({});
const attemptNo = ref<number | undefined>(undefined);
const filter = ref<'all' | 'wrong' | 'subjective'>('all');

const num = (v: unknown): number => {
  const n = typeof v === 'number' ? v : Number(v);
  return Number.isFinite(n) ? n : 0;
};
const fmt = (v: unknown): string => num(v).toFixed(2);
const fmtDuration = (seconds?: number): string => {
  if (!seconds) return '-';
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, '0')}`;
};
const statStatusText = (s?: string): string =>
  ({ COUNTED: '已入统', PENDING_MARK: '待阅卷', EXCLUDED: '已作废' } as Record<string, string>)[s ?? ''] ?? '-';
const statStatusType = (s?: string): any =>
  ({ COUNTED: 'success', PENDING_MARK: 'warning', EXCLUDED: 'info' } as Record<string, any>)[s ?? ''] ?? 'info';
const resultType = (r?: string): any =>
  ({ 正确: 'success', 部分正确: 'warning', 错误: 'danger', 未答: 'info', 待阅: 'warning' } as Record<string, any>)[r ?? ''] ?? 'info';

const scoreRate = computed(() => {
  const full = num(detail.value.fullScore);
  return full <= 0 ? '0.00' : ((num(detail.value.totalScore) / full) * 100).toFixed(2);
});

const filteredItems = computed<StatAnswerItemVO[]>(() => {
  const items = detail.value.items ?? [];
  if (filter.value === 'wrong') return items.filter((i) => i.result === '错误' || i.result === '部分正确' || i.result === '未答');
  if (filter.value === 'subjective') return items.filter((i) => i.questionCategory === 'subjective');
  return items;
});

const onFilterChange = () => {
  /* 纯前端过滤，不需要重新请求 */
};

const loadDetail = async () => {
  loading.value = true;
  try {
    const res: any = await getStatUserDetail(examId.value, userId.value, attemptNo.value);
    detail.value = res?.data ?? {};
  } finally {
    loading.value = false;
  }
};

const goBack = () => router.push({ path: '/system/stat/exam', query: { examId: examId.value } });

onMounted(() => {
  if (!examId.value || !userId.value) {
    ElMessage.error('缺少考试ID或考生ID');
    return;
  }
  loadDetail();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.kpi {
  text-align: center;
}
.kpi-label {
  font-size: 13px;
  color: #909399;
}
.kpi-value {
  font-size: 20px;
  font-weight: 600;
  margin-top: 4px;
}
.kpi-sub {
  font-size: 12px;
  color: #909399;
}
.mb-4 {
  margin-bottom: 16px;
}
.p-4 {
  padding: 16px;
}
</style>
