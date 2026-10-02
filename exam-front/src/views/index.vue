<template>
  <div ref="rootRef" class="home" :class="{ 'is-screen': screenMode }">
    <!-- 顶部欢迎条：一句话说清身份、日期和今天要关心的 -->
    <div class="hero">
      <div class="hero-main">
        <div class="hero-title">
          {{ greeting }}，{{ userStore.nickname }}
          <el-tag size="small" effect="plain" round class="ml-[8px]">{{ roleLabel }}</el-tag>
        </div>
        <div class="hero-sub">
          {{ todayText }}
          <span class="hero-dot">·</span>
          {{ summaryLine }}
        </div>
        <div class="hero-desc">在线考试系统：题库建设 → 组卷 → 考试发布 → 在线监考 → 阅卷 → 成绩与证书，一条链路做完。</div>
      </div>
      <div class="hero-actions">
        <el-button v-for="item in quickActions" :key="item.label" plain :icon="item.icon" @click="go(item.path)">
          {{ item.label }}
        </el-button>
        <el-dropdown class="ml-[8px]" @command="go">
          <el-button type="primary" :icon="screenMode ? 'Aim' : 'FullScreen'">
            {{ screenMode ? '退出大屏' : '大屏模式' }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="__screen__">{{ screenMode ? '退出大屏' : '进入大屏' }}</el-dropdown-item>
              <el-dropdown-item command="__refresh__" divided>刷新数据</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated class="mt-[8px]" />

    <template v-else>
      <!-- KPI：管理视角看全局，考生视角看自己 -->
      <div class="kpi-grid">
        <div v-for="card in kpiCards" :key="card.label" class="kpi-card" :style="{ '--kpi-color': card.color }">
          <div class="kpi-icon">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="kpi-body">
            <div class="kpi-label">{{ card.label }}</div>
            <div class="kpi-value">
              {{ card.value }}<span v-if="card.unit" class="kpi-unit">{{ card.unit }}</span>
            </div>
            <div class="kpi-tip">{{ card.tip }}</div>
          </div>
        </div>
      </div>

      <!-- 图表区 -->
      <div class="panel-grid">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">近 7 天交卷趋势</span>
            <span class="panel-note">柱=交卷数，线=及格数</span>
          </div>
          <div ref="trendRef" class="chart chart-md"></div>
        </div>
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">成绩概览</span>
            <span class="panel-note">已交卷</span>
          </div>
          <div ref="passRef" class="chart chart-sm"></div>
        </div>
      </div>

      <el-row :gutter="16">
        <el-col :xs="24" :md="14">
          <div class="panel">
            <div class="panel-head">
              <span class="panel-title">今日考试安排</span>
              <el-button link type="primary" @click="go('/system/exam')">查看全部</el-button>
            </div>
            <el-empty v-if="todayExams.length === 0" description="今天没有安排考试" :image-size="64" />
            <div v-else class="exam-list">
              <div v-for="item in todayExams" :key="item.examId" class="exam-item" @click="go('/system/exam')">
                <div class="exam-time">{{ timeText(item.startTime) }} - {{ timeText(item.endTime) }}</div>
                <div class="exam-info">
                  <span class="exam-name">{{ item.examName }}</span>
                  <el-tag size="small" :type="item.examType === '2' ? 'info' : 'primary'" effect="plain">
                    {{ item.examType === '2' ? '练习' : '正式' }}
                  </el-tag>
                </div>
                <el-tag size="small" :type="examStatusType(item)" effect="dark">{{ examStatusText(item) }}</el-tag>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :md="10">
          <div class="panel">
            <div class="panel-head">
              <span class="panel-title">考试热度榜</span>
              <span class="panel-note">按交卷量</span>
            </div>
            <el-empty v-if="examRank.length === 0" description="还没有交卷记录" :image-size="64" />
            <div v-else class="rank-list">
              <div v-for="(item, index) in examRank" :key="item.examId" class="rank-item">
                <div class="rank-no">{{ index + 1 }}</div>
                <div class="rank-main">
                  <div class="rank-title" :title="item.examName">{{ item.examName }}</div>
                  <div class="rank-bar">
                    <span :style="{ width: rankPercent(item) + '%' }"></span>
                  </div>
                </div>
                <div class="rank-right">
                  <div class="rank-count">{{ num(item.submitCount) }} 份</div>
                  <div class="rank-sub">及格率 {{ num(item.passRate) }}%</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 项目说明：能力矩阵 + 系统定位 -->
      <div class="panel intro">
        <div class="panel-head">
          <span class="panel-title">系统能力</span>
          <span class="panel-note">覆盖一场考试从出题到发证的全过程</span>
        </div>
        <div class="feat-grid">
          <div v-for="feat in features" :key="feat.title" class="feat-card" @click="go(feat.path)">
            <div class="feat-icon" :style="{ '--feat-color': feat.color }">
              <el-icon><component :is="feat.icon" /></el-icon>
            </div>
            <div class="feat-title">{{ feat.title }}</div>
            <div class="feat-desc">{{ feat.desc }}</div>
          </div>
        </div>
        <div class="intro-foot">
          <div class="intro-text">
            题库按科目分层维护，试卷支持固定组卷与随机抽题并统一配置总分、及格分；考试阶段控制时间窗、迟到入场与重考规则，
            防作弊服务按每场考试的配置记录切屏、粘贴、摄像头抓拍等行为；客观题自动判分，主观题进阅卷任务；
            及格后由证书服务自动签发证书，错题归入练习模块继续练。
          </div>
          <div class="intro-meta">
            <div><span>当前版本</span><b>v2.6.2 · exam 1.0</b></div>
            <div><span>技术栈</span><b>Spring Cloud + Dubbo + Vue3</b></div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup name="Index" lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import screenfull from 'screenfull';
import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { getHomeOverview } from '@/api/exam/stat';
import type { HomeExamVO, HomeStatVO } from '@/api/exam/stat/types';
import { useUserStore } from '@/store/modules/user';

echarts.use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

const router = useRouter();
const userStore = useUserStore();

const rootRef = ref<HTMLElement>();
const trendRef = ref<HTMLElement>();
const passRef = ref<HTMLElement>();
let trendChart: echarts.ECharts | null = null;
let passChart: echarts.ECharts | null = null;

const loading = ref(false);
const stat = ref<HomeStatVO | null>(null);
const screenMode = ref(false);

/** 能看考试列表 = 管理 / 老师视角，其余走「我的」视图 */
const isManager = computed(() => {
  return userStore.roles.includes('admin') || userStore.permissions.includes('system:exam:list');
});

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '夜深了';
  if (hour < 11) return '早上好';
  if (hour < 13) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const weekText = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][new Date().getDay()];
