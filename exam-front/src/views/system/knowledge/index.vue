<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="名称" prop="name">
              <el-input v-model="queryParams.name" placeholder="章节或知识点名称" clearable @keyup.enter="handleQuery" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['system:knowledge:add']">新增章节</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="handleToggleExpandAll">展开/折叠</el-button>
          </el-col>
          <el-col :span="20">
            <span class="text-[12px] text-[#909399]">
              两级结构：章节 → 知识点。知识点挂上去后，出题、组卷、错题归因都按它聚合；已被引用的知识点不允许删除。
            </span>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table
        ref="knowledgeTableRef"
        v-loading="loading"
        :data="knowledgeList"
        row-key="id"
        border
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column label="名称" prop="name" min-width="260" />
        <el-table-column label="层级" align="center" width="110">
          <template #default="scope">
            <el-tag size="small" :type="String(scope.row.parentId) === '0' ? 'primary' : 'info'" effect="light">
              {{ String(scope.row.parentId) === '0' ? '章节' : '知识点' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" align="center" prop="sort" width="100" />
        <el-table-column label="操作" fixed="right" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:knowledge:edit']" />
            </el-tooltip>
            <!-- 只支持两级：知识点下不能再挂子节点 -->
            <el-tooltip v-if="String(scope.row.parentId) === '0'" content="新增知识点" placement="top">
              <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['system:knowledge:add']" />
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:knowledge:remove']" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改知识点对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="knowledgeFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级章节" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="knowledgeOptions"
            :props="{ value: 'id', label: 'name', children: 'children' } as any"
            value-key="id"
            placeholder="不选则作为章节（顶级）"
            check-strictly
            class="w-full"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" />
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

<script setup name="Knowledge" lang="ts">
import { treeKnowledge, getKnowledge, delKnowledge, addKnowledge, updateKnowledge } from '@/api/system/knowledge';
import { KnowledgePointVO, KnowledgePointQuery, KnowledgePointForm } from '@/api/system/knowledge/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const knowledgeList = ref<KnowledgePointVO[]>([]);
const knowledgeOptions = ref<KnowledgePointVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const isExpandAll = ref(true);

const queryFormRef = ref<ElFormInstance>();
const knowledgeFormRef = ref<ElFormInstance>();
const knowledgeTableRef = ref<ElTableInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: KnowledgePointForm = {
  id: undefined,
  parentId: 0,
  name: undefined,
  sort: 0
};

const data = reactive<PageData<KnowledgePointForm, KnowledgePointQuery>>({
  form: { ...initFormData },
  queryParams: {
    // 树接口返回全量数据，分页参数只是为了满足类型定义
    pageNum: 1,
    pageSize: 1000,
    name: undefined
  },
  rules: {
    parentId: [{ required: true, message: '请选择上级章节', trigger: 'change' }],
    name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/**
 * 查询知识点树
 *
 * 后端已按 parentId 组装好 children，前端直接用，不用再 handleTree 一次
 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await treeKnowledge(queryParams.value);
    knowledgeList.value = res.data ?? [];
  } finally {
    loading.value = false;
  }
};

/**
 * 上级章节下拉：只给「顶级」+ 章节层，知识点不能当上级（否则就三级了）
 */
const getTreeSelect = async () => {
  const res = await treeKnowledge();
  const chapters = (res.data ?? []).map((item) => ({ ...item, children: undefined }) as KnowledgePointVO);
  knowledgeOptions.value = [{ id: 0, name: '作为章节（顶级）' } as KnowledgePointVO, ...chapters];
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  knowledgeFormRef.value?.resetFields();
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

/** 新增按钮操作（传入行则为在该章节下新增知识点） */
const handleAdd = async (row?: KnowledgePointVO) => {
  reset();
  await getTreeSelect();
  form.value.parentId = row?.id ?? 0;
  dialog.visible = true;
  dialog.title = row?.id ? '添加知识点' : '添加章节';
};

/** 展开/折叠操作 */
const handleToggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value;
  toggleExpandAll(knowledgeList.value, isExpandAll.value);
};

const toggleExpandAll = (list: KnowledgePointVO[], status: boolean) => {
  list.forEach((item) => {
    knowledgeTableRef.value?.toggleRowExpansion(item, status);
    if (item.children && item.children.length > 0) {
      toggleExpandAll(item.children, status);
    }
  });
};

/** 修改按钮操作 */
const handleUpdate = async (row: KnowledgePointVO) => {
  reset();
  await getTreeSelect();
  const res = await getKnowledge(row.id);
  Object.assign(form.value, res.data, { parentId: res.data.parentId ?? 0 });
  dialog.visible = true;
  dialog.title = '修改知识点';
};

/** 提交按钮 */
const submitForm = () => {
  knowledgeFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateKnowledge(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addKnowledge(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row: KnowledgePointVO) => {
  await proxy?.$modal.confirm('是否确认删除名称为"' + row.name + '"的' + (String(row.parentId) === '0' ? '章节' : '知识点') + '？');
  loading.value = true;
  await delKnowledge(row.id).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

onMounted(() => {
  getList();
});
</script>
