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
        <el-table-column label="阅卷进度" align="center" min-width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.progress ?? 0" :stroke-width="10" :text-inside="true" />
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
</style>
