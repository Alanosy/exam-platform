<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="字典名称" prop="dictType">
              <el-select v-model="queryParams.dictType">
                <el-option v-for="item in typeOptions" :key="item.dictId" :label="item.dictName" :value="item.dictType" />
              </el-select>
            </el-form-item>
            <el-form-item label="字典标签" prop="dictLabel">
              <el-input v-model="queryParams.dictLabel" placeholder="请输入字典标签" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['system:dict:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:dict:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:dict:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:dict:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Close" @click="handleClose">关闭</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column v-if="false" label="字典编码" align="center" prop="dictCode" />
        <el-table-column label="字典标签" align="center" prop="dictLabel">
          <template #default="scope">
            <span
              v-if="(scope.row.listClass === '' || scope.row.listClass === 'default') && (scope.row.cssClass === '' || scope.row.cssClass == null)"
              >{{ scope.row.dictLabel }}</span
            >
            <el-tag
              v-else
              :type="scope.row.listClass === 'primary' || scope.row.listClass === 'default' ? 'primary' : scope.row.listClass"
              :class="scope.row.cssClass"
              >{{ scope.row.dictLabel }}</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="字典键值" align="center" prop="dictValue" />
        <el-table-column label="字典排序" align="center" prop="dictSort" />
        <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:dict:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:dict:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改参数配置对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="dataFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="字典类型">
          <el-input v-model="form.dictType" :disabled="true" />
        </el-form-item>
        <el-form-item label="数据标签" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="请输入数据标签" />
        </el-form-item>
        <el-form-item label="数据键值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="请输入数据键值" />
        </el-form-item>
        <el-form-item label="样式属性" prop="cssClass">
          <el-input v-model="form.cssClass" placeholder="请输入样式属性" />
        </el-form-item>
        <el-form-item label="显示排序" prop="dictSort">
          <el-input-number v-model="form.dictSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="回显样式" prop="listClass">
          <el-select v-model="form.listClass">
            <el-option
              v-for="item in listClassOptions"
              :key="item.value"
              :label="item.label + '(' + item.value + ')'"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Data" lang="ts">
import useDictStore from '@/store/modules/dict';
import { optionselect as getDictOptionselect, getType } from '@/api/system/dict/type';
import { listData, getData, delData, addData, updateData } from '@/api/system/dict/data';
import { DictTypeVO } from '@/api/system/dict/type/types';
import { DictDataForm, DictDataQuery, DictDataVO } from '@/api/system/dict/data/types';
import { RouteLocationNormalized } from 'vue-router';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();

const dataList = ref<DictDataVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const defaultDictType = ref('');
const typeOptions = ref<DictTypeVO[]>([]);

const dataFormRef = ref<ElFormInstance>();
const queryFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

// 数据标签回显样式
const listClassOptions = ref<Array<{ value: string; label: string }>>([
  { value: 'default', label: '默认' },
  { value: 'primary', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'info', label: '信息' },
  { value: 'warning', label: '警告' },
  { value: 'danger', label: '危险' }
]);

const initFormData: DictDataForm = {
  dictCode: undefined,
  dictLabel: '',
  dictValue: '',
  cssClass: '',
  listClass: 'primary',
  dictSort: 0,
  remark: ''
};
const data = reactive<PageData<DictDataForm, DictDataQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictName: '',
    dictType: '',
    dictLabel: ''
  },
  rules: {
    dictLabel: [{ required: true, message: '数据标签不能为空', trigger: 'blur' }],
    dictValue: [{ required: true, message: '数据键值不能为空', trigger: 'blur' }],
    dictSort: [{ required: true, message: '数据顺序不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询字典类型详细 */
const getTypes = async (dictId: string | number) => {
  const { data } = await getType(dictId);
  queryParams.value.dictType = data.dictType;
  defaultDictType.value = data.dictType;
  getList();
};

/** 查询字典类型列表 */
const getTypeList = async () => {
  const res = await getDictOptionselect();
  typeOptions.value = res.data;
};
/** 查询字典数据列表 */
const getList = async () => {
  loading.value = true;
  const res = await listData(queryParams.value);
  dataList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};
/** 取消按钮 */
const cancel = () => {
  dialog.visible = false;
  reset();
};
/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  dataFormRef.value?.resetFields();
};
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
/** 返回按钮操作 */
const handleClose = () => {
  const obj: RouteLocationNormalized = {
    fullPath: '',
    hash: '',
    matched: [],
    meta: undefined,
    name: undefined,
    params: undefined,
    query: undefined,
    redirectedFrom: undefined,
    path: '/system/dict'
  };
  proxy?.$tab.closeOpenPage(obj);
};
/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  queryParams.value.dictType = defaultDictType.value;
  handleQuery();
};
/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  form.value.dictType = queryParams.value.dictType;
  dialog.visible = true;
  dialog.title = '添加字典数据';
};
/** 多选框选中数据 */
const handleSelectionChange = (selection: DictDataVO[]) => {
  ids.value = selection.map((item) => item.dictCode);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};
/** 修改按钮操作 */
const handleUpdate = async (row?: DictDataVO) => {
  reset();
  const dictCode = row?.dictCode || ids.value[0];
  const res = await getData(dictCode);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改字典数据';
};
/** 提交按钮 */
const submitForm = () => {
  dataFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      form.value.dictCode ? await updateData(form.value) : await addData(form.value);
      useDictStore().removeDict(queryParams.value.dictType);
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};
/** 删除按钮操作 */
const handleDelete = async (row?: DictDataVO) => {
  const dictCodes = row?.dictCode || ids.value;
  await proxy?.$modal.confirm('是否确认删除字典编码为"' + dictCodes + '"的数据项？');
  await delData(dictCodes);
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
  useDictStore().removeDict(queryParams.value.dictType);
};
/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/dict/data/export',
    {
      ...queryParams.value
    },
    `dict_data_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getTypes(route.params && (route.params.dictId as string));
  getTypeList();
});
</script>
