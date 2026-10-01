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

      <!-- 先按考试类型筛，再按来源（我创建的 / 我加入的）筛 -->
      <el-tabs v-model="activeType" class="exam-tabs">
        <el-tab-pane label="全部" name="all" />
        <!-- 考试类型页签跟着字典 exam_type 走，改字典文案这里就跟着变 -->
        <el-tab-pane v-for="item in examTypeOptions" :key="item.value" :label="item.label" :name="item.value" />
      </el-tabs>

      <el-radio-group v-model="activeOwner" class="owner-filter">
        <el-radio-button value="all">全部 {{ typeCount }}</el-radio-button>
        <el-radio-button value="mine">我创建的 {{ mineCount }}</el-radio-button>
        <el-radio-button value="joined">我加入的 {{ joinedCount }}</el-radio-button>
      </el-radio-group>

      <el-empty v-if="!loading && filtered.length === 0" :description="emptyTip" />

      <el-skeleton v-else-if="loading" :rows="4" animated />

      <div v-else class="exam-grid">
        <el-card v-for="item in filtered" :key="item.examId" shadow="hover" class="exam-card">
          <template #header>
            <div class="flex items-start justify-between gap-2">
              <div class="flex items-center gap-1 min-w-0">
                <!-- 考试类型单独成列：正式考试和练习考试一眼分得开 -->
                <dict-tag :options="examTypeOptions" :value="item.examType || '1'" />
                <!-- 自己创建的标出来，顺便验证 owner 字段有没有从后端回来 -->
                <el-tag v-if="isMine(item)" size="small" type="warning" effect="plain">我创建的</el-tag>
                <span class="exam-name" :title="item.examName">{{ item.examName }}</span>
              </div>
              <dict-tag :options="examMyStatusOptions" :value="item.myStatus" />
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
import { useExamDicts } from '@/hooks/useExamDicts';

const router = useRouter();
// 首屏先显示骨架屏，避免数据回来前闪一下空状态
const loading = ref(true);
const list = ref<ExamCenterVO[]>([]);

// 考试类型与我的状态都走字典，改字典文案即可，不用改代码
const { examTypeOptions, examMyStatusOptions } = useExamDicts();

/** 标签页：全部 / 正式考试 / 练习刷题 */
const activeType = ref<string>('all');
/** 来源筛选：全部 / 我创建的 / 我加入的 */
const activeOwner = ref<string>('all');

/** 来源判定统一走这里：后端 owner 为 Boolean，取真值即可 */
const isMine = (item: ExamCenterVO) => item.owner === true;

/** 按考试类型过滤，数量统计和列表展示都复用它 */
const filteredOfType = (type: string): ExamCenterVO[] =>
  type === 'all' ? list.value : list.value.filter((item) => item.examType === type);

const filtered = computed(() => {
  let result = filteredOfType(activeType.value);
  if (activeOwner.value === 'mine') {
    result = result.filter(isMine);
  } else if (activeOwner.value === 'joined') {
    result = result.filter((item) => !isMine(item));
  }
  return result;
});

/** 按钮上的数量：跟着当前类型页签走，方便一眼确认筛选有没有生效 */
const typeCount = computed(() => filteredOfType(activeType.value).length);
const mineCount = computed(() => filteredOfType(activeType.value).filter(isMine).length);
const joinedCount = computed(() => filteredOfType(activeType.value).filter((item) => !isMine(item)).length);

const emptyTip = computed(() => {
  if (activeOwner.value === 'mine') return '你还没有创建考试，去「考试管理」新建一场即可直接在这里参加';
  if (activeOwner.value === 'joined') return '你还没有通过链接加入任何考试';
  if (activeType.value === 'all') return '你还没有加入任何考试，可通过邀请链接加入';
  return '暂无该类型的考试';
});

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
.exam-tabs {
  margin-bottom: 12px;
}

.owner-filter {
  margin-bottom: 12px;
}

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
