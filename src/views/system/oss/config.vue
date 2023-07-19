<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div class="mb-[10px]" v-show="showSearch">
        <el-card shadow="hover">
          <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
            <el-form-item label="配置key" prop="configKey">
              <el-input v-model="queryParams.configKey" placeholder="配置key" clearable style="width: 200px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="桶名称" prop="bucketName">
              <el-input v-model="queryParams.bucketName" placeholder="请输入桶名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="是否默认" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
                <el-option key="0" label="是" value="0" />
                <el-option key="1" label="否" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="search" @click="handleQuery">搜索</el-button>
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:oss:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:oss:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:oss:remove']">
              删除
            </el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="ossConfigList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="主建" align="center" prop="ossConfigId" v-if="columns[0].visible" />
        <el-table-column label="配置key" align="center" prop="configKey" v-if="columns[1].visible" />
        <el-table-column label="访问站点" align="center" prop="endpoint" v-if="columns[2].visible" width="200" />
        <el-table-column label="自定义域名" align="center" prop="domain" v-if="columns[3].visible" width="200" />
        <el-table-column label="桶名称" align="center" prop="bucketName" v-if="columns[4].visible" />
        <el-table-column label="前缀" align="center" prop="prefix" v-if="columns[5].visible" />
        <el-table-column label="域" align="center" prop="region" v-if="columns[6].visible" />
        <el-table-column label="桶权限类型" align="center" prop="accessPolicy" v-if="columns[7].visible">
          <template #default="scope">
            <el-tag type="warning" v-if="scope.row.accessPolicy === '0'">private</el-tag>
            <el-tag type="success" v-if="scope.row.accessPolicy === '1'">public</el-tag>
            <el-tag type="info" v-if="scope.row.accessPolicy === '2'">custom</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否默认" align="center" prop="status" v-if="columns[8].visible">
          <template #default="scope">
            <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" align="center" width="150" class-name="small-padding">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:oss:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:oss:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改对象存储配置对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="800px" append-to-body>
      <el-form ref="ossConfigFormRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="配置key" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入配置key" />
        </el-form-item>
        <el-form-item label="访问站点" prop="endpoint">
          <el-input v-model="form.endpoint" placeholder="请输入访问站点" />
        </el-form-item>
        <el-form-item label="自定义域名" prop="domain">
          <el-input v-model="form.domain" placeholder="请输入自定义域名" />
        </el-form-item>
        <el-form-item label="accessKey" prop="accessKey">
          <el-input v-model="form.accessKey" placeholder="请输入accessKey" />
        </el-form-item>
        <el-form-item label="secretKey" prop="secretKey">
          <el-input v-model="form.secretKey" placeholder="请输入秘钥" show-password />
        </el-form-item>
        <el-form-item label="桶名称" prop="bucketName">
          <el-input v-model="form.bucketName" placeholder="请输入桶名称" />
        </el-form-item>
        <el-form-item label="前缀" prop="prefix">
          <el-input v-model="form.prefix" placeholder="请输入前缀" />
        </el-form-item>
        <el-form-item label="是否HTTPS">
          <el-radio-group v-model="form.isHttps">
            <el-radio v-for="dict in sys_yes_no" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="桶权限类型">
          <el-radio-group v-model="form.accessPolicy">
            <el-radio label="0">private</el-radio>
            <el-radio label="1">public</el-radio>
            <el-radio label="2">custom</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="域" prop="region">
          <el-input v-model="form.region" placeholder="请输入域" />
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

<script setup name="OssConfig" lang="ts">
import {
  listOssConfig,
  getOssConfig,
  delOssConfig,
  addOssConfig,
  updateOssConfig,
  changeOssConfigStatus
} from "@/api/system/ossConfig";
import { OssConfigForm, OssConfigQuery, OssConfigVO } from "@/api/system/ossConfig/types";


const { proxy } = getCurrentInstance() as ComponentInternalInstance
const { sys_yes_no } = toRefs<any>(proxy?.useDict("sys_yes_no"));

const ossConfigList = ref<OssConfigVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<number | string>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const ossConfigFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

