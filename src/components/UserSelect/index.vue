<template>
  <div>
    <el-dialog v-model="userDialog.visible.value" :title="userDialog.title.value" width="80%" append-to-body>
      <el-row :gutter="20">
        <!-- 部门树 -->
        <el-col :lg="4" :xs="24" style="">
          <el-card shadow="hover">
            <el-input v-model="deptName" placeholder="请输入部门名称" prefix-icon="Search" clearable />
            <el-tree
              ref="deptTreeRef"
              class="mt-2"
              node-key="id"
              :data="deptOptions"
              :props="{ label: 'label', children: 'children' }"
              :expand-on-click-node="false"
              :filter-node-method="filterNode"
              highlight-current
              default-expand-all
              @node-click="handleNodeClick"
            />
          </el-card>
        </el-col>
        <el-col :lg="20" :xs="24">
          <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
            <div v-show="showSearch" class="mb-[10px]">
              <el-card shadow="hover">
                <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="68px">
                  <el-form-item label="用户名称" prop="userName">
                    <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
                  </el-form-item>
                  <el-form-item label="手机号码" prop="phonenumber">
                    <el-input
                      v-model="queryParams.phonenumber"
                      placeholder="请输入手机号码"
                      clearable
                      style="width: 200px"
                      @keyup.enter="handleQuery"
                    />
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
              <el-tag v-for="user in selectUserList" :key="user.userId" closable style="margin: 2px" @close="handleCloseTag(user)">
                {{ user.userName }}
              </el-tag>
            </template>

            <vxe-table
              ref="tableRef"
              height="400px"
              border
              show-overflow
              :data="userList"
              :loading="loading"
              :row-config="{ keyField: 'userId' }"
              :checkbox-config="{ reserve: true, checkRowKeys: userIds }"
              highlight-current-row
              @checkbox-all="handleCheckboxAll"
              @checkbox-change="handleCheckboxChange"
            >
              <vxe-column type="checkbox" width="50" align="center" />
              <vxe-column key="userId" title="用户编号" align="center" field="userId" />
              <vxe-column key="userName" title="用户名称" align="center" field="userName" />
              <vxe-column key="nickName" title="用户昵称" align="center" field="nickName" />
              <vxe-column key="deptName" title="部门" align="center" field="deptName" />
              <vxe-column key="phonenumber" title="手机号码" align="center" field="phonenumber" width="120" />
              <vxe-column key="status" title="状态" align="center">
                <template #default="scope">
                  <dict-tag :options="sys_normal_disable" :value="scope.row.status"></dict-tag>
                </template>
              </vxe-column>

              <vxe-column title="创建时间" align="center" width="160">
                <template #default="scope">
                  <span>{{ scope.row.createTime }}</span>
                </template>
              </vxe-column>
            </vxe-table>

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

      <template #footer>
        <el-button @click="userDialog.closeDialog">取消</el-button>
        <el-button type="primary" @click="confirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import api from '@/api/system/user';
import { UserQuery, UserVO } from '@/api/system/user/types';
import { DeptVO } from '@/api/system/dept/types';
import { VxeTableInstance } from 'vxe-table';
import useDialog from '@/hooks/useDialog';

interface PropType {
  modelValue?: UserVO[];
}
const prop = withDefaults(defineProps<PropType>(), {
  modelValue: () => []
});
const emit = defineEmits(['update:modelValue']);

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const userIds = computed(() => prop.modelValue.map((item) => item.userId as string));

const userList = ref<UserVO[]>();
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const dateRange = ref<[DateModelType, DateModelType]>(['', '']);
const deptName = ref('');
const deptOptions = ref<DeptVO[]>([]);
const selectUserList = ref<UserVO[]>([]);

const deptTreeRef = ref<ElTreeInstance>();
const queryFormRef = ref<ElFormInstance>();
const tableRef = ref<VxeTableInstance<UserVO>>();

const userDialog = useDialog({
  title: '用户选择'
});

const queryParams = ref<UserQuery>({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  phonenumber: '',
  status: '',
  deptId: '',
  roleId: ''
});

/** 通过条件过滤节点  */
const filterNode = (value: string, data: any) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};
/** 根据名称筛选部门树 */
watchEffect(
  () => {
    deptTreeRef.value?.filter(deptName.value);
  },
  {
    flush: 'post' // watchEffect会在DOM挂载或者更新之前就会触发，此属性控制在DOM元素更新后运行
  }
);

const confirm = () => {
  emit('update:modelValue', [...selectUserList.value]);
  userDialog.closeDialog();
};

/** 查询部门下拉树结构 */
const getTreeSelect = async () => {
  const res = await api.deptTreeSelect();
  deptOptions.value = res.data;
};

/** 查询用户列表 */
const getList = async () => {
  loading.value = true;
  const res = await api.listUser(proxy?.addDateRange(queryParams.value, dateRange.value));
  loading.value = false;
  userList.value = res.rows;
  total.value = res.total;
};

/** 节点单击事件 */
const handleNodeClick = (data: DeptVO) => {
  queryParams.value.deptId = data.id;
  handleQuery();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
/** 重置按钮操作 */
const resetQuery = () => {
  dateRange.value = ['', ''];
  queryFormRef.value?.resetFields();
  queryParams.value.pageNum = 1;
  queryParams.value.deptId = undefined;
  deptTreeRef.value?.setCurrentKey(undefined);
  handleQuery();
};

const handleCheckboxChange = (checked) => {
  const row = checked.row;
  if (checked.checked) {
    selectUserList.value.push(row);
  } else {
    selectUserList.value = selectUserList.value.filter((item) => {
      return item.userId !== row.userId;
    });
  }
};
const handleCheckboxAll = (checked) => {
  const rows = userList.value;
  if (checked.checked) {
    rows.forEach((row) => {
      if (!selectUserList.value.some((item) => item.userId === row.userId)) {
        selectUserList.value.push(row);
      }
    });
  } else {
    selectUserList.value = selectUserList.value.filter((item) => {
      return !rows.some((row) => row.userId === item.userId);
    });
  }
};

const handleCloseTag = (user: UserVO) => {
  const userId = user.userId;
  const index = selectUserList.value.findIndex((item) => item.userId === userId);
  const rows = selectUserList.value[index];
  tableRef.value?.setCheckboxRow(rows, false);
  selectUserList.value.splice(index, 1);
};
watch(
  () => prop.modelValue,
  (newVal, oldValue) => {
    Object.assign(selectUserList.value, newVal);
  },
  { deep: true }
);

onMounted(() => {
  getTreeSelect();
  getList();
});

defineExpose({
  open: userDialog.openDialog,
  close: userDialog.closeDialog
});
</script>

<style lang="scss" scoped></style>