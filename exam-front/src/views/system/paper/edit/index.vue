<template>
  <div class="p-[16px] pb-[70px]">
    <div class="flex items-center justify-between mb-[12px]">
      <div class="flex items-center gap-2">
        <el-button plain icon="Back" @click="goBack">返回</el-button>
        <span class="text-[16px] font-500">{{ isEdit ? '编辑试卷' : '新增试卷' }}</span>
        <el-tag size="small" effect="plain">{{ selectedList.length }} 题 / {{ totalScoreOfSelected }} 分</el-tag>
      </div>
      <div class="flex items-center gap-2">
        <el-button plain @click="submitForm('draft')">存为草稿</el-button>
        <el-button type="primary" :loading="buttonLoading" @click="submitForm('ready')">完成组卷</el-button>
      </div>
    </div>

    <el-steps :active="step" finish-status="success" align-center class="mb-[16px]">
      <el-step title="选择试题" description="随机抽题或手动勾选" />
      <el-step title="填写试卷信息" description="名称、分类、分值、判分规则" />
    </el-steps>

    <!-- 第一步：选题 -->
    <template v-if="step === 0">
      <el-card shadow="never" class="mb-[12px]">
        <el-tabs v-model="pickMode">
          <!-- 随机抽题 -->
          <el-tab-pane label="随机抽题" name="RANDOM">
            <el-alert type="info" :closable="false" class="mb-[12px]">
              <span class="text-[12px]">
                按「题库 + 题型 + 难度 + 数量」配置抽题规则，可添加多条。只会抽取<b>启用状态</b>的试题；同一条规则重复抽题会自动跳过已选中的题。
              </span>
            </el-alert>

            <el-table :data="randomRules" border size="small" class="mb-[12px]">
              <el-table-column label="序号" type="index" width="60" align="center" />
              <el-table-column label="题库" min-width="180">
                <template #default="{ row }">
                  <el-select v-model="row.bankId" filterable clearable placeholder="全部题库" class="w-full">
                    <el-option v-for="item in bankList" :key="item.id" :label="item.bankName" :value="item.id" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="题型" min-width="150">
                <template #default="{ row }">
                  <el-select v-model="row.questionType" clearable placeholder="不限题型" class="w-full">
                    <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="难度" min-width="130">
                <template #default="{ row }">
                  <el-select v-model="row.difficulty" clearable placeholder="不限难度" class="w-full">
                    <el-option v-for="item in questionDifficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="抽取数量" width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.count" :min="1" :max="200" :precision="0" controls-position="right" class="w-full" />
                </template>
              </el-table-column>
              <el-table-column label="单题分值" width="130">
                <template #default="{ row }">
                  <el-input-number v-model="row.score" :min="0" :precision="1" controls-position="right" class="w-full" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" icon="Delete" :disabled="randomRules.length <= 1" @click="removeRule($index)" />
                </template>
              </el-table-column>
            </el-table>

            <div class="flex items-center gap-2">
              <el-button plain icon="Plus" @click="addRule">添加规则</el-button>
              <el-button type="primary" :loading="picking" icon="MagicStick" @click="handleRandomPick">开始抽题</el-button>
              <el-button plain icon="Delete" :disabled="!selectedList.length" @click="clearSelected">清空已选</el-button>
            </div>
          </el-tab-pane>

          <!-- 手动选题 -->
          <el-tab-pane label="手动选题" name="MANUAL">
            <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="mb-[8px]">
              <el-form-item label="题库">
                <el-select v-model="queryParams.bankId" filterable clearable placeholder="全部题库" class="w-[180px]" @change="handleQuery">
                  <el-option v-for="item in bankList" :key="item.id" :label="item.bankName" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="题干">
                <el-input v-model="queryParams.title" placeholder="关键词" clearable class="w-[180px]" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="题型">
                <el-select v-model="queryParams.questionType" clearable placeholder="不限" class="w-[140px]" @change="handleQuery">
                  <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="难度">
                <el-select v-model="queryParams.difficulty" clearable placeholder="不限" class="w-[120px]" @change="handleQuery">
                  <el-option v-for="item in questionDifficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <el-alert type="success" :closable="false" class="mb-[8px]">
              <span class="text-[12px]">勾选后自动加入下方「已选试题」，<b>支持跨分页勾选</b>：翻页、改筛选条件后已勾选的题仍然保留。</span>
            </el-alert>

            <el-table
              ref="questionTableRef"
              v-loading="questionLoading"
              :data="questionList"
              border
              size="small"
              row-key="id"
              :reserve-selection="true"
              @select="handleSelect"
              @select-all="handleSelectAll"
            >
              <el-table-column type="selection" width="50" :reserve-selection="true" align="center" />
              <el-table-column label="题库" min-width="140" show-overflow-tooltip>
                <template #default="{ row }">
                  <span>{{ row.bankName || bankNameMap[row.bankId] || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="题干" min-width="300" show-overflow-tooltip>
                <template #default="{ row }">
                  <span>{{ plainText(row.title) || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="题型" width="110" align="center">
                <template #default="{ row }">
                  <el-tag size="small" effect="plain">{{ questionTypeLabel(row.questionType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="难度" width="90" align="center">
                <template #default="{ row }">
                  <el-tag size="small" :type="questionDifficultyTagType(row.difficulty)" effect="light">
                    {{ questionDifficultyLabel(row.difficulty) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="默认分值" width="90" align="center" prop="score" />
            </el-table>

            <pagination
              v-show="questionTotal > 0"
              :total="questionTotal"
              v-model:page="queryParams.pageNum"
              v-model:limit="queryParams.pageSize"
              @pagination="getQuestionList"
            />
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex items-center justify-between">
            <span>已选试题（{{ selectedList.length }} 题，合计 {{ totalScoreOfSelected }} 分）</span>
            <div class="flex items-center gap-2">
              <el-input-number
                v-model="form.defaultScore"
                :min="0"
                :precision="1"
                controls-position="right"
                size="small"
                class="w-[130px]"
                placeholder="单题分值"
              />
              <el-button size="small" plain @click="applyDefaultScore">按此分值批量应用</el-button>
              <el-button size="small" plain icon="Delete" :disabled="!selectedList.length" @click="clearSelected">清空</el-button>
            </div>
          </div>
        </template>

        <el-table :data="selectedList" border size="small" row-key="id">
          <el-table-column label="顺序" width="90" align="center">
            <template #default="{ $index }">
              <div class="flex items-center justify-center gap-1">
                <el-button link icon="Top" :disabled="$index === 0" @click="moveSelected($index, -1)" />
                <el-button link icon="Bottom" :disabled="$index === selectedList.length - 1" @click="moveSelected($index, 1)" />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="题库" width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.bankName || bankNameMap[row.bankId] || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="题干" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ plainText(row.title) || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="题型" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ questionTypeLabel(row.questionType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="难度" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="questionDifficultyTagType(row.difficulty)" effect="light">
                {{ questionDifficultyLabel(row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分值" width="130" align="center">
            <template #default="{ row }">
              <el-input-number v-model="row.paperScore" :min="0" :precision="1" controls-position="right" size="small" class="w-full" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" icon="Delete" @click="removeSelected($index)" />
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!selectedList.length" description="还没有选择试题" :image-size="70" />

        <div class="mt-[16px] flex justify-end">
          <el-button type="primary" :disabled="!selectedList.length" @click="step = 1"> 下一步：填写试卷信息 </el-button>
        </div>
      </el-card>
    </template>

    <!-- 第二步：填写试卷信息 -->
    <template v-else>
      <el-card shadow="never" class="mb-[12px]">
        <template #header><span>基础信息</span></template>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="试卷名称" prop="paperName">
                <el-input v-model="form.paperName" placeholder="请输入试卷名称" maxlength="100" show-word-limit />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="试卷分类" prop="category">
                <el-select v-model="form.category" filterable clearable placeholder="请选择试卷分类" class="w-full">
                  <el-option v-for="item in paperCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="试卷总分" prop="totalScore">
                <div class="w-full">
                  <el-input-number v-model="form.totalScore" :min="0" :precision="1" controls-position="right" class="w-full" />
                  <div class="mt-1 flex items-center text-xs text-gray-400">
                    <span>已选题目合计 {{ totalScoreOfSelected }} 分 / {{ selectedList.length }} 题</span>
                    <el-button link type="primary" class="ml-2" @click="calcTotalScore">按题目自动计算</el-button>
                  </div>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="及格分数" prop="passScore">
                <el-input-number v-model="form.passScore" :min="0" :precision="1" controls-position="right" class="w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="默认单题分值" prop="defaultScore">
                <el-input-number v-model="form.defaultScore" :min="0" :precision="1" controls-position="right" class="w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="考试时长" prop="timeLimit">
                <el-input-number v-model="form.timeLimit" :min="0" :precision="0" controls-position="right" class="w-full" />
                <span class="ml-2 text-xs text-gray-400">分钟，0 表示不限时</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="试卷描述" prop="paperDesc">
            <el-input v-model="form.paperDesc" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入试卷描述" />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="mb-[12px]">
        <template #header><span>考试与判分规则</span></template>
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="flex items-center justify-between py-[6px]">
              <span>题目乱序</span>
              <el-switch v-model="form.questionShuffle" active-value="1" inactive-value="0" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="flex items-center justify-between py-[6px]">
              <span>选项乱序</span>
              <el-switch v-model="form.optionShuffle" active-value="1" inactive-value="0" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="flex items-center justify-between py-[6px]">
              <span>客观题自动判分</span>
              <el-switch v-model="form.autoJudge" active-value="1" inactive-value="0" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="flex items-center justify-between py-[6px]">
              <span>主观题人工阅卷</span>
              <el-switch v-model="form.manualReview" active-value="1" inactive-value="0" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="flex items-center justify-between py-[6px]">
              <span>部分得分</span>
              <el-switch v-model="form.partialScore" active-value="1" inactive-value="0" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="flex items-center justify-between py-[6px]">
              <span>答错扣分</span>
              <el-switch v-model="form.wrongDeduct" active-value="1" inactive-value="0" />
            </div>
          </el-col>
        </el-row>
        <el-divider class="my-[10px]" />
        <el-form label-width="120px">
          <el-form-item label="试卷可见范围">
            <el-radio-group v-model="form.shareScope">
              <el-radio value="SELF">仅自己可编辑</el-radio>
              <el-radio value="SHARED">共享给其他管理员</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="flex justify-between">
        <el-button plain icon="Back" @click="step = 0">上一步：重新选题</el-button>
        <el-button type="primary" :loading="buttonLoading" @click="submitForm('ready')">完成组卷</el-button>
      </div>
    </template>
  </div>
</template>

<script setup name="PaperEdit" lang="ts">
import { useRoute, useRouter } from 'vue-router';
import type { FormRules } from 'element-plus';
import { getPaper, savePaper } from '@/api/system/paper';
import { PaperForm, PaperQuestionItem } from '@/api/system/paper/types';
import { listQuestion, randomQuestion, listQuestionByIds } from '@/api/system/question';
import { QuestionVO, QuestionQuery } from '@/api/system/question/types';
import { listBank } from '@/api/system/bank';
import { BankVO } from '@/api/system/bank/types';
import { useQuestionDicts } from '@/views/system/question/useQuestionDict';

/** 列表页路由地址，需要与后台「试卷管理」菜单的路由地址保持一致 */
const PAPER_LIST_PATH = '/system/paper';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();
const router = useRouter();

// 题型 / 难度统一走字典
const { questionTypeOptions, questionDifficultyOptions, questionTypeLabel, questionDifficultyLabel, questionDifficultyTagType } = useQuestionDicts();
// 试卷分类字典
const { paper_category } = toRefs<any>(proxy?.useDict('paper_category'));
const paperCategoryOptions = computed<Array<{ label: string; value: string }>>(() => paper_category?.value ?? []);

/** 路由上的试卷ID，为空表示新增 */
const paperId = computed<string | undefined>(() => (route.params.paperId as string) || undefined);
const isEdit = computed(() => !!paperId.value);

const step = ref(0);
const buttonLoading = ref(false);
const picking = ref(false);
const questionLoading = ref(false);

/** 选题方式：RANDOM 随机抽题 / MANUAL 手动选题 */
const pickMode = ref<'RANDOM' | 'MANUAL'>('MANUAL');

const formRef = ref<ElFormInstance>();
const queryFormRef = ref<ElFormInstance>();
const questionTableRef = ref<ElTableInstance>();

/* ---------------------------------- 表单数据 ---------------------------------- */

const initFormData: PaperForm = {
  id: undefined,
  paperName: undefined,
  paperDesc: undefined,
  paperType: 'MANUAL',
  totalScore: 0,
  passScore: 0,
  timeLimit: 0,
  visibility: 'private',
  status: 'draft',
  category: undefined,
  defaultScore: 5,
  questionShuffle: '0',
  optionShuffle: '0',
  autoJudge: '1',
  manualReview: '1',
  partialScore: '0',
  wrongDeduct: '0',
  shareScope: 'SELF',
  questions: []
};

const form = reactive<PaperForm>({ ...initFormData });

const rules: FormRules = {
  paperName: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  totalScore: [{ required: true, message: '请输入试卷总分', trigger: 'blur' }]
};

/* ---------------------------------- 已选试题 ---------------------------------- */

interface SelectedQuestion {
  id: string | number;
  bankId?: string | number;
  bankName?: string;
  title?: string;
  questionType?: string;
  difficulty?: string;
  paperScore: number;
}

const selectedList = ref<SelectedQuestion[]>([]);

const totalScoreOfSelected = computed(() => Number(selectedList.value.reduce((sum, item) => sum + (Number(item.paperScore) || 0), 0).toFixed(2)));

const toSelected = (question: QuestionVO, score?: number): SelectedQuestion => ({
  id: question.id,
  bankId: question.bankId,
  bankName: question.bankName,
  title: question.title,
  questionType: question.questionType,
  difficulty: question.difficulty,
  paperScore: Number(score ?? question.score ?? form.defaultScore ?? 0)
});

const addSelected = (question: QuestionVO, score?: number) => {
  if (selectedList.value.some((item) => String(item.id) === String(question.id))) return;
  selectedList.value.push(toSelected(question, score));
};

const removeSelected = (index: number) => {
  const [removed] = selectedList.value.splice(index, 1);
  if (removed) {
    manualSelected.value.delete(String(removed.id));
    questionTableRef.value?.clearSelection();
    syncTableSelection();
  }
};

const moveSelected = (index: number, offset: number) => {
  const target = index + offset;
  if (target < 0 || target >= selectedList.value.length) return;
  const rows = selectedList.value;
  [rows[index], rows[target]] = [rows[target], rows[index]];
};

const clearSelected = () => {
  selectedList.value = [];
  manualSelected.value.clear();
  questionTableRef.value?.clearSelection();
};

/** 把「默认单题分值」批量应用到所有已选题 */
const applyDefaultScore = () => {
  const score = Number(form.defaultScore ?? 0);
  selectedList.value.forEach((item) => {
    item.paperScore = score;
  });
};

/** 最近一次自动算出的总分，用来判断用户是否手工改过 */
const lastAutoTotal = ref<number | undefined>(undefined);

const calcTotalScore = () => {
  form.totalScore = totalScoreOfSelected.value;
  lastAutoTotal.value = form.totalScore;
};

/**
 * 新增试卷进入第二步时，总分默认按已选题目自动计算。
 * 用户手工改过（当前值已不等于上次自动值）就不覆盖；回填编辑页保留库里的原值。
 */
watch(step, (val) => {
  if (val !== 1 || isEdit.value) return;
  const current = form.totalScore;
  if (current === undefined || current === null || current === 0 || current === lastAutoTotal.value) {
    calcTotalScore();
  }
});

/* ---------------------------------- 随机抽题 ---------------------------------- */

interface RandomRule {
  bankId?: string | number;
  questionType?: string;
  difficulty?: string;
  count: number;
  score?: number;
}

const randomRules = ref<RandomRule[]>([{ bankId: undefined, questionType: undefined, difficulty: undefined, count: 10, score: undefined }]);

const addRule = () => {
  randomRules.value.push({ bankId: undefined, questionType: undefined, difficulty: undefined, count: 10, score: undefined });
};

const removeRule = (index: number) => {
  randomRules.value.splice(index, 1);
};

const handleRandomPick = async () => {
  const invalid = randomRules.value.findIndex((rule) => !rule.count || rule.count <= 0);
  if (invalid !== -1) {
    proxy?.$modal.msgError(`第 ${invalid + 1} 条规则的抽取数量不合法`);
    return;
  }
  picking.value = true;
  try {
    let added = 0;
    const shortages: string[] = [];
    for (const rule of randomRules.value) {
      // 排除已选中的题，保证同一份试卷里不出现重复试题
      const excludeIds = selectedList.value.map((item) => item.id);
      const res = await randomQuestion({
        bankId: rule.bankId,
        questionType: rule.questionType,
        difficulty: rule.difficulty,
        count: rule.count,
        excludeIds
      });
      const picked = (res.data ?? []) as QuestionVO[];
      picked.forEach((question) => addSelected(question, rule.score));
      added += picked.length;
      if (picked.length < rule.count) {
        shortages.push(`第 ${randomRules.value.indexOf(rule) + 1} 条规则可选题不足，需要 ${rule.count} 道，实际抽到 ${picked.length} 道`);
      }
    }
    if (shortages.length) {
      proxy?.$modal.msgWarning(shortages.join('；'));
    } else {
      proxy?.$modal.msgSuccess(`已抽中 ${added} 道试题`);
    }
  } finally {
    picking.value = false;
  }
};

/* ---------------------------------- 手动选题 ---------------------------------- */

const questionList = ref<QuestionVO[]>([]);
const questionTotal = ref(0);
const bankList = ref<BankVO[]>([]);

/** 手动勾选的试题（跨分页累积），key 为试题ID */
const manualSelected = ref<Map<string, QuestionVO>>(new Map());

const queryParams = ref<QuestionQuery>({
  pageNum: 1,
  pageSize: 10,
  bankId: undefined,
  title: undefined,
  questionType: undefined,
  difficulty: undefined,
  params: {}
});

const bankNameMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {};
  bankList.value.forEach((item) => {
    map[String(item.id)] = item.bankName;
  });
  return map;
});

/** 富文本转纯文本，用于列表展示 */
const plainText = (html?: string): string => {
  if (!html) return '';
  return html
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim();
};

const getQuestionList = async () => {
  questionLoading.value = true;
  try {
    const res = await listQuestion(queryParams.value);
    questionList.value = res.rows ?? [];
    questionTotal.value = res.total ?? 0;
  } catch {
    questionList.value = [];
    questionTotal.value = 0;
  } finally {
    questionLoading.value = false;
    // 数据刷新后把已勾选的行重新勾上（跨分页记忆）
    syncTableSelection();
  }
};

/** 把 manualSelected 里属于当前页的行置为选中态 */
const syncTableSelection = () => {
  nextTick(() => {
    questionList.value.forEach((row) => {
      if (manualSelected.value.has(String(row.id))) {
        questionTableRef.value?.toggleRowSelection(row, true);
      }
    });
  });
};

/** 单行勾选 / 取消 */
const handleSelect = (selection: QuestionVO[], row: QuestionVO) => {
  const key = String(row.id);
  const checked = selection.some((item) => String(item.id) === key);
  if (checked) {
    manualSelected.value.set(key, row);
    addSelected(row);
  } else {
    manualSelected.value.delete(key);
    const index = selectedList.value.findIndex((item) => String(item.id) === key);
    if (index !== -1) selectedList.value.splice(index, 1);
  }
};

/** 全选 / 取消全选（只作用于当前页） */
const handleSelectAll = (selection: QuestionVO[]) => {
  // 开启跨分页记忆后 selection 里还带着其他页的选中行，
  // 不能用 selection.length 判断，要看当前页的行是不是全在里面
  const allChecked = questionList.value.every((row) => selection.some((item) => String(item.id) === String(row.id)));
  questionList.value.forEach((row) => {
    const id = String(row.id);
    if (allChecked) {
      manualSelected.value.set(id, row);
      addSelected(row);
    } else {
      manualSelected.value.delete(id);
      const index = selectedList.value.findIndex((item) => String(item.id) === id);
      if (index !== -1) selectedList.value.splice(index, 1);
    }
  });
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getQuestionList();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

/* ----------------------------------- 数据回显 ---------------------------------- */

const loadBankList = async () => {
  try {
    const res = await listBank({ pageNum: 1, pageSize: 500 });
    bankList.value = res.rows ?? [];
  } catch {
    bankList.value = [];
  }
};

const initPage = async () => {
  Object.assign(form, { ...initFormData });
  selectedList.value = [];
  manualSelected.value.clear();
  randomRules.value = [{ bankId: undefined, questionType: undefined, difficulty: undefined, count: 10, score: undefined }];
  step.value = 0;
  formRef.value?.clearValidate();

  const id = paperId.value;
  if (!id) {
    await getQuestionList();
    return;
  }
  const res = await getPaper(id);
  const data = res.data;
  Object.assign(form, {
    id: data.id,
    paperName: data.paperName,
    paperDesc: data.paperDesc,
    paperType: data.paperType ?? 'MANUAL',
    totalScore: data.totalScore ?? 0,
    passScore: data.passScore ?? 0,
    timeLimit: data.timeLimit ?? 0,
    visibility: data.visibility ?? 'private',
    sharePassword: data.sharePassword,
    shareExpireTime: data.shareExpireTime,
    status: data.status ?? 'draft',
    category: data.category,
    defaultScore: data.defaultScore ?? 0,
    questionShuffle: data.questionShuffle ?? '0',
    optionShuffle: data.optionShuffle ?? '0',
    autoJudge: data.autoJudge ?? '1',
    manualReview: data.manualReview ?? '1',
    partialScore: data.partialScore ?? '0',
    wrongDeduct: data.wrongDeduct ?? '0',
    shareScope: data.shareScope ?? 'SELF'
  });

  // 抽题规则回显，方便在原规则基础上重新抽题
  if (data.randomRule) {
    try {
      const parsed = JSON.parse(data.randomRule);
      if (Array.isArray(parsed) && parsed.length) {
        randomRules.value = parsed.map((item: RandomRule) => ({
          bankId: item.bankId,
          questionType: item.questionType,
          difficulty: item.difficulty,
          count: Number(item.count) || 10,
          score: item.score
        }));
      }
    } catch {
      // 规则不是合法 JSON 时忽略，用默认规则
    }
  }

  // 已选试题：先拿到明细，再批量查题干
  pickMode.value = data.paperType === 'RANDOM' ? 'RANDOM' : 'MANUAL';
  const items = (data.questions ?? []) as PaperQuestionItem[];
  const idList = items.map((item) => item.questionId).filter((id): id is string | number => id !== undefined && id !== null);
  if (idList.length) {
    const questionRes = await listQuestionByIds(idList);
    const questions = (questionRes.data ?? []) as QuestionVO[];
    // 明细里携带的分值优先
    const scoreMap = new Map<string | number, number>();
    items.forEach((item) => {
      if (item.questionId !== undefined) scoreMap.set(item.questionId, Number(item.paperScore ?? 0));
    });
    questions.forEach((question) => {
      addSelected(question, scoreMap.get(question.id));
      manualSelected.value.set(String(question.id), question);
    });
  }
  await getQuestionList();
};

/* ------------------------------------ 提交 ------------------------------------ */

const validateForm = async (): Promise<boolean> => {
  if (!formRef.value) return true;
  try {
    await formRef.value.validate();
    return true;
  } catch {
    return false;
  }
};

const submitForm = async (status: string) => {
  // 还在选题步骤时，先切到填信息步骤（试卷名称必填，必须先填）
  if (step.value === 0) {
    if (!selectedList.value.length) {
      proxy?.$modal.msgError('请先选择试题');
      return;
    }
    step.value = 1;
    proxy?.$modal.msgWarning('请填写试卷名称等信息后再保存');
    return;
  }
  if (!form.paperName) {
    proxy?.$modal.msgError('请输入试卷名称');
    return;
  }
  if (!selectedList.value.length) {
    proxy?.$modal.msgError('请至少选择一道试题');
    return;
  }
  if (!(await validateForm())) return;

  const questions: PaperQuestionItem[] = selectedList.value.map((item) => ({
    questionId: item.id,
    paperScore: Number(item.paperScore) || 0
  }));

  const payload: PaperForm = {
    ...form,
    // 选题方式由最后使用的 Tab 决定
    paperType: pickMode.value,
    paperName: form.paperName,
    status,
    totalScore: form.totalScore ?? totalScoreOfSelected.value,
    defaultScore: form.defaultScore ?? 0,
    // 随机抽题把规则一并存下来，便于下次在原规则上重抽
    randomRule: pickMode.value === 'RANDOM' ? JSON.stringify(randomRules.value) : undefined,
    questions
  };

  buttonLoading.value = true;
  try {
    await savePaper(payload);
    proxy?.$modal.msgSuccess(status === 'ready' ? '组卷完成' : '已存为草稿');
    goBack();
  } finally {
    buttonLoading.value = false;
  }
};

const goBack = () => {
  const historyState = window.history.state as { back?: string } | null;
  const from = historyState?.back;
  if (from && !from.includes('/paper/edit')) {
    proxy?.$tab.closePage(router.currentRoute.value);
    router.back();
    return;
  }
  proxy?.$tab.closeOpenPage({ path: PAPER_LIST_PATH });
};

watch(pickMode, (mode) => {
  if (mode === 'MANUAL' && !questionList.value.length) {
    getQuestionList();
  }
});

onMounted(async () => {
  await loadBankList();
  await initPage();
});
</script>
