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

    <!-- 步骤条支持点回上一步；往前跳必须经过「下一步」的校验，所以只能往回点 -->
    <el-steps :active="step" finish-status="success" align-center class="mb-[16px] exam-steps">
      <el-step title="基本信息" description="名称、试卷、考试时间" :class="{ 'is-back': step > 0 }" @click="goStep(0)" />
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
            <el-form-item label="考试类型" prop="examType">
              <el-radio-group v-model="form.examType">
                <el-radio v-for="item in examTypeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
              <div class="mt-1 text-xs text-gray-400">
                {{ isFormal ? '正式考试：必须设定起止时间，成绩计入考试记录' : '练习考试：可不设时间长期有效，可反复参加，不强制阅卷' }}
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试限时" prop="duration">
              <el-input-number v-model="form.duration" :min="0" :precision="0" controls-position="right" class="w-full" />
              <div class="mt-1 text-xs text-gray-400">分钟，0 表示不限时</div>
            </el-form-item>
          </el-col>
          <!-- 起止时间挨着放，填的时候不用在限时和结束时间之间跳 -->
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                :placeholder="isFormal ? '请选择开始时间' : '留空表示立即开放'"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                :placeholder="isFormal ? '请选择结束时间' : '留空表示长期有效'"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" class="w-full">
                <el-option v-for="item in examStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试描述" prop="examDesc">
          <el-input v-model="form.examDesc" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入考试描述" />
        </el-form-item>
      </el-form>
      <div class="flex justify-end">
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

          <!-- 白名单：按部门挑人，只有挑到的人能在考试中心看到本场考试 -->
          <template v-else>
            <el-form-item label="考试考生">
              <div class="w-full">
                <div class="mb-[8px] flex items-center gap-2">
                  <el-button icon="User" @click="openUserSelect">选择考生</el-button>
                  <el-button v-if="whiteUsers.length" plain icon="Delete" @click="clearWhiteUsers">清空</el-button>
                  <span class="text-xs text-gray-400">
                    {{ whiteUsers.length ? `已选择 ${whiteUsers.length} 人，保存后生效` : '按部门筛选出考生后勾选，可跨页累加' }}
                  </span>
                </div>
                <el-table v-if="whiteUsers.length" :data="whiteUsers" border size="small" max-height="260" row-key="userId">
                  <el-table-column label="姓名" prop="nickName" min-width="120" show-overflow-tooltip />
                  <el-table-column label="登录账号" prop="userName" min-width="130" show-overflow-tooltip />
                  <el-table-column label="部门" min-width="140" show-overflow-tooltip>
                    <template #default="scope">{{ scope.row.deptName || '-' }}</template>
                  </el-table-column>
                  <el-table-column label="手机号" prop="phonenumber" width="130" align="center" />
                  <el-table-column label="操作" width="80" align="center">
                    <template #default="scope">
                      <el-button link type="danger" @click="removeWhiteUser(scope.$index)">移除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-else class="text-xs text-gray-400">还没有选择考生：白名单考试没有加入链接，未选中的考生看不到也进不了这场考试</div>
              </div>
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
                  <el-option v-for="item in examShowAnswerModeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="部分得分" prop="partialScore">
                <el-switch
                  v-model="partialScoreOn"
                  active-text="部分对也给分"
                  inactive-text="必须全对"
                />
                <div class="form-tip">
                  作用于多选题（漏选）与填空题（只答对部分空）。
                  <span v-if="partialScoreOn">多选题选中任何错误选项一律 0 分，防止全选蒙分。</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="partialScoreOn">
              <el-form-item label="部分正确得分" prop="partialScoreRate">
                <el-select v-model="form.partialScoreRate" class="w-full">
                  <el-option label="按命中比例（答对一半给一半）" :value="100" />
                  <el-option label="一律给该题一半分" :value="50" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <el-card shadow="never" class="mb-[12px]">
        <template #header>
          <div class="flex items-center justify-between">
            <span>防作弊</span>
            <span class="text-xs text-gray-400">填了次数上限的项（大于 0）才会触发强制交卷，其余只记录不拦人</span>
          </div>
        </template>
        <el-form label-width="120px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="切屏次数">
                <el-input-number v-model="antiCheat.switchScreen" :min="0" :max="99" :precision="0" controls-position="right" class="w-full" />
                <div class="mt-1 text-xs text-gray-400">超过则强制交卷，0 表示不限制</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="禁止复制粘贴">
                <el-switch v-model="antiCheat.copyPaste" :active-value="1" :inactive-value="0" />
                <div class="mt-1 text-xs text-gray-400">开启后拦截复制/剪切/右键，并记录尝试行为</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">
            <span class="text-xs text-gray-400">摄像头抓拍</span>
          </el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="摄像头抓拍">
                <el-switch v-model="antiCheat.camera" :active-value="1" :inactive-value="0" />
                <div class="mt-1 text-xs text-gray-400">考生需授权摄像头，入场与定时各抓一张</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="抓拍间隔">
                <el-input-number
                  v-model="antiCheat.cameraInterval"
                  :min="15"
                  :max="600"
                  :precision="0"
                  :disabled="antiCheat.camera !== 1"
                  controls-position="right"
                  class="w-full"
                />
                <div class="mt-1 text-xs text-gray-400">单位：秒，15 ~ 600</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">
            <span class="text-xs text-gray-400">全屏与粘贴上限</span>
          </el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="强制全屏">
                <el-switch v-model="antiCheat.fullScreen" :active-value="1" :inactive-value="0" />
                <div class="mt-1 text-xs text-gray-400">浏览器要求手动点击，答题页提供进入全屏按钮</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="退出全屏次数">
                <el-input-number
                  v-model="antiCheat.maxExitFullscreen"
                  :min="0"
                  :max="99"
                  :precision="0"
                  :disabled="antiCheat.fullScreen !== 1"
                  controls-position="right"
                  class="w-full"
                />
                <div class="mt-1 text-xs text-gray-400">超过则强制交卷，0 表示不限制</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="允许粘贴次数">
                <el-input-number v-model="antiCheat.maxPaste" :min="0" :max="99" :precision="0" controls-position="right" class="w-full" />
                <div class="mt-1 text-xs text-gray-400">超过则强制交卷，0 表示不限制</div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">
            <span class="text-xs text-gray-400">其它可疑行为检测（只记录，不强制交卷）</span>
          </el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="开发者工具">
                <el-switch v-model="antiCheat.devtool" :active-value="1" :inactive-value="0" />
                <div class="mt-1 text-xs text-gray-400">检测 F12、审查元素等快捷键与窗口异常</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="多标签页检测">
                <el-switch v-model="antiCheat.multitab" :active-value="1" :inactive-value="0" />
                <div class="mt-1 text-xs text-gray-400">同一场答卷在多个标签页打开时告警</div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 及格证书：不配就不发，配了才在考生及格时自动颁发 -->
      <el-card shadow="never" class="mb-[12px]">
        <template #header><span>及格证书</span></template>
        <el-form :model="form" label-width="100px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="证书模板">
                <el-select v-model="form.certId" placeholder="不发证书" clearable filterable class="w-full">
                  <el-option v-for="item in certOptions" :key="item.id" :label="item.certName" :value="item.id">
                    <span>{{ item.certName }}</span>
                    <span class="ml-2 text-xs text-gray-400">
                      {{ item.validType === '1' ? `有效 ${item.validDays ?? 0} 天` : '永久有效' }}
                    </span>
                  </el-option>
                </el-select>
                <div class="mt-1 text-xs text-gray-400">
                  考生达到试卷及格分后自动颁发，留空表示本场考试不发证书
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="证书说明">
                <div class="text-xs text-gray-400 leading-6">
                  及格分取自所选试卷的及格分；含主观题的卷子要等阅卷完成后才颁发。<br />
                  证书模板在「证书管理」里维护，停用的模板不会自动颁发。
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 第二步底部操作栏：能退回上一步，也能就地保存，不必再滚回页头 -->
      <div class="flex items-center justify-between">
        <el-button plain icon="ArrowLeft" @click="prevStep">上一步：基本信息</el-button>
        <div class="flex items-center gap-2">
          <el-button plain :loading="savingAction === 'not_start'" :disabled="saving" @click="submitForm('not_start')">保存</el-button>
          <el-button type="primary" :loading="savingAction === 'ongoing'" :disabled="saving" @click="submitForm('ongoing')">保存并发布</el-button>
        </div>
      </div>
    </template>

    <!-- 选人弹窗：左侧部门树、右侧用户表，勾选后回填到白名单 -->
    <UserSelect ref="userSelectRef" :multiple="true" :data="whiteUserIds" @confirm-call-back="onUserSelected"></UserSelect>
  </div>
