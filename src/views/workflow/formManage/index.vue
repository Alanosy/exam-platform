<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="search">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item label="表单名称" prop="formName">
            <el-input v-model="queryParams.formName" placeholder="请输入表单名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['workflow:formManage:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['workflow:formManage:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['workflow:formManage:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['workflow:formManage:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="formManageList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="表单名称" align="center" prop="formName" />
        <el-table-column label="表单类型" align="center">
          <template #default="scope">
            <dict-tag :options="wf_form_type" :value="scope.row.formType"></dict-tag>
          </template>
        </el-table-column>
        <el-table-column label="地址" align="center" prop="router" />
        <el-table-column label="备注" align="center" prop="remark" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['workflow:formManage:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['workflow:formManage:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改表单管理对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="formManageFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="form.formName" placeholder="请输入表单名称" />
        </el-form-item>
        <el-form-item label="表单类型" prop="formType">
          <el-radio-group v-model="form.formType" @change="form.router = ''">
            <el-radio v-for="dict in wf_form_type" :key="dict.value" border :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.formType === 'static'" label="路由地址" prop="router">
          <el-input v-model="form.router" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item v-else label="表单" prop="router">
          <el-input v-model="form.router" disabled placeholder="请选择表单">
            <template #append>
              <el-button icon="Search" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

<script setup name="FormManage" lang="ts">
import { listFormManage, getFormManage, delFormManage, addFormManage, updateFormManage } from '@/api/workflow/formManage';
import { FormManageVO, FormManageQuery, FormManageForm } from '@/api/workflow/formManage/types';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_form_type } = toRefs<any>(proxy?.useDict('wf_form_type'));

const formManageList = ref<FormManageVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const formManageFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: FormManageForm = {
  id: undefined,
  formName: undefined,
  formType: 'static',
  remark: undefined
};
const data = reactive<PageData<FormManageForm, FormManageQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    formName: undefined,
    formType: undefined
  },
  rules: {
    id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
    formName: [{ required: true, message: '表单名称不能为空', trigger: 'blur' }],
    formType: [{ required: true, message: '表单类型不能为空', trigger: 'change' }],
    router: [{ required: true, message: '不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询表单管理列表 */
const getList = async () => {
  loading.value = true;
  const res = await listFormManage(queryParams.value);
  formManageList.value = res.rows;
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
  formManageFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: FormManageVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加表单管理';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: FormManageVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getFormManage(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改表单管理';
};

/** 提交按钮 */
const submitForm = () => {
  formManageFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateFormManage(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addFormManage(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: FormManageVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除表单管理编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delFormManage(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'workflow/formManage/export',
    {
      ...queryParams.value
    },
    `formManage_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
