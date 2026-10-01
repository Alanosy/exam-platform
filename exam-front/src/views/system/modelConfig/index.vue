<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="名称" prop="configName">
              <el-input v-model="queryParams.configName" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="模型类型" prop="modelType">
              <el-select v-model="queryParams.modelType" placeholder="请选择模型类型" clearable >
                <el-option v-for="dict in model_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="模型名称" prop="modelName">
              <el-select v-model="queryParams.modelName" placeholder="请选择模型名称" clearable >
                <el-option v-for="dict in model_name" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="接口地址" prop="apiBase">
              <el-input v-model="queryParams.apiBase" placeholder="请输入接口地址" clearable @keyup.enter="handleQuery" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:modelConfig:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:modelConfig:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:modelConfig:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:modelConfig:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="modelConfigList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="主键" align="center" prop="id" v-if="true" />
        <el-table-column label="名称" align="center" prop="configName" />
        <el-table-column label="模型类型" align="center" prop="modelType">
          <template #default="scope">
            <dict-tag :options="model_type" :value="scope.row.modelType"/>
          </template>
        </el-table-column>
        <el-table-column label="模型名称" align="center" prop="modelName">
          <template #default="scope">
            <dict-tag :options="model_name" :value="scope.row.modelName"/>
          </template>
        </el-table-column>
        <el-table-column label="接口地址" align="center" prop="apiBase" />
        <el-table-column label="API密钥" align="center" prop="apiKey" />
        <el-table-column label="温度" align="center" prop="temperature" />
        <el-table-column label="最大输出" align="center" prop="maxTokens" />
        <el-table-column label="请求超时时间(ms)" align="center" prop="timeout" />
        <el-table-column label="失败重试次数" align="center" prop="retryCount" />
        <el-table-column label="优先级" align="center" prop="priority" />
        <el-table-column label="权重" align="center" prop="weight" />
        <el-table-column label="状态" align="center" prop="status" />
        <el-table-column label="备注" align="center" prop="remark" />
        <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:modelConfig:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:modelConfig:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改AI大模型配置对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="modelConfigFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType">
          <el-select v-model="form.modelType" placeholder="请选择模型类型">
            <el-option
                v-for="dict in model_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-select v-model="form.modelName" placeholder="请选择模型名称">
            <el-option
                v-for="dict in model_name"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="接口地址" prop="apiBase">
          <el-input v-model="form.apiBase" placeholder="请输入接口地址" />
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
            <el-input v-model="form.apiKey" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="温度" prop="temperature">
          <el-input v-model="form.temperature" placeholder="请输入温度" />
        </el-form-item>
        <el-form-item label="最大输出" prop="maxTokens">
          <el-input v-model="form.maxTokens" placeholder="请输入最大输出" />
        </el-form-item>
        <el-form-item label="请求超时时间(ms)" prop="timeout">
          <el-input v-model="form.timeout" placeholder="请输入请求超时时间(ms)" />
        </el-form-item>
        <el-form-item label="失败重试次数" prop="retryCount">
          <el-input v-model="form.retryCount" placeholder="请输入失败重试次数" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input v-model="form.priority" placeholder="请输入优先级" />
        </el-form-item>
        <el-form-item label="权重" prop="weight">
          <el-input v-model="form.weight" placeholder="请输入权重" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

<script setup name="ModelConfig" lang="ts">
import { listModelConfig, getModelConfig, delModelConfig, addModelConfig, updateModelConfig } from '@/api/system/modelConfig';
import { ModelConfigVO, ModelConfigQuery, ModelConfigForm } from '@/api/system/modelConfig/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { model_type, model_name } = toRefs<any>(proxy?.useDict('model_type', 'model_name'));

const modelConfigList = ref<ModelConfigVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const modelConfigFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: ModelConfigForm = {
  id: undefined,
  configName: undefined,
  modelType: undefined,
  modelName: undefined,
  apiBase: undefined,
  apiKey: undefined,
  temperature: undefined,
  maxTokens: undefined,
  timeout: undefined,
  retryCount: undefined,
  priority: undefined,
  weight: undefined,
  status: undefined,
  remark: undefined,
}
const data = reactive<PageData<ModelConfigForm, ModelConfigQuery>>({
  form: {...initFormData},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configName: undefined,
    modelType: undefined,
    modelName: undefined,
    apiBase: undefined,
    params: {
    }
  },
  rules: {
    id: [
      { required: true, message: "主键不能为空", trigger: "blur" }
    ],
    configName: [
      { required: true, message: "名称不能为空", trigger: "blur" }
    ],
    modelType: [
      { required: true, message: "模型类型不能为空", trigger: "change" }
    ],
    modelName: [
      { required: true, message: "模型名称不能为空", trigger: "change" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询AI大模型配置列表 */
const getList = async () => {
  loading.value = true;
  const res = await listModelConfig(queryParams.value);
  modelConfigList.value = res.rows;
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
  modelConfigFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: ModelConfigVO[]) => {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = "添加AI大模型配置";
}

/** 修改按钮操作 */
const handleUpdate = async (row?: ModelConfigVO) => {
  reset();
  const _id = row?.id || ids.value[0]
  const res = await getModelConfig(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "修改AI大模型配置";
}

/** 提交按钮 */
const submitForm = () => {
  modelConfigFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateModelConfig(form.value).finally(() =>  buttonLoading.value = false);
      } else {
        await addModelConfig(form.value).finally(() =>  buttonLoading.value = false);
      }
      proxy?.$modal.msgSuccess("操作成功");
      dialog.visible = false;
      await getList();
    }
  });
}

/** 删除按钮操作 */
const handleDelete = async (row?: ModelConfigVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除AI大模型配置编号为"' + _ids + '"的数据项？').finally(() => loading.value = false);
  await delModelConfig(_ids);
  proxy?.$modal.msgSuccess("删除成功");
  await getList();
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download('system/modelConfig/export', {
    ...queryParams.value
  }, `modelConfig_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  getList();
});
</script>
