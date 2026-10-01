<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">考试中心</span>
          <div class="flex items-center gap-2">
            <el-button plain icon="Tickets" @click="goRecords">考试记录</el-button>
            <el-button plain icon="Refresh" @click="() => loadList()">刷新</el-button>
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
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="mine">我创建的</el-radio-button>
        <el-radio-button value="joined">我加入的</el-radio-button>
      </el-radio-group>

      <el-empty v-if="!loading && filtered.length === 0" :description="emptyTip" />

      <el-skeleton v-else-if="loading" :rows="4" animated />

      <div v-else class="exam-grid">
        <el-card v-for="item in filtered" :key="item.examId" shadow="hover" class="exam-card">
          <template #header>
            <div class="flex items-center gap-1 min-w-0">
              <!-- 考试类型单独成列：正式考试和练习考试一眼分得开 -->
              <dict-tag :options="examTypeOptions" :value="item.examType || '1'" />
              <span class="exam-name" :title="item.examName">{{ item.examName }}</span>
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
          <!-- 未开始显示倒计时；开考后如果还在入场窗口内，提示还剩多久截止 -->
          <el-alert
            v-if="statusTip(item)"
            class="mt-[10px]"
            :type="tipType(item)"
            :closable="false"
            show-icon
            :title="statusTip(item)"
          />

          <div class="exam-actions">
            <el-button v-if="canStartOf(item)" type="primary" size="small" @click="goBrief(item)">
              {{ item.myStatus === 'answering' ? '继续答题' : '开始考试' }}
            </el-button>
            <el-button v-else-if="item.recordId" size="small" plain @click="goResult(item)">查看成绩</el-button>
            <el-button v-else size="small" plain disabled>{{ item.myStatus === 'not_start' ? '未开始' : '不可参加' }}</el-button>
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
import { formatCountdown, toTs, useServerClock } from '@/hooks/useServerClock';

const router = useRouter();
// 首屏先显示骨架屏，避免数据回来前闪一下空状态
const loading = ref(true);
const list = ref<ExamCenterVO[]>([]);

// 考试类型走字典，改字典文案即可，不用改代码
const { examTypeOptions } = useExamDicts();

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

const emptyTip = computed(() => {
  if (activeOwner.value === 'mine') return '你还没有创建考试，去「考试管理」新建一场即可直接在这里参加';
  if (activeOwner.value === 'joined') return '你还没有通过链接加入任何考试';
  if (activeType.value === 'all') return '你还没有加入任何考试，可通过邀请链接加入';
  return '暂无该类型的考试';
});

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const ts = toTs(value);
  if (Number.isNaN(ts)) return value;
  const date = new Date(ts);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const timeRange = (item: ExamCenterVO) => `${formatTime(item.startTime)} 至 ${formatTime(item.endTime)}`;

/** 倒计时与到点判定一律以服务端时钟为准，别信本地时间 */
const { serverNow, syncServerTime } = useServerClock();

/** 倒计时最后这段时间内提高刷新频率，保证整点前后状态是对的 */
const IMMINENT_MS = 60_000;

/**
 * 是否已经开考
 *
 * <p>只按「到了开始时间」判，不跟着后端叠那 10 秒提前量：
 * 前端可以比后端保守，绝不能比后端激进，否则就会出现
 * 「列表里能点进去、说明页却说还没开始」这种割裂。
 */
const hasStarted = (item: ExamCenterVO): boolean => {
  const startTs = toTs(item.startTime);
  if (Number.isNaN(startTs)) return true;
  return serverNow.value >= startTs;
};

/** 距离开考还有多久（毫秒），已开考返回 0 */
const millisToStart = (item: ExamCenterVO): number => {
  const startTs = toTs(item.startTime);
  if (Number.isNaN(startTs)) return 0;
  return Math.max(0, startTs - serverNow.value);
};

/** 距离「最晚入场时间」还有多久（毫秒），已过期返回 0；后端没给这个时间返回 NaN */
const millisToDeadline = (item: ExamCenterVO): number => {
  const deadline = toTs(item.latestEntryTime);
  if (Number.isNaN(deadline)) return Number.NaN;
  return Math.max(0, deadline - serverNow.value);
};

