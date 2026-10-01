<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">阅卷管理</span>
          <el-button plain icon="Refresh" @click="getList">刷新</el-button>
        </div>
      </template>

      <el-form :model="queryParams" :inline="true" label-width="68px">
        <el-form-item label="考试" prop="examName">
          <el-input v-model="queryParams.examName" placeholder="考试名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="试卷" prop="paperName">
          <el-input v-model="queryParams.paperName" placeholder="试卷名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="queryParams.onlyUnfinished" label="只看未阅完" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="考试名称" prop="examName" min-width="180" show-overflow-tooltip />
        <el-table-column label="试卷" prop="paperName" min-width="150" show-overflow-tooltip />
        <el-table-column label="答卷（待阅 / 总）" align="center" width="140">
          <template #default="{ row }">
            <span class="num-pending">{{ row.pendingTaskCount ?? 0 }}</span>
            <span class="num-split"> / </span>
            <span>{{ row.taskCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="主观题（待阅 / 总）" align="center" width="150">
          <template #default="{ row }">
            <span class="num-pending">{{ row.pendingItemCount ?? 0 }}</span>
            <span class="num-split"> / </span>
            <span>{{ row.itemCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="阅卷进度" align="center" min-width="200">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress
                class="mark-progress"
                :class="{ 'mark-progress--empty': progressOf(row) <= 0 }"
                :percentage="progressOf(row)"
                :stroke-width="20"
                :text-inside="true"
                :color="progressColor"
              />
              <span v-if="progressOf(row) <= 0" class="progress-zero">0%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="goRecord(row)">答卷列表</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

      <el-empty v-if="!loading && total === 0" description="暂无需要阅卷的考试，只有正式考试的主观题才会进阅卷列表" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="SystemMark">
import { getMarkExamList } from '@/api/system/mark';
import type { MarkExamVO, MarkExamQuery } from '@/api/system/mark/types';

const router = useRouter();

const loading = ref(true);
const total = ref(0);
const list = ref<MarkExamVO[]>([]);

const queryParams = ref<MarkExamQuery>({ pageNum: 1, pageSize: 10, onlyUnfinished: false });

const getList = async () => {
  loading.value = true;
  try {
    const res = await getMarkExamList(queryParams.value);
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
  queryParams.value = { pageNum: 1, pageSize: 10, onlyUnfinished: false };
  getList();
};

/** 下钻到这场考试的答卷列表 */
const goRecord = (row: MarkExamVO) => {
  router.push({
    path: '/system/mark/record',
    query: { examId: String(row.examId ?? ''), examName: row.examName ?? '' }
  });
};

/** 进度百分比（后端 Integer，这里只做边界收敛，避免脏数据撑爆进度条） */
const progressOf = (row: MarkExamVO): number => {
  const p = row.progress ?? 0;
  return Math.min(100, Math.max(0, p));
};

/** 100% 用成功色，其余保持主色 */
const progressColor = (percentage: number): string => (percentage >= 100 ? '#67c23a' : '#409eff');

onMounted(getList);
</script>

<style scoped lang="scss">
.num-pending {
  font-weight: 600;
  color: #e6a23c;
}

.num-split {
  color: #909399;
}

.progress-cell {
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 4px;
}

.mark-progress {
  width: 100%;

  /* 进度条加高，避免内嵌百分比文字被 overflow:hidden 裁掉 */
  :deep(.el-progress-bar__outer) {
    background-color: #ebeef5;
    border-radius: 10px;
  }

  :deep(.el-progress-bar__inner) {
    border-radius: 10px;
    /* 低进度时内嵌文字也需要展示空间 */
    min-width: 44px;
  }

  :deep(.el-progress-bar__innerText) {
    margin: 0 8px;
    font-size: 12px;
    font-weight: 600;
    line-height: 20px;
  }

  /* 0% 不画填充色，避免看起来像已经有进度 */
  &.mark-progress--empty :deep(.el-progress-bar__inner) {
    min-width: 0;
  }
}

.progress-zero {
  position: absolute;
  left: 12px;
  color: #909399;
  font-size: 12px;
  line-height: 20px;
}
</style>
