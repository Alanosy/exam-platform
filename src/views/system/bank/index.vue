<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="题库名称" prop="bankName">
              <el-input v-model="queryParams.bankName" placeholder="请输入题库名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <!-- <el-form-item label="题库描述" prop="bankDesc">
              <el-input v-model="queryParams.bankDesc" placeholder="请输入题库描述" clearable @keyup.enter="handleQuery" />
            </el-form-item> -->
            <!-- <el-form-item label="创建人" prop="creatorId">
              <el-input v-model="queryParams.creatorId" placeholder="按创建人用户ID筛选" clearable @keyup.enter="handleQuery" />
            </el-form-item> -->
            <el-form-item label="可见性" prop="visibility">
              <el-select v-model="queryParams.visibility" placeholder="请选择可见性" clearable>
                <el-option v-for="dict in bank_visibility_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
                <el-option v-for="dict in bank_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="题库分类" prop="categoryId">
              <el-tree-select
                v-model="queryParams.categoryId"
                :data="bankCategoryOptions"
                :props="{ value: 'id', label: 'categoryName', children: 'children' } as any"
                value-key="id"
                placeholder="请选择题库分类"
                check-strictly
                clearable
                class="w-[200px]"
              />
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
          <el-col :span="1.5">
            <el-button type="info" plain icon="Management" :disabled="single" @click="handleManage()" v-hasPermi="['system:question:list']"
              >管理试题</el-button
            >
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="bankList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" align="center" width="60" type="index" :index="indexMethod" />
        <el-table-column label="题库名称" align="center" prop="bankName" />
        <el-table-column label="题库描述" align="center" prop="bankDesc" show-overflow-tooltip />
        <el-table-column label="题库分类" align="center" prop="categoryName" min-width="120">
          <template #default="{ row }">
            <span>{{ row.categoryName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建人" align="center" prop="creatorName" show-overflow-tooltip />
        <el-table-column label="可见性" align="center" prop="visibility">
          <template #default="scope">
            <dict-tag :options="bank_visibility_type" :value="scope.row.visibility" />
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <dict-tag :options="bank_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:bank:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="管理试题" placement="top">
              <el-button link type="primary" icon="Management" @click="handleManage(scope.row)" v-hasPermi="['system:question:list']"></el-button>
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
        <el-form-item label="题库分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="bankCategoryOptions"
            :props="{ value: 'id', label: 'categoryName', children: 'children' } as any"
            value-key="id"
            placeholder="请选择题库分类"
            check-strictly
            clearable
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="可见性" prop="visibility">
          <el-select v-model="form.visibility" placeholder="请选择可见性">
            <el-option v-for="dict in bank_visibility_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option v-for="dict in bank_status" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 题库下的试题管理（分页查看 / 筛选 / 批量操作） -->
    <question-manage v-model:visible="manageVisible" :bank-id="currentBank.id" :bank-name="currentBank.bankName" />
  </div>
</template>

<script setup name="Bank" lang="ts">
import { listBank, getBank, delBank, addBank, updateBank } from '@/api/system/bank';
import { BankVO, BankQuery, BankForm } from '@/api/system/bank/types';
import { treeBankCategory } from '@/api/system/bankCategory';
import { BankCategoryTreeVO } from '@/api/system/bankCategory/types';
import QuestionManage from './questionManage.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// 题库可见性、状态字典（下拉框选项）
const { bank_visibility_type, bank_status } = toRefs<any>(proxy?.useDict('bank_visibility_type', 'bank_status'));

// 题库分类树（新增/修改/筛选共用）
const bankCategoryOptions = ref<BankCategoryTreeVO[]>([]);

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

// 试题管理弹窗：当前操作的题库
const manageVisible = ref(false);
const currentBank = ref<{ id?: string | number; bankName?: string }>({});

const initFormData: BankForm = {
  id: undefined,
  bankName: undefined,
  bankDesc: undefined,
  categoryId: undefined,
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
    categoryId: undefined,
    visibility: undefined,
    status: undefined,
    params: {}
  },
  rules: {
    id: [{ required: true, message: '主键ID不能为空', trigger: 'blur' }],
    bankName: [{ required: true, message: '题库名称不能为空', trigger: 'blur' }],
    visibility: [{ required: true, message: '可见性不能为空', trigger: 'change' }],
    status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 下拉框默认选中字典的第一项 */
const setDefaultSelectValue = () => {
  if (!form.value.visibility && bank_visibility_type.value?.length) {
    form.value.visibility = bank_visibility_type.value[0].value;
  }
  if ((form.value.status === undefined || form.value.status === null || form.value.status === '') && bank_status.value?.length) {
    form.value.status = bank_status.value[0].value;
  }
};
// 字典异步加载完成后，补一次默认值
watch([bank_visibility_type, bank_status], setDefaultSelectValue);

/** 表格序号（跨页连续自增） */
const indexMethod = (index: number) => {
  return (queryParams.value.pageNum - 1) * queryParams.value.pageSize + index + 1;
};

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
  // resetFields 会把字段还原为初始值，之后再赋默认选中项
  setDefaultSelectValue();
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
  // 后端返回的 status 是数字，下拉框的字典值是字符串，统一转成字符串才能回显选中
  Object.assign(form.value, { ...res.data, status: res.data.status?.toString() });
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

/** 管理题库下的试题 */
const handleManage = async (row?: BankVO) => {
  const bankId = row?.id || ids.value[0];
  if (!bankId) {
    return;
  }
  const rowData = row ?? bankList.value.find((item) => String(item.id) === String(bankId));
  currentBank.value = { id: bankId, bankName: rowData?.bankName };
  manageVisible.value = true;
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
    'question/bank/export',
    {
      ...queryParams.value
    },
    `bank_${new Date().getTime()}.xlsx`
  );
};

/** 加载题库分类树 */
const loadBankCategoryTree = async () => {
  try {
    const res = await treeBankCategory();
    bankCategoryOptions.value = proxy?.handleTree<BankCategoryTreeVO>(res.data, 'id', 'parentId') ?? [];
  } catch {
    bankCategoryOptions.value = [];
  }
};

onMounted(() => {
  loadBankCategoryTree();
  getList();
});
</script>
