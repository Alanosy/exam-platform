<template>
  <el-drawer v-model="visible" title="AI 出题" size="60%" :close-on-click-modal="false" append-to-body>
    <div v-loading="generating" class="ai-gen">
      <!-- 生成条件 -->
      <el-form :model="form" label-width="88px" class="gen-form">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="目标题库">
              <el-select v-model="form.bankId" filterable clearable placeholder="保存到哪个题库" class="w-full">
                <el-option v-for="item in bankList" :key="item.id" :label="item.bankName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="题型">
              <el-select v-model="form.questionType" class="w-full">
                <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" class="w-full">
                <el-option v-for="item in questionDifficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="知识点">
              <!--
                值取知识点「名称」：AI 出题入参 knowledge_points 是名称数组；
                保存试题时再按名称反查成 ID 写关联表。章节只作分组（disabled）
              -->
              <el-tree-select
                v-model="form.knowledgePoints"
                :data="knowledgeTree"
                :props="{ value: 'name', label: 'name', children: 'children', disabled: 'disabled' } as any"
                value-key="name"
                multiple
                show-checkbox
                check-strictly
                filterable
                collapse-tags
                collapse-tags-tooltip
                placeholder="从知识点树选择，没有的请先到「知识点管理」建"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="数量">
              <el-input-number v-model="form.count" :min="1" :max="30" controls-position="right" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="分值">
              <el-input-number v-model="form.score" :min="0.5" :max="100" :step="0.5" controls-position="right" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="自动质检">
              <el-switch v-model="form.withAudit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="附加要求">
          <el-input
            v-model="form.extra"
            type="textarea"
            :rows="2"
            maxlength="300"
            show-word-limit
            placeholder="例如：结合生产事故场景、不要出现计算题、选项避免『以上都不对』"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="MagicStick" :loading="generating" @click="handleGenerate">生成</el-button>
          <span class="tip-text">AI 生成的题需人工核对后再启用，尤其是选择题的正确项；知识点库里没有的名称不会写入关联</span>
        </el-form-item>
      </el-form>

      <!-- 生成结果 -->
      <div v-if="list.length > 0" class="gen-result">
        <div class="result-head">
          <span>共生成 {{ list.length }} 道题，已选 {{ selectedIds.length }} 道</span>
          <div class="flex items-center gap-2">
            <!-- 入库状态：后端 QuestionBo.status 是必填项（@NotNull），不传直接报「0草稿 1启用 2废弃不能为空」 -->
            <span class="text-[12px] text-[#909399]">保存状态</span>
            <el-select v-model="form.status" size="small" class="w-[110px]">
              <el-option v-for="item in questionStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button link type="primary" @click="selectAll(true)">全选</el-button>
            <el-button link @click="selectAll(false)">清空</el-button>
          </div>
        </div>

        <div v-for="(item, index) in list" :key="index" class="result-item" :class="{ 'is-checked': item.checked }">
          <div class="item-head">
            <el-checkbox v-model="item.checked" />
            <span class="item-index">第 {{ index + 1 }} 题</span>
            <el-tag size="small" effect="plain" type="primary">{{ questionTypeLabel(item.questionType) }}</el-tag>
            <el-tag size="small" effect="light" :type="questionDifficultyTagType(item.difficulty)">
              {{ questionDifficultyLabel(item.difficulty || 'medium') }}
            </el-tag>
            <!-- AI 自己标的知识点，保存到题库时会按名称匹配到知识点库的 ID -->
            <el-tag v-for="name in item.knowledgePoints ?? []" :key="name" size="small" effect="plain">{{ name }}</el-tag>
            <!-- 质检结论：fatal 直接红标，教师一眼看到哪道题不能用 -->
            <el-tag v-if="item.audit" size="small" effect="dark" :type="item.audit.passed ? 'success' : 'danger'">
              质检 {{ item.audit.qualityScore ?? '-' }} 分
            </el-tag>
            <el-button link type="danger" class="ml-auto" @click="removeItem(index)">移除</el-button>
          </div>

          <el-input v-model="item.stem" type="textarea" :rows="2" placeholder="题干" class="mb-2" />

          <div v-if="item.options && item.options.length > 0" class="option-box">
            <div v-for="opt in item.options" :key="opt.key" class="option-row">
              <span class="option-key">{{ opt.key }}</span>
              <el-input v-model="opt.content" size="small" />
            </div>
          </div>

          <div class="answer-row">
            <span class="answer-label">答案</span>
            <el-input v-model="item.answer" size="small" placeholder='字符串形式的 JSON，如 {"rightKeys":["A"]}' />
          </div>
          <el-input v-model="item.analysis" type="textarea" :rows="2" placeholder="解析（可选）" class="mt-2" />

          <el-alert v-if="item.audit && item.audit.issues && item.audit.issues.length > 0" :type="item.audit.passed ? 'warning' : 'error'" :closable="false" class="mt-2">
            <div class="text-[12px] leading-[18px]">
              <div v-for="(issue, i) in item.audit.issues" :key="i">
                <b>[{{ issue.level }}]</b> {{ issue.detail }}
                <span v-if="issue.suggestion" class="text-[#909399]">→ {{ issue.suggestion }}</span>
              </div>
            </div>
          </el-alert>
        </div>
      </div>

      <el-empty v-else-if="!generating" description="填好条件后点「生成」，AI 会给出题干、选项、答案与解析" />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="saving" :disabled="selectedIds.length === 0" @click="handleSave">
          保存选中的 {{ selectedIds.length }} 道题
        </el-button>
        <el-button @click="visible = false">关 闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts" name="QuestionAiGenerateDrawer">
