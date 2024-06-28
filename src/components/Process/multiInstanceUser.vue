<template>
  <el-dialog v-model="visible" draggable :title="title" :width="width" :height="height" append-to-body :close-on-click-modal="false">
    <div v-if="multiInstance === 'add'" class="p-2">
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
            ></el-tree>
          </el-card>
        </el-col>
        <el-col :lg="20" :xs="24">
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

          <el-card shadow="hover">
            <template #header>
              <el-row :gutter="10">
                <right-toolbar v-model:showSearch="showSearch" :search="true" @query-table="handleQuery"></right-toolbar>
              </el-row>
            </template>

            <el-table ref="multipleTableRef" v-loading="loading" :data="userList" row-key="userId" @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column key="userId" label="用户编号" align="center" prop="userId" />
              <el-table-column key="userName" label="用户名称" align="center" prop="userName" :show-overflow-tooltip="true" />
              <el-table-column key="nickName" label="用户昵称" align="center" prop="nickName" :show-overflow-tooltip="true" />
              <el-table-column key="phonenumber" label="手机号码" align="center" prop="phonenumber" width="120" />
              <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                  <span>{{ scope.row.createTime }}</span>
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
          <el-card shadow="hover">
            <el-tag v-for="(user, index) in chooseUserList" :key="user.userId" style="margin: 2px" closable @close="handleCloseTag(user, index)"
              >{{ user.userName }}
            </el-tag>
          </el-card>
        </el-col>
      </el-row>
    </div>
    <div v-if="multiInstance === 'delete'" class="p-2">
      <el-table v-loading="loading" :data="taskList" @selection-change="handleTaskSelection">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="assigneeName" label="办理人" />
      </el-table>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="visible = false">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="User" lang="ts">
import { deptTreeSelect, listUser, optionSelect } from '@/api/system/user';
import {
  addMultiInstanceExecution,
  deleteMultiInstanceExecution,
  getTaskUserIdsByAddMultiInstance,
  getListByDeleteMultiInstance
} from '@/api/workflow/task';
import { UserVO } from '@/api/system/user/types';
import { DeptVO } from '@/api/system/dept/types';
import { ComponentInternalInstance } from 'vue';
import { ElTree, ElTable } from 'element-plus';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const props = defineProps({
  // 宽
  width: {
    type: String,
    default: '70%'
  },
  // 高
  height: {
    type: String,
    default: '100%'
  },
  // 标题
  title: {
    type: String,
    default: '加签人员'
  },
  //是否多选
  multiple: {
    type: Boolean,
    default: true
  },
  //回显用户id
  userIdList: {
    type: Array,
    default: () => []
  }
});
const deptTreeRef = ref(ElTree);
const multipleTableRef = ref(ElTable);

const userList = ref<UserVO[]>();
const taskList = ref<Array<any>[]>();
const loading = ref(true);
const showSearch = ref(true);
const selectionTask = ref<Array<any>[]>();
const visible = ref(false);
const total = ref(0);
const deptName = ref('');
const deptOptions = ref<DeptVO[]>([]);
const chooseUserList = ref(ref<UserVO[]>());
const userIds = ref<Array<number | string>>([]);
//加签或者减签
const multiInstance = ref('');
const queryParams = ref<Record<string, any>>({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  nickName: '',
  taskId: ''
});
/** 查询用户列表 */
const getAddMultiInstanceList = async (taskId: string, userIdList: Array<number | string>) => {
  deptOptions.value = [];
  getTreeSelect();
  multiInstance.value = 'add';
  userIds.value = userIdList;
  visible.value = true;
  queryParams.value.taskId = taskId;
  loading.value = true;
  const res1 = await getTaskUserIdsByAddMultiInstance(taskId);
  queryParams.value.excludeUserIds = res1.data;
  const res = await listUser(queryParams.value);
  loading.value = false;
  userList.value = res.rows;
  total.value = res.total;
  if (userList.value && userIds.value.length > 0) {
    const data = await optionSelect(userIds.value);
    if (data.data && data.data.length > 0) {
      chooseUserList.value = data.data;
      data.data.forEach((user: UserVO) => {
        multipleTableRef.value!.toggleRowSelection(
          userList.value.find((item) => {
            return item.userId == user.userId;
          }),
          true
        );
      });
    }
  }
};

const getList = async () => {
  loading.value = true;
  const res1 = await getTaskUserIdsByAddMultiInstance(queryParams.value.taskId);
  queryParams.value.excludeUserIds = res1.data;
  const res = await listUser(queryParams.value);
  loading.value = false;
  userList.value = res.rows;
  total.value = res.total;
  if (userList.value && userIds.value.length > 0) {
    const data = await optionSelect(userIds.value);
    if (data.data && data.data.length > 0) {
      chooseUserList.value = data.data;
      data.data.forEach((user: UserVO) => {
        multipleTableRef.value!.toggleRowSelection(
          userList.value.find((item) => {
            return item.userId == user.userId;
          }),
          true
        );
      });
    }
  }
};

