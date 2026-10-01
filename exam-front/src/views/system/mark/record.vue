<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2 min-w-0">
            <el-button link icon="ArrowLeft" @click="goBack">返回</el-button>
            <span class="font-medium">答卷列表</span>
            <span class="exam-name" :title="examName">{{ examName || '全部考试' }}</span>
          </div>
          <el-button plain icon="Refresh" @click="getList">刷新</el-button>
        </div>
      </template>

      <el-form :model="queryParams" :inline="true" label-width="68px">
        <el-form-item label="考生" prop="account">
          <el-input v-model="queryParams.account" placeholder="账号 / 姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in markTaskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="考生" min-width="150">
          <template #default="{ row }">
            <span>{{ row.userName || row.account || '-' }}</span>
            <span v-if="row.userName && row.account" class="account-text">（{{ row.account }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="次数" align="center" width="80">
          <template #default="{ row }">第 {{ row.attemptNo ?? 1 }} 次</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="{ row }">
            <dict-tag :options="markTaskStatusOptions" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="主观题（已阅 / 总）" align="center" width="150">
          <template #default="{ row }">
            <span class="num-done">{{ row.markedCount ?? 0 }}</span>
            <span class="num-split"> / </span>
            <span>{{ row.questionCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客观 / 主观" align="center" width="120">
          <template #default="{ row }">{{ row.objectiveScore ?? 0 }} / {{ row.subjectiveScore ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="总分" align="center" width="120">
          <template #default="{ row }">
            <span class="score-num">{{ row.totalScore ?? 0 }}</span>
            <span class="num-split"> / {{ row.passScore ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结果" align="center" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status !== 'finished'" type="info" size="small" effect="plain">未出分</el-tag>
            <el-tag v-else-if="row.passed" type="success" size="small" effect="plain">及格</el-tag>
            <el-tag v-else type="danger" size="small" effect="plain">未及格</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="交卷时间" align="center" width="160">
          <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="阅卷人" align="center" width="110">
          <template #default="{ row }">{{ row.markerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="EditPen" @click="goMarking(row)">
              {{ row.status === 'finished' ? '查看' : '去阅卷' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />

      <el-empty v-if="!loading && total === 0" description="这场考试还没有需要阅卷的答卷" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="SystemMarkRecord">
import { getMarkTaskList } from '@/api/system/mark';
import type { MarkTaskVO, MarkTaskQuery } from '@/api/system/mark/types';
import { useExamDicts } from '@/hooks/useExamDicts';

const route = useRoute();
const router = useRouter();

const { markTaskStatusOptions } = useExamDicts();

const loading = ref(true);
const total = ref(0);
const list = ref<MarkTaskVO[]>([]);

// 从阅卷首页下钻进来，带的是考试ID；直接刷新页面时 examId 会丢，这里允许查全部
const examId = ref('');
const examName = ref('');

const queryParams = ref<MarkTaskQuery>({ pageNum: 1, pageSize: 10 });

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
    const res = await getMarkTaskList({
      ...queryParams.value,
      // 19 位雪花 ID 必须字符串透传，不能 Number() 转换
      examId: examId.value || undefined
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
  queryParams.value = { pageNum: 1, pageSize: 10 };
  getList();
};

const goMarking = (row: MarkTaskVO) => {
  router.push({
    path: '/system/mark/marking',
    query: {
      taskId: String(row.taskId ?? ''),
      examName: row.examName ?? '',
      userName: row.userName || row.account || ''
    }
  });
};

const goBack = () => router.push('/system/mark');

onMounted(() => {
  const q = route.query;
  examId.value = String(q.examId ?? '');
  examName.value = String(q.examName ?? '');
  getList();
});
</script>

<style scoped lang="scss">
.exam-name {
  overflow: hidden;
  font-size: 13px;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-text {
  margin-left: 4px;
  font-size: 12px;
  color: #909399;
}

.score-num {
  font-size: 15px;
  font-weight: 600;
  color: #409eff;
}

.num-done {
  font-weight: 600;
  color: #67c23a;
}

.num-split {
  color: #909399;
}
</style>