import { aiGenerateQuestions, getAiEnabled } from '@/api/system/ai';
import type { AiQuestionGenVO, AiQuestionAuditVO } from '@/api/system/ai/types';
// 必须走 createQuestion（/question/create）：addQuestion（/question）走 insertByBo，
// 不写选项、不填 create_user、也不兜底 status
import { createQuestion } from '@/api/system/question';
import type { QuestionForm } from '@/api/system/question/types';
import { treeKnowledge } from '@/api/system/knowledge';
import type { KnowledgePointVO } from '@/api/system/knowledge/types';
import { BankVO } from '@/api/system/bank/types';
import { useQuestionDicts } from './useQuestionDict';

/** 列表项 = AI 生成的题 + 勾选状态 + 质检结论 */
interface GenItem extends AiQuestionGenVO {
  checked: boolean;
  audit?: AiQuestionAuditVO;
}

const props = defineProps<{
  modelValue: boolean;
  bankList: BankVO[];
  defaultBankId?: string | number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'saved'): void;
}>();

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { questionTypeOptions, questionDifficultyOptions, questionStatusOptions, questionTypeLabel, questionDifficultyLabel, questionDifficultyTagType } =
  useQuestionDicts();

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v)
});

const generating = ref(false);
const saving = ref(false);
const list = ref<GenItem[]>([]);
const aiEnabled = ref(false);

/** 知识点树：章节只作分组（disabled），勾选的是其下的知识点 */
const knowledgeTree = ref<KnowledgePointVO[]>([]);
/** 知识点名称 → ID，保存试题时用它把 AI 给的名称翻译成关联表要的 ID */
const knowledgeNameToId = ref<Record<string, string | number>>({});

const loadKnowledgeTree = async () => {
  try {
    const res = await treeKnowledge();
    const tree = res.data ?? [];
    const map: Record<string, string | number> = {};
    knowledgeTree.value = tree.map((chapter) => ({
      ...chapter,
      disabled: true,
      children: (chapter.children ?? []).map((point) => {
        map[point.name] = point.id;
        return { ...point };
      })
    }));
    knowledgeNameToId.value = map;
  } catch {
    knowledgeTree.value = [];
  }
};

/** 知识点名称数组 → ID 数组，认不出的名称直接丢掉（AI 可能自造知识点） */
const toKnowledgeIds = (names?: string[]): Array<string | number> =>
  (names ?? []).map((name) => knowledgeNameToId.value[name]).filter((id) => id !== undefined);

const form = reactive({
  bankId: undefined as string | number | undefined,
  questionType: 'SINGLE',
  difficulty: 'medium',
  knowledgePoints: [] as string[],
  count: 5,
  score: 5,
  withAudit: true,
  extra: '',
  /**
   * 入库状态，后端 QuestionBo.status 必填（@NotNull）
   *
   * 默认草稿：AI 出题必须人工核对答案后才启用，直接启用等于把未核对的题放进题库
   */
  status: 'draft' as string | number
});

const selectedIds = computed(() => list.value.filter((item) => item.checked).map((_, index) => index));
const selectedCount = computed(() => list.value.filter((item) => item.checked).length);

const selectAll = (checked: boolean) => {
  list.value.forEach((item) => (item.checked = checked));
};

const removeItem = (index: number) => {
  list.value.splice(index, 1);
};

/** 打开时探测一次 AI 可用性，不可用直接在生成时给出明确提示 */
const checkEnabled = async () => {
  try {
    const res = await getAiEnabled();
    aiEnabled.value = res.data?.enabled === true;
  } catch {
    aiEnabled.value = false;
  }
};

