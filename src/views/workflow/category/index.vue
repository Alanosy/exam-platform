<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="search">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item label="分类名称" prop="categoryName">
            <el-input v-model="queryParams.categoryName" placeholder="请输入分类名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="分类编码" prop="categoryCode">
            <el-input v-model="queryParams.categoryCode" placeholder="请输入分类编码" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['workflow:category:add']" type="primary" plain icon="Plus" @click="handleAdd()">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="handleToggleExpandAll">展开/折叠</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <el-table
        ref="categoryTableRef"
        v-loading="loading"
        :data="categoryList"
        row-key="id"
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column label="分类名称" prop="categoryName" />
        <el-table-column label="分类编码" align="center" prop="categoryCode" />
        <el-table-column label="排序" align="center" prop="sortNum" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['workflow:category:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" />
            </el-tooltip>
            <el-tooltip content="新增" placement="top">
              <el-button v-hasPermi="['workflow:category:add']" link type="primary" icon="Plus" @click="handleAdd(scope.row)" />
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['workflow:category:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <!-- 添加或修改流程分类对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="categoryFormRef" v-loading="loading" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="父级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryOptions"
            :props="{ value: 'id', label: 'categoryName', children: 'children' }"
            value-key="id"
            placeholder="请选择父级id"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="排序" prop="sortNum">
          <el-input-number v-model="form.sortNum" placeholder="请输入排序" controls-position="right" :min="0" />
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

<script setup name="Category" lang="ts">
import { listCategory, getCategory, delCategory, addCategory, updateCategory } from '@/api/workflow/category';
import { CategoryVO, CategoryQuery, CategoryForm } from '@/api/workflow/category/types';

type CategoryOption = {
  id: number;
  categoryName: string;
  children?: CategoryOption[];
};

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const categoryList = ref<CategoryVO[]>([]);
const categoryOptions = ref<CategoryOption[]>([]);
const buttonLoading = ref(false);
const showSearch = ref(true);
const isExpandAll = ref(true);
const loading = ref(false);

const queryFormRef = ref<ElFormInstance>();
const categoryFormRef = ref<ElFormInstance>();
const categoryTableRef = ref<ElTableInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: CategoryForm = {
  id: undefined,
  categoryName: undefined,
  categoryCode: undefined,
  parentId: undefined,
  sortNum: 0
};

const data = reactive<PageData<CategoryForm, CategoryQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    categoryName: undefined,
    categoryCode: undefined
  },
  rules: {
    id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
    categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    categoryCode: [{ required: true, message: '分类编码不能为空', trigger: 'blur' }],
    parentId: [{ required: true, message: '父级id不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询流程分类列表 */
const getList = async () => {
  loading.value = true;
  const res = await listCategory(queryParams.value);
  const data = proxy?.handleTree<CategoryVO>(res.data, 'id', 'parentId');
  if (data) {
    categoryList.value = data;
    loading.value = false;
  }
};

/** 查询流程分类下拉树结构 */
const getTreeselect = async () => {
  const res = await listCategory();
  categoryOptions.value = [];
  const data: CategoryOption = { id: 0, categoryName: '顶级节点', children: [] };
  data.children = proxy?.handleTree<CategoryOption>(res.data, 'id', 'parentId');
  categoryOptions.value.push(data);
};

// 取消按钮
const cancel = () => {
  reset();
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  categoryFormRef.value?.resetFields();
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

/** 新增按钮操作 */
const handleAdd = (row?: CategoryVO) => {
  dialog.visible = true;
  dialog.title = '添加流程分类';
  nextTick(() => {
    reset();
    getTreeselect();
    if (row != null && row.id) {
      form.value.parentId = row.id;
    } else {
      form.value.parentId = 0;
    }
  });
};

/** 展开/折叠操作 */
const handleToggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value;
  toggleExpandAll(categoryList.value, isExpandAll.value);
};

/** 展开/折叠操作 */
const toggleExpandAll = (data: CategoryVO[], status: boolean) => {
  data.forEach((item) => {
    categoryTableRef.value?.toggleRowExpansion(item, status);
    if (item.children && item.children.length > 0) toggleExpandAll(item.children, status);
  });
};

/** 修改按钮操作 */
const handleUpdate = (row: CategoryVO) => {
  loading.value = true;
  dialog.visible = true;
  dialog.title = '修改流程分类';
  nextTick(async () => {
    reset();
    await getTreeselect();
    if (row != null) {
      form.value.parentId = row.id;
    }
    const res = await getCategory(row.id);
    loading.value = false;
    Object.assign(form.value, res.data);
  });
};

/** 提交按钮 */
const submitForm = () => {
  categoryFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateCategory(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addCategory(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row: CategoryVO) => {
  await proxy?.$modal.confirm('是否确认删除流程分类编号为"' + row.id + '"的数据项？');
  loading.value = true;
  await delCategory(row.id).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

onMounted(() => {
  getList();
});
</script>
