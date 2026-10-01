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
          <!-- 未开始时这里显示倒计时，到点自动换成「正在进入」，别让人对着一个死按钮发呆 -->
          <el-alert
            v-if="statusTip(item)"
            class="mt-[10px]"
            :type="item.myStatus === 'late' || item.myStatus === 'blocked' ? 'warning' : 'info'"
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

const emptyTip = computed(() => {
  if (activeOwner.value === 'mine') return '你还没有创建考试，去「考试管理」新建一场即可直接在这里参加';
  if (activeOwner.value === 'joined') return '你还没有通过链接加入任何考试';
  if (activeType.value === 'all') return '你还没有加入任何考试，可通过邀请链接加入';
  return '暂无该类型的考试';
});

/** 把后端给的时间串转成时间戳，解析不出来返回 NaN */
const toTs = (value?: string): number => {
  if (!value) return Number.NaN;
  const text = String(value).trim();
  // 后端给的是本地时间串（yyyy-MM-dd HH:mm:ss），按本地时区解析，别当成 UTC
  const ts = new Date(text.includes('T') ? text : text.replace(' ', 'T')).getTime();
  return Number.isNaN(ts) ? Number.NaN : ts;
};

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const ts = toTs(value);
  if (Number.isNaN(ts)) return value;
  const date = new Date(ts);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const timeRange = (item: ExamCenterVO) => `${formatTime(item.startTime)} 至 ${formatTime(item.endTime)}`;

/**
 * 本地时钟和服务端时钟的差值（服务端 - 本地）
 *
 * <p>考生电脑时间不准时，按本地时间算的倒计时会早一截或晚一截，
 * 按钮要么早亮（点进去被后端拒绝）要么晚亮（干等），这里用接口返回的 serverTime 校准。
 */
const serverOffset = ref(0);
/** 每秒走一次，驱动倒计时 */
const nowTs = ref(Date.now());
/** 校准后的「现在」 */
const serverNow = computed(() => nowTs.value + serverOffset.value);

/** 与后端一致的提前入场余量：到点前后这 10 秒都算已经开考 */
const EARLY_GRACE_MS = 10_000;
/** 倒计时最后这段时间里提高刷新频率，保证整点前后状态是对的 */
const IMMINENT_MS = 60_000;

/** 本地已判定开考、但列表还没刷回来的考试，先把开始按钮放出来，避免干等一次接口往返 */
const startedIds = ref<Set<string>>(new Set());

/** 没配开始时间的练习考试随时可考；配了的按服务端时间判，留 10 秒提前量 */
const hasStarted = (item: ExamCenterVO): boolean => {
  const startTs = toTs(item.startTime);
  if (Number.isNaN(startTs)) return true;
  return serverNow.value >= startTs - EARLY_GRACE_MS;
};

/** 距离开考还有多久（毫秒），已开考返回 0 */
const millisToStart = (item: ExamCenterVO): number => {
  const startTs = toTs(item.startTime);
  if (Number.isNaN(startTs)) return 0;
  return Math.max(0, startTs - EARLY_GRACE_MS - serverNow.value);
};

const canStartOf = (item: ExamCenterVO): boolean =>
  item.canStart === true || (item.myStatus === 'not_start' && startedIds.value.has(String(item.examId)));

/** 倒计时文案：超过一天带天数，否则 hh:mm:ss */
const countdownText = (item: ExamCenterVO): string => {
  const left = Math.floor(millisToStart(item) / 1000);
  if (left <= 0) return '';
  const days = Math.floor(left / 86400);
  const pad = (n: number) => String(n).padStart(2, '0');
  const clock = `${pad(Math.floor((left % 86400) / 3600))}:${pad(Math.floor((left % 3600) / 60))}:${pad(left % 60)}`;
  return days > 0 ? `${days} 天 ${clock}` : clock;
};

/**
 * 卡片上的状态提示
 *
 * <p>未开始时给倒计时，让人知道还要等多久；到点后即使接口还没刷回来说「可以考」，
 * 也先提示正在进入，别让人对着一个一动不动的「未开始」猜是不是卡了。
 */
const statusTip = (item: ExamCenterVO): string => {
  const started = hasStarted(item);
  if (item.myStatus === 'not_start') {
    if (started) return '考试已开始，正在进入…';
    const countdown = countdownText(item);
    return countdown ? `距开始还有 ${countdown}` : item.tip || '考试尚未开始';
  }
  if (item.myStatus === 'pending' && started && item.latestEntryTime) {
    return `最晚 ${formatTime(item.latestEntryTime)} 前入场`;
  }
  return item.tip || '';
};

const loadList = async (silent = false) => {
  // 静默刷新不切骨架屏，否则倒计时最后几分钟页面会一直闪
  if (!silent) {
    loading.value = true;
  }
  try {
    const res = await listMyExams();
    list.value = res.data ?? [];
    const serverTs = toTs(list.value.find((item) => item.serverTime)?.serverTime);
    if (!Number.isNaN(serverTs)) {
      serverOffset.value = serverTs - Date.now();
      nowTs.value = Date.now();
    }
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

let timer: ReturnType<typeof setInterval> | undefined;
let lastReloadAt = 0;

onMounted(() => {
  loadList();
  timer = setInterval(() => {
    nowTs.value = Date.now();
    let hitStart = false;
    for (const item of list.value) {
      if (item.myStatus === 'not_start' && hasStarted(item) && !startedIds.value.has(String(item.examId))) {
        startedIds.value.add(String(item.examId));
        hitStart = true;
      }
    }
    // 有考试刚开考：立刻刷一次拿权威状态；临近开考的每 10 秒静默刷一次，避免时钟漂移
    const imminent = list.value.some((item) => item.myStatus === 'not_start' && millisToStart(item) <= IMMINENT_MS);
    if (hitStart || (imminent && Date.now() - lastReloadAt > 10_000)) {
      lastReloadAt = Date.now();
      void loadList(true);
    }
  }, 1000);
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
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
