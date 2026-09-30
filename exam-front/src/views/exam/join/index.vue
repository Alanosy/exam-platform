<template>
  <div class="join-page">
    <div class="join-card">
      <!-- 加载中 -->
      <el-skeleton v-if="loading" :rows="5" animated />

      <!-- 链接无效 / 加载失败 -->
      <el-result v-else-if="errorMsg" icon="warning" title="无法打开该考试" :sub-title="errorMsg">
        <template #extra>
          <el-button type="primary" plain @click="goHome">返回首页</el-button>
        </template>
      </el-result>

      <template v-else>
        <div class="join-header">
          <span class="join-badge">考试邀请</span>
          <h1 class="join-title">{{ info.examName }}</h1>
          <div class="join-meta">
            <el-tag :type="statusTag.type" size="small" effect="light">{{ statusTag.label }}</el-tag>
            <span v-if="info.duration" class="join-meta-item">限时 {{ info.duration }} 分钟</span>
            <span v-else class="join-meta-item">限时以试卷为准</span>
          </div>
        </div>

        <el-descriptions :column="1" border class="join-desc">
          <el-descriptions-item label="考试时间">{{ timeRange }}</el-descriptions-item>
          <el-descriptions-item label="链接有效期">{{ expireText }}</el-descriptions-item>
          <el-descriptions-item label="考试说明">{{ info.examDesc || '暂无说明' }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="joined"
          class="join-alert"
          type="success"
          :closable="false"
          show-icon
          title="你已加入本场考试"
          description="后续可在考试中心查看你参与的考试"
        />
        <el-alert
          v-else-if="!info.joinable"
          class="join-alert"
          type="warning"
          :closable="false"
          show-icon
          :title="info.joinTip || '当前无法加入该考试'"
        />

        <el-form v-if="showPassword" class="join-form" @submit.prevent>
          <el-form-item label="参与密码">
            <el-input
              v-model.trim="password"
              type="password"
              show-password
              maxlength="32"
              placeholder="请输入考试组织者提供的参与密码"
              @keyup.enter="handleJoin"
            />
          </el-form-item>
        </el-form>

        <div class="join-actions">
          <el-button v-if="!joined" type="primary" size="large" :loading="submitting" :disabled="!info.joinable" @click="handleJoin">
            {{ info.needPassword ? '验证并加入' : '加入考试' }}
          </el-button>
          <el-button v-else type="primary" size="large" @click="goCenter">前往考试中心</el-button>
          <el-button size="large" plain @click="goHome">返回首页</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts" name="ExamJoin">
import { getExamJoinInfo, joinExam } from '@/api/system/exam';
import type { ExamJoinVO } from '@/api/system/exam/types';

const route = useRoute();
const router = useRouter();

/** 考试状态对应的展示文案与标签色 */
const STATUS_TAG: Record<string, { label: string; type: 'success' | 'warning' | 'info' | 'danger' }> = {
  not_start: { label: '未开始', type: 'warning' },
  ongoing: { label: '进行中', type: 'success' },
  finished: { label: '已结束', type: 'info' },
  archived: { label: '已归档', type: 'info' }
};

// 首屏先显示骨架屏，避免数据回来前闪一下空表单
const loading = ref(true);
const submitting = ref(false);
const joined = ref(false);
const errorMsg = ref('');
const password = ref('');

const info = ref<ExamJoinVO>({
  examId: '',
  examName: '',
  examDesc: '',
  startTime: '',
  endTime: '',
  duration: 0,
  status: '',
  joinExpireTime: '',
  needPassword: false,
  joined: false,
  joinable: false,
  joinTip: ''
});

/** 链接上的加入码 */
const code = computed(() => (route.params.code as string) || '');

const statusTag = computed(() => STATUS_TAG[info.value.status] ?? { label: '未开始', type: 'info' });

/** 只有需要密码、当前可加入、且还没加入过时才显示密码框 */
const showPassword = computed(() => !!info.value.needPassword && !!info.value.joinable && !joined.value);

/** 后端返回的时间串可能是 'yyyy-MM-dd HH:mm:ss'，换成 '/' 兼容 Safari */
const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const timeRange = computed(() => {
  const start = formatTime(info.value.startTime);
  const end = formatTime(info.value.endTime);
  return start === '-' && end === '-' ? '-' : `${start} 至 ${end}`;
});

const expireText = computed(() => (info.value.joinExpireTime ? `${formatTime(info.value.joinExpireTime)} 前可加入` : '与考试结束时间一致'));

/** 拉取考试概要；失败时给出页面级提示（拦截器已弹过一次错误） */
const loadInfo = async () => {
  if (!code.value) {
    errorMsg.value = '加入链接缺少加入码';
    return;
  }
  loading.value = true;
  try {
    const res = await getExamJoinInfo(code.value);
    info.value = res.data;
    joined.value = !!res.data.joined;
  } catch (e: any) {
    errorMsg.value = (e instanceof Error && e.message) || '加入链接无效或已失效，请联系考试组织者确认';
  } finally {
    loading.value = false;
  }
};

const handleJoin = async () => {
  if (info.value.needPassword && !password.value) {
    ElMessage.warning('请输入参与密码');
    return;
  }
  submitting.value = true;
  try {
    await joinExam(code.value, password.value);
    joined.value = true;
    ElMessage.success('已加入本场考试');
  } catch {
    // 失败原因由响应拦截器统一提示，这里只结束按钮的加载态
  } finally {
    submitting.value = false;
  }
};

const goHome = () => router.push('/index');

/** 加入后直接去考试中心，那里能看到这场考试并开考 */
const goCenter = () => router.push('/exam/center');

onMounted(loadInfo);
</script>

<style scoped lang="scss">
.join-page {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  min-height: 100vh;
  padding: 48px 16px;
  background: linear-gradient(180deg, #f2f6fc 0%, #f7f9fc 100%);
  box-sizing: border-box;
}

.join-card {
  width: 100%;
  max-width: 640px;
  padding: 32px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgb(0 0 0 / 6%);
}

.join-badge {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  color: #337ecc;
  background: #ecf5ff;
  border-radius: 10px;
}

.join-title {
  margin: 12px 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
}

.join-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.join-meta-item {
  font-size: 13px;
  color: #909399;
}

.join-desc {
  margin-bottom: 20px;
}

.join-alert {
  margin-bottom: 20px;
}

.join-form {
  margin-bottom: 4px;
}

.join-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

@media (width <= 640px) {
  .join-page {
    padding: 16px 12px;
  }

  .join-card {
    padding: 20px;
  }
}
</style>