</template>

<script setup name="ExamEdit" lang="ts">
import { useRoute, useRouter } from 'vue-router';
import type { FormRules } from 'element-plus';
import { getExam, addExam, updateExam, refreshJoinCode, listExamWhiteUsers, saveExamWhiteUsers } from '@/api/system/exam';
import { ExamForm, AntiCheatConfig, ExamWhiteUserVO } from '@/api/system/exam/types';
import { listPaper } from '@/api/system/paper';
import { PaperVO } from '@/api/system/paper/types';
import { UserVO } from '@/api/system/user/types';
import UserSelect from '@/components/UserSelect/index.vue';
import { listCertificateOptions } from '@/api/exam/cert';
import type { CertificateOptionVO } from '@/api/exam/cert/types';
import { useExamDicts } from '@/hooks/useExamDicts';
import { buildJoinLink } from '@/utils/joinLink';

/** 列表页路由地址，需要与后台「考试管理」菜单的路由地址保持一致 */
const EXAM_LIST_PATH = '/system/exam';

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

/** 状态 / 考试类型 / 答案展示时机走字典；参加方式保留灰字说明，字典里放不下 tip */
const { examTypeOptions, examStatusOptions, examShowAnswerModeOptions } = useExamDicts();

const participantTypeOptions = [
  { label: '白名单', value: 'white', tip: '只有导入名单的考生可参加' },
  { label: '公开链接', value: 'public', tip: '任何人凭链接（及密码）可参加' }
];

