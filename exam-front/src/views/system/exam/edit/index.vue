<template>
  <div class="p-[16px] pb-[70px]">
    <div class="flex items-center justify-between mb-[12px]">
      <div class="flex items-center gap-2">
        <el-button plain icon="Back" @click="goBack">返回</el-button>
        <span class="text-[16px] font-500">{{ isEdit ? '编辑考试' : '新增考试' }}</span>
        <el-tag v-if="form.paperName" size="small" effect="plain">{{ form.paperName }}</el-tag>
      </div>
      <div class="flex items-center gap-2">
        <el-button plain :loading="savingAction === 'not_start'" :disabled="saving" @click="submitForm('not_start')">保存</el-button>
        <el-button type="primary" :loading="savingAction === 'ongoing'" :disabled="saving" @click="submitForm('ongoing')">保存并发布</el-button>
      </div>
    </div>

    <el-steps :active="step" finish-status="success" align-center class="mb-[16px]">
      <el-step title="基本信息" description="名称、试卷、考试时间" />
      <el-step title="参加方式与规则" description="参加方式、加入链接、防作弊" />
    </el-steps>

    <!-- 第一步：基本信息 -->
    <el-card v-if="step === 0" shadow="never" class="mb-[12px]">
      <el-form ref="basicFormRef" :model="form" :rules="basicRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="考试名称" prop="examName">
              <el-input v-model="form.examName" maxlength="200" show-word-limit placeholder="请输入考试名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联试卷" prop="paperId">
              <el-select v-model="form.paperId" filterable placeholder="请选择试卷" class="w-full" @change="onPaperChange">
                <el-option v-for="item in paperList" :key="item.id" :label="item.paperName" :value="item.id">
                  <span>{{ item.paperName }}</span>
                  <span class="float-right text-xs text-gray-400">{{ item.totalScore }} 分</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择开始时间"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择结束时间" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试限时" prop="duration">
              <el-input-number v-model="form.duration" :min="0" :precision="0" controls-position="right" class="w-full" />
              <div class="mt-1 text-xs text-gray-400">分钟，0 表示沿用试卷时长</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" class="w-full">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试描述" prop="examDesc">
          <el-input v-model="form.examDesc" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入考试描述" />
        </el-form-item>
      </el-form>
      <div class="text-right">
        <el-button type="primary" @click="nextStep">下一步：参加方式与规则</el-button>
      </div>
    </el-card>

    <!-- 第二步：参加方式与考试规则 -->
    <template v-else>
      <el-card shadow="never" class="mb-[12px]">
        <template #header><span>参加方式</span></template>
        <el-form ref="joinFormRef" :model="form" :rules="joinRules" label-width="100px">
          <el-form-item label="参加方式" prop="participantType">
            <el-radio-group v-model="form.participantType">
              <el-radio v-for="item in participantTypeOptions" :key="item.value" :value="item.value">
                {{ item.label }}
                <span class="text-xs text-gray-400">{{ item.tip }}</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <template v-if="form.participantType === 'public'">
            <el-form-item label="加入链接">
              <div class="w-full flex items-center gap-2">
                <el-input :model-value="joinLink" readonly placeholder="选择公开链接后自动生成">
                  <template #append>
                    <el-button :disabled="!joinLink" @click="copyJoinLink">复制</el-button>
                  </template>
                </el-input>
                <el-button @click="handleRefreshJoinCode">重新生成</el-button>
              </div>
              <div class="mt-1 text-xs text-gray-400">考生通过该链接进入考试；链接在保存后正式生效，重新生成后旧链接立即失效</div>
            </el-form-item>
            <el-form-item label="参与密码" prop="joinPassword">
              <div class="w-full flex items-center gap-2">
                <el-input v-model="form.joinPassword" placeholder="留空表示无密码" show-password />
                <el-button @click="form.joinPassword = randomPassword()">随机生成</el-button>
              </div>
            </el-form-item>
            <el-form-item label="链接有效期" prop="joinExpireTime">
              <el-date-picker
                v-model="form.joinExpireTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="留空表示与考试结束时间一致"
                class="w-full"
              />
            </el-form-item>
          </template>
        </el-form>
      </el-card>

      <el-card shadow="never" class="mb-[12px]">
        <template #header><span>考试规则</span></template>
        <el-form ref="ruleFormRef" :model="form" :rules="ruleRules" label-width="100px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="允许迟到入场" prop="allowLate">
                <el-switch v-model="form.allowLate" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="form.allowLate === 1">
              <el-form-item label="允许迟到" prop="lateMinute">
                <el-input-number v-model="form.lateMinute" :min="1" :precision="0" controls-position="right" class="w-full" />
                <div class="mt-1 text-xs text-gray-400">分钟，超过则无法进入</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="允许重考" prop="allowRetry">
                <el-switch v-model="form.allowRetry" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="form.allowRetry === 1">
              <el-form-item label="最大重考次数" prop="maxRetryCount">
                <el-input-number v-model="form.maxRetryCount" :min="1" :precision="0" controls-position="right" class="w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="答案展示" prop="showAnswerMode">
                <el-select v-model="form.showAnswerMode" placeholder="请选择答案展示时机" class="w-full">
                  <el-option v-for="item in showAnswerModeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <el-card shadow="never" class="mb-[12px]">
        <template #header><span>防作弊</span></template>
        <el-form label-width="100px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="切屏次数">
                <el-input-number v-model="antiCheat.switchScreen" :min="0" :precision="0" controls-position="right" class="w-full" />
                <div class="mt-1 text-xs text-gray-400">超过则强制交卷，0 表示不限制</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="禁止复制粘贴">
                <el-switch v-model="antiCheat.copyPaste" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="摄像头抓拍">
                <el-switch v-model="antiCheat.camera" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="强制全屏">
                <el-switch v-model="antiCheat.fullScreen" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </template>
  </div>
