<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">考试记录</span>
          <el-button plain icon="Refresh" @click="handleQuery">刷新</el-button>
        </div>
      </template>

      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
        <el-form-item label="考试" prop="examId">
          <el-select v-model="queryParams.examId" placeholder="全部考试" clearable filterable style="width: 220px">
            <el-option v-for="item in examOptions" :key="item.examId" :label="item.examName" :value="item.examId" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试类型" prop="examType">
          <el-select v-model="queryParams.examType" placeholder="全部类型" clearable style="width: 140px">
            <el-option v-for="item in examTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in examRecordStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否及格" prop="passed">
          <el-select v-model="queryParams.passed" placeholder="不限" clearable style="width: 120px">
            <el-option label="及格" :value="true" />
            <el-option label="未及格" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="考试名称" prop="examName" min-width="180" show-overflow-tooltip />
        <el-table-column label="试卷" prop="paperName" min-width="140" show-overflow-tooltip />
        <el-table-column label="次数" align="center" width="80">
          <template #default="{ row }">第 {{ row.attemptNo ?? 1 }} 次</template>
        </el-table-column>
        <el-table-column label="考试类型" align="center" width="110">
          <template #default="{ row }">
            <dict-tag :options="examTypeOptions" :value="row.examType" />
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="{ row }">
            <dict-tag :options="examRecordStatusOptions" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="开考时间" align="center" width="150">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="交卷时间" align="center" width="150">
          <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="用时" align="center" width="90">
          <template #default="{ row }">{{ usedText(row.usedSeconds) }}</template>
        </el-table-column>
        <el-table-column label="得分" align="center" width="120">
          <template #default="{ row }">
            <span class="score-num">{{ row.totalScore ?? 0 }}</span>
            <span class="score-total"> / {{ row.paperTotalScore ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客观 / 主观" align="center" width="120">
          <template #default="{ row }">{{ row.objectiveScore ?? 0 }} / {{ row.subjectiveScore ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="答对 / 答错" align="center" width="110">
          <template #default="{ row }">
            <span class="text-right">{{ row.correctCount ?? 0 }}</span>
            <span class="score-total"> / {{ row.wrongCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="及格分" align="center" width="80">
          <template #default="{ row }">{{ row.passScore ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="结果" align="center" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status !== 'submitted'" type="info" size="small" effect="plain">未出分</el-tag>
            <el-tag v-else-if="row.passed" type="success" size="small" effect="plain">及格</el-tag>
            <el-tag v-else type="danger" size="small" effect="plain">未及格</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="goDetail(row)">答题记录</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

      <el-empty v-if="!loading && total === 0" description="还没有考试记录，先去考试中心参加一场考试吧" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="ExamRecords">
import { getExamRecords, listMyExams } from '@/api/exam/answer';
import type { ExamRecordVO, ExamRecordQuery, ExamCenterVO } from '@/api/exam/answer/types';
import { useExamDicts } from '@/hooks/useExamDicts';

const router = useRouter();
// 首屏先落在 loading 上：首次 render 早于 onMounted，否则会闪一下空表格
const loading = ref(true);
const total = ref(0);
const list = ref<ExamRecordVO[]>([]);
const examOptions = ref<ExamCenterVO[]>([]);

const queryFormRef = ref<ElFormInstance>();
const queryParams = ref<ExamRecordQuery>({ pageNum: 1, pageSize: 10 });

/** 考试类型与答卷状态都走字典 */
const { examTypeOptions, examRecordStatusOptions } = useExamDicts();

const pad = (n: number) => String(n).padStart(2, '0');

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

/** 秒 → x分x秒 */
const usedText = (seconds?: number): string => {
  if (seconds === undefined || seconds === null) return '-';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return m > 0 ? `${m}分${s}秒` : `${s}秒`;
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await getExamRecords(queryParams.value);
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
  queryFormRef.value?.resetFields();
  queryParams.value = { pageNum: 1, pageSize: 10 };
  getList();
};

/** 考试下拉复用考试中心的数据，考生能看到的就是自己加入过的考试 */
const loadExamOptions = async () => {
  try {
    const res = await listMyExams();
    examOptions.value = res.data ?? [];
  } catch {
    examOptions.value = [];
  }
};

const goDetail = (row: ExamRecordVO) => router.push(`/exam/record/${row.recordId}`);

onMounted(async () => {
  await loadExamOptions();
  getList();
});
</script>

<style scoped lang="scss">
.score-num {
  font-size: 15px;
  font-weight: 600;
  color: #409eff;
}

.score-total {
  color: #909399;
}

.text-right {
  color: #67c23a;
}
</style>
