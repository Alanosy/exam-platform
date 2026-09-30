<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="组件名称" prop="componentName">
              <el-input v-model="queryParams.componentName" placeholder="请输入组件名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="方法名" prop="methodName">
              <el-input v-model="queryParams.methodName" placeholder="请输入方法名" clearable @keyup.enter="handleQuery" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['workflow:spel:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['workflow:spel:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['workflow:spel:remove']">删除</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="spelList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="60" align="center">
          <template #default="scope">
            <span>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="组件名称" align="center">
          <template #default="scope">
            {{ scope.row.componentName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="方法名称" align="center">
          <template #default="scope">
            {{ scope.row.methodName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="参数名称" align="center">
          <template #default="scope">
            {{ scope.row.methodParams || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="SPEL表达式" align="center" prop="viewSpel" />
        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '0'">正常</el-tag>
            <el-tag v-else>停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" align="center">
          <template #default="scope">
            {{ scope.row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['workflow:spel:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['workflow:spel:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改流程spel表达式定义对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="550px" append-to-body>
      <el-form ref="spelFormRef" :model="form" :rules="rules" label-width="100px">
        <!-- 组件名称 -->
        <el-form-item label="组件名称" prop="componentName">
          <el-input v-model="form.componentName" placeholder="请输入组件名称" @input="updateViewSpel" />
          <template #label>
            <span>
              <el-tooltip content="注册到Spring容器中的组件名，如：spelRuleComponent" placement="top">
                <el-icon><question-filled /></el-icon>
              </el-tooltip>
              组件名称
            </span>
          </template>
        </el-form-item>
        <el-form-item label="方法名称" prop="methodName">
          <el-input v-model="form.methodName" placeholder="请输入方法名称" @input="updateViewSpel" />
          <template #label>
            <span>
              <el-tooltip content="组件中的方法名称，如：selectDeptLeaderById" placement="top">
                <el-icon><question-filled /></el-icon>
              </el-tooltip>
              方法名称
            </span>
          </template>
        </el-form-item>
        <el-form-item label="方法参数" prop="methodParams">
          <el-input v-model="form.methodParams" placeholder="请输入方法参数" @input="updateViewSpel" />
          <template #label>
            <span>
              <el-tooltip content="方法参数，如：deptId, 多个使用 ',' 分隔，单参数变量仅支持单个方法参数" placement="top">
                <el-icon><question-filled /></el-icon>
              </el-tooltip>
              方法参数
            </span>
          </template>
        </el-form-item>

        <!-- 改为只读文本展示 -->
        <el-form-item label="SPEL表达式">
          <span class="preview-box">
            {{ form.viewSpel || '例如：#{@组件名.方法名(#方法参数)} 或 ${方法参数}' }}
          </span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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

<script setup name="Spel" lang="ts">
import { listSpel, getSpel, delSpel, addSpel, updateSpel } from '@/api/workflow/spel';
import { SpelVO, SpelQuery, SpelForm } from '@/api/workflow/spel/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_show_hide, sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_show_hide', 'sys_normal_disable'));

const spelList = ref<SpelVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const spelFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: SpelForm = {
  id: undefined,
  componentName: undefined,
  methodName: undefined,
  methodParams: undefined,
  viewSpel: undefined,
  status: '0',
  remark: undefined,
}
const data = reactive<PageData<SpelForm, SpelQuery>>({
  form: {...initFormData},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    componentName: undefined,
    methodName: undefined,
    methodParams: undefined,
    viewSpel: undefined,
    status: '0',
    params: {
    }
  },
  rules: {
    status: [
      { required: true, message: "状态不能为空", trigger: "change" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询流程spel表达式定义列表 */
const getList = async () => {
  loading.value = true;
  const res = await listSpel(queryParams.value);
  spelList.value = res.rows;
  total.value = res.total;
  loading.value = false;
}

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
}

/** 表单重置 */
const reset = () => {
  form.value = {...initFormData};
  spelFormRef.value?.resetFields();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: SpelVO[]) => {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = "添加流程spel表达式定义";
}

/** 修改按钮操作 */
const handleUpdate = async (row?: SpelVO) => {
  reset();
  const _id = row?.id || ids.value[0]
  const res = await getSpel(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "修改流程spel表达式定义";
}

/** 提交按钮 */
const submitForm = () => {
  spelFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateSpel(form.value).finally(() =>  buttonLoading.value = false);
      } else {
        await addSpel(form.value).finally(() =>  buttonLoading.value = false);
      }
      proxy?.$modal.msgSuccess("操作成功");
      dialog.visible = false;
      await getList();
    }
  });
}

/** 删除按钮操作 */
const handleDelete = async (row?: SpelVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除流程spel表达式定义编号为"' + _ids + '"的数据项？').finally(() => loading.value = false);
  await delSpel(_ids);
  proxy?.$modal.msgSuccess("删除成功");
  await getList();
}

/** 控制是否显示 viewSpel 输入框 */
const showViewSpelInput = ref(false);

/** 更新 spel 预览值并决定是否显示输入框 */
const updateViewSpel = () => {
  const comp = (form.value.componentName || '').trim();
  const method = (form.value.methodName || '').trim();
  const paramStr = (form.value.methodParams || '').trim();

  if (!comp && !method && !paramStr) {
    form.value.viewSpel = '';
    return;
  }

  // 替换变量值：只有参数存在，组件和方法都不存在
  if (!comp && !method && paramStr) {
    const paramList = paramStr.split(',')
      .map(p => p.trim())
      .filter(p => p.length > 0);

    if (paramList.length === 1) {
      form.value.viewSpel = `\${${paramList[0]}}`;
      return;
    }
  }

  // 如果缺少组件或方法，提示填写
  if (!comp || !method) {
    form.value.viewSpel = '请填写组件名称和方法名';
    return;
  }

  let paramList = [];

  if (paramStr) {
    // 分割并过滤掉空参数
    paramList = paramStr.split(',')
      .map(p => p.trim())
      .filter(p => p.length > 0);
  }

  const paramPart = paramList.length > 0
    ? '(' + paramList.map(p => `#${p}`).join(',') + ')'
    : '()';

  form.value.viewSpel = `#{@${comp}.${method}${paramPart}}`;
};

/** 监听所有字段变化 */
watch(
  () => [form.value.componentName, form.value.methodName, form.value.methodParams],
  updateViewSpel
);

onMounted(() => {
  getList();
});
</script>
<style lang="scss">
.preview-box {
  width: 100%;
  padding: 10px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #333;
  font-family: monospace; /* 等宽字体更清晰 */
  white-space: nowrap;    /* 禁止换行 */
  overflow-x: auto;       /* 超出宽度时显示水平滚动条 */
  min-height: 36px;       /* 与 el-input 高度对齐 */
  line-height: 1.5;
}
</style>
