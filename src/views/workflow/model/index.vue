<template>
  <div class="p-2">
    <el-row :gutter="20">
      <!-- 流程分类树 -->
      <el-col :lg="4" :xs="24" style="">
        <el-card shadow="hover">
          <el-input v-model="categoryName" placeholder="请输入流程分类名" prefix-icon="Search" clearable />
          <el-tree
            ref="categoryTreeRef"
            class="mt-2"
            node-key="id"
            :data="categoryOptions"
            :props="{ label: 'categoryName', children: 'children' }"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          ></el-tree>
        </el-card>
      </el-col>
      <el-col :lg="20" :xs="24">
        <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
          <div v-show="showSearch" class="mb-[10px]">
            <el-card shadow="hover">
              <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true">
                <el-form-item label="模型名称" prop="name">
                  <el-input v-model="queryParams.name" placeholder="请输入模型名称" clearable @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item label="模型KEY" prop="key">
                  <el-input v-model="queryParams.key" placeholder="请输入模型KEY" clearable @keyup.enter="handleQuery" />
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
                <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="primary" plain :disabled="multiple" icon="Download" @click="clickExportZip()">导出</el-button>
              </el-col>
              <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
            </el-row>
          </template>

          <el-table v-loading="loading" border :data="modelList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column fixed align="center" type="index" label="序号" width="80"></el-table-column>
            <el-table-column align="center" :show-overflow-tooltip="true" prop="name" label="模型名称" width="200"></el-table-column>
            <el-table-column align="center" prop="key" label="模型KEY"></el-table-column>
            <el-table-column align="center" prop="version" label="版本号" width="90">
              <template #default="scope"> v{{ scope.row.version }}.0</template>
            </el-table-column>
            <el-table-column align="center" prop="metaInfo" label="备注说明" min-width="130"></el-table-column>
            <el-table-column align="center" :show-overflow-tooltip="true" prop="createTime" label="创建时间" width="160"></el-table-column>
            <el-table-column align="center" :show-overflow-tooltip="true" prop="lastUpdateTime" label="更新时间" width="160"></el-table-column>
            <el-table-column fixed="right" label="操作" align="center" width="170" class-name="small-padding fixed-width">
              <template #default="scope">
                <el-row :gutter="10" class="mb8">
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="Pointer" @click="clickDesign(scope.row.id)">设计流程</el-button>
                  </el-col>
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
                  </el-col>
                </el-row>
                <el-row :gutter="10" class="mb8">
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="ScaleToOriginal" @click="clickDeploy(scope.row.id, scope.row.key)">
                      流程部署
                    </el-button>
                  </el-col>
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="CopyDocument" @click="handleCopy(scope.row)"> 复制模型 </el-button>
                  </el-col>
                </el-row>
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
      </el-col>
    </el-row>
    <!-- 设计流程开始 -->
    <Design ref="designRef" @close-call-back="handleQuery"></Design>
    <!-- 设计流程结束 -->
    <!-- 添加模型对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="650px" append-to-body :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模型名称：" prop="name">
          <el-input v-model="form.name" :disabled="ids && ids.length > 0 && billType === 'update'" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="模型KEY：" prop="key">
          <el-input v-model="form.key" :disabled="ids && ids.length > 0 && billType === 'update'" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="流程分类" prop="categoryCode">
          <el-tree-select
            v-model="form.categoryCode"
            :data="categoryOptions"
            :props="{ value: 'categoryCode', label: 'categoryName', children: 'children' }"
            value-key="categoryCode"
            placeholder="请选择流程分类"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="备注：" prop="description">
          <el-input v-model="form.description" type="textarea" maxlength="200" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup name="Model">
import Design from '../../../components/BpmnDesign/index.vue';
import { listModel, addModel, delModel, modelDeploy, getInfo, update } from '@/api/workflow/model';
import { ModelQuery, ModelForm, ModelVO } from '@/api/workflow/model/types';
import { listCategory } from '@/api/workflow/category';
import { copyModel } from '@/api/workflow/model';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const formRef = ref<ElFormInstance>();
const queryFormRef = ref<ElFormInstance>();
const categoryTreeRef = ref<ElTreeInstance>();
const designRef = ref<InstanceType<typeof Design>>();

type CategoryOption = {
  categoryCode: string;
  categoryName: string;
  children?: CategoryOption[];
};

