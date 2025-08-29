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
            :props="{ label: 'label', children: 'children' } as any"
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
              <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" label-width="120px">
                <el-form-item label="流程定义名称" prop="flowName">
                  <el-input v-model="queryParams.flowName" placeholder="请输入流程定义名称" clearable @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item label="流程定义编码" prop="flowCode">
                  <el-input v-model="queryParams.flowCode" placeholder="请输入流程定义编码" clearable @keyup.enter="handleQuery" />
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
                <el-button type="primary" icon="Plus" @click="handleAdd()">添加</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="danger" icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="primary" icon="UploadFilled" @click="uploadDialog.visible = true">部署流程文件</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="warning" icon="Download" :disabled="single" @click="handleExportDef">导出</el-button>
              </el-col>
              <right-toolbar v-model:show-search="showSearch" @query-table="handleQuery"></right-toolbar>
            </el-row>
          </template>
          <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
            <el-tab-pane label="已发布" name="0"></el-tab-pane>
            <el-tab-pane label="未发布" name="1"></el-tab-pane>
            <el-table v-loading="loading" border :data="processDefinitionList" @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column align="center" prop="id" label="主键" v-if="false"></el-table-column>
              <el-table-column align="center" prop="flowName" label="流程定义名称" :show-overflow-tooltip="true"></el-table-column>
              <el-table-column align="center" prop="flowCode" label="标识KEY" :show-overflow-tooltip="true"></el-table-column>
              <el-table-column align="center" prop="categoryName" label="流程分类" :show-overflow-tooltip="true"></el-table-column>
              <el-table-column align="center" prop="version" label="版本号" width="80">
                <template #default="scope"> v{{ scope.row.version }}.0</template>
              </el-table-column>
              <el-table-column align="center" prop="activityStatus" label="激活状态" width="130">
                <template #default="scope">
                  <el-switch
                    v-model="scope.row.activityStatus"
                    :active-value="1"
                    :inactive-value="0"
                    @change="(status) => handleProcessDefState(scope.row, status)"
                  />
                </template>
              </el-table-column>
              <el-table-column align="center" prop="isPublish" label="发布状态" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.isPublish == 0" type="danger">未发布</el-tag>
                  <el-tag v-else-if="scope.row.isPublish == 1" type="success">已发布</el-tag>
                  <el-tag v-else type="danger">失效</el-tag>
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" align="center" width="170" class-name="small-padding fixed-width">
                <template #default="scope">
                  <el-row :gutter="10" class="mb8">
                    <el-col :span="1.5">
                      <el-button link type="primary" size="small" icon="Delete" @click="handleDelete(scope.row)">删除流程</el-button>
                    </el-col>
                    <el-col :span="1.5">
                      <el-button link type="primary" size="small" icon="CopyDocument" @click="handleCopyDef(scope.row)">复制流程</el-button>
                    </el-col>
                  </el-row>
                  <el-row :gutter="10" class="mb8">
                    <el-col :span="1.5">
                      <el-button link type="primary" v-if="scope.row.isPublish === 0" icon="Pointer" size="small" @click="design(scope.row)"
                        >流程设计</el-button
                      >
                      <el-button link type="primary" v-else icon="View" size="small" @click="designView(scope.row)">查看流程</el-button>
                    </el-col>
                    <el-col v-if="scope.row.isPublish !== 1" :span="1.5">
                      <el-button link type="primary" size="small" icon="CircleCheck" @click="handlePublish(scope.row)">发布流程</el-button>
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
              @pagination="getPageList"
            />
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 部署文件 -->
    <el-dialog v-if="uploadDialog.visible" v-model="uploadDialog.visible" :title="uploadDialog.title" width="30%">
      <div v-loading="uploadDialogLoading">
        <div class="mb5">
          <el-text class="mx-1" size="large"><span class="text-danger">*</span>请选择部署流程分类：</el-text>
          <el-tree-select
            v-model="selectCategory"
            :data="categoryOptions"
            :props="{ value: 'id', label: 'label', children: 'children' } as any"
            filterable
            value-key="id"
            :render-after-expand="false"
            check-strictly
            style="width: 240px"
          />
        </div>
        <el-upload
          class="upload-demo"
          drag
          multiple
          accept="application/json,application/text"
          :before-upload="handlerBeforeUpload"
          :http-request="handlerImportDefinition"
        >
          <el-icon class="UploadFilled"><upload-filled /></el-icon>
          <div class="el-upload__text"><em>点击上传，选择JSON流程文件</em></div>
          <div class="el-upload__text">仅支持json格式文件</div>
          <div class="el-upload__text">PS:如若部署请部署从本项目模型管理导出的数据</div>
        </el-upload>
      </div>
    </el-dialog>

    <!-- 新增/编辑流程定义 -->
    <el-dialog v-model="modelDialog.visible" :title="modelDialog.title" width="650px" append-to-body :close-on-click-modal="false">
      <template #footer>
        <el-form ref="defFormRef" :model="form" :rules="rules" label-width="120px">
          <el-form-item label="流程类别" prop="category">
            <el-tree-select
              v-model="form.category"
              :data="categoryOptions"
              :props="{ value: 'id', label: 'label', children: 'children' } as any"
              filterable
              value-key="id"
              :render-after-expand="false"
              check-strictly
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="流程编码" prop="flowCode">
            <el-input v-model="form.flowCode" placeholder="请输入流程编码" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="流程名称" prop="flowName">
            <el-input v-model="form.flowName" placeholder="请输入流程名称" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="设计器模式" prop="modelValue">
            <el-radio-group v-model="form.modelValue" :disabled="!!form.id">
              <el-radio value="CLASSICS" size="large" border>经典模式</el-radio>
              <el-radio value="MIMIC" size="large" border>仿钉钉模式</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="流程配置">
            <el-checkbox v-model="autoPass" label="下一节点执行人是当前任务处理人自动审批" />
          </el-form-item>
          <el-form-item label="是否动态表单" prop="formCustom">
            <el-radio-group v-model="form.formCustom">
              <el-radio value="Y" size="large" border>是</el-radio>
              <el-radio value="N" size="large" border>否</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="表单路径" prop="formPath">
            <el-input v-model="form.formPath" placeholder="请输入表单路径" maxlength="100" show-word-limit />
          </el-form-item>
        </el-form>
        <div class="dialog-footer">
          <el-button @click="modelDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="processDefinition" lang="ts">