/** 考试类型常量：后端 exam_type 字典里 1 正式 / 2 练习 */
const EXAM_TYPE_FORMAL = '1';
const EXAM_TYPE_PRACTICE = '2';

/** 新增时的初始表单值，编辑回显前也会用它先把表单清空 */
const initFormData: ExamForm = {
  id: undefined,
  examName: undefined,
  examDesc: undefined,
  paperId: undefined,
  examType: EXAM_TYPE_FORMAL,
  startTime: undefined,
  endTime: undefined,
  duration: 0,
  allowLate: 0,
  lateMinute: 10,
  allowRetry: 0,
  maxRetryCount: 1,
  showAnswerMode: 'none',
  // 客观题部分得分：0 必须全对（默认，与历史行为一致），1 启用部分得分
  partialScore: '0',
  partialScoreRate: 100,
  antiCheatConfig: undefined,
  participantType: 'white',
  joinCode: undefined,
  joinPassword: undefined,
  joinExpireTime: undefined,
  status: 'not_start',
  certId: undefined
};

// form 必须先于下面的 computed / watch 声明：Vue 创建 watch 时会立即执行一次 getter，
// 放在后面会命中 TDZ（ReferenceError: Cannot access 'form' before initialization）
const form = reactive<ExamForm & { paperName?: string }>({ ...initFormData });

/**
 * 部分得分开关：后端存的是 char(1) 的 '0'/'1'，el-switch 要 boolean，这里做一层转换。
 * 必须写在 form 之后——computed 的 getter 会在创建时同步跑一次，写在前面会命中 TDZ。
 */
const partialScoreOn = computed({
  get: () => form.partialScore === '1',
  set: (val: boolean) => {
    form.partialScore = val ? '1' : '0';
  }
});

/** 正式考试才强制要求起止时间 */
const isFormal = computed(() => form.examType === EXAM_TYPE_FORMAL);

// 切到练习时按练习的默认规则重置：时间可空、可反复参加
watch(
  () => form.examType,
  (val) => {
    if (val === EXAM_TYPE_PRACTICE) {
      form.allowRetry = 1;
      if (!form.maxRetryCount || form.maxRetryCount < 1) {
        form.maxRetryCount = 1;
      }
    } else if (val === EXAM_TYPE_FORMAL) {
      // 正式考试默认不允许重考，需要的话由用户手动打开
      form.allowRetry = 0;
    }
  }
);

/**
 * 防作弊默认值
 * <p>原则：默认一律「只记录、不强制」，避免老考试或没仔细配的考试把考生直接卡在门外。
 * 只有明确填了次数上限的项（>0）才会在监考端触发强制交卷。
 */
