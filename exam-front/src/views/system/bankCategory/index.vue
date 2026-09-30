<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="分类名称" prop="categoryName">
              <el-input v-model="queryParams.categoryName" placeholder="请输入分类名称" clearable @keyup.enter="handleQuery" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['system:bankCategory:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="handleToggleExpandAll">展开/折叠</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:bankCategory:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table
        ref="bankCategoryTableRef"
        v-loading="loading"
        :data="bankCategoryList"
        row-key="id"
        border
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column label="分类名称" prop="categoryName" min-width="260" />
        <el-table-column label="排序" align="center" prop="sort" width="120" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
        <el-table-column label="操作" fixed="right" align="center" width="180" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:bankCategory:edit']" />
            </el-tooltip>
            <el-tooltip content="新增子分类" placement="top">
              <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['system:bankCategory:add']" />
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:bankCategory:remove']" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改题库分类目录对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="bankCategoryFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="bankCategoryOptions"
            :props="{ value: 'id', label: 'categoryName', children: 'children' } as any"
            value-key="id"
            placeholder="请选择上级分类（不选则为顶级）"
            check-strictly
            class="w-full"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类名称" prop="categoryName">
              <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" controls-position="right" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
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

<script setup name="BankCategory" lang="ts">
import { treeBankCategory, getBankCategory, delBankCategory, addBankCategory, updateBankCategory } from '@/api/system/bankCategory';
import { BankCategoryVO, BankCategoryQuery, BankCategoryForm, BankCategoryTreeVO } from '@/api/system/bankCategory/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const bankCategoryList = ref<BankCategoryTreeVO[]>([]);
const bankCategoryOptions = ref<BankCategoryTreeVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const isExpandAll = ref(true);

const queryFormRef = ref<ElFormInstance>();
const bankCategoryFormRef = ref<ElFormInstance>();
const bankCategoryTableRef = ref<ElTableInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: BankCategoryForm = {
  id: undefined,
  parentId: 0,
  categoryName: undefined,
  sort: 0,
  isDeleted: undefined
};

const data = reactive<PageData<BankCategoryForm, BankCategoryQuery>>({
  form: { ...initFormData },
  queryParams: {
    // 树接口返回全量数据，分页参数只是为了满足类型定义
    pageNum: 1,
    pageSize: 1000,
    categoryName: undefined
  },
  rules: {
    parentId: [{ required: true, message: '请选择上级分类', trigger: 'change' }],
    categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询题库分类目录树 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await treeBankCategory(queryParams.value);
    const tree = proxy?.handleTree<BankCategoryTreeVO>(res.data, 'id', 'parentId');
    bankCategoryList.value = tree ?? [];
  } finally {
    loading.value = false;
  }
};

/** 查询分类下拉树结构（带一个「顶级分类」根节点，方便选回顶级） */
const getTreeSelect = async () => {
  const res = await treeBankCategory();
  const tree = proxy?.handleTree<BankCategoryTreeVO>(res.data, 'id', 'parentId') ?? [];
  bankCategoryOptions.value = [{ id: 0, categoryName: '顶级分类', children: tree } as BankCategoryTreeVO];
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  bankCategoryFormRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

/** 新增按钮操作（传入行则为新增子分类） */
const handleAdd = async (row?: BankCategoryVO) => {
  reset();
  await getTreeSelect();
  form.value.parentId = row?.id ?? 0;
  dialog.visible = true;
  dialog.title = row?.id ? '添加子分类' : '添加题库分类';
};

/** 展开/折叠操作 */
const handleToggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value;
  toggleExpandAll(bankCategoryList.value, isExpandAll.value);
};

const toggleExpandAll = (list: BankCategoryTreeVO[], status: boolean) => {
  list.forEach((item) => {
    bankCategoryTableRef.value?.toggleRowExpansion(item, status);
    if (item.children && item.children.length > 0) {
      toggleExpandAll(item.children, status);
    }
  });
};

/** 修改按钮操作 */
const handleUpdate = async (row: BankCategoryVO) => {
  reset();
  await getTreeSelect();
  const res = await getBankCategory(row.id);
  Object.assign(form.value, res.data, { parentId: res.data.parentId ?? 0 });
  dialog.visible = true;
  dialog.title = '修改题库分类';
};

/** 提交按钮 */
const submitForm = () => {
  bankCategoryFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateBankCategory(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addBankCategory(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row: BankCategoryVO) => {
  await proxy?.$modal.confirm('是否确认删除名称为"' + row.categoryName + '"的分类？');
  loading.value = true;
  await delBankCategory(row.id).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'question/bankCategory/export',
    {
      ...queryParams.value
    },
    `bankCategory_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
