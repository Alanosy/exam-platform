<template>
  <div class="p-2">
    <el-row :gutter="20">
      <!-- 部门树 -->
      <el-col :lg="4" :xs="24" style="">
        <el-card shadow="hover">
          <el-input v-model="deptName" placeholder="请输入部门名称" prefix-icon="Search" clearable />
          <el-tree
            ref="deptTreeRef"
            class="mt-2"
            node-key="id"
            :data="deptOptions"
            :props="{ label: 'label', children: 'children' }"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          />
        </el-card>
      </el-col>
      <el-col :lg="20" :xs="24">
        <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
          <div v-show="showSearch" class="mb-[10px]">
            <el-card shadow="hover">
              <el-form ref="queryFormRef" :model="queryParams" :inline="true">
                <el-form-item label="岗位编码" prop="postCode">
                  <el-input v-model="queryParams.postCode" placeholder="请输入岗位编码" clearable @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item label="类别编码" prop="postCategory">
                  <el-input
                    v-model="queryParams.postCategory"
                    placeholder="请输入类别编码"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                  />
                </el-form-item>
                <el-form-item label="岗位名称" prop="postName">
                  <el-input v-model="queryParams.postName" placeholder="请输入岗位名称" clearable @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item label="部门" prop="deptId">
                  <el-tree-select
                    v-model="queryParams.deptId"
                    :data="deptOptions"
                    :props="{ value: 'id', label: 'label', children: 'children' }"
                    value-key="id"
                    placeholder="请选择部门"
                    check-strictly
                  />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                  <el-select v-model="queryParams.status" placeholder="岗位状态" clearable>
                    <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
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
                <el-button v-hasPermi="['system:post:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button v-hasPermi="['system:post:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()"
                  >修改</el-button
                >
              </el-col>
              <el-col :span="1.5">
                <el-button v-hasPermi="['system:post:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">
                  删除
                </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button v-hasPermi="['system:post:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
              </el-col>
              <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
            </el-row>
          </template>
          <el-table v-loading="loading" :data="postList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column v-if="false" label="岗位编号" align="center" prop="postId" />
            <el-table-column label="岗位编码" align="center" prop="postCode" />
            <el-table-column label="类别编码" align="center" prop="postCategory" />
            <el-table-column label="岗位名称" align="center" prop="postName" />
            <el-table-column label="部门" align="center" prop="deptName" />
            <el-table-column label="排序" align="center" prop="postSort" />
            <el-table-column label="状态" align="center" prop="status">
              <template #default="scope">
                <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="180">
              <template #default="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
              <template #default="scope">
                <el-tooltip content="修改" placement="top">
                  <el-button v-hasPermi="['system:post:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button v-hasPermi="['system:post:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="total > 0"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            :total="total"
            @pagination="getList"
          />
        </el-card>

        <!-- 添加或修改岗位对话框 -->
        <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
          <el-form ref="postFormRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="岗位名称" prop="postName">
              <el-input v-model="form.postName" placeholder="请输入岗位名称" />
            </el-form-item>
            <el-form-item label="部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptOptions"
                :props="{ value: 'id', label: 'label', children: 'children' }"
                value-key="id"
                placeholder="请选择部门"
                check-strictly
              />
            </el-form-item>
            <el-form-item label="岗位编码" prop="postCode">
              <el-input v-model="form.postCode" placeholder="请输入编码名称" />
            </el-form-item>
            <el-form-item label="类别编码" prop="postCategory">
              <el-input v-model="form.postCategory" placeholder="请输入类别编码" />
            </el-form-item>
            <el-form-item label="岗位顺序" prop="postSort">
              <el-input-number v-model="form.postSort" controls-position="right" :min="0" />
            </el-form-item>
            <el-form-item label="岗位状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button type="primary" @click="submitForm">确 定</el-button>
              <el-button @click="cancel">取 消</el-button>
            </div>
          </template>
        </el-dialog>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Post" lang="ts">
import { listPost, addPost, delPost, getPost, updatePost } from '@/api/system/post';
import { PostForm, PostQuery, PostVO } from '@/api/system/post/types';
import { DeptVO } from '@/api/system/dept/types';
import api from '@/api/system/user';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const postList = ref<PostVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<number | string>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const deptName = ref('');
const deptOptions = ref<DeptVO[]>([]);
const deptTreeRef = ref<ElTreeInstance>();
const postFormRef = ref<ElFormInstance>();
const queryFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: PostForm = {
  postId: undefined,
  deptId: undefined,
  postCode: '',
  postName: '',
  postCategory: '',
  postSort: 0,
  status: '0',
  remark: ''
};

const data = reactive<PageData<PostForm, PostQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    postCode: '',
    postName: '',
    postCategory: '',
    status: ''
  },
  rules: {
    postName: [{ required: true, message: '岗位名称不能为空', trigger: 'blur' }],
    postCode: [{ required: true, message: '岗位编码不能为空', trigger: 'blur' }],
    deptId: [{ required: true, message: '部门不能为空', trigger: 'blur' }],
    postSort: [{ required: true, message: '岗位顺序不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs<PageData<PostForm, PostQuery>>(data);

/** 通过条件过滤节点  */
const filterNode = (value: string, data: any) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};

/** 根据名称筛选部门树 */
watchEffect(
  () => {
    deptTreeRef.value?.filter(deptName.value);
  },
  {
    flush: 'post' // watchEffect会在DOM挂载或者更新之前就会触发，此属性控制在DOM元素更新后运行
  }
);

/** 查询部门下拉树结构 */
const getTreeSelect = async () => {
  const res = await api.deptTreeSelect();
  deptOptions.value = res.data;
};

/** 节点单击事件 */
const handleNodeClick = (data: DeptVO) => {
  queryParams.value.belongDeptId = data.id;
  queryParams.value.deptId = undefined;
  handleQuery();
};

/** 查询岗位列表 */
const getList = async () => {
  loading.value = true;
  const res = await listPost(queryParams.value);
  postList.value = res.rows;
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
  postFormRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  if (queryParams.value.deptId) {
    queryParams.value.belongDeptId = undefined;
  }
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  queryParams.value.pageNum = 1;
  queryParams.value.deptId = undefined;
  deptTreeRef.value?.setCurrentKey(undefined);
  /** 清空左边部门树选中值 */
  queryParams.value.belongDeptId = undefined;
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: PostVO[]) => {
  ids.value = selection.map((item) => item.postId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加岗位';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: PostVO) => {
  reset();
  const postId = row?.postId || ids.value[0];
  const res = await getPost(postId);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改岗位';
};

/** 提交按钮 */
const submitForm = () => {
  postFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      form.value.postId ? await updatePost(form.value) : await addPost(form.value);
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: PostVO) => {
  const postIds = row?.postId || ids.value;
  await proxy?.$modal.confirm('是否确认删除岗位编号为"' + postIds + '"的数据项？');
  await delPost(postIds);
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/post/export',
    {
      ...queryParams.value
    },
    `post_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getTreeSelect(); // 初始化部门数据
  getList();
});
</script>
