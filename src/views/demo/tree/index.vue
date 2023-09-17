<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div class="mb-[10px]" v-show="showSearch">
        <el-card shadow="hover">
          <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
            <el-form-item label="树节点名" prop="treeName">
              <el-input v-model="queryParams.treeName" placeholder="请输入树节点名" clearable style="width: 240px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['demo:tree:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="handleToggleExpandAll">展开/折叠</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>
      <el-table
        v-loading="loading"
        :data="treeList"
        row-key="id"
        :default-expand-all="isExpandAll"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        ref="treeTableRef"
      >
        <el-table-column label="父id" align="center" prop="parentId" />
        <el-table-column label="部门id" align="center" prop="deptId" />
        <el-table-column label="用户id" align="center" prop="userId" />
        <el-table-column label="树节点名" align="center" prop="treeName" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['demo:tree:edit']" />
            </el-tooltip>
            <el-tooltip content="新增" placement="top">
              <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['demo:tree:add']" />
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['demo:tree:remove']" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <!-- 添加或修改测试树对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="treeFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="父id" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeOptions"
            :props="{ value: 'id', label: 'treeName', children: 'children' }"
            value-key="id"
            placeholder="请选择父id"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="部门id" prop="deptId">
          <el-input v-model="form.deptId" placeholder="请输入部门id" />
        </el-form-item>
        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="值" prop="treeName">
          <el-input v-model="form.treeName" placeholder="请输入值" />
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

<script setup name="Tree" lang="ts">
import { listTree, getTree, delTree, addTree, updateTree } from "@/api/demo/tree";
import { TreeVO, TreeQuery, TreeForm } from '@/api/demo/tree/types';


type TreeOption = {
  id: number;
  treeName: string;
  children?: TreeOption[];
}

const { proxy } = getCurrentInstance() as ComponentInternalInstance;;


const treeList = ref<TreeVO[]>([]);
const treeOptions = ref<TreeOption[]>([]);
const buttonLoading = ref(false);
const showSearch = ref(true);
const isExpandAll = ref(true);
const loading = ref(false);

const queryFormRef = ref<ElFormInstance>();
const treeFormRef = ref<ElFormInstance>();
const treeTableRef = ref<ElTableInstance>()

const dialog = reactive<DialogOption>({
    visible: false,
    title: ''
});


const initFormData: TreeForm = {
    id: undefined,
    parentId: undefined,
    deptId: undefined,
    userId: undefined,
    treeName: undefined,
}

const data = reactive<PageData<TreeForm, TreeQuery>>({
  form: {...initFormData},
  queryParams: {
    parentId: undefined,
    deptId: undefined,
    userId: undefined,
    treeName: undefined,
  },
  rules: {
    id: [
      { required: true, message: "主键不能为空", trigger: "blur" }
    ],
    parentId: [
      { required: true, message: "父id不能为空", trigger: "blur" }
    ],
    deptId: [
      { required: true, message: "部门id不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "用户id不能为空", trigger: "blur" }
    ],
    treeName: [
      { required: true, message: "值不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询测试树列表 */
const getList = async () => {
  loading.value = true;
  const res = await listTree(queryParams.value);
  const data = proxy?.handleTree<TreeVO>(res.data, "id", "parentId");
  if (data) {
    treeList.value = data;
    loading.value = false;
  }
}

/** 查询测试树下拉树结构 */
const getTreeselect = async () => {
  const res = await listTree();
  treeOptions.value = [];
  const data: TreeOption = { id: 0, treeName: '顶级节点', children: [] };
  data.children = proxy?.handleTree<TreeOption>(res.data, "id", "parentId");
  treeOptions.value.push(data);
}

// 取消按钮
const cancel = () => {
  reset();
  dialog.visible = false;
}

// 表单重置
const reset = () => {
  form.value = {...initFormData}
  treeFormRef.value?.resetFields();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  getList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
}

/** 新增按钮操作 */
const handleAdd = (row?: TreeVO) => {
  reset();
  getTreeselect();
  if (row && row.id) {
    form.value.parentId = row.id;
  } else {
    form.value.parentId = 0;
  }
  dialog.visible = true;
  dialog.title = "添加测试树";
}

/** 展开/折叠操作 */
const handleToggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value;
  toggleExpandAll(treeList.value, isExpandAll.value)
}

/** 展开/折叠操作 */
const toggleExpandAll = (data: TreeVO[], status: boolean) => {
  data.forEach((item) => {
    treeTableRef.value?.toggleRowExpansion(item, status)
    if (item.children && item.children.length > 0) toggleExpandAll(item.children, status)
  })
}

/** 修改按钮操作 */
const handleUpdate = async (row: TreeVO) => {
  reset();
  await getTreeselect();
  if (row) {
    form.value.parentId = row.id;
  }
  const res = await getTree(row.id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "修改测试树";
}

/** 提交按钮 */
const submitForm = () => {
  treeFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateTree(form.value).finally(() => buttonLoading.value = false);
      } else {
        await addTree(form.value).finally(() => buttonLoading.value = false);
      }
      proxy?.$modal.msgSuccess("操作成功");
      dialog.visible = false;
      await getList();
    }
  });
}

/** 删除按钮操作 */
const handleDelete = async (row: TreeVO) => {
  await proxy?.$modal.confirm('是否确认删除测试树编号为"' + row.id + '"的数据项？');
  loading.value = true;
  await delTree(row.id).finally(() => loading.value = false);
  await getList();
  proxy?.$modal.msgSuccess("删除成功");
}

onMounted(() => {
  getList();
});
</script>