import { listDefinition, deleteDefinition, active, importDef, unPublishList, publish, add, edit, getInfo, copy } from '@/api/workflow/definition';
import { categoryTree } from '@/api/workflow/category';
import { CategoryTreeVO } from '@/api/workflow/category/types';
import { FlowDefinitionQuery, FlowDefinitionVo, FlowDefinitionForm } from '@/api/workflow/definition/types';
import { UploadRequestOptions, TabsPaneContext } from 'element-plus';
import { ElMessageBoxOptions } from 'element-plus/es/components/message-box/src/message-box.type';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const queryFormRef = ref<ElFormInstance>();
const categoryTreeRef = ref<ElTreeInstance>();

const loading = ref(true);
const ids = ref<Array<any>>([]);
const flowCodeList = ref<Array<any>>([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const uploadDialogLoading = ref(false);
const processDefinitionList = ref<FlowDefinitionVo[]>([]);
const categoryOptions = ref<CategoryTreeVO[]>([]);
const categoryName = ref('');
const autoPass = ref(false);
/** 部署文件分类选择 */
const selectCategory = ref();
const defFormRef = ref<ElFormInstance>();
const activeName = ref('0');
const uploadDialog = reactive<DialogOption>({
  visible: false,
  title: '部署流程文件'
});

const processDefinitionDialog = reactive<DialogOption>({
  visible: false,
  title: '历史版本'
});

const modelDialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

// 查询参数
const queryParams = ref<FlowDefinitionQuery>({
  pageNum: 1,
  pageSize: 10,
  flowName: undefined,
  flowCode: undefined,
  category: undefined
});
const rules = {
  category: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  flowName: [{ required: true, message: '流程定义名称不能为空', trigger: 'blur' }],
  formCustom: [{ required: true, message: '请选择是否动态表单', trigger: 'change' }],
  modelValue: [{ required: true, message: '设计器模式不能为空', trigger: 'change' }],
  flowCode: [{ required: true, message: '流程定义编码不能为空', trigger: 'blur' }]
};
const initFormData: FlowDefinitionForm = {
  id: '',
  flowName: '',
  flowCode: '',
  category: '',
  ext: '',
  formPath: '',
  formCustom: '',
  modelValue: ''
};
//流程定义参数
const form = ref<FlowDefinitionForm>({
  id: '',
  flowName: '',
  flowCode: '',
  category: '',
  ext: '',
  formPath: '',
  formCustom: '',
  modelValue: ''
});
onMounted(() => {
  getPageList();
  getTreeselect();
});

/** 节点单击事件 */
const handleNodeClick = (data: CategoryTreeVO) => {
  queryParams.value.category = data.id;
  if (data.id === '0') {
    queryParams.value.category = '';
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
    categoryTreeRef.value.filter(categoryName.value);
  },
  {
    flush: 'post' // watchEffect会在DOM挂载或者更新之前就会触发，此属性控制在DOM元素更新后运行
  }
);

/** 查询流程分类下拉树结构 */
const getTreeselect = async () => {
  const res = await categoryTree();
  categoryOptions.value = res.data;
};
const handleClick = (tab: TabsPaneContext, event: Event) => {
  // v-model处理有延迟 需要手动处理
  activeName.value = tab.index;
  handleQuery();
};
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  if (activeName.value === '0') {
    getList();
  } else {
    getUnPublishList();
  }
};
/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  queryParams.value.category = '';
  queryParams.value.pageNum = 1;
  queryParams.value.pageSize = 10;
  handleQuery();
};
// 多选框选中数据
const handleSelectionChange = (selection: any) => {
  ids.value = selection.map((item: any) => item.id);
  flowCodeList.value = selection.map((item: any) => item.flowCode);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};
