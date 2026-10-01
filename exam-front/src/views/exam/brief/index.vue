<template>
  <div class="brief-page">
    <div class="brief-card">
      <el-skeleton v-if="loading" :rows="5" animated />

      <el-result v-else-if="errorMsg" icon="warning" title="无法进入该考试" :sub-title="errorMsg">
        <template #extra>
          <el-button plain @click="goCenter">返回考试中心</el-button>
        </template>
      </el-result>

      <template v-else>
        <div class="brief-header">
          <span class="brief-badge">考试须知</span>
          <h1 class="brief-title">{{ info.examName }}</h1>
          <div class="brief-meta">
            <dict-tag :options="examMyStatusOptions" :value="info.myStatus" />
            <span class="brief-meta-item">限时 {{ info.duration ? `${info.duration} 分钟` : '不限时' }}</span>
          </div>
        </div>

        <el-descriptions :column="1" border class="brief-desc">
          <el-descriptions-item label="考试时间">{{ timeRange }}</el-descriptions-item>
          <el-descriptions-item label="考试时长">{{ info.duration ? `${info.duration} 分钟` : '不限时，以考试结束时间为准' }}</el-descriptions-item>
          <el-descriptions-item label="参加次数">已参加 {{ info.attemptCount }} 次</el-descriptions-item>
          <el-descriptions-item label="考试说明">{{ info.examDesc || '暂无说明' }}</el-descriptions-item>
        </el-descriptions>

        <el-alert v-if="info.tip" class="brief-alert" type="warning" :closable="false" show-icon :title="info.tip" />

        <div class="brief-rules">
          <div class="brief-rules-title">答题注意事项</div>
          <ol class="brief-rules-list">
            <li>进入考试后开始计时，<b>中途退出不会暂停计时</b>，剩余时间用完系统会自动交卷。</li>
            <li>答案会实时保存，意外退出后重新进入可继续作答。</li>
            <li>考试时间结束后将无法继续答题，请合理分配时间。</li>
            <li>答题过程中请勿刷新页面或关闭浏览器，避免丢失未保存的作答。</li>
          </ol>
        </div>

        <div class="brief-actions">
          <el-button type="primary" size="large" :loading="starting" :disabled="!info.canStart" @click="handleStart">
            {{ info.myStatus === 'answering' ? '继续答题' : '确认并开始考试' }}
          </el-button>
          <el-button size="large" plain @click="goCenter">返回考试中心</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts" name="ExamBrief">
import { listMyExams, startExam } from '@/api/exam/answer';
import type { ExamCenterVO } from '@/api/exam/answer/types';
import { useExamDicts } from '@/hooks/useExamDicts';

const route = useRoute();
const router = useRouter();

/** 我的考试状态走 exam_my_status 字典 */
const { examMyStatusOptions } = useExamDicts();

// 首屏先显示骨架屏，避免数据回来前闪一下空表单
const loading = ref(true);
const starting = ref(false);
const errorMsg = ref('');
const info = ref<ExamCenterVO>({
  examId: '',
  examName: '',
  examType: '',
  examDesc: '',
  startTime: '',
  endTime: '',
  duration: 0,
  examStatus: '',
  myStatus: '',
  tip: '',
  attemptCount: 0,
  canStart: false
});

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const timeRange = computed(() => `${formatTime(info.value.startTime)} 至 ${formatTime(info.value.endTime)}`);

/** 说明页直接复用考试中心的数据，避免再开一个查询接口 */
const loadInfo = async () => {
  const examId = route.params.examId as string;
  if (!examId) {
    errorMsg.value = '缺少考试ID';
    return;
  }
  loading.value = true;
  try {
    const res = await listMyExams();
    const hit = (res.data ?? []).find((item) => String(item.examId) === examId);
    if (!hit) {
      errorMsg.value = '你还没有加入该考试，或该考试已不存在';
      return;
    }
    info.value = hit;
  } catch (e: any) {
    errorMsg.value = (e instanceof Error && e.message) || '加载考试信息失败';
  } finally {
    loading.value = false;
  }
};

const handleStart = async () => {
  starting.value = true;
  try {
    // 后端有答题中的答卷时会直接返回原答卷，实现中途退出续答
    const res = await startExam(info.value.examId);
    await router.push(`/exam/answer/${res.data}`);
  } finally {
    starting.value = false;
  }
};

const goCenter = () => router.push('/exam/center');

onMounted(loadInfo);
</script>

<style scoped lang="scss">
.brief-page {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  min-height: 100vh;
  padding: 48px 16px;
  background: linear-gradient(180deg, #f2f6fc 0%, #f7f9fc 100%);
  box-sizing: border-box;
}

.brief-card {
  width: 100%;
  max-width: 680px;
  padding: 32px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgb(0 0 0 / 6%);
}

.brief-badge {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  color: #337ecc;
  background: #ecf5ff;
  border-radius: 10px;
}

.brief-title {
  margin: 12px 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
}

.brief-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.brief-meta-item {
  font-size: 13px;
  color: #909399;
}

.brief-desc {
  margin-bottom: 20px;
}

.brief-alert {
  margin-bottom: 20px;
}

.brief-rules {
  padding: 16px;
  margin-bottom: 20px;
  background: #fafcff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.brief-rules-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.brief-rules-list {
  padding-left: 20px;
  font-size: 13px;
  line-height: 1.9;
  color: #606266;
}

.brief-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

@media (width <= 640px) {
  .brief-page {
    padding: 16px 12px;
  }

  .brief-card {
    padding: 20px;
  }
}
</style>