const buttonLoading = ref(false);
const loading = ref(true);
const ids = ref<string[]>([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const modelList = ref<ModelVO[]>([]);
const categoryOptions = ref<CategoryOption[]>([]);
const categoryName = ref('');
const billType = ref<string>('');

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: ModelForm = {
  id: '',
  name: '',
  key: '',
  categoryCode: '',
  xml: '',
  svg: '',
  description: ''
};
const data = reactive<PageData<ModelForm, ModelQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: '',
    key: '',
    categoryCode: ''
  },
  rules: {
    name: [{ required: true, message: '模型不能为空', trigger: 'blur' }],
    key: [{ required: true, message: '模型KEY不能为空', trigger: 'blur' }],
    categoryCode: [{ required: true, message: '流程分类不能为空', trigger: 'blur' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

onMounted(() => {
  getList();
  getTreeselect();
});

/** 节点单击事件 */
const handleNodeClick = (data: ModelForm) => {
  queryParams.value.categoryCode = data.categoryCode;
  if (data.categoryCode === 'ALL') {
    queryParams.value.categoryCode = '';
  }
  handleQuery();
};
/** 通过条件过滤节点  */
const filterNode = (value: string, data: any) => {
  if (!value) return true;
  return data.categoryName.indexOf(value) !== -1;
};
/** 根据名称筛选部门树 */
watchEffect(
  () => {
    categoryTreeRef.value?.filter(categoryName.value);
  },
  {
    flush: 'post' // watchEffect会在DOM挂载或者更新之前就会触发，此属性控制在DOM元素更新后运行
  }
);

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  queryParams.value.categoryCode = '';
  queryParams.value.pageNum = 1;
  queryParams.value.pageSize = 10;
  handleQuery();
};
// 多选框选中数据
const handleSelectionChange = (selection: ModelVO[]) => {
  ids.value = selection.map((item: ModelVO) => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};
//分页
const getList = async () => {
  loading.value = true;
  const resp = await listModel(queryParams.value);
  modelList.value = resp.rows;
  total.value = resp.total;
  loading.value = false;
};
/** 删除按钮操作 */
const handleDelete = async (row?: ModelVO) => {
  const id = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除模型id为【' + id + '】的数据项？');
  loading.value = true;
  await delModel(id).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};
// 流程部署
const clickDeploy = async (id: string, key: string) => {
  await proxy?.$modal.confirm('是否部署模型key为【' + key + '】流程？');
  loading.value = true;
  await modelDeploy(id).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('部署成功');
};
//新增打开
const handleAdd = () => {
  billType.value = 'add';
  ids.value = [];
  getTreeselect();
  form.value = { ...initFormData };
  dialog.visible = true;
  dialog.title = '新增模型';
};
//修改打开
const handleUpdate = () => {
  billType.value = 'update';
  dialog.title = '修改模型';
  nextTick(async () => {
    await getTreeselect();
    const _id = ids.value[0];
    const res = await getInfo(_id);
    Object.assign(form.value, res.data);
    dialog.visible = true;
  });
};

//复制打开
const handleCopy = (row?: ModelVO) => {
  billType.value = 'copy';
  dialog.title = '复制模型';
  nextTick(async () => {
    await getTreeselect();
    form.value = { ...initFormData };
    form.value.id = row.id;
    dialog.visible = true;
  });
};

/** 提交按钮 */
const submitForm = () => {
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if ('copy' === billType.value) {
        await copyModel(form.value);
        proxy?.$modal.msgSuccess('操作成功');
      } else if (ids.value && ids.value.length > 0 && 'update' === billType.value) {
        form.value.id = ids.value[0];
        await update(form.value);
        proxy?.$modal.msgSuccess('操作成功');
      } else {
        initXml(form.value.key, form.value.name);
        form.value.xml = xml.value;
        await addModel(form.value);
        proxy?.$modal.msgSuccess('操作成功');
      }
      dialog.visible = false;
      await getList();
    }
  });
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  formRef.value.resetFields();
};

// 打开设计流程
const clickDesign = async (id: string) => {
  await designRef.value.open(id);
};
// 导出流程模型
const clickExportZip = () => {
  proxy?.$download.zip('/workflow/model/export/zip/' + ids.value, '模型');
};
/** 查询流程分类下拉树结构 */
const getTreeselect = async () => {
  const res = await listCategory();
  categoryOptions.value = [];
  const data: CategoryOption = { categoryCode: 'ALL', categoryName: '顶级节点', children: [] };
  data.children = proxy?.handleTree<CategoryOption>(res.data, 'id', 'parentId');
  categoryOptions.value.push(data);
};

const xml = ref<string>('');

const initXml = async (key: string, name: string) => {
  xml.value = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" xmlns:bioc="http://bpmn.io/schema/bpmn/biocolor/1.0" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:flowable="http://flowable.org/bpmn" targetNamespace="http://www.flowable.org/processdef">
  <process id="${key}" name="${name}">
    <startEvent id="startNode1" name="开始" />
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_flow">
    <bpmndi:BPMNPlane id="BPMNPlane_flow" bpmnElement="T-2d89e7a3-ba79-4abd-9f64-ea59621c258c">
      <bpmndi:BPMNShape id="BPMNShape_startNode1" bpmnElement="startNode1" bioc:stroke="">
        <omgdc:Bounds x="240" y="200" width="30" height="30" />
        <bpmndi:BPMNLabel>
          <omgdc:Bounds x="242" y="237" width="23" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>`;
  return xml;
};
</script>