//分页
const getPageList = async () => {
  const query = proxy.$route.query;
  if (query.activeName) {
    activeName.value = query.activeName;
    proxy.$route.query.activeName = '';
  }
  if (activeName.value === '0') {
    getList();
  } else {
    getUnPublishList();
  }
};
//分页
const getList = async () => {
  loading.value = true;
  const resp = await listDefinition(queryParams.value);
  processDefinitionList.value = resp.rows;
  total.value = resp.total;
  loading.value = false;
};
//查询未发布的流程定义列表
const getUnPublishList = async () => {
  loading.value = true;
  const resp = await unPublishList(queryParams.value);
  processDefinitionList.value = resp.rows;
  total.value = resp.total;
  loading.value = false;
};

/** 删除按钮操作 */
const handleDelete = async (row?: FlowDefinitionVo) => {
  const id = row?.id || ids.value;
  const defList = processDefinitionList.value.filter((x) => id.indexOf(x.id) != -1).map((x) => x.flowCode);
  await proxy?.$modal.confirm('是否确认删除流程定义编码为【' + defList + '】的数据项？');
  loading.value = true;
  await deleteDefinition(id).finally(() => (loading.value = false));
  await handleQuery();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 发布流程定义 */
const handlePublish = async (row?: FlowDefinitionVo) => {
  await proxy?.$modal.confirm(
    '是否确认发布流程定义编码为【' + row.flowCode + '】版本为【' + row.version + '】的数据项？，发布后会将已发布流程定义改为失效！'
  );
  loading.value = true;
  await publish(row.id).finally(() => (loading.value = false));
  processDefinitionDialog.visible = false;
  activeName.value = '0';
  await handleQuery();
  proxy?.$modal.msgSuccess('发布成功');
};
/** 挂起/激活 */
const handleProcessDefState = async (row: FlowDefinitionVo, status: number | string | boolean) => {
  let msg: string;
  if (status === 0) {
    msg = `暂停后，此流程下的所有任务都不允许往后流转，您确定挂起【${row.flowName || row.flowCode}】吗？`;
  } else {
    msg = `启动后，此流程下的所有任务都允许往后流转，您确定激活【${row.flowName || row.flowCode}】吗？`;
  }
  try {
    loading.value = true;
    await proxy?.$modal.confirm(msg);
    await active(row.id, !!status);
    await handleQuery();
    proxy?.$modal.msgSuccess('操作成功');
  } catch (error) {
    row.activityStatus = status === 0 ? 1 : 0;
    console.error(error);
  } finally {
    loading.value = false;
  }
};

//上传文件前的钩子
const handlerBeforeUpload = () => {
  if (selectCategory.value === 'ALL') {
    proxy?.$modal.msgError('顶级节点不可作为分类！');
    return false;
  }
  if (!selectCategory.value) {
    proxy?.$modal.msgError('请选择左侧要上传的分类！');
    return false;
  }
};
//部署文件
const handlerImportDefinition = (data: UploadRequestOptions): XMLHttpRequest => {
  const formData = new FormData();
  uploadDialogLoading.value = true;
  formData.append('file', data.file);
  formData.append('category', selectCategory.value);
  importDef(formData)
    .then(() => {
      uploadDialog.visible = false;
      proxy?.$modal.msgSuccess('部署成功');
      activeName.value = '1';
      handleQuery();
    })
    .finally(() => {
      uploadDialogLoading.value = false;
    });
  return;
};
/**
 * 设计流程
 * @param row
 */
const design = async (row: FlowDefinitionVo) => {
  proxy.$router.push({
    path: `/workflow/design/index`,
    query: {
      definitionId: row.id,
      disabled: false,
      activeName: activeName.value
    }
  });
};

/**
 * 查看流程
 * @param row
 */
const designView = async (row: FlowDefinitionVo) => {
  proxy.$router.push({
    path: `/workflow/design/index`,
    query: {
      definitionId: row.id,
      disabled: true,
      activeName: activeName.value
    }
  });
};
/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  defFormRef.value?.resetFields();
};
/**
 * 新增
 */