/**
 * 是否还在入场窗口里（以服务端时间对比后端下发的最晚入场时间）
 *
 * <p>后端没下发 latestEntryTime（比如服务跑的还是旧代码）时返回 false，
 * 这时不敢自己放开按钮，免得点了又被后端一句「已超过入场时间」打回来。
 */
const inEntryWindow = (item: ExamCenterVO): boolean => {
  const left = millisToDeadline(item);
  return !Number.isNaN(left) && left > 0;
};

/**
 * 能不能点「开始考试」
 *
 * <p>后端放行当然能点；后端还停在「未开始 / 迟到」但只要服务端时间没过最晚入场时间，
 * 也把按钮放开——不然整点守在这儿的人，倒计时一归零按钮还没来得及点就变灰了。
 * 这是对「人不可能卡在整秒点按钮」的兜底，不是绕过规则：窗口一过照样进不去。
 */
const canStartOf = (item: ExamCenterVO): boolean => {
  if (item.canStart === true) return true;
  if (item.myStatus !== 'not_start' && item.myStatus !== 'late') return false;
  return inEntryWindow(item);
};

const countdownText = (item: ExamCenterVO): string => formatCountdown(millisToStart(item));

/**
 * 卡片上的状态提示
 *
 * <p>未开始：倒计时到开考；已开考且还在入场窗口里：提示还剩多久截止入场，
 * 让人知道现在能进、但得快点，而不是一过整点就冷冰冰一句「已超过允许入场时间」。
 */
const statusTip = (item: ExamCenterVO): string => {
  if (item.myStatus === 'not_start') {
    if (!hasStarted(item)) {
      const countdown = countdownText(item);
      return countdown ? `距开始还有 ${countdown}` : item.tip || '考试尚未开始';
    }
    return inEntryWindow(item) ? '考试已开始，可以入场了' : item.tip || '考试已开始';
  }
  // 已开考：还在入场窗口就提醒截止倒计时，别等过期了才知道
  if (inEntryWindow(item) && item.myStatus !== 'answering' && item.myStatus !== 'submitted') {
    return `入场截止还剩 ${formatCountdown(millisToDeadline(item))}（最晚 ${formatTime(item.latestEntryTime)} 前入场）`;
  }
  return item.tip || '';
};

/** 入场截止倒计时是催人的，用警告色；其余用提示色 */
const tipType = (item: ExamCenterVO): 'warning' | 'info' =>
  item.myStatus === 'late' || item.myStatus === 'blocked' || (hasStarted(item) && inEntryWindow(item)) ? 'warning' : 'info';

const loadList = async (silent = false) => {
  // 静默刷新不切骨架屏，否则倒计时最后几分钟页面会一直闪
  if (!silent) {
    loading.value = true;
  }
  const sentAt = Date.now();
  try {
    const res = await listMyExams();
    list.value = res.data ?? [];
    syncServerTime(list.value.find((item) => item.serverTime)?.serverTime, sentAt);
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

/** 已经因为到点触发过刷新的考试，别每秒都刷一遍 */
const refreshedIds = new Set<string>();
let lastReloadAt = 0;

onMounted(loadList);

// 时钟每走一秒检查一次：有考试刚到开考时间就刷一次列表，拿后端权威状态
watch(serverNow, () => {
  let hitStart = false;
  for (const item of list.value) {
    const key = String(item.examId);
    if (item.myStatus === 'not_start' && hasStarted(item) && !refreshedIds.has(key)) {
      refreshedIds.add(key);
      hitStart = true;
    }
  }
  // 临近开考的定期静默刷一次，让「可以考」尽快落到后端状态上，避免时钟漂移
  const needSync = list.value.some((item) => item.myStatus === 'not_start' && millisToStart(item) <= IMMINENT_MS);
  if (hitStart || (needSync && Date.now() - lastReloadAt > 10_000)) {
    lastReloadAt = Date.now();
    void loadList(true);
  }
});
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