// 列显隐信息
const columns = ref<FieldOption[]>([
  { key: 0, label: `主建`, visible: true },
  { key: 1, label: `配置key`, visible: false },
  { key: 2, label: `访问站点`, visible: true },
  { key: 3, label: `自定义域名`, visible: true },
  { key: 4, label: `桶名称`, visible: true },
  { key: 5, label: `前缀`, visible: true },
  { key: 6, label: `域`, visible: true },
  { key: 7, label: `桶权限类型`, visible: true },
  { key: 8, label: `状态`, visible: true }
]);


const initFormData: OssConfigForm = {
  ossConfigId: undefined,
  configKey: '',
  accessKey: '',
  secretKey: '',
  bucketName: '',
  prefix: '',
  endpoint: '',
  domain: '',
  isHttps: "N",
  accessPolicy: "1",
  region: '',
  status: "1",
  remark: '',
}
const data = reactive<PageData<OssConfigForm, OssConfigQuery>>({
  form: { ...initFormData },
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configKey: '',
    bucketName: '',
    status: '',
  },
  rules: {
    configKey: [{ required: true, message: "configKey不能为空", trigger: "blur" },],
    accessKey: [
      { required: true, message: "accessKey不能为空", trigger: "blur" },
      {
        min: 2,
        max: 200,
        message: "accessKey长度必须介于 2 和 100 之间",
        trigger: "blur",
      },
    ],
    secretKey: [
      { required: true, message: "secretKey不能为空", trigger: "blur" },
      {
        min: 2,
        max: 100,
        message: "secretKey长度必须介于 2 和 100 之间",
        trigger: "blur",
      },
    ],
    bucketName: [
      { required: true, message: "bucketName不能为空", trigger: "blur" },
      {
        min: 2,
        max: 100,
        message: "bucketName长度必须介于 2 和 100 之间",
        trigger: "blur",
      },
    ],
    endpoint: [
      { required: true, message: "endpoint不能为空", trigger: "blur" },
      {
        min: 2,
        max: 100,
        message: "endpoint名称长度必须介于 2 和 100 之间",
        trigger: "blur",
      },
    ],
    accessPolicy: [{ required: true, message: "accessPolicy不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询对象存储配置列表 */
const getList = async () => {
  loading.value = true;
  const res = await listOssConfig(queryParams.value);
  ossConfigList.value = res.rows;
  total.value = res.total;
  loading.value = false;
}
/** 取消按钮 */
const cancel = () => {
  dialog.visible = false;
  reset();
}
/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  ossConfigFormRef.value?.resetFields();
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
/** 选择条数  */
const handleSelectionChange = (selection: OssConfigVO[]) => {
  ids.value = selection.map(item => item.ossConfigId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}
/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = "添加对象存储配置";
}
/** 修改按钮操作 */
const handleUpdate = async (row?: OssConfigVO) => {
  reset();
  const ossConfigId = row?.ossConfigId || ids.value[0];
  const res = await getOssConfig(ossConfigId);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "修改对象存储配置";
}
/** 提交按钮 */
const submitForm = () => {
  ossConfigFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.ossConfigId) {
        await updateOssConfig(form.value).finally(() => buttonLoading.value = false);
      } else {
        await addOssConfig(form.value).finally(() => buttonLoading.value = false);
      }
      proxy?.$modal.msgSuccess("新增成功");
      dialog.visible = false;
      await getList();
    }
  });
}
/** 状态修改  */
const handleStatusChange = async (row: OssConfigVO) => {
  let text = row.status === "0" ? "启用" : "停用";
  try {
    await proxy?.$modal.confirm('确认要"' + text + '""' + row.configKey + '"配置吗?');
    await changeOssConfigStatus(row.ossConfigId, row.status, row.configKey);
    await getList()
    proxy?.$modal.msgSuccess(text + "成功");
  } catch { return } finally {
    row.status = row.status === "0" ? "1" : "0";
  }

}
/** 删除按钮操作 */
const handleDelete = async (row?: OssConfigVO) => {
  const ossConfigIds = row?.ossConfigId || ids.value;
  await proxy?.$modal.confirm('是否确认删除OSS配置编号为"' + ossConfigIds + '"的数据项?');
  loading.value = true;
  await delOssConfig(ossConfigIds).finally(() => loading.value = false);
  await getList();
  proxy?.$modal.msgSuccess("删除成功");

}

onMounted(() => {
  getList();
})
</script>