const handleAdd = async () => {
  reset();
  if (queryParams.value.category != '') {
    form.value.category = queryParams.value.category;
  }
  form.value.modelValue = 'CLASSICS';
  form.value.formCustom = 'N';
  modelDialog.visible = true;
  modelDialog.title = '新增流程';
};
/** 修改按钮操作 */
const handleUpdate = async (row?: FlowDefinitionVo) => {
  reset();
  const id = row?.id || ids.value[0];
  const res = await getInfo(id);
  Object.assign(form.value, res.data);
  autoPass.value = false;
  if (form.value.ext != null && form.value.ext != '') {
    const extJson = JSON.parse(form.value.ext);
    if (extJson.autoPass != null && extJson.autoPass != '') {
      autoPass.value = extJson.autoPass;
    }
  }
  modelDialog.visible = true;
  modelDialog.title = '修改流程';
};

const handleSubmit = async () => {
  defFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true;
      const ext = {};
      ext.autoPass = autoPass.value;
      form.value.ext = JSON.stringify(ext);
      if (form.value.id) {
        await edit(form.value).finally(() => (loading.value = false));
      } else {
        await add(form.value).finally(() => (loading.value = false));
        activeName.value = '1';
      }
      proxy?.$modal.msgSuccess('操作成功');
      modelDialog.visible = false;
      handleQuery();
    }
  });
};
//复制
const handleCopyDef = async (row: FlowDefinitionVo) => {
  ElMessageBox.confirm(`是否确认复制【${row.flowCode}】版本为【${row.version}】的流程定义！`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  } as ElMessageBoxOptions).then(() => {
    loading.value = true;
    copy(row.id)
      .then((resp) => {
        if (resp.code === 200) {
          proxy?.$modal.msgSuccess('操作成功');
          activeName.value = '1';
          handleQuery();
        }
      })
      .finally(() => (loading.value = false));
  });
};

/** 导出按钮操作 */
const handleExportDef = () => {
  proxy?.download(`/workflow/definition/exportDef/${ids.value[0]}`, {}, `${flowCodeList.value[0]}.json`);
};
</script>