const defaultAntiCheat: AntiCheatConfig = {
  switchScreen: 0,
  copyPaste: 1,
  camera: 0,
  fullScreen: 0,
  cameraInterval: 60,
  maxPaste: 0,
  maxExitFullscreen: 0,
  devtool: 1,
  multitab: 1
};
const antiCheat = reactive<AntiCheatConfig>({ ...defaultAntiCheat });

/** 结束时间必须晚于开始时间；练习考试可以不填时间，填了才校验先后 */
const endTimeValidator = (_rule: any, value: any, callback: (e?: Error) => void) => {
  if (value && form.startTime && new Date(value).getTime() <= new Date(form.startTime).getTime()) {
    callback(new Error('结束时间必须晚于开始时间'));
    return;
  }
  callback();
};

// 正式考试起止时间必填，练习考试可留空（长期有效），所以规则要跟着类型走
const basicRules = computed<FormRules>(() => ({
  examName: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  paperId: [{ required: true, message: '请选择关联试卷', trigger: 'change' }],
  examType: [{ required: true, message: '请选择考试类型', trigger: 'change' }],
  startTime: isFormal.value ? [{ required: true, message: '请选择开始时间', trigger: 'change' }] : [],
  endTime: isFormal.value
    ? [
        { required: true, message: '请选择结束时间', trigger: 'change' },
        { validator: endTimeValidator, trigger: 'change' }
      ]
    : [{ validator: endTimeValidator, trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}));

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

/* ---------------------------------- 证书下拉 ---------------------------------- */

const certOptions = ref<CertificateOptionVO[]>([]);

/** 只拉启用中的模板：停用的不该再被选到新考试上 */
const loadCertOptions = async () => {
  try {
    const res = await listCertificateOptions();
    certOptions.value = res.data ?? [];
  } catch {
    // 证书服务没起来时只是选不了证书，不能让整个考试编辑页打不开
    certOptions.value = [];
  }
};

/* ---------------------------------- 加入链接 ---------------------------------- */

/** 带上部署时的上下文路径，否则非根路径部署时复制出去的链接打不开；工具内部会压掉多余斜杠 */
const joinLink = computed(() => buildJoinLink(form.joinCode));

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

/* ---------------------------------- 白名单考生 ---------------------------------- */

const userSelectRef = ref<InstanceType<typeof UserSelect>>();

/** 已选考生：只留页面展示用的快照，提交时只把 userId 传给后端 */
const whiteUsers = ref<UserVO[]>([]);

/** 已选考生ID，给 UserSelect 回显勾选状态 */
const whiteUserIds = computed<string[]>(() => whiteUsers.value.map((item) => String(item.userId)));

const openUserSelect = () => userSelectRef.value?.open();

/** UserSelect 点确定：按 userId 去重后追加，避免重复勾选同一个人 */
const onUserSelected = (users: UserVO[]) => {
  const picked = users ?? [];
  const exist = new Set(whiteUserIds.value);
  whiteUsers.value = [...whiteUsers.value, ...picked.filter((item) => item && item.userId !== undefined && !exist.has(String(item.userId)))];
};

const clearWhiteUsers = () => {
  whiteUsers.value = [];
};

const removeWhiteUser = (index: number) => {
  whiteUsers.value.splice(index, 1);
};

/** 编辑已有考试时把名单拉回来，拉不到就让管理员重选，不影响考试本体 */
const loadWhiteUsers = async (id: string) => {
  try {
    const res = await listExamWhiteUsers(id);
    whiteUsers.value = (res.data ?? []).map(
      (item: ExamWhiteUserVO) =>
        ({
          userId: item.userId,
          userName: item.userName,
          nickName: item.nickName,
          deptName: item.deptName,
          phonenumber: item.phonenumber
        } as UserVO)
    );
  } catch {
    whiteUsers.value = [];
  }
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
  // 老配置没有新字段、或被人手改成 null：统一兜底成数字，否则数字框拿到 null 会回显异常
  const num = (v: unknown, d: number): number => (typeof v === 'number' && !Number.isNaN(v) ? v : d);
  antiCheat.switchScreen = num(antiCheat.switchScreen, defaultAntiCheat.switchScreen);
  antiCheat.cameraInterval = num(antiCheat.cameraInterval, defaultAntiCheat.cameraInterval);
  antiCheat.maxPaste = num(antiCheat.maxPaste, defaultAntiCheat.maxPaste);
  antiCheat.maxExitFullscreen = num(antiCheat.maxExitFullscreen, defaultAntiCheat.maxExitFullscreen);
  antiCheat.copyPaste = num(antiCheat.copyPaste, defaultAntiCheat.copyPaste);
  antiCheat.camera = num(antiCheat.camera, defaultAntiCheat.camera);
  antiCheat.fullScreen = num(antiCheat.fullScreen, defaultAntiCheat.fullScreen);
  antiCheat.devtool = num(antiCheat.devtool, defaultAntiCheat.devtool);
  antiCheat.multitab = num(antiCheat.multitab, defaultAntiCheat.multitab);
};

const initPage = async () => {
  Object.assign(form, { ...initFormData });
  parseAntiCheat(undefined);
  whiteUsers.value = [];
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
    examType: data.examType ?? EXAM_TYPE_FORMAL,
    startTime: data.startTime,
    endTime: data.endTime,
    duration: data.duration ?? 0,
    allowLate: data.allowLate ?? 0,
    lateMinute: data.lateMinute ?? 0,
    allowRetry: data.allowRetry ?? 0,
    maxRetryCount: data.maxRetryCount ?? 1,
    showAnswerMode: data.showAnswerMode ?? 'none',
    // 老数据没有这两个字段，回退到「必须全对」
    partialScore: data.partialScore ?? '0',
    partialScoreRate: data.partialScoreRate ?? 100,
    participantType: data.participantType ?? 'white',
    joinCode: data.joinCode,
    joinPassword: data.joinPassword,
    joinExpireTime: data.joinExpireTime,
    status: data.status ?? 'not_start',
    certId: data.certId ?? undefined
  });
  parseAntiCheat(data.antiCheatConfig);
  // 白名单考试要先把名单拉回来；公开链接考试用不上名单，留空即可
  if (form.participantType === 'white') {
    await loadWhiteUsers(id);
  }
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

/** 回到上一步：已填的内容都在同一个 form 里，退回不会丢 */
const prevStep = () => {
  step.value = 0;
};

/**
 * 点步骤条跳转
 *
 * <p>只允许往回跳：往前必须经过「下一步」的校验，否则基本信息没填全就能进第二步。
 */
const goStep = (target: number) => {
  if (target >= step.value) {
    return;
  }
  step.value = target;
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
  const isWhite = form.participantType === 'white';
  // 白名单考试发出去却没有考生的话，没有任何人能看到它，所以发布前强制先选人
  if (isWhite && whiteUsers.value.length === 0 && status === 'ongoing') {
    proxy?.$modal.msgError('请先选择参加本场考试的考生，再发布');
    return;
  }

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
    // 关掉的开关不带出关联阈值，避免留下「不强制全屏但退出 3 次就交卷」这类脏数据
    antiCheatConfig: JSON.stringify({
      ...antiCheat,
      cameraInterval: antiCheat.camera === 1 ? Math.max(15, antiCheat.cameraInterval) : defaultAntiCheat.cameraInterval,
      maxExitFullscreen: antiCheat.fullScreen === 1 ? antiCheat.maxExitFullscreen : 0
    })
  };

  savingAction.value = status === 'ongoing' ? 'ongoing' : 'not_start';
  try {
    let id: string | number | undefined = form.id;
    if (id) {
      await updateExam(payload);
    } else {
      const res = await addExam(payload);
      // 雪花 ID 19 位超过 Number.MAX_SAFE_INTEGER，一律按字符串透传
      id = String(res.data);
      form.id = id;
    }
    // 白名单得先有考试ID才挂得上去，所以排在考试保存之后
    if (isWhite && id) {
      await saveExamWhiteUsers(id, whiteUserIds.value);
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
  await loadCertOptions();
  await initPage();
});
</script>

<style scoped lang="scss">
/* 步骤条上能点回去的那一步给个手型，否则看不出可以点 */
.exam-steps :deep(.el-step.is-back) {
  cursor: pointer;
}

/* 开关下方的灰字说明：规则不写清楚，老师会以为「部分得分」= 随便给分 */
.form-tip {
  width: 100%;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}
</style>
