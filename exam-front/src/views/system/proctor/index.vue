<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">监考中心</span>
          <div class="flex items-center gap-2">
            <el-checkbox v-model="realtime" label="实时刷新" />
            <el-button plain icon="Refresh" @click="() => getExamGroups()">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 第一层：先按考试看，哪场有问题点哪场 -->
      <el-form :model="groupQuery" :inline="true" label-width="68px">
        <el-form-item label="考试" prop="keyword">
          <el-input
            v-model="groupQuery.keyword"
            placeholder="考试名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleGroupQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="groupQuery.onlyRisk" label="只看有异常的考试" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleGroupQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetGroupQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 全部考试的合计：一眼看到总体规模 -->
      <div class="stat-row">
        <div class="stat-card">
          <div class="stat-value">{{ sumBy((g) => g.totalCount) }}</div>
          <div class="stat-label">参加人次</div>
        </div>
        <div class="stat-card">
          <div class="stat-value is-online">{{ sumBy((g) => g.onlineCount) }}</div>
          <div class="stat-label">作答中</div>
        </div>
        <div class="stat-card">
          <div class="stat-value is-offline">{{ sumBy((g) => g.offlineCount) }}</div>
          <div class="stat-label">已掉线</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ sumBy((g) => g.submittedCount) }}</div>
          <div class="stat-label">已交卷</div>
        </div>
        <div class="stat-card">
          <div class="stat-value is-warn">{{ sumBy((g) => g.suspectCount) }}</div>
          <div class="stat-label">可疑</div>
        </div>
        <div class="stat-card">
          <div class="stat-value is-danger">{{ sumBy((g) => g.seriousCount) }}</div>
          <div class="stat-label">严重</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ examGroups.length }}</div>
          <div class="stat-label">考试场次</div>
        </div>
      </div>

      <el-table v-loading="loading" :data="examGroups" border stripe>
        <el-table-column label="考试" min-width="180" show-overflow-tooltip prop="examName" />
        <el-table-column label="风险" align="center" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="riskType(row.riskLevel)">{{ riskText(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="参加" align="center" width="80" prop="totalCount" />
        <el-table-column label="作答中" align="center" width="90">
          <template #default="{ row }">
            <span :class="{ 'is-online-text': row.onlineCount > 0 }">{{ row.onlineCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="掉线" align="center" width="80" prop="offlineCount" />
        <el-table-column label="已交卷" align="center" width="90" prop="submittedCount" />
        <el-table-column label="可疑" align="center" width="80">
          <template #default="{ row }">
            <span :class="{ 'is-warn-text': row.suspectCount > 0 }">{{ row.suspectCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="严重" align="center" width="80">
          <template #default="{ row }">
            <span :class="{ 'is-danger': row.seriousCount > 0 }">{{ row.seriousCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="强制交卷" align="center" width="100">
          <template #default="{ row }">
            <span :class="{ 'is-danger': row.forceSubmitCount > 0 }">{{ row.forceSubmitCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="切屏总数" align="center" width="100" prop="switchTotal" />
        <el-table-column label="粘贴总数" align="center" width="100" prop="pasteTotal" />
        <el-table-column label="抓拍总数" align="center" width="100" prop="cameraTotal" />
        <el-table-column label="最近活跃" align="center" width="160" prop="lastActiveTime" />
        <el-table-column label="操作" align="center" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="User" @click="openStudents(row)">查看考生</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && examGroups.length === 0" description="还没有监考记录，考生进入答题页后这里会实时出现" />
    </el-card>

    <!-- 第二层：这场考试的考生；第三层：某个考生的详情。同一抽屉内切换，带返回 -->
    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="72%" @close="closeDrawer">
      <!-- ---------- 考生列表 ---------- -->
      <template v-if="drawerView === 'students'">
        <el-form :model="studentQuery" :inline="true" label-width="52px" class="mb-[8px]">
          <el-form-item label="考生" prop="keyword">
            <el-input
              v-model="studentQuery.keyword"
              placeholder="账号 / 姓名"
              clearable
              style="width: 160px"
              @keyup.enter="handleStudentQuery"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="studentQuery.status" placeholder="全部" clearable style="width: 120px" @change="handleStudentQuery">
              <el-option label="作答中" value="online" />
              <el-option label="已掉线" value="offline" />
              <el-option label="已交卷" value="submitted" />
              <el-option label="强制交卷" value="force_submit" />
            </el-select>
          </el-form-item>
          <el-form-item label="风险" prop="riskLevel">
            <el-select v-model="studentQuery.riskLevel" placeholder="全部" clearable style="width: 110px" @change="handleStudentQuery">
              <el-option label="正常" value="normal" />
              <el-option label="可疑" value="suspect" />
              <el-option label="严重" value="serious" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="studentQuery.onlyRisk" label="只看有异常" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleStudentQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetStudentQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="studentLoading" :data="studentList" border stripe>
          <el-table-column label="考生" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.nickName || row.account }}</span>
              <span v-if="row.nickName && row.account" class="sub-text">{{ row.account }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" width="100">
            <template #default="{ row }">
              <el-tag size="small" effect="plain" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="切屏" align="center" width="90">
            <template #default="{ row }">
              <span :class="{ 'is-danger': overLimit(row.switchCount, row.maxSwitch) }">{{ row.switchCount }}</span>
              <span v-if="row.maxSwitch" class="sub-text">/ {{ row.maxSwitch }}</span>
            </template>
          </el-table-column>
          <el-table-column label="复制" align="center" width="80" prop="copyCount" />
          <el-table-column label="粘贴" align="center" width="90">
            <template #default="{ row }">
              <span :class="{ 'is-danger': overLimit(row.pasteCount, row.maxPaste) }">{{ row.pasteCount }}</span>
              <span v-if="row.maxPaste" class="sub-text">/ {{ row.maxPaste }}</span>
            </template>
          </el-table-column>
          <el-table-column label="退出全屏" align="center" width="100">
            <template #default="{ row }">
              <span :class="{ 'is-danger': overLimit(row.exitFullscreenCount, row.maxExitFullscreen) }">
                {{ row.exitFullscreenCount }}
              </span>
              <span v-if="row.maxExitFullscreen" class="sub-text">/ {{ row.maxExitFullscreen }}</span>
            </template>
          </el-table-column>
          <el-table-column label="抓拍" align="center" width="80" prop="cameraCount" />
          <el-table-column label="开发者工具" align="center" width="110" prop="devtoolCount" />
          <el-table-column label="多开" align="center" width="80" prop="multitabCount" />
          <el-table-column label="风险" align="center" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="riskType(row.riskLevel)">{{ riskText(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最近活跃" align="center" width="160" prop="lastActiveTime" />
          <el-table-column label="操作" align="center" width="90" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" icon="View" @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="studentTotal > 0"
          v-model:page="studentQuery.pageNum"
          v-model:limit="studentQuery.pageSize"
          :total="studentTotal"
          @pagination="getStudentList"
        />

        <el-empty v-if="!studentLoading && studentTotal === 0" description="这场考试还没有考生进场" />
      </template>

      <!-- ---------- 某个考生的详情 ---------- -->
      <template v-else>
        <el-button link type="primary" icon="ArrowLeft" class="mb-[8px]" @click="backToStudents">返回考生列表</el-button>

        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="考试">{{ current.examName }}</el-descriptions-item>
          <el-descriptions-item label="账号">{{ current.account }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" effect="plain" :type="statusType(current.status)">{{ statusText(current.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风险">
            <el-tag size="small" :type="riskType(current.riskLevel)">{{ riskText(current.riskLevel) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="切屏">{{ current.switchCount }} 次（上限 {{ current.maxSwitch || '不限' }}）</el-descriptions-item>
          <el-descriptions-item label="粘贴">{{ current.pasteCount }} 次（上限 {{ current.maxPaste || '不限' }}）</el-descriptions-item>
          <el-descriptions-item label="退出全屏">{{ current.exitFullscreenCount }} 次</el-descriptions-item>
          <el-descriptions-item label="抓拍">{{ current.cameraCount }} 张</el-descriptions-item>
          <el-descriptions-item label="进入时间">{{ current.startTime }}</el-descriptions-item>
          <el-descriptions-item label="在线时长">{{ durationText(current.durationSeconds) }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{ current.ip || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备">{{ current.device || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="detailTab" class="detail-tabs">
          <el-tab-pane label="事件流水" name="event">
            <el-table v-loading="eventLoading" :data="eventList" border stripe size="small">
              <el-table-column label="时间" width="160" prop="eventTime" />
              <el-table-column label="事件" width="140">
                <template #default="{ row }">
                  <el-tag size="small" effect="plain" :type="levelType(row.level)">{{ row.eventName || row.eventType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="说明" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">{{ row.content || '-' }}</template>
              </el-table-column>
            </el-table>
            <pagination
              v-show="eventTotal > 0"
              v-model:page="eventQuery.pageNum"
              v-model:limit="eventQuery.pageSize"
              :total="eventTotal"
              @pagination="getEventList"
            />
            <el-empty v-if="!eventLoading && eventTotal === 0" description="暂无事件记录" />
          </el-tab-pane>

          <el-tab-pane label="摄像头抓拍" name="snapshot">
            <div v-if="snapshotList.length" class="snapshot-wall">
              <div v-for="item in snapshotList" :key="item.id" class="snapshot-item">
                <el-image :src="item.url" :preview-src-list="previewList" fit="cover" class="snapshot-img" />
                <div class="snapshot-time">{{ item.captureTime }}</div>
              </div>
            </div>
            <el-empty v-else description="该考生没有抓拍记录（考试未开启摄像头抓拍，或摄像头不可用）" />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="SystemProctor">
import {
  getProctorOverview,
  listProctorEvents,
  listProctorExamGroups,
  listProctorSessions,
  listProctorSnapshots
} from '@/api/exam/proctor';
import type { ProctorEventVO, ProctorExamGroupVO, ProctorSessionVO, ProctorSnapshotVO } from '@/api/exam/proctor/types';

const route = useRoute();

/* --------------------------- 第一层：考试分组 --------------------------- */

const loading = ref(false);
const examGroups = ref<ProctorExamGroupVO[]>([]);
/** 实时刷新：监考就是要盯着正在考的人，默认打开 */
const realtime = ref(true);

const groupQuery = ref({ keyword: '', onlyRisk: false });

/** 顶部合计：直接对分组求和，不必再单独请求一个总览接口 */
const sumBy = (pick: (g: ProctorExamGroupVO) => number): number =>
  examGroups.value.reduce((sum, g) => sum + (pick(g) ?? 0), 0);

/* --------------------------- 第二层：考生列表 --------------------------- */

const drawerVisible = ref(false);
/** students 考生列表 / detail 考生详情，同一抽屉内切换 */
const drawerView = ref<'students' | 'detail'>('students');
const currentExam = ref<{ examId: string; examName: string }>({ examId: '', examName: '' });
const studentList = ref<ProctorSessionVO[]>([]);
const studentTotal = ref(0);
const studentLoading = ref(false);
const studentQuery = ref({
  keyword: '',
  status: '',
  riskLevel: '',
  onlyRisk: false,
  pageNum: 1,
  pageSize: 10
});

const drawerTitle = computed(() =>
  drawerView.value === 'students'
    ? `监考 · ${currentExam.value.examName || '考试'}`
    : `监考详情 · ${current.value.nickName || current.value.account || ''}`
);

/* --------------------------- 第三层：考生详情 --------------------------- */

const detailTab = ref('event');
const current = ref<ProctorSessionVO>({} as ProctorSessionVO);
const eventList = ref<ProctorEventVO[]>([]);
const eventTotal = ref(0);
const eventLoading = ref(false);
const snapshotList = ref<ProctorSnapshotVO[]>([]);
const eventQuery = ref({ pageNum: 1, pageSize: 10 });

const previewList = computed(() => snapshotList.value.map((item) => item.url));

/* --------------------------------- 文案映射 --------------------------------- */

/** el-tag 的 type 只允许这几个字面量，返回 string 会让模板类型检查报错 */
type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger';

const statusText = (status?: string): string =>
  ({ online: '作答中', offline: '已掉线', submitted: '已交卷', force_submit: '强制交卷' })[status ?? ''] ?? '-';

const tagOf = (map: Record<string, TagType>, key?: string): TagType => map[key ?? ''] ?? 'info';

const statusType = (status?: string): TagType =>
  tagOf({ online: 'success', offline: 'info', submitted: 'primary', force_submit: 'danger' }, status);

const riskText = (level?: string): string => ({ normal: '正常', suspect: '可疑', serious: '严重' })[level ?? ''] ?? '-';

const riskType = (level?: string): TagType => tagOf({ normal: 'success', suspect: 'warning', serious: 'danger' }, level);

const levelType = (level?: string): TagType => tagOf({ info: 'info', warn: 'warning', danger: 'danger' }, level);

/** 超过配置的次数就标红，一眼看出是谁被卡了上限 */
const overLimit = (count?: number, limit?: number): boolean => !!limit && limit > 0 && (count ?? 0) > limit;

const durationText = (seconds?: number): string => {
  if (!seconds) return '-';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return m > 0 ? `${m} 分 ${s} 秒` : `${s} 秒`;
};

/* --------------------------------- 数据加载 --------------------------------- */

const getExamGroups = async (silent = false) => {
  if (!silent) {
    loading.value = true;
  }
  try {
    const res: any = await listProctorExamGroups({
      keyword: groupQuery.value.keyword || undefined,
      onlyRisk: groupQuery.value.onlyRisk || undefined
    });
    examGroups.value = res?.data ?? [];
  } finally {
    loading.value = false;
  }
};

const handleGroupQuery = () => {
  void getExamGroups();
};

const resetGroupQuery = () => {
  groupQuery.value.keyword = '';
  groupQuery.value.onlyRisk = false;
  void getExamGroups();
};

const openStudents = (row: ProctorExamGroupVO) => {
  currentExam.value = { examId: String(row.examId ?? ''), examName: row.examName ?? '' };
  studentQuery.value.pageNum = 1;
  drawerView.value = 'students';
  drawerVisible.value = true;
  void getStudentList();
};

const getStudentList = async (silent = false) => {
  if (!currentExam.value.examId) {
    return;
  }
  if (!silent) {
    studentLoading.value = true;
  }
  try {
    const res: any = await listProctorSessions({
      examId: currentExam.value.examId,
      keyword: studentQuery.value.keyword || undefined,
      status: studentQuery.value.status || undefined,
      riskLevel: studentQuery.value.riskLevel || undefined,
      onlyRisk: studentQuery.value.onlyRisk || undefined,
      pageNum: studentQuery.value.pageNum,
      pageSize: studentQuery.value.pageSize
    });
    studentList.value = res?.rows ?? [];
    studentTotal.value = res?.total ?? 0;
  } finally {
    studentLoading.value = false;
  }
};

const handleStudentQuery = () => {
  studentQuery.value.pageNum = 1;
  void getStudentList();
};

const resetStudentQuery = () => {
  studentQuery.value.keyword = '';
  studentQuery.value.status = '';
  studentQuery.value.riskLevel = '';
  studentQuery.value.onlyRisk = false;
  handleStudentQuery();
};

const openDetail = (row: ProctorSessionVO) => {
  current.value = row;
  eventQuery.value.pageNum = 1;
  detailTab.value = 'event';
  drawerView.value = 'detail';
  void getEventList();
  void getSnapshotList();
};

const backToStudents = () => {
  drawerView.value = 'students';
  void getStudentList(true);
};

const closeDrawer = () => {
  drawerVisible.value = false;
  drawerView.value = 'students';
  studentList.value = [];
};

const getEventList = async () => {
  if (!current.value.id) {
    return;
  }
  eventLoading.value = true;
  try {
    const res: any = await listProctorEvents({ sessionId: current.value.id, ...eventQuery.value });
    eventList.value = res?.rows ?? [];
    eventTotal.value = res?.total ?? 0;
  } finally {
    eventLoading.value = false;
  }
};

const getSnapshotList = async () => {
  if (!current.value.id) {
    return;
  }
  try {
    const res: any = await listProctorSnapshots(current.value.id);
    snapshotList.value = res?.data ?? [];
  } catch {
    snapshotList.value = [];
  }
};

/* --------------------------------- 轮询 --------------------------------- */

let timer: ReturnType<typeof setInterval> | undefined;

onMounted(async () => {
  await getExamGroups();
  // 从考试管理点「监考记录」进来：直接展开那场考试的考生
  const examId = (route.query.examId as string) || '';
  if (examId) {
    const found = examGroups.value.find((g) => String(g.examId) === examId);
    if (found) {
      openStudents(found);
    } else {
      // 还没人进场时分组里没有这场，用概览接口取个考试名再展开
      try {
        const res: any = await getProctorOverview(examId);
        openStudents({ examId, examName: res?.data?.examName ?? '' } as ProctorExamGroupVO);
      } catch {
        // 取不到就算了，留在考试列表页
      }
    }
  }
  // 10 秒一次静默刷新：不切 loading，页面不会闪
  timer = setInterval(() => {
    if (!realtime.value) {
      return;
    }
    void getExamGroups(true);
    if (!drawerVisible.value) {
      return;
    }
    if (drawerView.value === 'students') {
      void getStudentList(true);
    } else {
      void getEventList();
      if (detailTab.value === 'snapshot') {
        void getSnapshotList();
      }
    }
  }, 10_000);
});

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
    timer = undefined;
  }
});
</script>

<style scoped lang="scss">
.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(96px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.stat-card {
  padding: 10px 8px;
  text-align: center;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;

  &.is-online {
    color: #67c23a;
  }

  &.is-offline {
    color: #909399;
  }

  &.is-warn {
    color: #e6a23c;
  }

  &.is-danger {
    color: #f56c6c;
  }
}

.stat-label {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}

.sub-text {
  margin-left: 4px;
  font-size: 12px;
  color: #909399;
}

.is-danger {
  font-weight: 600;
  color: #f56c6c;
}

.is-warn-text {
  font-weight: 600;
  color: #e6a23c;
}

.is-online-text {
  font-weight: 600;
  color: #67c23a;
}

.detail-desc {
  margin-bottom: 8px;
}

.detail-tabs {
  margin-top: 8px;
}

.snapshot-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.snapshot-item {
  overflow: hidden;
  background: #f5f7fa;
  border-radius: 8px;
}

.snapshot-img {
  display: block;
  width: 100%;
  height: 110px;
}

.snapshot-time {
  padding: 4px 6px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
