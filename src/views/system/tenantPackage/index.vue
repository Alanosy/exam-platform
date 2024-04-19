<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="套餐名称" prop="packageName">
              <el-input v-model="queryParams.packageName" placeholder="请输入套餐名称" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['system:tenantPackage:add']" type="primary" plain icon="Plus" @click="handleAdd"> 新增 </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:tenantPackage:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">
              修改
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:tenantPackage:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:tenantPackage:export']" type="warning" plain icon="Download" @click="handleExport">导出 </el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="tenantPackageList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column v-if="false" label="租户套餐id" align="center" prop="packageId" />
        <el-table-column label="套餐名称" align="center" prop="packageName" />
        <el-table-column label="备注" align="center" prop="remark" />
        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @click="handleStatusChange(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:tenantPackage:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:tenantPackage:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改租户套餐对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="tenantPackageFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="关联菜单">
          <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">展开/折叠</el-checkbox>
          <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">全选/全不选 </el-checkbox>
          <el-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">父子联动 </el-checkbox>
          <el-tree
            ref="menuTreeRef"
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            node-key="id"
            :check-strictly="!form.menuCheckStrictly"
            empty-text="加载中，请稍候"
            :props="{ label: 'label', children: 'children' }"
          ></el-tree>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
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

<script setup name="TenantPackage" lang="ts">
import {
  listTenantPackage,
  getTenantPackage,
  delTenantPackage,
  addTenantPackage,
  updateTenantPackage,
  changePackageStatus
} from '@/api/system/tenantPackage';
import { treeselect as menuTreeselect, tenantPackageMenuTreeselect } from '@/api/system/menu';
import { TenantPkgForm, TenantPkgQuery, TenantPkgVO } from '@/api/system/tenantPackage/types';
import { MenuTreeOption } from '@/api/system/menu/types';
import to from 'await-to-js';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const tenantPackageList = ref<TenantPkgVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const menuExpand = ref(false);
const menuNodeAll = ref(false);
const menuOptions = ref<MenuTreeOption[]>([]);

const menuTreeRef = ref<ElTreeInstance>();
const queryFormRef = ref<ElFormInstance>();
const tenantPackageFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: TenantPkgForm = {
  packageId: undefined,
  packageName: '',
  menuIds: '',
  remark: '',
  menuCheckStrictly: true
};
const data = reactive<PageData<TenantPkgForm, TenantPkgQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    packageName: ''
  },
  rules: {
    packageId: [{ required: true, message: '租户套餐id不能为空', trigger: 'blur' }],
    packageName: [{ required: true, message: '套餐名称不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询菜单树结构 */
const getMenuTreeselect = async () => {
  const { data } = await menuTreeselect();
  menuOptions.value = data;
};

// 所有菜单节点数据
const getMenuAllCheckedKeys = (): any => {
  // 目前被选中的菜单节点
  let checkedKeys = menuTreeRef.value?.getCheckedKeys();
  // 半选中的菜单节点
  let halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys();
  if (halfCheckedKeys) {
    checkedKeys?.unshift.apply(checkedKeys, halfCheckedKeys);
  }
  return checkedKeys;
};

/** 根据租户套餐ID查询菜单树结构 */
const getPackageMenuTreeselect = async (packageId: string | number) => {
  const res = await tenantPackageMenuTreeselect(packageId);
  menuOptions.value = res.data.menus;
  return Promise.resolve(res);
};

/** 查询租户套餐列表 */
const getList = async () => {
  loading.value = true;
  const res = await listTenantPackage(queryParams.value);
  tenantPackageList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

// 租户套餐状态修改
const handleStatusChange = async (row: TenantPkgVO) => {
  let text = row.status === '0' ? '启用' : '停用';
  const [err] = await to(proxy?.$modal.confirm('确认要"' + text + '""' + row.packageName + '"套餐吗？') as Promise<any>);
  if (err) {
    row.status = row.status === '0' ? '1' : '0';
  } else {
    await changePackageStatus(row.packageId, row.status);
    proxy?.$modal.msgSuccess(text + '成功');
  }
};

// 取消按钮
const cancel = () => {
  reset();
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  menuTreeRef.value?.setCheckedKeys([]);
  menuExpand.value = false;
  menuNodeAll.value = false;
  form.value = { ...initFormData };
  tenantPackageFormRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection: TenantPkgVO[]) => {
  ids.value = selection.map((item) => item.packageId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

// 树权限（展开/折叠）
const handleCheckedTreeExpand = (value: CheckboxValueType, type: string) => {
  if (type == 'menu') {
    let treeList = menuOptions.value;
    for (let i = 0; i < treeList.length; i++) {
      if (menuTreeRef.value) {
        menuTreeRef.value.store.nodesMap[treeList[i].id].expanded = value as boolean;
      }
    }
  }
};

// 树权限（全选/全不选）
const handleCheckedTreeNodeAll = (value: CheckboxValueType, type: string) => {
  if (type == 'menu') {
    menuTreeRef.value?.setCheckedNodes(value ? (menuOptions.value as any) : []);
  }
};

// 树权限（父子联动）
const handleCheckedTreeConnect = (value: CheckboxValueType, type: string) => {
  if (type == 'menu') {
    form.value.menuCheckStrictly = value as boolean;
  }
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  getMenuTreeselect();
  dialog.visible = true;
  dialog.title = '添加租户套餐';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: TenantPkgVO) => {
  reset();
  const _packageId = row?.packageId || ids.value[0];
  const response = await getTenantPackage(_packageId);
  form.value = response.data;
  const res = await getPackageMenuTreeselect(_packageId);
  dialog.visible = true;
  dialog.title = '修改租户套餐';
  res.data.checkedKeys.forEach((v) => {
    nextTick(() => {
      menuTreeRef.value?.setChecked(v, true, false);
    });
  });
};

/** 提交按钮 */
const submitForm = () => {
  tenantPackageFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      form.value.menuIds = getMenuAllCheckedKeys();
      if (form.value.packageId != null) {
        await updateTenantPackage(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addTenantPackage(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: TenantPkgVO) => {
  const _packageIds = row?.packageId || ids.value;
  await proxy?.$modal.confirm('是否确认删除租户套餐编号为"' + _packageIds + '"的数据项？').finally(() => {
    loading.value = false;
  });
  await delTenantPackage(_packageIds);
  loading.value = true;
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/tenantPackage/export',
    {
      ...queryParams.value
    },
    `tenantPackage_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