</template>

<script setup name="ExamEdit" lang="ts">
import { useRoute, useRouter } from 'vue-router';
import type { FormRules } from 'element-plus';
import { getExam, addExam, updateExam, refreshJoinCode } from '@/api/system/exam';
import { ExamForm, AntiCheatConfig } from '@/api/system/exam/types';
import { listPaper } from '@/api/system/paper';
import { PaperVO } from '@/api/system/paper/types';

/** 列表页路由地址，需要与后台「考试管理」菜单的路由地址保持一致 */
const EXAM_LIST_PATH = '/system/exam';
/** 考生端加入考试的页面路径，实际部署时按前端真实路由调整 */
const JOIN_PATH = '/exam/join/';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();
const router = useRouter();

/** 路由上的考试ID，为空表示新增 */
const examId = computed<string | undefined>(() => (route.params.examId as string) || undefined);
const isEdit = computed(() => !!examId.value);

const step = ref(0);
/** 正在保存的动作：'' 未保存 / 'not_start' 保存 / 'ongoing' 保存并发布，用于让 loading 落在被点的那个按钮上 */
const savingAction = ref<'not_start' | 'ongoing' | ''>('');
const saving = computed(() => savingAction.value !== '');

const basicFormRef = ref<ElFormInstance>();
const joinFormRef = ref<ElFormInstance>();
const ruleFormRef = ref<ElFormInstance>();

const statusOptions = [
  { label: '未开始', value: 'not_start' },
  { label: '进行中', value: 'ongoing' },
  { label: '已结束', value: 'finished' },
  { label: '已归档', value: 'archived' }
];
const participantTypeOptions = [
  { label: '白名单', value: 'white', tip: '只有导入名单的考生可参加' },
  { label: '公开链接', value: 'public', tip: '任何人凭链接（及密码）可参加' }
];
const showAnswerModeOptions = [
  { label: '不展示', value: 'none' },
  { label: '交卷后展示', value: 'after_submit' },
  { label: '考试结束后展示', value: 'after_exam' }
];

