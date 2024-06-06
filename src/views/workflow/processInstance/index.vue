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
        <div class="mb-[10px]">
          <el-card shadow="hover" class="text-center">
            <el-radio-group v-model="tab" @change="changeTab(tab)">
              <el-radio-button value="running">运行中</el-radio-button>
              <el-radio-button value="finish">已完成</el-radio-button>
            </el-radio-group>
          </el-card>
        </div>
        <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
          <div v-show="showSearch" class="mb-[10px]">
            <el-card shadow="hover">
              <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" label-width="120px">
                <el-form-item label="流程定义名称" prop="name">
                  <el-input v-model="queryParams.name" placeholder="请输入流程定义名称" @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item label="流程定义KEY" prop="key">
                  <el-input v-model="queryParams.key" placeholder="请输入流程定义KEY" @keyup.enter="handleQuery" />
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
                <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
              </el-col>
              <right-toolbar v-model:showSearch="showSearch" @query-table="handleQuery"></right-toolbar>
            </el-row>
          </template>

          <el-table v-loading="loading" border :data="processInstanceList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column align="center" type="index" label="序号" width="60"></el-table-column>
            <el-table-column :show-overflow-tooltip="true" align="center" label="流程定义名称">
              <template #default="scope">
                <span>{{ scope.row.processDefinitionName }}v{{ scope.row.processDefinitionVersion }}.0</span>
              </template>
            </el-table-column>
            <el-table-column align="center" prop="processDefinitionKey" label="流程定义KEY"></el-table-column>
            <el-table-column align="center" prop="processDefinitionVersion" label="版本号" width="90">
              <template #default="scope"> v{{ scope.row.processDefinitionVersion }}.0</template>
            </el-table-column>
            <el-table-column v-if="tab === 'running'" align="center" prop="isSuspended" label="状态" min-width="70">
              <template #default="scope">
                <el-tag v-if="!scope.row.isSuspended" type="success">激活</el-tag>
                <el-tag v-else type="danger">挂起</el-tag>
              </template>
            </el-table-column>
            <el-table-column align="center" label="流程状态" min-width="70">
              <template #default="scope">
                <dict-tag :options="wf_business_status" :value="scope.row.businessStatus"></dict-tag>
              </template>
            </el-table-column>
            <el-table-column align="center" prop="startTime" label="启动时间" width="160"></el-table-column>
            <el-table-column v-if="tab === 'finish'" align="center" prop="endTime" label="结束时间" width="160"></el-table-column>
            <el-table-column label="操作" align="center" :width="130">
              <template #default="scope">
                <el-row v-if="tab === 'running'" :gutter="10" class="mb8">
                  <el-col :span="1.5">
                    <el-popover :ref="`popoverRef${scope.$index}`" trigger="click" placement="left" :width="300">
                      <el-input v-model="deleteReason" resize="none" :rows="3" type="textarea" placeholder="请输入作废原因" />
                      <div style="text-align: right; margin: 5px 0px 0px 0px">
                        <el-button size="small" text @click="cancelPopover(scope.$index)">取消</el-button>
                        <el-button size="small" type="primary" @click="handleInvalid(scope.row)">确认</el-button>
                      </div>
                      <template #reference>
                        <el-button link type="primary" size="small" icon="CircleClose">作废</el-button>
                      </template>
                    </el-popover>
                  </el-col>
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
                  </el-col>
                </el-row>
                <el-row :gutter="10" class="mb8">
                  <el-col :span="1.5">
                    <el-button link type="primary" size="small" icon="View" @click="handleView(scope.row)">查看</el-button>
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
            @pagination="handleQuery"
          />
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-if="processDefinitionDialog.visible" v-model="processDefinitionDialog.visible" :title="processDefinitionDialog.title" width="70%">
      <el-table v-loading="loading" :data="processDefinitionHistoryList">
        <el-table-column fixed align="center" type="index" label="序号" width="60"></el-table-column>
        <el-table-column fixed align="center" prop="name" label="流程定义名称"></el-table-column>
        <el-table-column align="center" prop="key" label="标识Key"></el-table-column>
        <el-table-column align="center" prop="version" label="版本号" width="90">
          <template #default="scope"> v{{ scope.row.version }}.0</template>
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
            <el-button link type="primary" size="small" icon="Sort" @click="handleChange(scope.row.id)">切换</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  getPageByRunning,
  getPageByFinish,
  deleteRunAndHisInstance,
  deleteFinishAndHisInstance,
  deleteRunInstance
} from '@/api/workflow/processInstance';
import { getListByKey, migrationDefinition } from '@/api/workflow/processDefinition';
import { listCategory } from '@/api/workflow/category';
import { CategoryVO } from '@/api/workflow/category/types';
import { ProcessInstanceQuery, ProcessInstanceVO } from '@/api/workflow/processInstance/types';
import workflowCommon from '@/api/workflow/workflowCommon';
import { RouterJumpVo } from '@/api/workflow/workflowCommon/types';
//审批记录组件
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_business_status } = toRefs<any>(proxy?.useDict('wf_business_status'));
const queryFormRef = ref<ElFormInstance>();
const categoryTreeRef = ref<ElTreeInstance>();

// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref<Array<any>>([]);
// 选中业务id数组
const businessKeys = ref<Array<any>>([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 流程定义id
const processDefinitionId = ref<string>('');
// 模型定义表格数据
const processInstanceList = ref<ProcessInstanceVO[]>([]);
const processDefinitionHistoryList = ref<Array<any>>([]);
const categoryOptions = ref<CategoryOption[]>([]);
const categoryName = ref('');

const processDefinitionDialog = reactive<DialogOption>({
  visible: false,
  title: '流程定义'
});

type CategoryOption = {
  categoryCode: string;
  categoryName: string;
  children?: CategoryOption[];
};

const tab = ref('running');
// 作废原因
const deleteReason = ref('');
// 查询参数
const queryParams = ref<ProcessInstanceQuery>({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  key: undefined,
  categoryCode: undefined
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
  if ('running' === tab.value) {
    getProcessInstanceRunningList();
  } else {
    getProcessInstanceFinishList();
  }
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
const handleSelectionChange = (selection: ProcessInstanceVO[]) => {
  ids.value = selection.map((item: any) => item.id);
  businessKeys.value = selection.map((item: any) => item.businessKey);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};
//分页
const getProcessInstanceRunningList = () => {
  loading.value = true;
  getPageByRunning(queryParams.value).then((resp) => {
    processInstanceList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};
//分页
const getProcessInstanceFinishList = () => {
  loading.value = true;
  getPageByFinish(queryParams.value).then((resp) => {
    processInstanceList.value = resp.rows;
    total.value = resp.total;
    loading.value = false;
  });
};

/** 删除按钮操作 */
const handleDelete = async (row: any) => {
  const businessKey = row.businessKey || businessKeys.value;
  await proxy?.$modal.confirm('是否确认删除业务id为【' + businessKey + '】的数据项？');
  loading.value = true;
  if ('running' === tab.value) {
    await deleteRunAndHisInstance(businessKey).finally(() => (loading.value = false));
    getProcessInstanceRunningList();
  } else {
    await deleteFinishAndHisInstance(businessKey).finally(() => (loading.value = false));
    getProcessInstanceFinishList();
  }
  proxy?.$modal.msgSuccess('删除成功');
};
const changeTab = async (data: string) => {
  processInstanceList.value = [];
  queryParams.value.pageNum = 1;
  if ('running' === data) {
    getProcessInstanceRunningList();
  } else {
    getProcessInstanceFinishList();
  }
};
/** 作废按钮操作 */
const handleInvalid = async (row: ProcessInstanceVO) => {
  await proxy?.$modal.confirm('是否确认作废业务id为【' + row.businessKey + '】的数据项？');
  loading.value = true;
  if ('running' === tab.value) {
    let param = {
      businessKey: row.businessKey,
      deleteReason: deleteReason.value
    };
    await deleteRunInstance(param).finally(() => (loading.value = false));
    getProcessInstanceRunningList();
    proxy?.$modal.msgSuccess('操作成功');
  }
};
const cancelPopover = async (index: any) => {
  (proxy?.$refs[`popoverRef${index}`] as any).hide(); //关闭弹窗
};
//获取流程定义
const getProcessDefinitionHitoryList = (id: string, key: string) => {
  processDefinitionDialog.visible = true;
  processDefinitionId.value = id;
  loading.value = true;
  getListByKey(key).then((resp) => {
    if (resp.data && resp.data.length > 0) {
      processDefinitionHistoryList.value = resp.data.filter((item: any) => item.id !== id);
    }
    loading.value = false;
  });
};
//切换流程版本
const handleChange = async (id: string) => {
  await proxy?.$modal.confirm('是否确认切换？');
  loading.value = true;
  migrationDefinition(processDefinitionId.value, id).then((resp) => {
    proxy?.$modal.msgSuccess('操作成功');
    getProcessInstanceRunningList();
    processDefinitionDialog.visible = false;
    loading.value = false;
  });
};
/** 查看按钮操作 */
const handleView = (row) => {
  const routerJumpVo = reactive<RouterJumpVo>({
    wfDefinitionConfigVo: row.wfDefinitionConfigVo,
    wfNodeConfigVo: row.wfNodeConfigVo,
    businessKey: row.businessKey,
    taskId: row.id,
    type: 'view'
  });
  workflowCommon.routerJump(routerJumpVo, proxy);
};

onMounted(() => {
  getProcessInstanceRunningList();
  getTreeselect();
});
</script>
