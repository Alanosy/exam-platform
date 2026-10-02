<template>
  <div class="p-4">
    <el-card shadow="never" class="mb-4">
      <template #header>
        <div class="card-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/system/stat/dashboard' }">考试统计</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/system/stat/exam', query: { examId } }">考试详情</el-breadcrumb-item>
            <el-breadcrumb-item>试题分析</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button @click="() => goBack()">返回</el-button>
        </div>
      </template>
      <el-form :inline="true">
        <el-form-item label="题型">
          <el-select v-model="query.questionType" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="query.difficulty" placeholder="全部" clearable style="width: 110px">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="query.onlyAbnormal" label="只看异常题" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="() => loadList()">查询</el-button>
          <el-button @click="() => resetQuery()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="12">
      <el-col :span="14">
        <el-card shadow="never">
          <el-table
            :data="rows"
            border
            stripe
            highlight-current-row
            @current-change="onSelect"
          >
            <el-table-column label="题号" width="60" prop="sort" align="center" />
            <el-table-column label="题型" width="90" prop="questionType" align="center" />
            <el-table-column label="难度" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="difficultyType(row.difficulty)" size="small">{{ difficultyText(row.difficulty) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="题干" min-width="200" prop="title" show-overflow-tooltip />
            <el-table-column label="作答" width="70" prop="answerCount" align="center" />
            <el-table-column label="正确率" width="90" align="center" sortable>
              <template #default="{ row }">
                <span :class="rateClass(row)">{{ fmt(mainRate(row)) }}%</span>
              </template>
            </el-table-column>
            <el-table-column label="区分度" width="90" prop="discrimination" align="center" sortable />
          </el-table>
          <pagination
            v-show="total > 0"
            v-model:page="query.pageNum"
            v-model:limit="query.pageSize"
            :total="total"
            @pagination="() => loadList()"
          />
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span>单题详情</span></template>
          <template v-if="current">
            <div class="mb-2">
              <el-tag size="small">{{ current.questionType }}</el-tag>
              <el-tag size="small" class="ml-1">{{ current.questionCategory === 'objective' ? '客观题' : '主观题' }}</el-tag>
              <span class="ml-1">满分 {{ fmt(current.fullScore) }}</span>
            </div>
            <div class="question-title mb-2" v-html="current.title" />

            <!-- 客观题：选项分布 -->
            <template v-if="current.questionCategory === 'objective'">
              <div class="section-title">选项分布</div>
              <div v-for="opt in current.options" :key="opt.optionKey" class="chart-row">
                <span class="chart-name">
                  {{ opt.optionKey }}
                  <el-tag v-if="opt.isCorrect === '1'" type="success" size="small">正确</el-tag>
                  <el-tag v-else-if="opt.trap" type="danger" size="small">易错</el-tag>
                </span>
                <div class="chart-track">
                  <div class="chart-bar" :class="opt.isCorrect === '1' ? 'bar-green' : 'bar-blue'" :style="{ width: pct(opt.selectRate) + '%' }" />
                </div>
                <span class="chart-num">{{ opt.selectCount ?? 0 }} 人</span>
              </div>
            </template>

            <!-- 主观题：得分分布 -->
            <template v-else>
              <div class="section-title">得分情况</div>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="平均得分">{{ fmt(current.avgScore) }}</el-descriptions-item>
                <el-descriptions-item label="得分率">{{ fmt(current.scoreRate) }}%</el-descriptions-item>
                <el-descriptions-item label="最高 / 最低">{{ fmt(current.maxScore) }} / {{ fmt(current.minScore) }}</el-descriptions-item>
                <el-descriptions-item label="零分 / 满分">{{ current.zeroCount }} / {{ current.fullCount }}</el-descriptions-item>
              </el-descriptions>
            </template>

            <div class="section-title mt-2">指标</div>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="作答人数">{{ current.answerCount }}</el-descriptions-item>
              <el-descriptions-item label="未答">{{ current.blankCount }}</el-descriptions-item>
              <el-descriptions-item label="完全答对">{{ current.correctCount }}</el-descriptions-item>
              <el-descriptions-item label="部分正确">{{ current.partialCount }}</el-descriptions-item>
              <el-descriptions-item label="答错">{{ current.wrongCount }}</el-descriptions-item>
              <el-descriptions-item label="区分度">{{ fmt(current.discrimination) }}</el-descriptions-item>
            </el-descriptions>
            <el-alert
              v-if="current.suggestion"
              class="mt-2"
              type="warning"
              :closable="false"
              :title="current.suggestion"
            />
          </template>
          <el-empty v-else description="点击左侧题目查看详情" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { listStatQuestion } from '@/api/exam/stat';
import type { StatQuestionQuery, StatQuestionVO } from '@/api/exam/stat/types';

defineOptions({ name: 'StatQuestion' });

const route = useRoute();
const router = useRouter();
const examId = ref<string>(String(route.query.examId ?? ''));
const rows = ref<StatQuestionVO[]>([]);
const total = ref(0);
const current = ref<StatQuestionVO | null>(null);
const query = ref<StatQuestionQuery>({ pageNum: 1, pageSize: 20 });

const types = [
  { label: '单选题', value: 'SINGLE' },
  { label: '多选题', value: 'MULTIPLE' },
  { label: '判断题', value: 'JUDGE' },
  { label: '填空题', value: 'BLANK' },
  { label: '简答题', value: 'SHORT_ANSWER' },
  { label: '论述题', value: 'ESSAY' },
  { label: '代码题', value: 'CODE' },
  { label: '匹配题', value: 'MATCH' }
];

const num = (v: unknown): number => {
  const n = typeof v === 'number' ? v : Number(v);
  return Number.isFinite(n) ? n : 0;
};
const fmt = (v: unknown): string => num(v).toFixed(2);
const pct = (v: unknown): number => Math.min(100, Math.max(0, num(v)));
const difficultyText = (d?: string): string => ({ easy: '简单', medium: '中等', hard: '困难' } as Record<string, string>)[d ?? ''] ?? '-';
const difficultyType = (d?: string): any => ({ easy: 'success', medium: 'warning', hard: 'danger' } as Record<string, any>)[d ?? ''] ?? 'info';
const mainRate = (row: StatQuestionVO): unknown =>
  row.questionCategory === 'objective' ? row.correctRate : row.scoreRate;
const rateClass = (row: StatQuestionVO): string => {
  const v = num(mainRate(row));
  return v < 30 ? 'text-red' : v < 60 ? 'text-orange' : v >= 85 ? 'text-green' : '';
};

const loadList = async () => {
  const res: any = await listStatQuestion(examId.value, query.value);
  rows.value = res?.rows ?? [];
  total.value = res?.total ?? 0;
  if (rows.value.length && !current.value) {
    current.value = rows.value[0];
  }
};

const resetQuery = () => {
  query.value = { pageNum: 1, pageSize: 20 };
  loadList();
};

/** 点击列表行切右侧详情；current-change 在取消选中时会传 null，这里要能接住 */
const onSelect = (row: StatQuestionVO | null) => {
  if (!row) {
    return;
  }
  current.value = row;
};

const goBack = () => router.push({ path: '/system/stat/exam', query: { examId: examId.value } });

onMounted(() => {
  if (!examId.value) {
    ElMessage.error('缺少考试ID');
    return;
  }
  loadList();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.question-title {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  max-height: 160px;
  overflow: auto;
}
.chart-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.chart-name {
  width: 90px;
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
  border-radius: 7px;
}
.bar-blue {
  background: #409eff;
}
.bar-green {
  background: #67c23a;
}
.chart-num {
  width: 60px;
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
.mt-2 {
  margin-top: 8px;
}
.ml-1 {
  margin-left: 4px;
}
.p-4 {
  padding: 16px;
}
</style>