const initFormData: ExamForm = {
  id: undefined,
  examName: undefined,
  examDesc: undefined,
  paperId: undefined,
  startTime: undefined,
  endTime: undefined,
  duration: 0,
  allowLate: 0,
  lateMinute: 10,
  allowRetry: 0,
  maxRetryCount: 1,
  showAnswerMode: 'none',
  antiCheatConfig: undefined,
  participantType: 'white',
  joinCode: undefined,
  joinPassword: undefined,
  joinExpireTime: undefined,
  status: 'not_start'
};

const form = reactive<ExamForm & { paperName?: string }>({ ...initFormData });

const defaultAntiCheat: AntiCheatConfig = { switchScreen: 0, copyPaste: 1, camera: 0, fullScreen: 0 };
const antiCheat = reactive<AntiCheatConfig>({ ...defaultAntiCheat });

const basicRules: FormRules = {
  examName: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  paperId: [{ required: true, message: '请选择关联试卷', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (value && form.startTime && new Date(value).getTime() <= new Date(form.startTime).getTime()) {
          callback(new Error('结束时间必须晚于开始时间'));
          return;
        }
        callback();
      },
      trigger: 'change'
    }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
};

const joinRules: FormRules = {
  participantType: [{ required: true, message: '请选择参加方式', trigger: 'change' }]
};

const ruleRules: FormRules = {
  showAnswerMode: [{ required: true, message: '请选择答案展示时机', trigger: 'change' }]
};

/* ---------------------------------- 试卷下拉 ---------------------------------- */

const paperList = ref<PaperVO[]>([]);

const loadPaperList = async () => {
  try {
    const res = await listPaper({ pageNum: 1, pageSize: 500 });
    paperList.value = res.rows ?? [];
  } catch {
    paperList.value = [];
  }
};

const onPaperChange = (paperId: string | number) => {
  form.paperName = paperList.value.find((item) => String(item.id) === String(paperId))?.paperName;
};

/* ---------------------------------- 加入链接 ---------------------------------- */

const joinLink = computed(() => (form.joinCode ? `${window.location.origin}${JOIN_PATH}${form.joinCode}` : ''));

/** 加入码：去掉 0/1/I/O 等易混淆字符，避免考生抄错链接 */
const JOIN_CODE_LENGTH = 10;
const randomJoinCode = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789';
  return Array.from({ length: JOIN_CODE_LENGTH }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
};

// 切到「公开链接」时立刻生成加入码，不需要等保存后才看得到
watch(
  () => form.participantType,
  (val) => {
    if (val === 'public' && !form.joinCode) {
      form.joinCode = randomJoinCode();
    }
  },
  { immediate: true }
);

const copyJoinLink = async () => {
  if (!joinLink.value) return;
  try {
    await navigator.clipboard.writeText(joinLink.value);
    proxy?.$modal.msgSuccess('链接已复制');
  } catch {
    // 非 HTTPS 或浏览器不支持时退回选中式复制
    proxy?.$modal.msgWarning('复制失败，请手动选中链接复制');
  }
};