const handleGenerate = async () => {
  if (!aiEnabled.value) {
    await checkEnabled();
    if (!aiEnabled.value) {
      proxy?.$modal.msgError('AI 服务不可用，请检查 ruoyi-exam-agent 是否已启动');
      return;
    }
  }
  generating.value = true;
  try {
    // withAudit 交给 AI 侧做 Generate-Critique：没过质检的题不进结果列表
    const res = await aiGenerateQuestions({
      questionType: form.questionType,
      difficulty: form.difficulty,
      knowledgePoints: form.knowledgePoints,
      count: form.count,
      score: form.score,
      extra: form.extra,
      withAudit: form.withAudit
    });
    const rows = (res.data ?? []) as AiQuestionGenVO[];
    list.value = rows.map((row) => ({ ...row, checked: true }));
    if (rows.length === 0) {
      proxy?.$modal.msgWarning('AI 没有返回可用题目，可换个知识点或降低数量重试');
    }
  } catch {
    // 失败提示由全局拦截器统一弹出
  } finally {
    generating.value = false;
  }
};

const handleSave = async () => {
  if (!form.bankId) {
    proxy?.$modal.msgWarning('请先选择要保存到的题库');
    return;
  }
  const targets = list.value.filter((item) => item.checked);
  if (targets.length === 0) return;

  saving.value = true;
  let ok = 0;
  const errors: string[] = [];
  for (const [i, item] of targets.entries()) {
    if (!item.stem || !item.stem.trim()) {
      errors.push(`第 ${i + 1} 题题干为空`);
      continue;
    }
    const payload: QuestionForm = {
      bankId: form.bankId,
      title: item.stem,
      // AI 没给题型 / 难度时回落到生成条件里选的值，后端这两个字段必填
      questionType: item.questionType || form.questionType,
      difficulty: item.difficulty || form.difficulty,
      score: item.score ?? form.score,
      analysis: item.analysis,
      answer: item.answer,
      // 后端必填，不传会报「0草稿 1启用 2废弃不能为空」
      status: form.status,
      // AI 自己给的知识点优先，没有就用在生成条件里选的那批；名称翻译成 ID
      knowledgeIds: toKnowledgeIds(item.knowledgePoints?.length ? item.knowledgePoints : form.knowledgePoints),
      // 选项字段与题库实体一致（optionKey / optionContent），AI 给的 key/content 在这里对齐
      options: (item.options ?? []).map((opt, idx) => ({ optionKey: opt.key, optionContent: opt.content, sort: idx + 1 }))
    };
    try {
      await createQuestion(payload);
      ok++;
    } catch (e) {
      // 单题失败不中断，最后按成功数提示，教师能看到「保存了几道」
      const err = e as { msg?: string; message?: string };
      errors.push(err?.msg || err?.message || `第 ${i + 1} 题保存失败`);
    }
  }
  saving.value = false;
  if (ok > 0) {
    proxy?.$modal.msgSuccess(`已保存 ${ok} 道题`);
    emit('saved');
    // 保存成功的题从结果里移除，避免重复保存
    list.value = list.value.filter((item) => !item.checked);
    if (list.value.length === 0) {
      visible.value = false;
    }
  } else {
    // 后端校验失败的第一条原因带上，避免只看到「保存失败」不知道缺什么
    proxy?.$modal.msgError(`保存失败：${errors[0] ?? '请检查题目内容是否完整'}`);
  }
};

watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      if (props.defaultBankId !== undefined && !form.bankId) {
        form.bankId = props.defaultBankId;
      }
      loadKnowledgeTree();
      checkEnabled();
    }
  }
);

// 状态字典是异步返回的，加载完成后要确认默认值命中可选项，否则下拉会「选了个空值」
watch(questionStatusOptions, () => {
  const hit = questionStatusOptions.value.some((item) => String(item.value) === String(form.status));
  if (!hit && questionStatusOptions.value.length > 0) {
    form.status = questionStatusOptions.value[0].value;
  }
});
</script>

<style scoped lang="scss">
.ai-gen {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.gen-form {
  padding: 12px;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.tip-text {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.gen-result {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
}

.result-item {
  padding: 12px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;

  &.is-checked {
    border-color: #b3d8ff;
    background: #fafcff;
  }
}

.item-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.item-index {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.option-box {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 8px;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-key {
  width: 24px;
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
}

.answer-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.answer-label {
  width: 40px;
  flex-shrink: 0;
  font-size: 13px;
  color: #909399;
}
</style>