const getDeleteMultiInstanceList = async (taskId: string) => {
  deptOptions.value = [];
  loading.value = true;
  queryParams.value.taskId = taskId;
  multiInstance.value = 'delete';
  visible.value = true;
  const res = await getListByDeleteMultiInstance(taskId);
  taskList.value = res.data;
  loading.value = false;
};
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getAddMultiInstanceList(queryParams.value.taskId, userIds.value);
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.value.pageNum = 1;
  queryParams.value.deptId = undefined;
  queryParams.value.userName = undefined;
  queryParams.value.nickName = undefined;
  deptTreeRef.value.setCurrentKey(null);
  handleQuery();
};

/** 选择条数  */
const handleSelectionChange = (selection: UserVO[]) => {
  if (props.multiple) {
    chooseUserList.value = selection.filter((element, index, self) => {
      return self.findIndex((x) => x.userId === element.userId) === index;
    });
    selection.forEach((u) => {
      if (chooseUserList.value && !chooseUserList.value.includes(u)) {
        multipleTableRef.value!.toggleRowSelection(u, undefined);
      }
    });
    userIds.value = chooseUserList.value.map((item) => {
      return item.userId;
    });
  } else {
    chooseUserList.value = selection;
    if (selection.length > 1) {
      let delRow = selection.shift();
      multipleTableRef.value!.toggleRowSelection(delRow, undefined);
    }
    if (selection.length === 0) {
      chooseUserList.value = [];
    }
  }
};
/** 选择条数  */
const handleTaskSelection = (selection: any) => {
  selectionTask.value = selection;
};

/** 查询部门下拉树结构 */
const getTreeSelect = async () => {
  const res = await deptTreeSelect();
  deptOptions.value = res.data;
};

/** 通过条件过滤节点  */
const filterNode = (value: string, data: any) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};
/** 根据名称筛选部门树 */
watchEffect(
  () => {
    if (visible.value && deptOptions.value && deptOptions.value.length > 0) {
      deptTreeRef.value.filter(deptName.value);
    }
  },
  {
    flush: 'post' // watchEffect会在DOM挂载或者更新之前就会触发，此属性控制在DOM元素更新后运行
  }
);
/** 节点单击事件 */
const handleNodeClick = (data: DeptVO) => {
  queryParams.value.deptId = data.id;
  getList();
};
//删除tag
const handleCloseTag = (user: UserVO, index: any) => {
  if (multipleTableRef.value.selection && multipleTableRef.value.selection.length > 0) {
    multipleTableRef.value.selection.forEach((u: UserVO, i: number) => {
      if (user.userId === u.userId) {
        multipleTableRef.value.selection.splice(i, 1);
      }
    });
  }
  if (chooseUserList.value && chooseUserList.value.length > 0) {
    chooseUserList.value.splice(index, 1);
  }
  multipleTableRef.value.toggleRowSelection(user, undefined);

  if (userIds.value && userIds.value.length > 0) {
    userIds.value.forEach((userId, i) => {
      if (userId === user.userId) {
        userIds.value.splice(i, 1);
      }
    });
  }
};
const submitFileForm = async () => {
  if (multiInstance.value === 'add') {
    if (chooseUserList.value && chooseUserList.value.length > 0) {
      loading.value = true;
      let userIds = chooseUserList.value.map((item) => {
        return item.userId;
      });
      let nickNames = chooseUserList.value.map((item) => {
        return item.nickName;
      });
      let params = {
        taskId: queryParams.value.taskId,
        assignees: userIds,
        assigneeNames: nickNames
      };
      await addMultiInstanceExecution(params);
      emits('submitCallback');
      loading.value = false;
      proxy?.$modal.msgSuccess('操作成功');
      visible.value = false;
    }
  } else {
    if (selectionTask.value && selectionTask.value.length > 0) {
      loading.value = true;
      let taskIds = selectionTask.value.map((item: any) => {
        return item.id;
      });
      let executionIds = selectionTask.value.map((item: any) => {
        return item.executionId;
      });
      let assigneeIds = selectionTask.value.map((item: any) => {
        return item.assignee;
      });
      let assigneeNames = selectionTask.value.map((item: any) => {
        return item.assigneeName;
      });
      let params = {
        taskId: queryParams.value.taskId,
        taskIds: taskIds,
        executionIds: executionIds,
        assigneeIds: assigneeIds,
        assigneeNames: assigneeNames
      };
      await deleteMultiInstanceExecution(params);
      emits('submitCallback');
      loading.value = false;
      proxy?.$modal.msgSuccess('操作成功');
      visible.value = false;
    }
  }
};
//事件
const emits = defineEmits(['submitCallback']);

/**
 * 对外暴露子组件方法
 */
defineExpose({
  getAddMultiInstanceList,
  getDeleteMultiInstanceList
});
</script>