const randomPassword = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  return Array.from({ length: 6 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
};

/** 重新生成加入码，旧链接立即失效 */
const handleRefreshJoinCode = async () => {
  // 新增状态还没有落库，本地换一个码即可，保存时一起写入
  if (!examId.value) {
    form.joinCode = randomJoinCode();
    proxy?.$modal.msgSuccess('已生成新的加入链接，保存后生效');
    return;
  }
  await proxy?.$modal.confirm('重新生成后，已发出的旧链接将立即失效，是否继续？');
  const res = await refreshJoinCode(examId.value);
  form.joinCode = res.data;
  proxy?.$modal.msgSuccess('已生成新的加入链接');
};

/* ---------------------------------- 数据回显 ---------------------------------- */

const parseAntiCheat = (json?: string) => {
  Object.assign(antiCheat, { ...defaultAntiCheat });
  if (!json) return;
  try {
    Object.assign(antiCheat, JSON.parse(json));
  } catch {
    // 历史脏数据不阻塞编辑，按默认值兜底
  }
};

const initPage = async () => {
  Object.assign(form, { ...initFormData });
  parseAntiCheat(undefined);
  step.value = 0;
  basicFormRef.value?.clearValidate();

  const id = examId.value;
  if (!id) return;
  const res = await getExam(id);
  const data = res.data;
  Object.assign(form, {
    id: data.id,
    examName: data.examName,
    examDesc: data.examDesc,
    paperId: data.paperId,
    paperName: paperList.value.find((item) => String(item.id) === String(data.paperId))?.paperName,
    startTime: data.startTime,
    endTime: data.endTime,
    duration: data.duration ?? 0,
    allowLate: data.allowLate ?? 0,
    lateMinute: data.lateMinute ?? 0,
    allowRetry: data.allowRetry ?? 0,
    maxRetryCount: data.maxRetryCount ?? 1,
    showAnswerMode: data.showAnswerMode ?? 'none',
    participantType: data.participantType ?? 'white',
    joinCode: data.joinCode,
    joinPassword: data.joinPassword,
    joinExpireTime: data.joinExpireTime,
    status: data.status ?? 'not_start'
  });
  parseAntiCheat(data.antiCheatConfig);
};

/* ----------------------------------- 提交 ----------------------------------- */

const nextStep = async () => {
  try {
    await basicFormRef.value?.validate();
    step.value = 1;
  } catch {
    // 校验不通过时停留在当前步骤
  }
};

const submitForm = async (status: string) => {
  // 还在第一步时，先校验并跳到第二步，让考生规则也确认一遍
  if (step.value === 0) {
    await nextStep();
    if (step.value === 0) return;
    proxy?.$modal.msgWarning('请确认参加方式与考试规则后再保存');
    return;
  }
  try {
    await Promise.all([joinFormRef.value?.validate(), ruleFormRef.value?.validate()]);
  } catch {
    proxy?.$modal.msgError('请先补全必填项');
    return;
  }

  const isPublic = form.participantType === 'public';
  const payload: ExamForm = {
    ...form,
    status,
    // 关闭的开关不带出关联值，避免留下「不允许迟到但迟到10分钟」这类脏数据
    lateMinute: form.allowLate === 1 ? form.lateMinute : 0,
    maxRetryCount: form.allowRetry === 1 ? form.maxRetryCount : 0,
    joinPassword: isPublic ? form.joinPassword : undefined,
    joinExpireTime: isPublic ? form.joinExpireTime : undefined,
    // 加入码：新增时前端生成，编辑时沿用库里的，原样回传避免每次保存都换链接
    joinCode: isPublic ? form.joinCode : undefined,
    antiCheatConfig: JSON.stringify(antiCheat)
  };

  savingAction.value = status === 'ongoing' ? 'ongoing' : 'not_start';
  try {
    if (form.id) {
      await updateExam(payload);
    } else {
      await addExam(payload);
    }
    proxy?.$modal.msgSuccess(status === 'ongoing' ? '已保存并发布' : '保存成功');
    // 保存成功后返回列表页（与试卷组卷页保持一致）
    goBack();
  } finally {
    savingAction.value = '';
  }
};

/**
 * 考试列表页的路由地址。
 * 优先从已注册的路由里找（后台菜单路由不一定是 /system/exam，比如 /exam/exam），
 * 找不到再用常量兜底。
 */
const examListPath = () => {
  const dynamic = router
    .getRoutes()
    .find((item) => typeof item.name === 'string' && item.name.startsWith('Exam') && !item.name.includes('Edit') && !item.path.includes('edit'));
  return dynamic?.path ?? EXAM_LIST_PATH;
};

const goBack = () => {
  const historyState = window.history.state as { back?: string } | null;
  const from = historyState?.back;
  // 从列表页点进来时，直接回退即可回到原来的列表（带原来的查询条件）
  if (from && !from.includes('/exam/edit')) {
    proxy?.$tab.closePage(router.currentRoute.value);
    router.back();
    return;
  }
  proxy?.$tab.closeOpenPage({ path: examListPath() });
};

onMounted(async () => {
  await loadPaperList();
  await initPage();
});
</script>
