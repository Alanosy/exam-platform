<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="试卷名称" prop="paperName">
              <el-input v-model="queryParams.paperName" placeholder="请输入试卷名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="试卷描述" prop="paperDesc">
              <el-input v-model="queryParams.paperDesc" placeholder="请输入试卷描述" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="试卷总分" prop="totalScore">
              <el-input v-model="queryParams.totalScore" placeholder="请输入试卷总分" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="及格分数" prop="passScore">
              <el-input v-model="queryParams.passScore" placeholder="请输入及格分数" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="考试时长(分钟)，0代表不限时" prop="timeLimit">
              <el-input v-model="queryParams.timeLimit" placeholder="请输入考试时长(分钟)，0代表不限时" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="可见性 private私有 / public公开" prop="visibility">
              <el-input v-model="queryParams.visibility" placeholder="请输入可见性 private私有 / public公开" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="公开分享密码，公开模式生效，空则无密码" prop="sharePassword">
              <el-input v-model="queryParams.sharePassword" placeholder="请输入公开分享密码，公开模式生效，空则无密码" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="分享链接过期时间，NULL永久有效" prop="shareExpireTime">
              <el-date-picker clearable
                v-model="queryParams.shareExpireTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择分享链接过期时间，NULL永久有效"
              />
            </el-form-item>
            <el-form-item label="创建人ID" prop="creatorId">
              <el-input v-model="queryParams.creatorId" placeholder="请输入创建人ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:paper:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:paper:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:paper:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:paper:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="paperList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="试卷主键ID" align="center" prop="id" v-if="true" />
        <el-table-column label="试卷名称" align="center" prop="paperName" />
        <el-table-column label="试卷描述" align="center" prop="paperDesc" />
        <el-table-column label="组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)" align="center" prop="paperType" />
        <el-table-column label="试卷总分" align="center" prop="totalScore" />
        <el-table-column label="及格分数" align="center" prop="passScore" />
        <el-table-column label="考试时长(分钟)，0代表不限时" align="center" prop="timeLimit" />
        <el-table-column label="可见性 private私有 / public公开" align="center" prop="visibility" />
        <el-table-column label="公开分享密码，公开模式生效，空则无密码" align="center" prop="sharePassword" />
        <el-table-column label="分享链接过期时间，NULL永久有效" align="center" prop="shareExpireTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.shareExpireTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}" align="center" prop="randomRule" />
        <el-table-column label="draft草稿 / ready已组卷 / archived归档" align="center" prop="status" />
        <el-table-column label="创建人ID" align="center" prop="creatorId" />
        <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:paper:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:paper:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改试卷主对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="paperFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="试卷名称" prop="paperName">
          <el-input v-model="form.paperName" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="试卷描述" prop="paperDesc">
            <el-input v-model="form.paperDesc" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="试卷总分" prop="totalScore">
          <el-input v-model="form.totalScore" placeholder="请输入试卷总分" />
        </el-form-item>
        <el-form-item label="及格分数" prop="passScore">
          <el-input v-model="form.passScore" placeholder="请输入及格分数" />
        </el-form-item>
        <el-form-item label="考试时长(分钟)，0代表不限时" prop="timeLimit">
          <el-input v-model="form.timeLimit" placeholder="请输入考试时长(分钟)，0代表不限时" />
        </el-form-item>
        <el-form-item label="可见性 private私有 / public公开" prop="visibility">
          <el-input v-model="form.visibility" placeholder="请输入可见性 private私有 / public公开" />
        </el-form-item>
        <el-form-item label="公开分享密码，公开模式生效，空则无密码" prop="sharePassword">
          <el-input v-model="form.sharePassword" placeholder="请输入公开分享密码，公开模式生效，空则无密码" />
        </el-form-item>
        <el-form-item label="分享链接过期时间，NULL永久有效" prop="shareExpireTime">
          <el-date-picker clearable
            v-model="form.shareExpireTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择分享链接过期时间，NULL永久有效">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="创建人ID" prop="creatorId">
          <el-input v-model="form.creatorId" placeholder="请输入创建人ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Paper" lang="ts">
import { listPaper, getPaper, delPaper, addPaper, updatePaper } from '@/api/system/paper';
import { PaperVO, PaperQuery, PaperForm } from '@/api/system/paper/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const paperList = ref<PaperVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const paperFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: PaperForm = {
  id: undefined,
  paperName: undefined,
  paperDesc: undefined,
  paperType: undefined,
  totalScore: undefined,
  passScore: undefined,
  timeLimit: undefined,
  visibility: undefined,
  sharePassword: undefined,
  shareExpireTime: undefined,
  randomRule: undefined,
  status: undefined,
  creatorId: undefined,
}
const data = reactive<PageData<PaperForm, PaperQuery>>({
  form: {...initFormData},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    paperName: undefined,
    paperDesc: undefined,
    paperType: undefined,
    totalScore: undefined,
    passScore: undefined,
    timeLimit: undefined,
    visibility: undefined,
    sharePassword: undefined,
    shareExpireTime: undefined,
    randomRule: undefined,
    status: undefined,
    creatorId: undefined,
    params: {
    }
  },
  rules: {
    id: [
      { required: true, message: "试卷主键ID不能为空", trigger: "blur" }
    ],
    paperName: [
      { required: true, message: "试卷名称不能为空", trigger: "blur" }
    ],
    paperType: [
      { required: true, message: "组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)不能为空", trigger: "change" }
    ],
    visibility: [
      { required: true, message: "可见性 private私有 / public公开不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "draft草稿 / ready已组卷 / archived归档不能为空", trigger: "change" }
    ],
    creatorId: [
      { required: true, message: "创建人ID不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询试卷主列表 */
const getList = async () => {
  loading.value = true;
  const res = await listPaper(queryParams.value);
  paperList.value = res.rows;
  total.value = res.total;
  loading.value = false;
}

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
}

/** 表单重置 */
const reset = () => {
  form.value = {...initFormData};
  paperFormRef.value?.resetFields();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: PaperVO[]) => {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = "添加试卷主";
}

/** 修改按钮操作 */
const handleUpdate = async (row?: PaperVO) => {
  reset();
  const _id = row?.id || ids.value[0]
  const res = await getPaper(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "修改试卷主";
}

/** 提交按钮 */
const submitForm = () => {
  paperFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updatePaper(form.value).finally(() =>  buttonLoading.value = false);
      } else {
        await addPaper(form.value).finally(() =>  buttonLoading.value = false);
      }
      proxy?.$modal.msgSuccess("操作成功");
      dialog.visible = false;
      await getList();
    }
  });
}

/** 删除按钮操作 */
const handleDelete = async (row?: PaperVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除试卷主编号为"' + _ids + '"的数据项？').finally(() => loading.value = false);
  await delPaper(_ids);
  proxy?.$modal.msgSuccess("删除成功");
  await getList();
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download('system/paper/export', {
    ...queryParams.value
  }, `paper_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  getList();
});
</script>