const todayText = computed(() => {
  const now = new Date();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()} 年 ${m} 月 ${d} 日 ${weekText}`;
});

const roleLabel = computed(() => (isManager.value ? '管理视角' : '考生视角'));

/** Long / BigDecimal 到前端可能是字符串，统一兜一层 */
const num = (value: number | string | undefined | null): number => {
  if (value === null || value === undefined || value === '') return 0;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

const todayExams = computed<HomeExamVO[]>(() => stat.value?.todayExams ?? []);
const examRank = computed(() => stat.value?.examRank ?? []);
const maxRankCount = computed(() => Math.max(1, ...examRank.value.map((item) => num(item.submitCount))));
const rankPercent = (item: { submitCount: number | string }) => Math.round((num(item.submitCount) / maxRankCount.value) * 100);

/* ------------------------------- 考生视角 ------------------------------- */
// 考生没有全局统计权限，用已有的「我的考试 / 错题 / 证书」接口折算
const myPending = ref(0);
const myFinished = ref(0);
const wrong = ref<{ totalCount: number | string; notMasterCount: number | string; todayCount: number | string }>({
  totalCount: 0,
  notMasterCount: 0,
  todayCount: 0
});
const myCertCount = ref(0);

const loadMine = async () => {
  const [{ listMyExams }, { getWrongOverview }, { listMyCertificates }] = await Promise.all([
    import('@/api/exam/answer'),
    import('@/api/exam/wrong'),
    import('@/api/exam/cert')
  ]);
  const settled = await Promise.allSettled([listMyExams(), getWrongOverview(), listMyCertificates()]);
  const [examRes, wrongRes, certRes] = settled;
  if (examRes.status === 'fulfilled') {
    const list = examRes.value.data ?? [];
    myPending.value = list.filter((item) => item.myStatus === 'pending' || item.myStatus === 'answering').length;
    myFinished.value = list.filter((item) => item.myStatus === 'submitted').length;
  }
  if (wrongRes.status === 'fulfilled' && wrongRes.value.data) {
    wrong.value = wrongRes.value.data;
  }
  if (certRes.status === 'fulfilled') {
    myCertCount.value = (certRes.value.data ?? []).length;
  }
};

const summaryLine = computed(() => {
  if (isManager.value) {
    const vo = stat.value;
    if (!vo) return '正在加载统计数据';
    return `进行中 ${num(vo.examOngoing)} 场 · 今日 ${num(vo.examToday)} 场考试 · 今日交卷 ${num(vo.todaySubmit)} 份`;
  }
  const certText = myCertCount.value > 0 ? ` · 证书 ${myCertCount.value} 张` : '';
  return `可参加 ${myPending.value} 场 · 已完成 ${myFinished.value} 场 · 错题 ${num(wrong.value.totalCount)} 道${certText}`;
});

/* ------------------------------- KPI 卡片 ------------------------------- */
const kpiCards = computed(() => {
  if (isManager.value) {
    const vo = stat.value;
    return [
      { label: '考试总数', value: num(vo?.examTotal), unit: '场', tip: `已结束 ${num(vo?.examFinished)} 场`, icon: 'Calendar', color: '#4096ff' },
      { label: '进行中', value: num(vo?.examOngoing), unit: '场', tip: '当前时间落在考试时间段内', icon: 'Loading', color: '#13c2c2' },
      { label: '今日考试', value: num(vo?.examToday), unit: '场', tip: '今天有安排的场次', icon: 'Clock', color: '#9254de' },
      { label: '累计答卷', value: num(vo?.recordTotal), unit: '份', tip: `参考 ${num(vo?.examineeCount)} 人`, icon: 'Tickets', color: '#2f54eb' },
      { label: '今日交卷', value: num(vo?.todaySubmit), unit: '份', tip: `正在答题 ${num(vo?.answering)} 份`, icon: 'UploadFilled', color: '#fa8c16' },
      {
        label: '及格率 / 平均分',
        value: `${num(vo?.passRate)} / ${num(vo?.avgScore)}`,
        unit: '',
        tip: '只统计已交卷的答卷',
        icon: 'DataLine',
        color: '#52c41a'
      }
    ];
  }
  return [
    { label: '可参加考试', value: myPending.value, unit: '场', tip: '未开始或正在进行', icon: 'Calendar', color: '#4096ff' },
    { label: '已完成', value: myFinished.value, unit: '场', tip: '已交卷的考试', icon: 'CircleCheck', color: '#52c41a' },
    { label: '错题', value: num(wrong.value.totalCount), unit: '道', tip: `未掌握 ${num(wrong.value.notMasterCount)} 道`, icon: 'EditPen', color: '#fa541c' },
    { label: '今日新增错题', value: num(wrong.value.todayCount), unit: '道', tip: '今天记错的题目', icon: 'Bell', color: '#faad14' },
    { label: '我的证书', value: myCertCount.value, unit: '张', tip: '及格后自动颁发', icon: 'Medal', color: '#9254de' },
    { label: '累计参加', value: myPending.value + myFinished.value, unit: '场', tip: '我参加过的考试', icon: 'Tickets', color: '#2f54eb' }
  ];
});

/* ------------------------------- 快捷入口 ------------------------------- */
const features = [
  { title: '题库管理', desc: '按科目分层维护试题，支持单选 / 多选 / 判断 / 填空 / 简答', icon: 'Collection', color: '#4096ff', path: '/system/question' },
  { title: '试卷组卷', desc: '固定组卷与随机抽题，统一配置总分与及格分', icon: 'DocumentCopy', color: '#2f54eb', path: '/system/paper' },
  { title: '考试管理', desc: '时间窗、迟到入场、重考次数、防作弊规则', icon: 'Calendar', color: '#9254de', path: '/system/exam' },
  { title: '在线监考', desc: '切屏 / 粘贴 / 摄像头抓拍，实时风险提醒', icon: 'VideoCamera', color: '#fa541c', path: '/system/proctor' },
  { title: '阅卷评分', desc: '客观题自动判分，主观题按阅卷任务分发', icon: 'EditPen', color: '#fa8c16', path: '/system/mark' },
  { title: '成绩记录', desc: '逐份答卷明细、成绩分布与错题回溯', icon: 'DataAnalysis', color: '#13c2c2', path: '/exam/records' },
  { title: '证书颁发', desc: '及格后自动签发，模板与快照可配置', icon: 'Medal', color: '#52c41a', path: '/system/cert' },
  { title: '错题练习', desc: '错题自动归集，按掌握状态反复练', icon: 'RefreshRight', color: '#eb2f96', path: '/exam/wrong' }
];

const quickActions = computed(() => {
  if (isManager.value) {
    return [
      { label: '新建考试', icon: 'Plus', path: '/system/exam/edit' },
      { label: '题库', icon: 'Collection', path: '/system/question' },
      { label: '阅卷', icon: 'EditPen', path: '/system/mark' },
      { label: '监考中心', icon: 'VideoCamera', path: '/system/proctor' }
    ];
  }
  return [
    { label: '考试中心', icon: 'Calendar', path: '/exam/center' },
    { label: '考试记录', icon: 'Tickets', path: '/exam/records' },
    { label: '错题本', icon: 'EditPen', path: '/exam/wrong' },
    { label: '我的证书', icon: 'Medal', path: '/exam/certs' }
  ];
});

/* ------------------------------- 图表 ------------------------------- */
const chartColors = computed(() => (screenMode.value
  ? { text: 'rgba(235,245,255,.75)', axis: 'rgba(120,170,255,.35)', split: 'rgba(120,170,255,.18)' }
  : { text: '#5c6b7a', axis: '#e4e8ee', split: '#f0f3f7' }));

const renderTrend = () => {
  if (!trendRef.value) return;
  trendChart = trendChart ?? echarts.init(trendRef.value);
  const trend = stat.value?.trend ?? [];
  const dates = trend.length ? trend.map((item) => String(item.date).slice(5)) : ['暂无数据'];
  const option: any = {
    grid: { left: 8, right: 16, top: 32, bottom: 8, containLabel: true },
    tooltip: { trigger: 'axis' },
    legend: { data: ['交卷数', '及格数'], right: 0, top: 0, textStyle: { color: chartColors.value.text, fontSize: 12 } },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: chartColors.value.axis } },
      axisLabel: { color: chartColors.value.text }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: chartColors.value.text },
      splitLine: { lineStyle: { color: chartColors.value.split } }
    },
    series: [
      {
        name: '交卷数',
        type: 'bar',
        barWidth: 18,
        itemStyle: { color: screenMode.value ? '#2f7bff' : '#7cb0ff', borderRadius: [4, 4, 0, 0] },
        data: trend.length ? trend.map((item) => num(item.submitCount)) : [0]
      },
      {
        name: '及格数',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        lineStyle: { width: 2, color: screenMode.value ? '#3ee8b8' : '#52c41a' },
        itemStyle: { color: screenMode.value ? '#3ee8b8' : '#52c41a' },
        data: trend.length ? trend.map((item) => num(item.passCount)) : [0]
      }
    ]
  };
  trendChart.setOption(option, true);
};

const renderPass = () => {
  if (!passRef.value) return;
  passChart = passChart ?? echarts.init(passRef.value);
  const passed = num(stat.value?.passRate);
  const option: any = {
    tooltip: { trigger: 'item', formatter: (p: any) => `${p.name}：${p.value}%` },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { color: chartColors.value.text, fontSize: 12 } },
    series: [
      {
        type: 'pie',
        radius: ['52%', '74%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: false,
        itemStyle: { borderColor: screenMode.value ? '#0b1a33' : '#fff', borderWidth: 2 },
        label: {
          show: true,
          position: 'center',
          formatter: () => `${passed}%\n及格率`,
          color: screenMode.value ? '#eaf4ff' : '#303133',
          fontSize: 16,
          lineHeight: 22
        },
        data: [
          { name: '及格', value: passed, itemStyle: { color: screenMode.value ? '#3ee8b8' : '#52c41a' } },
          { name: '未及格', value: Math.max(0, 100 - passed), itemStyle: { color: screenMode.value ? '#31507f' : '#e8edf5' } }
        ]
      }
    ]
  };
  passChart.setOption(option, true);
};

const renderCharts = async () => {
  // 图表容器在 v-else 分支里，数据回来后要等 DOM 真正渲染出来才能初始化
  await nextTick();
  renderTrend();
  renderPass();
};

const resizeCharts = () => {
  trendChart?.resize();
  passChart?.resize();
};

/* ------------------------------- 大屏 ------------------------------- */
const toggleScreen = async (flag?: boolean) => {
  const next = flag ?? !screenMode.value;
  if (screenfull.isEnabled) {
    if (next && rootRef.value) {
      await screenfull.request(rootRef.value);
    } else if (!next) {
      await screenfull.exit();
    }
  } else if (next) {
    ElMessage.warning('当前浏览器不支持全屏，仅切换为大屏配色');
  }
  screenMode.value = next;
  // 全屏后容器宽高变了，图表要重算，否则会被压成窄条
  window.setTimeout(() => {
    resizeCharts();
    renderCharts();
  }, 160);
};

/* ------------------------------- 加载 ------------------------------- */
const loadOverview = async () => {
  loading.value = true;
  try {
    if (isManager.value) {
      const res = await getHomeOverview();
      stat.value = res.data ?? null;
    } else {
      await loadMine();
    }
  } catch {
    stat.value = null;
    ElMessage.warning('统计数据暂不可用，其余内容照常浏览');
  } finally {
    loading.value = false;
    await renderCharts();
  }
};

/* ------------------------------- 交互 ------------------------------- */
const go = async (target: string) => {
  if (target === '__screen__') {
    await toggleScreen();
    return;
  }
  if (target === '__refresh__') {
    await loadOverview();
    return;
  }
  if (!target) return;
  // 从大屏里点进去要看正常的后台页面，顺手退出全屏
  if (screenMode.value) {
    await toggleScreen(false);
  }
  await router.push(target);
};

const timeText = (value?: string) => {
  if (!value) return '--:--';
  return String(value).length > 5 ? String(value).slice(11, 16) : String(value);
};

const toTs = (value?: string) => new Date(String(value ?? '').replace(/-/g, '/')).getTime();

const examStatusText = (item: HomeExamVO) => {
  const now = Date.now();
  const start = toTs(item.startTime);
  const end = toTs(item.endTime);
  if (!start || !end) return '待开始';
  if (now < start) return '待开始';
  if (now > end) return '已结束';
  return '进行中';
};

const examStatusType = (item: HomeExamVO) => {
  const text = examStatusText(item);
  return text === '进行中' ? 'success' : text === '已结束' ? 'info' : 'warning';
};

/* ------------------------------- 生命周期 ------------------------------- */
watch(screenMode, () => {
  renderCharts();
});

const onFullscreenChange = () => {
  // 用户按 Esc 退出全屏时，页面配色要跟着退回浅色
  if (screenMode.value && !screenfull.isFullscreen) {
    screenMode.value = false;
  }
};

onMounted(async () => {
  await loadOverview();
  window.addEventListener('resize', resizeCharts);
  if (screenfull.isEnabled) {
    screenfull.on('change', onFullscreenChange);
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  if (screenfull.isEnabled) {
    screenfull.off('change', onFullscreenChange);
  }
  trendChart?.dispose();
  passChart?.dispose();
  trendChart = null;
  passChart = null;
});
</script>

<style lang="scss" scoped>
.home {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
  transition: background 0.3s ease;
}

/* ---------------- Hero ---------------- */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 20px 24px;
  border-radius: 14px;
  background: linear-gradient(120deg, #eaf1ff 0%, #f6f0ff 100%);
  border: 1px solid rgba(120, 150, 220, 0.18);
}
.hero-title {
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}
.hero-sub {
  margin-top: 6px;
  color: #5c6b7a;
  font-size: 14px;
}
.hero-dot {
  margin: 0 6px;
}
.hero-desc {
  margin-top: 10px;
  max-width: 760px;
  color: #7a8794;
  font-size: 13px;
  line-height: 1.7;
}
.hero-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* ---------------- KPI ---------------- */
.kpi-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 14px;
}
.kpi-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #eef1f5;
  box-shadow: 0 1px 2px rgba(31, 45, 61, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(31, 45, 61, 0.08);
  }
}
.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--kpi-color, #4096ff);
  background: rgba(64, 150, 255, 0.12);
}
.kpi-body {
  min-width: 0;
}
.kpi-label {
  font-size: 13px;
  color: #7a8794;
}
.kpi-value {
  margin-top: 2px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2d3d;
  line-height: 1.3;
}
.kpi-unit {
  margin-left: 3px;
  font-size: 13px;
  color: #98a3b0;
}
.kpi-tip {
  font-size: 12px;
  color: #a0abb8;
}

/* ---------------- 面板 ---------------- */
.panel-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}
.panel {
  margin-top: 16px;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #eef1f5;
}
.panel-grid > .panel {
  margin-top: 0;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}
.panel-note {
  font-size: 12px;
  color: #a0abb8;
}
.chart {
  width: 100%;
}
.chart-md {
  height: 260px;
}
.chart-sm {
  height: 220px;
}

/* ---------------- 今日考试 ---------------- */
.exam-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.exam-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fafbfc;
  cursor: pointer;
  transition: background 0.2s ease;
  &:hover {
    background: #f0f5ff;
  }
}
.exam-time {
  width: 96px;
  font-size: 13px;
  color: #5c6b7a;
  font-variant-numeric: tabular-nums;
}
.exam-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.exam-name {
  font-size: 14px;
  color: #1f2d3d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---------------- 热度榜 ---------------- */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rank-no {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: #f0f3f7;
  color: #7a8794;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rank-main {
  flex: 1;
  min-width: 0;
}
.rank-title {
  font-size: 13px;
  color: #1f2d3d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-bar {
  margin-top: 6px;
  height: 6px;
  border-radius: 3px;
  background: #f0f3f7;
  overflow: hidden;
  span {
    display: block;
    height: 100%;
    border-radius: 3px;
    background: linear-gradient(90deg, #7cb0ff, #2f7bff);
  }
}
.rank-right {
  width: 92px;
  text-align: right;
}
.rank-count {
  font-size: 13px;
  color: #1f2d3d;
  font-weight: 600;
}
.rank-sub {
  font-size: 12px;
  color: #a0abb8;
}

/* ---------------- 能力矩阵 ---------------- */
.intro {
  margin-bottom: 4px;
}
.feat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}
.feat-card {
  padding: 14px;
  border-radius: 10px;
  background: #fafbfc;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  &:hover {
    background: #fff;
    border-color: rgba(64, 150, 255, 0.4);
    transform: translateY(-2px);
  }
}
.feat-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--feat-color, #4096ff);
  background: rgba(64, 150, 255, 0.12);
  font-size: 17px;
}
.feat-title {
  margin-top: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}
.feat-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #7a8794;
  line-height: 1.6;
}
.intro-foot {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed #eceff3;
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: space-between;
}
.intro-text {
  flex: 1;
  min-width: 280px;
  font-size: 13px;
  color: #6a7683;
  line-height: 1.9;
}
.intro-meta {
  display: flex;
  gap: 24px;
  > div {
    display: flex;
    flex-direction: column;
    gap: 4px;
    span {
      font-size: 12px;
      color: #a0abb8;
    }
    b {
      font-size: 13px;
      color: #1f2d3d;
    }
  }
}

/* ---------------- 大屏模式 ---------------- */
.home.is-screen {
  position: fixed;
  inset: 0;
  z-index: 2000;
  overflow-y: auto;
  padding: 32px 40px;
  background:
    radial-gradient(1200px 600px at 20% -10%, rgba(47, 123, 255, 0.25), transparent 60%),
    linear-gradient(160deg, #07122a 0%, #0b1a33 55%, #0a1428 100%);

  .hero {
    background: linear-gradient(120deg, rgba(47, 123, 255, 0.22) 0%, rgba(146, 84, 222, 0.18) 100%);
    border-color: rgba(90, 150, 255, 0.35);
  }
  .hero-title {
    color: #eaf4ff;
    font-size: 30px;
  }
  .hero-sub {
    color: rgba(220, 235, 255, 0.8);
    font-size: 16px;
  }
  .hero-desc {
    color: rgba(190, 215, 255, 0.6);
  }
  .panel,
  .kpi-card {
    background: rgba(12, 32, 64, 0.72);
    border-color: rgba(80, 140, 240, 0.28);
    box-shadow: inset 0 0 24px rgba(30, 90, 200, 0.18);
  }
  .panel-title,
  .kpi-value,
  .exam-name,
  .rank-title,
  .feat-title {
    color: #eaf4ff;
  }
  .kpi-label,
  .panel-note,
  .kpi-tip,
  .exam-time,
  .rank-sub,
  .feat-desc,
  .intro-text,
  .intro-meta b {
    color: rgba(200, 220, 255, 0.65);
  }
  .intro-meta span {
    color: rgba(180, 205, 245, 0.45);
  }
  .kpi-value {
    font-size: 34px;
  }
  .exam-item,
  .feat-card {
    background: rgba(18, 42, 80, 0.6);
    &:hover {
      background: rgba(30, 70, 130, 0.7);
    }
  }
  .rank-bar {
    background: rgba(90, 140, 220, 0.18);
  }
  .intro-foot {
    border-top-color: rgba(90, 140, 220, 0.3);
  }
  .chart-md {
    height: 320px;
  }
  .chart-sm {
    height: 260px;
  }
}

@media (max-width: 1024px) {
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
