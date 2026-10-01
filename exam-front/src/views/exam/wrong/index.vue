<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">错题本</span>
          <el-button plain icon="Refresh" @click="getList">刷新</el-button>
        </div>
      </template>

      <!-- 总览：一眼看清还有多少坑没填 -->
      <div v-loading="overviewLoading" class="overview-box">
        <div class="overview-item">
          <div class="overview-value">{{ overview.totalCount ?? 0 }}</div>
          <div class="overview-label">错题总数</div>
        </div>
        <div class="overview-item">
          <div class="overview-value is-danger">{{ overview.notMasterCount ?? 0 }}</div>
          <div class="overview-label">未掌握</div>
        </div>
        <div class="overview-item">
          <div class="overview-value is-success">{{ overview.masteredCount ?? 0 }}</div>
          <div class="overview-label">已掌握</div>
        </div>
        <div class="overview-item">
          <div class="overview-value is-muted">{{ overview.ignoredCount ?? 0 }}</div>
          <div class="overview-label">已忽略</div>
        </div>
        <div class="overview-item">
          <div class="overview-value is-primary">{{ overview.todayCount ?? 0 }}</div>
          <div class="overview-label">今日新增</div>
        </div>
        <div class="overview-item">
          <div class="overview-value is-primary">{{ overview.weekCount ?? 0 }}</div>
          <div class="overview-label">近七天</div>
        </div>
      </div>

      <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="68px">
        <el-form-item label="来源名称" prop="sourceName">
          <el-input v-model="queryParams.sourceName" placeholder="考试 / 练习名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="来源类型" prop="sourceType">
          <el-select v-model="queryParams.sourceType" placeholder="全部来源" clearable style="width: 140px">
            <el-option v-for="item in wrongSourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试类型" prop="examType">
          <el-select v-model="queryParams.examType" placeholder="全部类型" clearable style="width: 140px">
            <el-option v-for="item in examTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="来源类型" align="center" width="110">
          <template #default="{ row }">
            <dict-tag :options="wrongSourceTypeOptions" :value="row.sourceType" />
          </template>
        </el-table-column>
        <el-table-column label="考试类型" align="center" width="110">
          <template #default="{ row }">
            <dict-tag :options="examTypeOptions" :value="row.examType" />
          </template>
        </el-table-column>
        <el-table-column label="来源名称" prop="sourceName" min-width="200" show-overflow-tooltip />
        <el-table-column label="错题数" align="center" width="90">
          <template #default="{ row }">
            <span class="num-danger">{{ row.wrongCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未掌握" align="center" width="90">
          <template #default="{ row }">
            <span class="num-danger">{{ row.notMasterCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已掌握" align="center" width="90">
          <template #default="{ row }">
            <span class="num-success">{{ row.masteredCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最近答错" align="center" width="160">
          <template #default="{ row }">{{ formatTime(row.lastWrongTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="goDetail(row)">查看</el-button>
            <el-button link type="success" icon="RefreshRight" @click="practiceSource(row)">一键重刷</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

      <el-empty v-if="!loading && total === 0" description="还没有错题，继续保持" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="ExamWrongBook">
import { getWrongOverview, getWrongSourcesPage } from '@/api/exam/wrong';
import type { WrongOverviewVO, WrongSourceVO, WrongSourceQuery } from '@/api/exam/wrong/types';
import { useExamDicts } from '@/hooks/useExamDicts';

const router = useRouter();

// 字典优先 + 代码兜底：下拉与标签文案都能在「字典管理」里改
const { examTypeOptions, wrongSourceTypeOptions } = useExamDicts();

const loading = ref(true);
const overviewLoading = ref(true);
const total = ref(0);
const list = ref<WrongSourceVO[]>([]);
const overview = ref<WrongOverviewVO>({
  totalCount: 0,
  notMasterCount: 0,
  masteredCount: 0,
  ignoredCount: 0,
  todayCount: 0,
  weekCount: 0,
  typeDistribution: {},
  difficultyDistribution: {}
});

const queryFormRef = ref<ElFormInstance>();
const queryParams = ref<WrongSourceQuery>({ pageNum: 1, pageSize: 10 });

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
    const res = await getWrongSourcesPage(queryParams.value);
    list.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } catch {
    list.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const loadOverview = async () => {
  overviewLoading.value = true;
  try {
    const res = await getWrongOverview();
    overview.value = res.data;
  } catch {
    // 统计挂了不影响列表，保持零值即可
  } finally {
    overviewLoading.value = false;
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

/** 看某一场考试的错题明细：参数一律字符串透传，避免雪花 ID 丢精度 */
const goDetail = (row: WrongSourceVO) => {
  router.push({
    path: '/exam/wrong/detail',
    query: {
      sourceType: row.sourceType,
      sourceId: String(row.sourceId ?? ''),
      examType: row.examType ?? '',
      sourceName: row.sourceName ?? ''
    }
  });
};

/** 直接开刷：只带本场考试的范围 */
const practiceSource = (row: WrongSourceVO) => {
  router.push({
    path: '/exam/wrong/practice',
    query: {
      sourceType: row.sourceType,
      sourceId: String(row.sourceId ?? ''),
      sourceName: row.sourceName ?? ''
    }
  });
};

onMounted(() => {
  loadOverview();
  getList();
});
</script>

<style scoped lang="scss">
.overview-box {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.overview-item {
  flex: 1;
  min-width: 96px;
  padding: 12px 8px;
  text-align: center;
  background: #f8f9fb;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.overview-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  font-variant-numeric: tabular-nums;
}

.overview-value.is-danger {
  color: #f56c6c;
}

.overview-value.is-success {
  color: #67c23a;
}

.overview-value.is-primary {
  color: #409eff;
}

.overview-value.is-muted {
  color: #909399;
}

.overview-label {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.num-danger {
  font-weight: 600;
  color: #f56c6c;
}

.num-success {
  font-weight: 600;
  color: #67c23a;
}
</style>
