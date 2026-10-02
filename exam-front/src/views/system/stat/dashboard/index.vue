<template>
  <div class="p-4">
    <!-- 筛选 -->
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="range"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 240px"
            @change="onRangeChange"
          />
        </el-form-item>
        <el-form-item label="考试类型">
          <el-select v-model="query.examType" placeholder="全部" clearable style="width: 120px">
            <el-option label="正式考试" value="1" />
            <el-option label="练习考试" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.examStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="未开始" value="not_start" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="finished" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="考试名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="query.onlyPendingMark" label="只看有待阅卷" />
          <el-checkbox v-model="query.onlyMine" label="只看我创建的" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="() => loadList()">查询</el-button>
          <el-button @click="() => resetQuery()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- KPI -->
    <el-row :gutter="12" class="mb-4">
      <el-col :span="4">
        <el-card shadow="never" class="kpi">
          <div class="kpi-label">考试场次</div>
          <div class="kpi-value">{{ kpi.examCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="kpi">
          <div class="kpi-label">参与人次</div>
          <div class="kpi-value">{{ kpi.countedCount ?? 0 }}</div>
          <div class="kpi-sub">已入统</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="kpi">
          <div class="kpi-label">平均及格率</div>
          <div class="kpi-value">{{ fmt(kpi.avgPassRate) }}%</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="kpi">
          <div class="kpi-label">平均分</div>
          <div class="kpi-value">{{ fmt(kpi.avgScore) }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="kpi" :class="{ 'kpi-warn': (kpi.pendingMarkCount ?? 0) > 0 }">
          <div class="kpi-label">待阅卷</div>
          <div class="kpi-value">{{ kpi.pendingMarkCount ?? 0 }}</div>
          <div class="kpi-sub">涉及 {{ kpi.pendingMarkExamCount ?? 0 }} 场</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="kpi">
          <div class="kpi-label">平均用时</div>
          <div class="kpi-value">{{ fmtDuration(kpi.avgUsedSeconds) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="12" class="mb-4">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>分数段总体分布</span></template>
          <div v-if="scoreDist.length" class="chart">
            <div v-for="seg in scoreDist" :key="seg.segmentLabel" class="chart-row">
              <span class="chart-name">{{ seg.segmentLabel }}</span>
              <div class="chart-track">
                <div class="chart-bar" :style="{ width: barWidth(seg.personCount) }" />
              </div>
              <span class="chart-num">{{ seg.personCount ?? 0 }} 人</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>及格率 TOP10</span></template>
          <div v-if="topList.length" class="chart">
            <div v-for="row in topList" :key="row.examId" class="chart-row">
              <span class="chart-name ellipsis" :title="row.examName">{{ row.examName }}</span>
              <div class="chart-track">
                <div class="chart-bar bar-green" :style="{ width: pct(row.passRate) + '%' }" />
              </div>
              <span class="chart-num">{{ fmt(row.passRate) }}%</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>考试统计</span>
          <div>
            <el-button type="primary" plain @click="() => exportOverview()">导出</el-button>
          </div>
        </div>
      </template>
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column label="考试名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="() => goDetail(row)">{{ row.examName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="examStatusType(row.examStatus)" size="small">{{ examStatusText(row.examStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="应考" width="70" prop="invitedCount" align="center" />
        <el-table-column label="已交卷" width="80" prop="submittedCount" align="center" />
        <el-table-column label="已入统" width="80" prop="countedCount" align="center" />
        <el-table-column label="待阅" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="(row.pendingMarkCount ?? 0) > 0" type="warning" size="small">
              {{ row.pendingMarkCount }}
            </el-tag>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column label="参考率" width="90" align="center">
          <template #default="{ row }">{{ fmt(row.attendanceRate) }}%</template>
        </el-table-column>
        <el-table-column label="及格率" width="140" align="center" sortable>
          <template #default="{ row }">
            <div class="rate-cell">
              <div class="rate-track">
                <div class="rate-bar" :class="rateClass(row.passRate)" :style="{ width: pct(row.passRate) + '%' }" />
              </div>
              <span>{{ fmt(row.passRate) }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="平均分" width="90" align="center" sortable>
          <template #default="{ row }">{{ fmt(row.avgScore) }}</template>
        </el-table-column>
        <el-table-column label="难度" width="80" align="center">
          <template #default="{ row }">{{ fmt(row.difficulty) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="160" align="center">
          <template #default="{ row }">{{ row.calcTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="() => goDetail(row)">详情</el-button>
            <el-button link type="primary" @click="() => handleRecalc(row)">重新计算</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="当前筛选下没有考试" /></template>
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        :total="total"
        @pagination="() => loadList()"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getStatDashboard, recalcStat } from '@/api/exam/stat';
import type { StatExamQuery, StatExamRowVO, StatSegmentVO } from '@/api/exam/stat/types';

defineOptions({ name: 'StatDashboard' });

const router = useRouter();
const loading = ref(false);
const rows = ref<StatExamRowVO[]>([]);
const total = ref(0);
const scoreDist = ref<StatSegmentVO[]>([]);
const topList = ref<StatExamRowVO[]>([]);
const range = ref<[string, string] | null>(null);
const kpi = ref<Record<string, any>>({});

const query = ref<StatExamQuery>({ pageNum: 1, pageSize: 10 });

/** 后端 BigDecimal 到前端是字符串，统一兜一层，避免出现 NaN */
const num = (v: unknown): number => {
  const n = typeof v === 'number' ? v : Number(v);
  return Number.isFinite(n) ? n : 0;
};
const fmt = (v: unknown): string => num(v).toFixed(2);
const pct = (v: unknown): number => Math.min(100, Math.max(0, num(v)));

const fmtDuration = (seconds?: number): string => {
  if (!seconds) return '-';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
};

const barWidth = (count?: number): string => {
  const max = Math.max(...scoreDist.value.map((s) => s.personCount ?? 0), 1);
  return `${((count ?? 0) / max) * 100}%`;
};

const examStatusText = (status?: string): string =>
  ({ not_start: '未开始', ongoing: '进行中', finished: '已结束', archived: '已归档' } as Record<string, string>)[status ?? ''] ?? '-';

const examStatusType = (status?: string): 'info' | 'success' | 'warning' | 'primary' =>
  ({ not_start: 'info', ongoing: 'success', finished: 'primary', archived: 'info' } as Record<string, any>)[status ?? ''] ?? 'info';

const rateClass = (rate?: string | number): string => {
  const v = num(rate);
  if (v < 60) return 'bar-red';
  if (v < 80) return 'bar-orange';
  return 'bar-green';
};

const onRangeChange = (val: [string, string] | null) => {
  query.value.beginTime = val?.[0] ?? undefined;
  query.value.endTime = val?.[1] ?? undefined;
};

const loadList = async () => {
  loading.value = true;
  try {
    const res: any = await getStatDashboard(query.value);
    kpi.value = res?.data?.kpi ?? {};
    scoreDist.value = res?.data?.scoreDistribution ?? [];
    topList.value = res?.data?.topPassRate ?? [];
    rows.value = res?.data?.rows?.rows ?? [];
    total.value = res?.data?.rows?.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  query.value = { pageNum: 1, pageSize: 10 };
  range.value = null;
  loadList();
};

const goDetail = (row: StatExamRowVO) => {
  router.push({ path: '/system/stat/exam', query: { examId: row.examId } });
};

const handleRecalc = async (row: StatExamRowVO) => {
  await ElMessageBox.confirm(`确定重新计算「${row.examName}」的统计数据吗？`, '提示', { type: 'warning' });
  await recalcStat(row.examId);
  ElMessage.success('已提交重新计算');
  loadList();
};

const exportOverview = () => {
  ElMessage.info('导出功能将在导出中心提供');
};

onMounted(() => {
  loadList();
});
</script>

<style scoped>
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
  font-size: 24px;
  font-weight: 600;
  margin: 4px 0;
}
.kpi-sub {
  font-size: 12px;
  color: #909399;
}
.chart-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.chart-name {
  width: 110px;
  font-size: 13px;
  color: #606266;
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
  width: 60px;
  text-align: right;
  font-size: 13px;
}
.bar-green {
  background: #67c23a;
}
.bar-orange {
  background: #e6a23c;
}
.bar-red {
  background: #f56c6c;
}
.rate-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.rate-track {
  flex: 1;
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}
.rate-bar {
  height: 100%;
  border-radius: 4px;
}
.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb-4 {
  margin-bottom: 16px;
}
.p-4 {
  padding: 16px;
}
</style>
