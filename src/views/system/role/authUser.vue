<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="search">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item label="用户名称" prop="userName">
            <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>
    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:role:add']" type="primary" plain icon="Plus" @click="openSelectUser">添加用户</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:role:remove']" type="danger" plain icon="CircleClose" :disabled="multiple" @click="cancelAuthUserAll">
              批量取消授权
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Close" @click="handleClose">关闭</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" :search="true" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="用户名称" prop="userName" :show-overflow-tooltip="true" />
        <el-table-column label="用户昵称" prop="nickName" :show-overflow-tooltip="true" />
        <el-table-column label="邮箱" prop="email" :show-overflow-tooltip="true" />
        <el-table-column label="手机" prop="phonenumber" :show-overflow-tooltip="true" />
        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ scope.row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="取消授权" placement="top">
              <el-button v-hasPermi="['system:role:remove']" link type="primary" icon="CircleClose" @click="cancelAuthUser(scope.row)"> </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      <select-user ref="selectRef" :role-id="queryParams.roleId" @ok="handleQuery" />
    </el-card>
  </div>
</template>

<script setup name="AuthUser" lang="ts">
import { allocatedUserList, authUserCancel, authUserCancelAll } from '@/api/system/role';
import { UserQuery } from '@/api/system/user/types';
import { UserVO } from '@/api/system/user/types';
import SelectUser from './selectUser.vue';
import { RouteLocationNormalized } from 'vue-router';

const route = useRoute();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const userList = ref<UserVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const multiple = ref(true);
const total = ref(0);
const userIds = ref<Array<string | number>>([]);

const queryFormRef = ref<ElFormInstance>();
const selectRef = ref<InstanceType<typeof SelectUser>>();

const queryParams = reactive<UserQuery>({
  pageNum: 1,
  pageSize: 10,
  roleId: route.params.roleId as string,
  userName: undefined,
  phonenumber: undefined
});

/** 查询授权用户列表 */
const getList = async () => {
  loading.value = true;
  const res = await allocatedUserList(queryParams);
  userList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};
// 返回按钮
const handleClose = () => {
  const obj: RouteLocationNormalized = {
    path: '/system/role',
    fullPath: '',
    hash: '',
    matched: [],
    meta: undefined,
    name: undefined,
    params: undefined,
    query: undefined,
    redirectedFrom: undefined
  };
  proxy?.$tab.closeOpenPage(obj);
};
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};
/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};
// 多选框选中数据
const handleSelectionChange = (selection: UserVO[]) => {
  userIds.value = selection.map((item) => item.userId);
  multiple.value = !selection.length;
};
/** 打开授权用户表弹窗 */
const openSelectUser = () => {
  selectRef.value?.show();
};
/** 取消授权按钮操作 */
const cancelAuthUser = async (row: UserVO) => {
  await proxy?.$modal.confirm('确认要取消该用户"' + row.userName + '"角色吗？');
  await authUserCancel({ userId: row.userId, roleId: queryParams.roleId });
  await getList();
  proxy?.$modal.msgSuccess('取消授权成功');
};
/** 批量取消授权按钮操作 */
const cancelAuthUserAll = async () => {
  const roleId = queryParams.roleId;
  const uIds = userIds.value.join(',');
  await proxy?.$modal.confirm('是否取消选中用户授权数据项?');
  await authUserCancelAll({ roleId: roleId, userIds: uIds });
  await getList();
  proxy?.$modal.msgSuccess('取消授权成功');
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped></style>
