<template>
  <div class="p-4" v-loading="pageLoading">
    <el-card shadow="never" class="mb-4">
      <template #header>
        <div class="card-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/system/stat/dashboard' }">考试统计</el-breadcrumb-item>
            <el-breadcrumb-item>{{ summary.examName || '考试详情' }}</el-breadcrumb-item>
          </el-breadcrumb>
          <div>
            <el-button type="primary" plain @click="() => handleRecalc()">重新计算</el-button>
            <el-button @click="() => goMark()">去阅卷</el-button>
          </div>
        </div>
      </template>

      <!-- 待阅卷提示条：这是本页最重要的提醒，不能省 -->
      <el-alert
        v-if="(summary.pendingMarkCount ?? 0) > 0"
        type="warning"
        :closable="false"
        class="mb-4"
        show-icon
      >
        <template #title>
          仍有 {{ summary.pendingMarkCount }} 份答卷在人工阅卷中，统计结果暂未包含。阅卷完成后将自动并入。
          <el-button link type="primary" @click="() => goMark()">去阅卷</el-button>
        </template>
      </el-alert>
      <el-alert
        v-if="summary.partialScore === '1'"
        type="info"
        :closable="false"
        class="mb-4"
        show-icon
        :title="`本场考试开启了客观题部分得分（${summary.partialScoreRate ?? 100}%），「正确率」只统计完全答对的，半对单独记为「部分正确」`"
      />

      <el-descriptions :column="4" border class="mb-4">
        <el-descriptions-item label="考试名称">{{ summary.examName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="examStatusType(summary.examStatus)" size="small">{{ examStatusText(summary.examStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总分 / 及格分">{{ fmt(summary.fullScore) }} / {{ fmt(summary.passScore) }}</el-descriptions-item>
        <el-descriptions-item label="考试时间">{{ fmtTimeRange() }}</el-descriptions-item>
      </el-descriptions>

      <el-row :gutter="12" class="mb-4">
        <el-col :span="3"><el-card shadow="never" class="kpi"><div class="kpi-label">应考</div><div class="kpi-value">{{ summary.invitedCount ?? 0 }}</div></el-card></el-col>
        <el-col :span="3"><el-card shadow="never" class="kpi"><div class="kpi-label">已交卷</div><div class="kpi-value">{{ summary.submittedCount ?? 0 }}</div></el-card></el-col>
        <el-col :span="3"><el-card shadow="never" class="kpi"><div class="kpi-label">已入统</div><div class="kpi-value">{{ summary.countedCount ?? 0 }}</div></el-card></el-col>
        <el-col :span="3"><el-card shadow="never" class="kpi kpi-warn"><div class="kpi-label">待阅卷</div><div class="kpi-value">{{ summary.pendingMarkCount ?? 0 }}</div></el-card></el-col>
        <el-col :span="3"><el-card shadow="never" class="kpi"><div class="kpi-label">及格率</div><div class="kpi-value">{{ fmt(summary.passRate) }}%</div></el-card></el-col>
        <el-col :span="3"><el-card shadow="never" class="kpi"><div class="kpi-label">平均分</div><div class="kpi-value">{{ fmt(summary.avgScore) }}</div></el-card></el-col>
        <el-col :span="3"><el-card shadow="never" class="kpi"><div class="kpi-label">最高分</div><div class="kpi-value">{{ fmt(summary.maxScore) }}</div></el-card></el-col>
        <el-col :span="3"><el-card shadow="never" class="kpi"><div class="kpi-label">难度系数</div><div class="kpi-value">{{ fmt(summary.difficulty) }}</div></el-card></el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- 成绩分布 -->
        <el-tab-pane label="成绩分布" name="segment">
          <div class="chart mb-4">
            <div v-for="seg in overview.segments" :key="seg.segmentLabel" class="chart-row">
              <span class="chart-name">{{ seg.segmentLabel }}</span>
              <div class="chart-track">
                <div class="chart-bar" :style="{ width: segBarWidth(seg.personCount) }" />
              </div>
              <span class="chart-num">{{ seg.personCount ?? 0 }} 人（{{ fmt(seg.rate) }}%）</span>
            </div>
            <el-empty v-if="!overview.segments?.length" description="暂无数据" :image-size="60" />
          </div>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="中位数">{{ fmt(summary.medianScore) }}</el-descriptions-item>
            <el-descriptions-item label="标准差">{{ fmt(summary.stdDev) }}</el-descriptions-item>
            <el-descriptions-item label="最低分">{{ fmt(summary.minScore) }}</el-descriptions-item>
            <el-descriptions-item label="平均用时">{{ fmtDuration(summary.avgUsedSeconds) }}</el-descriptions-item>
            <el-descriptions-item label="客观题平均分">{{ fmt(summary.avgObjectiveScore) }}</el-descriptions-item>
            <el-descriptions-item label="主观题平均分">{{ fmt(summary.avgSubjectiveScore) }}</el-descriptions-item>
            <el-descriptions-item label="区分度">{{ fmt(summary.discrimination) }}</el-descriptions-item>
            <el-descriptions-item label="最后计算时间">{{ summary.calcTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 考生成绩 -->
        <el-tab-pane label="考生成绩" name="user">
          <el-form :inline="true" class="mb-2">
            <el-form-item label="关键词">
              <el-input v-model="userQuery.keyword" placeholder="姓名 / 账号" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item label="是否及格">
              <el-select v-model="userQuery.passed" placeholder="全部" clearable style="width: 110px">
                <el-option label="及格" :value="1" />
                <el-option label="不及格" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="() => loadUsers()">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="userRows" border stripe>
            <el-table-column label="排名" width="70" prop="rankNo" align="center" />
            <el-table-column label="考生" min-width="140">
              <template #default="{ row }">
                <el-link type="primary" @click="() => goUserDetail(row)">{{ row.userName || row.account }}</el-link>
              </template>
            </el-table-column>
            <el-table-column label="部门" min-width="120" prop="deptName" show-overflow-tooltip />
            <el-table-column label="客观题分" width="100" prop="objectiveScore" align="center" />
            <el-table-column label="主观题分" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.statStatus === 'PENDING_MARK'" type="warning" size="small">待阅</el-tag>
                <span v-else>{{ fmt(row.subjectiveScore) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="总分" width="90" prop="totalScore" align="center" sortable />
            <el-table-column label="及格" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.passed === 1 ? 'success' : 'danger'" size="small">{{ row.passed === 1 ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="对/错/未答" width="110" align="center">
              <template #default="{ row }">{{ row.correctCount }} / {{ row.wrongCount }} / {{ row.blankCount }}</template>
            </el-table-column>
            <el-table-column label="用时" width="90" align="center">
              <template #default="{ row }">{{ fmtDuration(row.usedSeconds) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="statStatusType(row.statStatus)" size="small">{{ statStatusText(row.statStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="() => goUserDetail(row)">答卷明细</el-button>
                <el-button link :type="row.statStatus === 'EXCLUDED' ? 'success' : 'danger'" @click="() => toggleExclude(row)">
                  {{ row.statStatus === 'EXCLUDED' ? '取消作废' : '标记作废' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="userTotal > 0"
            v-model:page="userQuery.pageNum"
            v-model:limit="userQuery.pageSize"
            :total="userTotal"
            @pagination="() => loadUsers()"
          />
        </el-tab-pane>

        <!-- 试题分析 -->
        <el-tab-pane label="试题分析" name="question">
          <div class="mb-2">
            <el-button type="primary" @click="() => goQuestion()">进入试题分析</el-button>
          </div>
          <el-table :data="questionRows" border stripe max-height="420">
            <el-table-column label="题号" width="60" prop="sort" align="center" />
            <el-table-column label="题型" width="100" prop="questionType" align="center" />
            <el-table-column label="题干" min-width="220" prop="title" show-overflow-tooltip />
            <el-table-column label="作答" width="70" prop="answerCount" align="center" />
            <el-table-column label="正确率 / 得分率" width="130" align="center">
              <template #default="{ row }">
                <span :class="rateTextClass(row)">
                  {{ row.questionCategory === 'objective' ? fmt(row.correctRate) : fmt(row.scoreRate) }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="部分正确" width="90" prop="partialCount" align="center" />
            <el-table-column label="区分度" width="90" prop="discrimination" align="center" />
          </el-table>
        </el-tab-pane>

        <!-- 知识点 -->
        <el-tab-pane label="知识点分析" name="knowledge">
          <div class="mb-2">
            <el-button type="primary" @click="() => goKnowledge()">进入知识点薄弱分析</el-button>
          </div>
          <el-table :data="knowledgeRows" border stripe max-height="420">
            <el-table-column label="知识点" min-width="200" prop="knowledgePath" show-overflow-tooltip />
            <el-table-column label="题目数" width="80" prop="questionCount" align="center" />
            <el-table-column label="得分率" width="90" align="center">
              <template #default="{ row }">{{ fmt(row.scoreRate) }}%</template>
            </el-table-column>
            <el-table-column label="掌握度" width="180" align="center">
              <template #default="{ row }">
                <el-progress :percentage="pct(row.mastery)" :status="masteryStatus(row.mastery)" />
              </template>
            </el-table-column>
            <el-table-column label="等级" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="weakType(row.weakLevel)" size="small">{{ weakText(row.weakLevel) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 阅卷进度 -->
        <el-tab-pane v-if="overview.pendingMarks?.length" label="阅卷进度" name="mark">
          <el-table :data="overview.pendingMarks" border stripe>
            <el-table-column label="答卷ID" min-width="180" prop="recordId" show-overflow-tooltip />
            <el-table-column label="已阅 / 总数" width="120" align="center">
              <template #default="{ row }">{{ row.markedCount ?? 0 }} / {{ row.questionCount ?? 0 }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'finished' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'finished' ? '已阅完' : row.status === 'marking' ? '阅卷中' : '待阅' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="阅卷人" width="120" prop="markerName" align="center" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getStatOverview,
  listStatKnowledge,
  listStatQuestion,
  listStatUser,
  markStatExcluded,
  recalcStat
} from '@/api/exam/stat';
import type {
  StatExamRowVO,
  StatKnowledgeVO,
  StatOverviewVO,
  StatQuestionVO,
  StatUserQuery,
  StatUserVO
} from '@/api/exam/stat/types';

defineOptions({ name: 'StatExamDetail' });

const route = useRoute();
const router = useRouter();
const examId = ref<string>(String(route.query.examId ?? ''));
const pageLoading = ref(false);
const activeTab = ref('segment');
const overview = ref<StatOverviewVO>({});
// StatExamRowVO 的 examId 是必填，初始值给不了真实值，用断言顶上；数据到位后整体替换
const summary = ref<StatExamRowVO>({} as StatExamRowVO);

const userRows = ref<StatUserVO[]>([]);
const userTotal = ref(0);
const userQuery = ref<StatUserQuery>({ pageNum: 1, pageSize: 10 });
const questionRows = ref<StatQuestionVO[]>([]);
const knowledgeRows = ref<StatKnowledgeVO[]>([]);

const num = (v: unknown): number => {
  const n = typeof v === 'number' ? v : Number(v);
  return Number.isFinite(n) ? n : 0;
};
const fmt = (v: unknown): string => num(v).toFixed(2);
const pct = (v: unknown): number => Math.min(100, Math.max(0, Math.round(num(v))));
const fmtDuration = (seconds?: number): string => {
  if (!seconds) return '-';
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, '0')}`;
};
const examStatusText = (s?: string): string =>
  ({ not_start: '未开始', ongoing: '进行中', finished: '已结束', archived: '已归档' } as Record<string, string>)[s ?? ''] ?? '-';
const examStatusType = (s?: string): any =>
  ({ not_start: 'info', ongoing: 'success', finished: 'primary', archived: 'info' } as Record<string, any>)[s ?? ''] ?? 'info';
const statStatusText = (s?: string): string =>
  ({ COUNTED: '已入统', PENDING_MARK: '待阅卷', EXCLUDED: '已作废' } as Record<string, string>)[s ?? ''] ?? '-';
const statStatusType = (s?: string): any =>
  ({ COUNTED: 'success', PENDING_MARK: 'warning', EXCLUDED: 'info' } as Record<string, any>)[s ?? ''] ?? 'info';
const weakText = (s?: string): string => ({ good: '良好', normal: '一般', weak: '薄弱' } as Record<string, string>)[s ?? ''] ?? '-';
const weakType = (s?: string): any => ({ good: 'success', normal: 'warning', weak: 'danger' } as Record<string, any>)[s ?? ''] ?? 'info';
const masteryStatus = (v?: string | number): any => {
  const n = num(v);
  return n < 60 ? 'exception' : n < 80 ? 'warning' : 'success';
};
const rateTextClass = (row: StatQuestionVO): string => {
  const v = num(row.questionCategory === 'objective' ? row.correctRate : row.scoreRate);
  return v < 30 ? 'text-red' : v < 60 ? 'text-orange' : v >= 85 ? 'text-green' : '';
};
const segBarWidth = (count?: number): string => {
  const max = Math.max(...(overview.value.segments ?? []).map((s) => s.personCount ?? 0), 1);
  return `${((count ?? 0) / max) * 100}%`;
};
const fmtTimeRange = (): string => {
  if (!summary.value.startTime) return '-';
  return `${summary.value.startTime} ~ ${summary.value.endTime || '不限'}`;
};

const loadOverview = async () => {
  pageLoading.value = true;
  try {
    const res: any = await getStatOverview(examId.value);
    overview.value = res?.data ?? {};
    summary.value = (res?.data?.summary ?? {}) as StatExamRowVO;
    overview.value.segments = res?.data?.segments ?? [];
  } finally {
    pageLoading.value = false;
  }
};

const loadUsers = async () => {
  const res: any = await listStatUser(examId.value, userQuery.value);
  userRows.value = res?.rows ?? [];
  userTotal.value = res?.total ?? 0;
};

const loadQuestions = async () => {
  const res: any = await listStatQuestion(examId.value, { pageNum: 1, pageSize: 50 });
  questionRows.value = res?.rows ?? [];
};

const loadKnowledge = async () => {
  const res: any = await listStatKnowledge(examId.value);
  knowledgeRows.value = res?.data ?? [];
};

const handleRecalc = async () => {
  await ElMessageBox.confirm('确定重新计算本场考试的统计数据吗？', '提示', { type: 'warning' });
  await recalcStat(examId.value);
  ElMessage.success('已提交重新计算');
  loadOverview();
};

const toggleExclude = async (row: StatUserVO) => {
  const excluded = row.statStatus !== 'EXCLUDED';
  await ElMessageBox.confirm(
    excluded ? '作废后该答卷不计入任何统计，确定吗？' : '确定恢复该答卷的统计吗？',
    '提示',
    { type: 'warning' }
  );
  await markStatExcluded(examId.value, String(row.recordId), excluded);
  ElMessage.success('操作成功');
  loadUsers();
};

const goUserDetail = (row: StatUserVO) => {
  router.push({ path: '/system/stat/user', query: { examId: examId.value, userId: row.userId } });
};
const goQuestion = () => router.push({ path: '/system/stat/question', query: { examId: examId.value } });
const goKnowledge = () => router.push({ path: '/system/stat/knowledge', query: { examId: examId.value } });
// 跳阅卷的答卷列表页（/system/mark/record）而不是阅卷首页：首页不认 examId，
// 跳过去等于没定位到这场考试，得再手动筛一次
const goMark = () => router.push({ path: '/system/mark/record', query: { examId: examId.value } });

watch(activeTab, (tab) => {
  if (tab === 'user' && !userRows.value.length) loadUsers();
  if (tab === 'question' && !questionRows.value.length) loadQuestions();
  if (tab === 'knowledge' && !knowledgeRows.value.length) loadKnowledge();
});

onMounted(() => {
  if (!examId.value) {
    ElMessage.error('缺少考试ID');
    return;
  }
  loadOverview();
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
.kpi-warn {
  background: #fdf6ec;
  border-color: #f3d19e;
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
.chart-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.chart-name {
  width: 110px;
  font-size: 13px;
}
.chart-track {
  flex: 1;
  height: 14px;
  background: #f0f2f5;
  border-radius: 7px;
  overflow: hidden;
  margin: 0 10px;
}
.chart-bar {
  height: 100%;
  background: #409eff;
  border-radius: 7px;
}
.chart-num {
  width: 130px;
  text-align: right;
  font-size: 13px;
}
.text-red {
  color: #f56c6c;
}
.text-orange {
  color: #e6a23c;
}
.text-green {
  color: #67c23a;
}
.mb-4 {
  margin-bottom: 16px;
}
.mb-2 {
  margin-bottom: 8px;
}
.p-4 {
  padding: 16px;
}
</style>
