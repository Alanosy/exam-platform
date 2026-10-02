<template>
  <div class="p-4">
    <el-card shadow="never" class="mb-4">
      <template #header>
        <div class="card-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/system/stat/dashboard' }">考试统计</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/system/stat/exam', query: { examId } }">考试详情</el-breadcrumb-item>
            <el-breadcrumb-item>知识点薄弱分析</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button @click="() => goBack()">返回</el-button>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="mb-4"
        title="知识点取自题库分类树，掌握度按分值加权计算：2 分的选择题不会和 20 分的论述题有一样的话语权"
      />

      <el-row :gutter="12" class="mb-4">
        <el-col :span="6"><el-card shadow="never" class="kpi"><div class="kpi-label">知识点总数</div><div class="kpi-value">{{ rows.length }}</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="never" class="kpi"><div class="kpi-label">薄弱点数量</div><div class="kpi-value text-red">{{ weakCount }}</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="never" class="kpi"><div class="kpi-label">平均掌握度</div><div class="kpi-value">{{ avgMastery }}%</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="never" class="kpi"><div class="kpi-label">最薄弱知识点</div><div class="kpi-value kpi-text">{{ weakest }}</div></el-card></el-col>
      </el-row>

      <div class="mb-2">
        <el-button :type="onlyWeak ? 'primary' : 'default'" @click="() => toggleWeak()">只看薄弱</el-button>
      </div>

      <el-table :data="filteredRows" border stripe @row-click="onSelect">
        <el-table-column label="知识点" min-width="220" prop="knowledgePath" show-overflow-tooltip />
        <el-table-column label="题目数" width="80" prop="questionCount" align="center" />
        <el-table-column label="满分" width="80" align="center">
          <template #default="{ row }">{{ fmt(row.fullScore) }}</template>
        </el-table-column>
        <el-table-column label="平均得分" width="100" align="center">
          <template #default="{ row }">{{ fmt(row.avgScore) }}</template>
        </el-table-column>
        <el-table-column label="得分率" width="90" align="center">
          <template #default="{ row }">{{ fmt(row.scoreRate) }}%</template>
        </el-table-column>
        <el-table-column label="错误人次" width="90" prop="wrongCount" align="center" />
        <el-table-column label="错误率" width="90" align="center">
          <template #default="{ row }">{{ fmt(row.wrongRate) }}%</template>
        </el-table-column>
        <el-table-column label="掌握度" width="200" align="center" sortable>
          <template #default="{ row }">
            <el-progress :percentage="pct(row.mastery)" :status="masteryStatus(row.mastery)" />
          </template>
        </el-table-column>
        <el-table-column label="等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="weakType(row.weakLevel)" size="small">{{ weakText(row.weakLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="() => goQuestion(row)">看题目</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无知识点数据（题目需归属题库分类）" /></template>
      </el-table>
    </el-card>

    <el-card v-if="selected" shadow="never">
      <template #header><span>{{ selected.knowledgePath }} — 薄弱考生</span></template>
      <el-table :data="selectedUsers" border stripe max-height="320">
        <el-table-column label="考生" min-width="140">
          <template #default="{ row }">
            <el-link type="primary" @click="() => goUser(row)">{{ row.userName || row.account }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="部门" min-width="120" prop="deptName" show-overflow-tooltip />
        <el-table-column label="总分" width="90" prop="totalScore" align="center" />
        <el-table-column label="排名" width="80" prop="rankNo" align="center" />
      </el-table>
      <template v-if="!selectedUsers.length"><el-empty description="该知识点暂无答错的考生" /></template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { listStatKnowledge, listStatUser } from '@/api/exam/stat';
import type { StatKnowledgeVO, StatUserVO } from '@/api/exam/stat/types';

defineOptions({ name: 'StatKnowledge' });

const route = useRoute();
const router = useRouter();
const examId = ref<string>(String(route.query.examId ?? ''));
const rows = ref<StatKnowledgeVO[]>([]);
const onlyWeak = ref(false);
const selected = ref<StatKnowledgeVO | null>(null);
const selectedUsers = ref<StatUserVO[]>([]);

const num = (v: unknown): number => {
  const n = typeof v === 'number' ? v : Number(v);
  return Number.isFinite(n) ? n : 0;
};
const fmt = (v: unknown): string => num(v).toFixed(2);
const pct = (v: unknown): number => Math.min(100, Math.max(0, Math.round(num(v))));
const weakText = (s?: string): string => ({ good: '良好', normal: '一般', weak: '薄弱' } as Record<string, string>)[s ?? ''] ?? '-';
const weakType = (s?: string): any => ({ good: 'success', normal: 'warning', weak: 'danger' } as Record<string, any>)[s ?? ''] ?? 'info';
const masteryStatus = (v?: string | number): any => {
  const n = num(v);
  return n < 60 ? 'exception' : n < 80 ? 'warning' : 'success';
};

const filteredRows = computed<StatKnowledgeVO[]>(() => {
  const list = [...rows.value].sort((a, b) => num(a.mastery) - num(b.mastery));
  return onlyWeak.value ? list.filter((r) => r.weakLevel === 'weak') : list;
});

const weakCount = computed(() => rows.value.filter((r) => r.weakLevel === 'weak').length);
const avgMastery = computed(() => {
  if (!rows.value.length) return '0.00';
  const sum = rows.value.reduce((acc, r) => acc + num(r.mastery), 0);
  return (sum / rows.value.length).toFixed(2);
});
const weakest = computed(() => {
  if (!rows.value.length) return '-';
  const w = [...rows.value].sort((a, b) => num(a.mastery) - num(b.mastery))[0];
  return `${w.knowledgeName ?? '-'} ${fmt(w.mastery)}%`;
});

const toggleWeak = () => {
  onlyWeak.value = !onlyWeak.value;
};

const onSelect = async (row: StatKnowledgeVO | null) => {
  if (!row) {
    return;
  }
  selected.value = row;
  // 下钻：拉出这场考试里成绩靠后的考生，作为「谁最该补」的名单
  const res: any = await listStatUser(examId.value, { pageNum: 1, pageSize: 20, statStatus: 'COUNTED' });
  selectedUsers.value = (res?.rows ?? []).filter((u: StatUserVO) => (u.wrongCount ?? 0) > 0).slice(0, 20);
};

const goQuestion = (row: StatKnowledgeVO) => {
  router.push({ path: '/system/stat/question', query: { examId: examId.value, knowledgeId: row.knowledgeId } });
};
const goUser = (row: StatUserVO) => {
  router.push({ path: '/system/stat/user', query: { examId: examId.value, userId: row.userId } });
};
const goBack = () => router.push({ path: '/system/stat/exam', query: { examId: examId.value } });

onMounted(async () => {
  if (!examId.value) {
    ElMessage.error('缺少考试ID');
    return;
  }
  const res: any = await listStatKnowledge(examId.value);
  rows.value = res?.data ?? [];
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
.kpi-text {
  font-size: 15px;
}
.text-red {
  color: #f56c6c;
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
