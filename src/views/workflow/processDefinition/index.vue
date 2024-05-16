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
              <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" label-width="120px">
                <el-form-item label="流程定义名称" prop="name">
                  <el-input v-model="queryParams.name" placeholder="请输入流程定义名称" clearable @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item label="流程定义KEY" prop="key">
                  <el-input v-model="queryParams.key" placeholder="请输入流程定义KEY" clearable @keyup.enter="handleQuery" />
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
                <el-button type="danger" icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="primary" icon="UploadFilled" @click="uploadDialog.visible = true">部署流程文件</el-button>
              </el-col>
              <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
            </el-row>
          </template>

          <el-table v-loading="loading" border :data="processDefinitionList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column align="center" type="index" label="序号" width="60"></el-table-column>
            <el-table-column align="center" prop="name" label="流程定义名称" :show-overflow-tooltip="true"></el-table-column>
            <el-table-column align="center" prop="key" label="标识KEY" width="80"></el-table-column>
            <el-table-column align="center" prop="version" label="版本号" width="80">
              <template #default="scope"> v{{ scope.row.version }}.0</template>
            </el-table-column>
            <el-table-column align="center" prop="resourceName" label="流程XML" width="100" :show-overflow-tooltip="true">
              <template #default="scope">
                <el-link type="primary" @click="clickPreview(scope.row.id, 'xml')">{{ scope.row.resourceName }}</el-link>
              </template>
            </el-table-column>
            <el-table-column align="center" prop="diagramResourceName" label="流程图片" width="100" :show-overflow-tooltip="true">
              <template #default="scope">
                <el-link type="primary" @click="clickPreview(scope.row.id, 'bpmn')">{{ scope.row.diagramResourceName }}</el-link>
              </template>
            </el-table-column>
            <el-table-column align="center" prop="suspensionState" label="状态" width="80">
              <template #default="scope">
                <el-tag v-if="scope.row.suspensionState == 1" type="success">激活</el-tag>
                <el-tag v-else type="danger">挂起</el-tag>
              </template>
            </el-table-column>
            <el-table-column align="center" prop="deploymentTime" label="部署时间" width="120" :show-overflow-tooltip="true"></el-table-column>
            <el-table-column align="center" label="表名/表单KEY" width="120" :show-overflow-tooltip="true">
              <template #default="scope">
                <span v-if="scope.row.wfDefinitionConfigVo">
                  {{ scope.row.wfDefinitionConfigVo.tableName }}
                </span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" align="center" width="220" class-name="small-padding fixed-width">
              <template #default="scope">
                <el-row :gutter="10" class="mb8">
                  <el-col :span="1.5">
                    <el-button
                      link
                      type="primary"
                      size="small"
                      :icon="scope.row.suspensionState === 1 ? 'Lock' : 'Unlock'"
                      @click="handleProcessDefState(scope.row)"
                    >
                      {{ scope.row.suspensionState === 1 ? '挂起流程' : '激活流程' }}
                    </el-button>
                  </el-col>
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="Document" @click="getProcessDefinitionHitoryList(scope.row.id, scope.row.key)">
                      历史版本
                    </el-button>
                  </el-col>
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
                  </el-col>
                </el-row>
                <el-row :gutter="10" class="mb8">
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="Sort" @click="handleConvertToModel(scope.row)"> 转换模型 </el-button>
                  </el-col>
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="Tickets" @click="handleDefinitionConfigOpen(scope.row)">绑定业务</el-button>
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
    <!-- 预览图片或xml -->
    <process-preview ref="previewRef" />

    <!-- 部署文件 -->
    <el-dialog v-if="uploadDialog.visible" v-model="uploadDialog.visible" :title="uploadDialog.title" width="30%">
      <div v-loading="uploadDialogLoading">
        <div class="mb5">
          <el-text class="mx-1" size="large"><span class="text-danger">*</span>请选择部署流程分类：</el-text>
          <el-tree-select
            v-model="selectCategory"
            :data="categoryOptions"
            :props="{ value: 'categoryCode', label: 'categoryName', children: 'children' }"
            filterable
            value-key="categoryCode"
            :render-after-expand="false"
            check-strictly
            style="width: 240px"
          />
        </div>
        <el-upload
          class="upload-demo"
          drag
          multiple
          accept="application/zip,application/xml,.bpmn"
          :before-upload="handlerBeforeUpload"
          :http-request="handerDeployProcessFile"
        >
          <el-icon class="UploadFilled"><upload-filled /></el-icon>
          <div class="el-upload__text"><em>点击上传，选择BPMN流程文件</em></div>
          <div class="el-upload__text">仅支持 .zip、.bpmn20.xml、bpmn 格式文件</div>
          <div class="el-upload__text">PS:如若部署请部署从本项目模型管理导出的数据</div>
        </el-upload>
      </div>
    </el-dialog>

    <!-- 历史版本 -->
    <el-dialog v-if="processDefinitionDialog.visible" v-model="processDefinitionDialog.visible" :title="processDefinitionDialog.title" width="70%">
      <el-table v-loading="loading" :data="processDefinitionHistoryList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column align="center" type="index" label="序号" width="60"></el-table-column>
        <el-table-column align="center" prop="name" label="流程定义名称" :show-overflow-tooltip="true" min-width="80"></el-table-column>
        <el-table-column align="center" prop="key" label="标识KEY"></el-table-column>
        <el-table-column align="center" prop="version" label="版本号" width="90">
          <template #default="scope"> v{{ scope.row.version }}.0</template>
        </el-table-column>
        <el-table-column align="center" prop="resourceName" label="流程XML" min-width="80" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-link type="primary" @click="clickPreviewXML(scope.row.id)">{{ scope.row.resourceName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="diagramResourceName" label="流程图片" min-width="80" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-link type="primary" @click="clickPreviewImg(scope.row.id)">{{ scope.row.diagramResourceName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="suspensionState" label="状态" min-width="70">
          <template #default="scope">
            <el-tag v-if="scope.row.suspensionState == 1" type="success">激活</el-tag>
            <el-tag v-else type="danger">挂起</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="deploymentTime" label="部署时间" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column fixed="right" label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button
                  link
                  type="primary"
                  size="small"
                  :icon="scope.row.suspensionState === 1 ? 'Lock' : 'Unlock'"
                  @click="handleProcessDefState(scope.row)"
                >
                  {{ scope.row.suspensionState === 1 ? '挂起流程' : '激活流程' }}
                </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="text" size="small" icon="Tickets" @click="handleDefinitionConfigOpen(scope.row)">绑定业务</el-button>
              </el-col>
            </el-row>
            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button link type="primary" icon="Sort" size="small" @click="handleConvertToModel(scope.row)"> 转换模型 </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button link type="primary" icon="Delete" size="small" @click="handleDelete(scope.row)">删除</el-button>
              </el-col>
            </el-row>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 表单配置 -->
    <el-dialog
      v-model="definitionConfigDialog.visible"
      :title="definitionConfigDialog.title"
      width="650px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form :model="definitionConfigForm" label-width="auto">
        <el-form-item label="流程KEY">
          <el-input v-model="definitionConfigForm.processKey" disabled />
        </el-form-item>
        <el-form-item label="表名" prop="formId">
          <el-input v-model="definitionConfigForm.tableName" placeholder="示例:test_leave" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="definitionConfigForm.remark" type="textarea" resize="none" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="definitionConfigDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handlerSaveForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup name="processDefinition">
import {
  listProcessDefinition,
  definitionImage,
  definitionXml,
  deleteProcessDefinition,
  updateDefinitionState,
  convertToModel,
  deployProcessFile,
  getListByKey
} from '@/api/workflow/processDefinition';
import { getByTableNameNotDefId, getByDefId, saveOrUpdate } from '@/api/workflow/definitionConfig';
import ProcessPreview from './components/processPreview.vue';
import { listCategory } from '@/api/workflow/category';
import { CategoryVO } from '@/api/workflow/category/types';
import { ProcessDefinitionQuery, ProcessDefinitionVO } from '@/api/workflow/processDefinition/types';
import { DefinitionConfigForm } from '@/api/workflow/definitionConfig/types';
import { UploadRequestOptions, ElMessage, ElMessageBox } from 'element-plus';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const previewRef = ref<InstanceType<typeof ProcessPreview>>();
const queryFormRef = ref<ElFormInstance>();
const categoryTreeRef = ref<ElTreeInstance>();
const definitionConfigForm = ref<DefinitionConfigForm>({});

type CategoryOption = {
  categoryCode: string;
  categoryName: string;
  children?: CategoryOption[];
};

const loading = ref(true);
const ids = ref<Array<any>>([]);
const deploymentIds = ref<Array<any>>([]);
const keys = ref<Array<any>>([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const uploadDialogLoading = ref(false);
const processDefinitionList = ref<ProcessDefinitionVO[]>([]);
const processDefinitionHistoryList = ref<ProcessDefinitionVO[]>([]);
const categoryOptions = ref<CategoryOption[]>([]);
const categoryName = ref('');
/** 部署文件分类选择 */
const selectCategory = ref();

const uploadDialog = reactive<DialogOption>({
  visible: false,
  title: '部署流程文件'
});

const processDefinitionDialog = reactive<DialogOption>({
  visible: false,
  title: '历史版本'
});

const definitionConfigDialog = reactive<DialogOption>({
  visible: false,
  title: '流程定义配置'
});

// 查询参数
const queryParams = ref<ProcessDefinitionQuery>({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  key: undefined,
  categoryCode: undefined
});

onMounted(() => {
  getList();
  getTreeselect();
});

/** 节点单击事件 */
const handleNodeClick = (data: CategoryVO) => {
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
    categoryTreeRef.value.filter(categoryName.value);
  },
  {
    flush: 'post' // watchEffect会在DOM挂载或者更新之前就会触发，此属性控制在DOM元素更新后运行
  }
);

/** 查询流程分类下拉树结构 */
const getTreeselect = async () => {
  const res = await listCategory();
  categoryOptions.value = [];
  const data: CategoryOption = { categoryCode: 'ALL', categoryName: '顶级节点', children: [] };
  data.children = proxy?.handleTree<CategoryOption>(res.data, 'id', 'parentId');
  categoryOptions.value.push(data);
};

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
const handleSelectionChange = (selection: any) => {
  ids.value = selection.map((item: any) => item.id);
  deploymentIds.value = selection.map((item: any) => item.deploymentId);
  keys.value = selection.map((item: any) => item.key);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};
//分页
const getList = async () => {
  loading.value = true;
  const resp = await listProcessDefinition(queryParams.value);
  processDefinitionList.value = resp.rows;
  total.value = resp.total;
  loading.value = false;
};
//获取历史流程定义
const getProcessDefinitionHitoryList = async (id: string, key: string) => {
  processDefinitionDialog.visible = true;
  loading.value = true;
  const resp = await getListByKey(key);
  if (resp.data && resp.data.length > 0) {
    processDefinitionHistoryList.value = resp.data.filter((item: any) => item.id !== id);
  }
  loading.value = false;
};

type PreviewType = 'xml' | 'bpmn';
//预览 公共方法
const clickPreview = async (id: string, type: PreviewType) => {
  loading.value = true;
  const resp = await definitionXml(id);
  if (previewRef.value) {
    const xmlStr = resp.data.xmlStr;
    loading.value = false;
    previewRef.value.openDialog(xmlStr, type);
  }
};

/** 删除按钮操作 */
const handleDelete = async (row?: ProcessDefinitionVO) => {
  const id = row?.id || ids.value;
  const deployIds = row?.deploymentId || deploymentIds.value;
  const defKeys = row?.key || keys.value;
  await proxy?.$modal.confirm('是否确认删除流程定义KEY为【' + defKeys + '】的数据项？');
  loading.value = true;
  await deleteProcessDefinition(deployIds, id).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};
/** 挂起/激活 */
const handleProcessDefState = async (row: ProcessDefinitionVO) => {
  let msg: string;
  if (row.suspensionState === 1) {
    msg = `暂停后，此流程下的所有任务都不允许往后流转，您确定挂起【${row.name || row.key}】吗？`;
  } else {
    msg = `启动后，此流程下的所有任务都允许往后流转，您确定激活【${row.name || row.key}】吗？`;
  }
  await proxy?.$modal.confirm(msg);
  loading.value = true;
  await updateDefinitionState(row.id).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('操作成功');
};
/** 流程定义转换为模型 */
const handleConvertToModel = async (row: ProcessDefinitionVO) => {
  await proxy?.$modal.confirm('是否确认转换流程定义key为【' + row.key + '】的数据项？');
  await convertToModel(row.id).finally(() => (loading.value = false));
  getList();
  proxy?.$modal.msgSuccess('操作成功');
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
const handerDeployProcessFile = (data: UploadRequestOptions): XMLHttpRequest => {
  let formData = new FormData();
  uploadDialogLoading.value = true;
  formData.append('file', data.file);
  formData.append('categoryCode', selectCategory.value);
  deployProcessFile(formData)
    .then(() => {
      uploadDialog.visible = false;
      proxy?.$modal.msgSuccess('部署成功');
      handleQuery();
    })
    .finally(() => {
      uploadDialogLoading.value = false;
    });
  return;
};
//打开流程定义配置
const handleDefinitionConfigOpen = async (row: ProcessDefinitionVO) => {
  definitionConfigDialog.visible = true;
  definitionConfigForm.value.processKey = row.key;
  definitionConfigForm.value.definitionId = row.id;
  definitionConfigForm.value.version = row.version;
  const resp = await getByDefId(row.id);
  if (resp.data) {
    definitionConfigForm.value = resp.data;
  } else {
    definitionConfigForm.value.tableName = undefined;
    definitionConfigForm.value.remark = undefined;
  }
};
//保存表单
const handlerSaveForm = async () => {
  getByTableNameNotDefId(definitionConfigForm.value.tableName, definitionConfigForm.value.definitionId).then((res) => {
    if (res.data && res.data.length > 0) {
      ElMessageBox.confirm('表名已被【' + res.data[0].processKey + '】版本v' + res.data[0].version + '.0绑定确认后将会删除绑定的流程KEY!', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        saveOrUpdate(definitionConfigForm.value).then((resp) => {
          if (resp.code === 200) {
            proxy?.$modal.msgSuccess('操作成功');
            definitionConfigDialog.visible = false;
            getList();
          }
        });
      });
    } else {
      saveOrUpdate(definitionConfigForm.value).then((resp) => {
        if (resp.code === 200) {
          proxy?.$modal.msgSuccess('操作成功');
          definitionConfigDialog.visible = false;
          getList();
        }
      });
    }
  });
};
</script>
