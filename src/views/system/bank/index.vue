<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="题库名称" prop="bankName">
              <el-input v-model="queryParams.bankName" placeholder="请输入题库名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="题库描述" prop="bankDesc">
              <el-input v-model="queryParams.bankDesc" placeholder="请输入题库描述" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="创建人" prop="creatorId">
              <el-input v-model="queryParams.creatorId" placeholder="请输入创建人用户ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="可见性" prop="visibility">
              <el-input v-model="queryParams.visibility" placeholder="请输入可见性 private私有 / public公开" clearable @keyup.enter="handleQuery" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:bank:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:bank:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:bank:remove']"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:bank:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="bankList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="主键" align="center" prop="id" v-if="true" />
        <el-table-column label="题库名称" align="center" prop="bankName" />
        <el-table-column label="题库描述" align="center" prop="bankDesc" />
        <el-table-column label="创建人" align="center" prop="creatorId" />
        <el-table-column label="可见性" align="center" prop="visibility" />
        <el-table-column label="状态" align="center" prop="status" />
        <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:bank:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:bank:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改题库对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="bankFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="题库名称" prop="bankName">
          <el-input v-model="form.bankName" placeholder="请输入题库名称" />
        </el-form-item>
        <el-form-item label="题库描述" prop="bankDesc">
          <el-input v-model="form.bankDesc" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="创建人" prop="creatorId">
          <el-input v-model="form.creatorId" placeholder="请输入创建人用户ID" />
        </el-form-item>
        <el-form-item label="可见性" prop="visibility">
          <el-input v-model="form.visibility" placeholder="请输入可见性 private私有 / public公开" />
        </el-form-item>
        <el-form-item label="状态" prop="visibility">
          <el-input v-model="form.status" placeholder="请输入状态" />
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

<script setup name="Bank" lang="ts">
import { listBank, getBank, delBank, addBank, updateBank } from '@/api/system/bank';
import { BankVO, BankQuery, BankForm } from '@/api/system/bank/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const bankList = ref<BankVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const bankFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: BankForm = {
  id: undefined,
  bankName: undefined,
  bankDesc: undefined,
  creatorId: undefined,
  visibility: undefined,
  status: undefined
};
const data = reactive<PageData<BankForm, BankQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    bankName: undefined,
    bankDesc: undefined,
    creatorId: undefined,
    visibility: undefined,
    status: undefined,
    params: {}
  },
  rules: {
    id: [{ required: true, message: '主键ID不能为空', trigger: 'blur' }],
    bankName: [{ required: true, message: '题库名称不能为空', trigger: 'blur' }],
    creatorId: [{ required: true, message: '创建人用户ID不能为空', trigger: 'blur' }],
    visibility: [{ required: true, message: '可见性 private私有 / public公开不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '状态 0草稿 1正常 2归档不能为空', trigger: 'change' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询题库列表 */
const getList = async () => {
  loading.value = true;
  const res = await listBank(queryParams.value);
  bankList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  bankFormRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: BankVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加题库';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: BankVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getBank(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改题库';
};

/** 提交按钮 */
const submitForm = () => {
  bankFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateBank(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addBank(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: BankVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除题库编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delBank(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/bank/export',
    {
      ...queryParams.value
    },
    `bank_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
