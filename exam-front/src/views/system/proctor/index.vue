<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">监考中心</span>
          <div class="flex items-center gap-2">
            <el-checkbox v-model="realtime" label="实时刷新" />
            <el-button plain icon="Refresh" @click="() => getList()">刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :model="queryParams" :inline="true" label-width="68px">
        <el-form-item label="考试" prop="examId">
          <el-select v-model="queryParams.examId" placeholder="我发布的考试" filterable clearable style="width: 260px" @change="handleQuery">
            <el-option v-for="item in examOptions" :key="String(item.id)" :label="item.examName" :value="String(item.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="考生" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="账号 / 姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px" @change="handleQuery">
            <el-option label="作答中" value="online" />
            <el-option label="已掉线" value="offline" />
            <el-option label="已交卷" value="submitted" />
            <el-option label="强制交卷" value="force_submit" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险" prop="riskLevel">
          <el-select v-model="queryParams.riskLevel" placeholder="全部" clearable style="width: 130px" @change="handleQuery">
            <el-option label="正常" value="normal" />
            <el-option label="可疑" value="suspect" />
            <el-option label="严重" value="serious" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="queryParams.onlyRisk" label="只看有异常" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 概览：先看全局，再决定要细看谁 -->
      <div v-if="overview" class="stat-row">
        <div class="stat-card">
          <div class="stat-value">{{ overview.totalCount }}</div>
          <div class="stat-label">参加人数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value is-online">{{ overview.onlineCount }}</div>
          <div class="stat-label">作答中</div>
        </div>
        <div class="stat-card">
          <div class="stat-value is-offline">{{ overview.offlineCount }}</div>
          <div class="stat-label">已掉线</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ overview.submittedCount }}</div>
          <div class="stat-label">已交卷</div>
        </div>
        <div class="stat-card">
          <div class="stat-value is-warn">{{ overview.suspectCount }}</div>
          <div class="stat-label">可疑</div>
        </div>
        <div class="stat-card">
          <div class="stat-value is-danger">{{ overview.seriousCount }}</div>
          <div class="stat-label">严重</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ overview.switchTotal }}</div>
          <div class="stat-label">切屏总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ overview.pasteTotal }}</div>
          <div class="stat-label">粘贴总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ overview.cameraTotal }}</div>
          <div class="stat-label">抓拍总数</div>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
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
            <span :class="{ 'is-danger': overLimit(row.exitFullscreenCount, row.maxExitFullscreen) }">{{ row.exitFullscreenCount }}</span>
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
            <el-button link type="primary" icon="View" @click="openDetail(row)">查看</el-button>
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

      <el-empty v-if="!loading && total === 0" description="还没有监考记录，考生进入答题页后这里会实时出现" />
    </el-card>

    <!-- 详情：计数 + 事件流水 + 摄像头抓拍 -->
    <el-drawer v-model="detailVisible" :title="`监考详情 · ${current.nickName || current.account || ''}`" size="60%" @close="closeDetail">
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
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="SystemProctor">
import { listExam } from '@/api/system/exam';
import type { ExamVO } from '@/api/system/exam/types';
import {
  getProctorOverview,
  listProctorEvents,
  listProctorSessions,
  listProctorSnapshots
} from '@/api/exam/proctor';
import type { ProctorEventVO, ProctorOverviewVO, ProctorSessionVO, ProctorSnapshotVO } from '@/api/exam/proctor/types';

const route = useRoute();

const loading = ref(false);
const total = ref(0);
const list = ref<ProctorSessionVO[]>([]);
const examOptions = ref<ExamVO[]>([]);
const overview = ref<ProctorOverviewVO | null>(null);
/** 实时刷新：监考就是要盯着正在考的人，默认打开 */
const realtime = ref(true);

const queryParams = ref({
  examId: (route.query.examId as string) || '',
  keyword: '',
  status: '',
  riskLevel: '',
  onlyRisk: false,
  pageNum: 1,
  pageSize: 10
});

/* --------------------------------- 详情 --------------------------------- */

const detailVisible = ref(false);
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

/** 考试下拉：只列自己能看的考试（后端按创建人过滤） */
const loadExamOptions = async () => {
  try {
    // listExam 的声明返回类型是数组，实际是 TableDataInfo，这里按实际结构取 rows
    const res: any = await listExam({ pageNum: 1, pageSize: 200 });
    examOptions.value = res?.rows ?? [];
  } catch {
    examOptions.value = [];
  }
};

const loadOverview = async () => {
  if (!queryParams.value.examId) {
    overview.value = null;
    return;
  }
  try {
    const res: any = await getProctorOverview(queryParams.value.examId);
    overview.value = res?.data ?? null;
  } catch {
    // 概览取不到不影响列表
  }
};

const getList = async (silent = false) => {
  if (!silent) {
    loading.value = true;
  }
  try {
    const params = {
      examId: queryParams.value.examId || undefined,
      keyword: queryParams.value.keyword || undefined,
      status: queryParams.value.status || undefined,
      riskLevel: queryParams.value.riskLevel || undefined,
      onlyRisk: queryParams.value.onlyRisk || undefined,
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize
    };
    const res: any = await listProctorSessions(params);
    list.value = res?.rows ?? [];
    total.value = res?.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  void getList();
  void loadOverview();
};

const resetQuery = () => {
  queryParams.value.keyword = '';
  queryParams.value.status = '';
  queryParams.value.riskLevel = '';
  queryParams.value.onlyRisk = false;
  handleQuery();
};

const openDetail = (row: ProctorSessionVO) => {
  current.value = row;
  eventQuery.value.pageNum = 1;
  detailVisible.value = true;
  void getEventList();
  void getSnapshotList();
};

const closeDetail = () => {
  detailVisible.value = false;
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
  await loadExamOptions();
  await getList();
  await loadOverview();
  // 10 秒一次静默刷新：不切 loading，页面不会闪
  timer = setInterval(() => {
    if (!realtime.value) {
      return;
    }
    void getList(true);
    void loadOverview();
    if (detailVisible.value) {
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
