<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">考试中心</span>
          <div class="flex items-center gap-2">
            <el-button plain icon="Tickets" @click="goRecords">考试记录</el-button>
            <el-button plain icon="Refresh" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!loading && list.length === 0" description="你还没有加入任何考试，可通过邀请链接加入" />

      <el-skeleton v-else-if="loading" :rows="4" animated />

      <div v-else class="exam-grid">
        <el-card v-for="item in list" :key="item.examId" shadow="hover" class="exam-card">
          <template #header>
            <div class="flex items-start justify-between gap-2">
              <span class="exam-name" :title="item.examName">{{ item.examName }}</span>
              <el-tag :type="statusTag(item.myStatus).type" size="small" effect="light">{{ statusTag(item.myStatus).label }}</el-tag>
            </div>
          </template>

          <div class="exam-line">
            <span class="exam-label">考试时间</span>
            <span>{{ timeRange(item) }}</span>
          </div>
          <div class="exam-line">
            <span class="exam-label">考试时长</span>
            <span>{{ item.duration ? `${item.duration} 分钟` : '不限时' }}</span>
          </div>
          <div class="exam-line">
            <span class="exam-label">已参加</span>
            <span>{{ item.attemptCount }} 次</span>
          </div>
          <div v-if="item.myStatus === 'submitted' && item.totalScore !== undefined" class="exam-line">
            <span class="exam-label">最近成绩</span>
            <span>
              {{ item.totalScore }} 分
              <el-tag v-if="item.passed !== undefined" :type="item.passed ? 'success' : 'danger'" size="small" effect="plain" class="ml-1">
                {{ item.passed ? '及格' : '未及格' }}
              </el-tag>
            </span>
          </div>
          <el-alert v-if="item.tip" class="mt-[10px]" type="info" :closable="false" show-icon :title="item.tip" />

          <div class="exam-actions">
            <el-button v-if="item.canStart" type="primary" size="small" @click="goBrief(item)">
              {{ item.myStatus === 'answering' ? '继续答题' : '开始考试' }}
            </el-button>
            <el-button v-else-if="item.recordId" size="small" plain @click="goResult(item)">查看成绩</el-button>
            <el-button v-else size="small" plain disabled>不可参加</el-button>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts" name="ExamCenter">
import { listMyExams } from '@/api/exam/answer';
import type { ExamCenterVO } from '@/api/exam/answer/types';

const router = useRouter();
// 首屏先显示骨架屏，避免数据回来前闪一下空状态
const loading = ref(true);
const list = ref<ExamCenterVO[]>([]);

/** 我在考试上的状态 → 展示文案与标签色 */
const STATUS_TAG: Record<string, { label: string; type: 'primary' | 'success' | 'warning' | 'info' | 'danger' }> = {
  not_start: { label: '未开始', type: 'info' },
  pending: { label: '待考试', type: 'primary' },
  answering: { label: '答题中', type: 'warning' },
  submitted: { label: '已交卷', type: 'success' },
  ended: { label: '已结束', type: 'info' },
  late: { label: '迟到不可参加', type: 'danger' },
  blocked: { label: '不可参加', type: 'danger' }
};

const statusTag = (status: string) => STATUS_TAG[status] ?? { label: '待考试', type: 'info' };

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const timeRange = (item: ExamCenterVO) => `${formatTime(item.startTime)} 至 ${formatTime(item.endTime)}`;

const loadList = async () => {
  loading.value = true;
  try {
    const res = await listMyExams();
    list.value = res.data ?? [];
  } finally {
    loading.value = false;
  }
};

/** 进说明页确认后再开考；已经在答题中时说明页会直接续上原来的答卷 */
const goBrief = (item: ExamCenterVO) => router.push(`/exam/brief/${item.examId}`);

/** 已交卷的考试直接进答题记录详情，能看到逐题作答与解析 */
const goResult = (item: ExamCenterVO) => router.push(`/exam/record/${item.recordId}`);

/** 已交卷的考试直接进答题记录详情，能看到逐题作答 */
const goRecords = () => router.push('/exam/records');

onMounted(loadList);
</script>

<style scoped lang="scss">
.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.exam-card {
  border-radius: 8px;
}

.exam-name {
  overflow: hidden;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.exam-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}

.exam-label {
  color: #909399;
}

.exam-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
